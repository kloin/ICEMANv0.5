package com.teamunemployment.breadcrumbs.BackgroundServices;

import android.Manifest;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Process;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.teamunemployment.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.teamunemployment.breadcrumbs.Location.PlaceManager;
import com.teamunemployment.breadcrumbs.Location.SimpleGps;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Trails.TrailManagerWorker;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.caching.Utils;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jek40 on 6/06/2016.
 */
public class SaveCrumbService extends Service {
    private Looper mServiceLooper;

    private ArrayList<Event> eventsTosave = new ArrayList<>();
    private PreferencesAPI preferencesAPI;
    private LocationManager locationManager;
    private Context context;
    private Timer t;
    int timer = 0;

    private BreadCrumbsFusedLocationProvider fusedLocationProvider;

    private Bitmap bitmap;

    // Simple class to hold the data for each event that the service is currently saving.
    private class Event {

        public int eventId;
        public boolean isPhoto;

        public Event(int id, boolean isPhoto) {
            this.eventId = id;
            this.isPhoto = isPhoto;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        if (intent == null) {
            return 0;
        }

        boolean isPhoto = intent.getBooleanExtra("IsPhoto", false);
        int eventId = intent.getIntExtra("EventId", -1);

        if (eventId == -1) {
            throw new IllegalArgumentException("Event Id cannot be -1");
        }

        eventsTosave.add(new Event(eventId, isPhoto));
        if (eventsTosave.size() > 1) {
            return super.onStartCommand(intent, flags, startId);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return super.onStartCommand(intent, flags, startId);
        }

        // Grab location service.
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, locationListener);

        // Start thread. If we are still running, cancel it.
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timer += 50;
                // We only want to look for 20 seconds
                if (timer > 20000) {
                    // Stop video, and go to the next page
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    SimpleGps gps = new SimpleGps(context);
                    Location location = gps.GetInstantLocation();
                    processAndSaveEvent(location);
                    locationManager.removeUpdates(locationListener);
                    t.cancel();
                    stopSelf();
                }
            }
        }, 50, 50);

        return super.onStartCommand(intent, flags, startId);
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location.getAccuracy() <= 48) {
                processAndSaveEvent(location);
                t.cancel();
                // Save event.
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Shouldnt ever get here - this would be really bad.
                    stopSelf();
                    return;
                }
                locationManager.removeUpdates(this);
                stopSelf();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(context, "Cannot save a crumb without location services enabled.", Toast.LENGTH_LONG).show();
        }
    };

    private String getSuburb(Address address) {
        if (address != null) {
            return address.getSubLocality();
        }

        return " "; // return an empty string which will be sent to the server.
    }

    private String getCity(Address address) {
        if (address != null) {
            return address.getLocality();
        }
        return " "; // return an empty string which will be sent to the server.
    }

    private String getCountry(Address address) {
        if (address != null) {
            return address.getCountryName();
        }
        return " "; // return an empty string which will be sent to the server.
    }


    /**
     * Fetch the details needed and save our event.
     * @param location
     */
    private void processAndSaveEvent(final Location location) {

        // Fetch Time
        Calendar calendar = Calendar.getInstance();
        final String timeStamp = calendar.getTime().toString();

        // Get the address
        Address address = PlaceManager.GetPlace(context, location.getLatitude(), location.getLongitude());

        // Parameters that we need to save to for a crumb.
        final String suburb = getSuburb(address);
        final String finalCity = getCity(address);
        final String finalCountry = getCountry(address);

        fusedLocationProvider = new BreadCrumbsFusedLocationProvider(context);
        fusedLocationProvider.GetCurrentPlace(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                String placeId = " ";
                try {
                    PlaceLikelihood placeLikelihood = likelyPlaces.get(0);
                    if (placeLikelihood != null) {
                        Place place = placeLikelihood.getPlace();
                        if (place != null) {
                            placeId = place.getId();
                        }
                    }
                    likelyPlaces.release();
                } catch (IllegalStateException ex) {
                    // This happens when we have no network connection
                    ex.printStackTrace();
                }

                // For each item in the arrayList, save our shit.
                Iterator<Event> eventIterator = eventsTosave.iterator();
                while (eventIterator.hasNext()) {
                    Event next = eventIterator.next();
                    saveEvent(next, location, suburb, finalCity, finalCountry, placeId);
                }

                // Also need to save a path event here.
            }
        });
    }

    private void saveEvent(Event event, Location location, String suburb, String city, String country, String placeId) {
        if (preferencesAPI == null) {
            preferencesAPI = new PreferencesAPI(context);
        }

        String userId = preferencesAPI.GetUserId();
        String localTrailId = Integer.toString(preferencesAPI.GetLocalTrailId());
        DatabaseController dbc = new DatabaseController(context);
        int eventId = event.eventId;
        DateTime timeStamp = DateTime.now();
        String mime = "";
        if (event.isPhoto) {
            mime = ".jpg";
        } else {
            mime = ".mp4";
        }
        dbc.SaveCrumb(localTrailId, " ", userId, event.eventId, location.getLatitude(), location.getLongitude(), mime, timeStamp.toString(), "", placeId, suburb, city, country);
        TrailManagerWorker trailManagerWorker = new TrailManagerWorker(context);
        trailManagerWorker.CreateEventMetadata(TrailManagerWorker.CRUMB, location);
        int lastActivity = DetectedActivity.WALKING;

        // If we dont know what we were last doing, we default to walking. It is more likely that we are
        // taking a photo on foot than in a vehicle I am assuming.
        if (lastActivity == -1) {
            lastActivity = DetectedActivity.WALKING;
        }
        dbc.SaveActivityPoint(lastActivity, lastActivity, location.getLatitude(), location.getLongitude(), 0);

    }
}
