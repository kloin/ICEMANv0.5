package com.teamunemployment.breadcrumbs.Location;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.Circle;
import com.pathsense.android.sdk.location.PathsenseGeofenceEvent;
import com.pathsense.android.sdk.location.PathsenseLocationProviderApi;
import com.teamunemployment.breadcrumbs.Location.PathSense.Activity.PathSenseActivityManager;
import com.teamunemployment.breadcrumbs.Location.PathSense.Geofence.BreadCrumbsPathSenseGeofenceEventReceiver;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Trails.TrailManager;
import com.teamunemployment.breadcrumbs.caching.TextCaching;
import com.teamunemployment.breadcrumbs.client.BaseViewModel;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Written by Josiah Kendall.
 *
 * This is the Master/God/Wrapper class for Location Access. All gps calls should go through here.
 *
 * This class can:
 *      * Fetch Accurate GPS location on demand
 *      * Fetch last known gps Location
 *      * Fetch last known wi-fi location
 *      * Fetch current providers
 *      * Fetch location using cheapset provider
 *      * Track user constatly using :
 *          - PathSense
 *          - GPS
 *          - Fused (Kinda useless, becase wifi will rarely be available)
 *
 *  ************************************************
 *      PathSense - only currently used for geofence. HTier tracking is shit.
 *
 *      PathSense is an APK from some guys in san diago. They have really shitty support, so good
 *      luck contacting them.
 *
 *  **************************************************
 *      This class also has the regular gps, and the LocationManager has been made public to allow
 *      other classes to have access to it, rather than reimplement all the methods.
 *
 */
public class BreadcrumbsLocationProvider implements LocationListener {

    public int updates = 0;
    // We want to allow other classes to access this and do stuff.
    public LocationManager androidLocationManager;

    // Messages
    private static final int MESSAGE_ON_GEOFENCE_EVENT = 1;
    private static final String TAG = BreadcrumbsLocationProvider.class.getName();
    private static BreadcrumbsLocationProvider sInstance;
    // Manages fetching location from androids Location


    // Needs to be public so that we can access it from activity?
    private InternalHandler mHandler;
    private Context mContext;
    private Circle mGeofence;
    private Circle mGeofenceEgress;
    private Circle mGeofenceIngress;
    private InternalLocalGeofenceEventReceiver mLocalGeofenceEventReceiver;
    private BreadCrumbsFusedLocationProvider mBreadcrumbsFusedLocationProvider;
    private PathsenseLocationProviderApi mApi;
    private SharedPreferences mPreferences;
    private int mFindLocation;
    private DatabaseController dbc;
    // debug shit
    private TextCaching gpsHardLoggin;

    /*
        Singleton Entry point
     */
    public static synchronized BreadcrumbsLocationProvider getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new BreadcrumbsLocationProvider(context);
        }
        return sInstance;
    }

    Queue<InternalHolder> mHolders = new ConcurrentLinkedQueue<InternalHolder>();

    private BreadcrumbsLocationProvider(Context context) {
        mContext = context;
        androidLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        mLocalGeofenceEventReceiver = new InternalLocalGeofenceEventReceiver(this);
        LocalBroadcastManager.getInstance(context).registerReceiver(mLocalGeofenceEventReceiver, new IntentFilter("geofenceEvent"));
        mApi = PathsenseLocationProviderApi.getInstance(context);
        mHandler = new InternalHandler(this);
        gpsHardLoggin = new TextCaching(context);
        dbc = new DatabaseController(mContext);
        //mFusedLocationProvider = new BreadCrumbsFusedLocationProvider(context);
    }

    /*
        Location Listener Overrides Start
     *******************************************************************************
     *
     * This is what we use to start our geoFencing. We have to fetch the location first to know where we are.
      */
    @Override
    public void onLocationChanged(Location location) {

        final SharedPreferences preferences = mPreferences;
        final PathsenseLocationProviderApi api = mApi;
        Toast.makeText(mContext, "Location found. Attempting to start PathSense", Toast.LENGTH_SHORT).show();
        if (preferences != null && api != null) {
            if (mFindLocation == 1) {
                mFindLocation = 0;
            } else {
                // add 100m geofence around me
                Toast.makeText(mContext, "GeoFence in place", Toast.LENGTH_SHORT).show();
                mApi.removeGeofence("MYGEOFENCE");
                mApi.addGeofence("MYGEOFENCE", location.getLatitude(), location.getLongitude(), 100, BreadCrumbsPathSenseGeofenceEventReceiver.class);

                // Debug shit while pathsense is not that solid.
                gpsHardLoggin.CacheText("gps_location_found", "Added geoFence");
            }
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

    }

    public void StartListeningToPathSense() {
        cleanUp();
        //requestLocationUpdate(this);
        startListeningToActivityChangesForUser(12);
        BreadCrumbsFusedLocationProvider provider = new BreadCrumbsFusedLocationProvider(mContext);
        provider.StartBackgroundGPSService();
    }

    // Just sort shit out before we start listening to the pathsense, incase of previous instances.
    private void cleanUp() {
        if (mGeofence != null) {
            mGeofence.remove();
            mGeofence = null;
        }
        if (mGeofenceEgress != null) {
            mGeofenceEgress.remove();
            mGeofenceEgress = null;
        }
        if (mGeofenceIngress != null) {
            mGeofenceIngress.remove();
            mGeofenceIngress = null;
        }
    }

    public void StartFusedBackGroundService() {

    }

    public void StopListeningToPathSense() {

        // Set up the first geofence with the location request now.
        mApi.removeGeofences();
    }

    // Purpose of this method is to put a getfence for pathSense around a given location.
    private void triggerPathSenseGeofenceAtLocation(Location location) {
        mApi.addGeofence("MYGEOFENCE", location.getLatitude(), location.getLongitude(), 100, BreadCrumbsPathSenseGeofenceEventReceiver.class);

    }


    // ---------------------- Static Classes
    // This class is used for holding diferent locations listeners that are used when actual location updates.
    static class InternalHolder {
        LocationListener mListener;
        List<InternalLocationListenerFilter> mFilterList;
    }

    /*
        The InternalLocalGeoFenceEventReviever. This recieves the event that is thrown when
        a geofence is exceeded by pathsense.
     */
    private static class InternalLocalGeofenceEventReceiver extends BroadcastReceiver {
        BreadcrumbsLocationProvider mActivity;
        TextCaching localCache;

        //
        InternalLocalGeofenceEventReceiver(BreadcrumbsLocationProvider activity) {
            mActivity = activity;
            localCache = new TextCaching(mActivity.mContext);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final BreadcrumbsLocationProvider activity = mActivity;
            final InternalHandler handler = mActivity.mHandler;
            localCache.CacheText("GeoFenceEvent", "Recieved event! Doing work now");
            if (activity != null && handler != null) {
                localCache.CacheText("GeoFenceEvent", "Passed if statement");
                // local broadcast from PathsenseGeofenceEventBroadcastReceiver
                PathsenseGeofenceEvent geofenceEvent = (PathsenseGeofenceEvent) intent.getSerializableExtra("geofenceEvent");
                Message msg = Message.obtain();
                msg.what = MESSAGE_ON_GEOFENCE_EVENT;
                msg.obj = geofenceEvent;
                handler.sendMessage(msg);
                localCache.CacheText("GeoFenceEvent", "Sent message to handler");
            }
        }
    }

    /*
        This class is where we handle the location / Perimeter breach events.
     */
    private static class InternalHandler extends Handler {
        BreadcrumbsLocationProvider mActivity;
        TextCaching localCaching;

        InternalHandler(BreadcrumbsLocationProvider activity) {
            mActivity = activity;
            localCaching = new TextCaching(activity.mContext);
        }

        @Override
        public void handleMessage(Message msg) {
            localCaching.CacheText("Handler", "Handler triggered with message: " + msg.toString());
            final BreadcrumbsLocationProvider activity = mActivity;
            if (activity != null) {
                localCaching.CacheText("Handler", "msg.what: " + msg.what);
                switch (msg.what) {
                    case MESSAGE_ON_GEOFENCE_EVENT: {
                        localCaching.CacheText("Handler", "Case correct. Now doing geofence work: ");
                        PathsenseGeofenceEvent geofenceEvent = (PathsenseGeofenceEvent) msg.obj;
                        Location location = geofenceEvent.getLocation();
                        // Fetch event Id
                        int eventId = PreferenceManager.getDefaultSharedPreferences(mActivity.mContext).getInt("EVENTID", 0);
                        String trailId = PreferenceManager.getDefaultSharedPreferences(mActivity.mContext).getString("TRAILID", null);
                        DateTime timeStamp = new DateTime();

                        // Geofence is broken - start up gps again
                        activity.ListenPassivelyForGPSUpdatesInBackground(0);

                        NotificationManager notificationManager = (NotificationManager) mActivity.mContext.getSystemService(mActivity.mContext.NOTIFICATION_SERVICE);
                        NotificationCompat.Builder noti = new NotificationCompat.Builder(mActivity.mContext);
                        noti.setContentTitle("GEOFENCE");
                        noti.setContentText("Saved s");
                        noti.setSmallIcon(R.drawable.bc64);
                        notificationManager.notify(1234567, noti.build());
                        // Save break point to metadata
                        // mActivity.dbc.AddMetadata(eventId, timeStamp.toString(), location.getLatitude(), location.getLongitude(), trailId, "GPS");
                    }
                }
            }
        }
    }

    // Class to run locationListerns onChanged methods.
    static class InternalLocationListenerFilter implements LocationListener {
        BreadcrumbsLocationProvider mManager;
        LocationListener mListener;

        //
        InternalLocationListenerFilter(BreadcrumbsLocationProvider manager, LocationListener listener) {
            mManager = manager;
            mListener = listener;
        }

        @Override
        public void onLocationChanged(Location location) {
            final BreadcrumbsLocationProvider manager = mManager;
            final LocationListener listener = mListener;

            // Garbage Cleaner might have gobbled the listener, so check that it is not null
            if (manager != null && listener != null) {
                if (manager.validate(location) && manager.removeUpdates(listener)) {
                    // broadcast location
                    listener.onLocationChanged(location);
                }
            }
        }

        @Override
        public void onProviderDisabled(String s) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }
    }


    boolean removeUpdates(LocationListener listener) {
        final Queue<InternalHolder> holders = mHolders;
        final LocationManager locationManager = androidLocationManager;

        if (holders != null && locationManager != null) {
            synchronized (holders) {
                for (Iterator<InternalHolder> q = holders.iterator(); q.hasNext(); ) {
                    InternalHolder holder = q.next();
                    if (holder.mListener == listener) {
                        List<InternalLocationListenerFilter> filters = holder.mFilterList;
                        int numFilters = filters != null ? filters.size() : 0;
                        for (int i = numFilters - 1; i > -1; i--) {
                            InternalLocationListenerFilter filter = filters.remove(i);
                            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return false;
                            }
                            locationManager.removeUpdates(filter);
                        }
                        q.remove();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Start listening for activity changes from a user (walking driving, rest etc)
    public void startListeningToActivityChangesForUser(int durationInSeconds) {
        PathSenseActivityManager pathSenseActivityManager = new PathSenseActivityManager(mContext);
        pathSenseActivityManager.StartPathSenseActivityManager();
    }

    /*
        This requests the current location for an activity that extends LocationListener
     */
    public void requestLocationUpdate(LocationListener listener) {
        final LocationManager locationManager = androidLocationManager;
        final Queue<InternalHolder> holders = mHolders;

        if (locationManager != null && holders != null) {
            List<String> providers = locationManager.getProviders(true);
            int numProviders = providers != null ? providers.size() : 0;
            if (numProviders > 0) {
                // broadcast last known location if valid
                for (int i = 0; i < numProviders; i++) {
                    String provider = providers.get(i);
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
//                        String[] permissions = new String[2];
//                        permissions[0] =  Manifest.permission.ACCESS_FINE_LOCATION;
//                        permissions[1] =  Manifest.permission.ACCESS_COARSE_LOCATION;
//                        ActivityCompat.requestPermissions((Activity) mContext,permissions, PackageManager.PERMISSION_GRANTED);
//                        //    ActivityCompat#requestPermissions
//                        // here to request the missing permissions, and then overriding
//                           public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                                                  int[] grantResults)
//                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
                    if (validate(lastKnownLocation)) {
                        listener.onLocationChanged(lastKnownLocation);
                        return;
                    }
                }
                // request location updates
                InternalHolder holder = new InternalHolder();
                holder.mListener = listener;
                holder.mFilterList = new ArrayList<InternalLocationListenerFilter>(numProviders);
                //
                for (int i = 0; i < numProviders; i++) {
                    String provider = providers.get(i);
                    InternalLocationListenerFilter filter = new InternalLocationListenerFilter(this, listener);
                    locationManager.requestLocationUpdates(provider, 0, 0, filter);
                    holder.mFilterList.add(filter);
                }
                holders.add(holder);
            }
        }
    }

    public void RemoveGPSUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        androidLocationManager.removeUpdates(locationListener);
    }

    /*
        Listen to gps requests from other apps in the background.
     */
    public boolean ListenPassivelyForGPSUpdatesInBackground(int seconds) {
        StartListening(locationListener);
        return true;
    }


    public void AddGeofences(Location location) {
        mApi.removeGeofences();
        mApi.addGeofence("MYGEOFENCE2", location.getLatitude(), location.getLongitude(), 50, BreadCrumbsPathSenseGeofenceEventReceiver.class);
    }

    boolean validate(Location location) {
        if (location != null) {
            String provider = location.getProvider();
            if (androidLocationManager.NETWORK_PROVIDER.equals(provider)) {
                double accuracy = location.getAccuracy();
                long age = System.currentTimeMillis() - location.getTime();
                Log.i(TAG, "provider=" + provider + ",accuracy=" + accuracy + ",age=" + age);
                if (location.getAccuracy() <= 100.0 && age <= 20000) {
                    return true;
                }
            } else if (androidLocationManager.GPS_PROVIDER.equals(provider)) {
                if (location.getAccuracy() <= 50.0 && (System.currentTimeMillis() - location.getTime()) <= 5000) {
                    return true;
                }
            }
        }
        return false;
    }

    //************************* END overrides *****************************************
    // Usually the app will call at startup (unless battery is really low) and then start or stop when required.
    // as soon as we fetch one update we stop listening
    private void StartListening(LocationListener listener) {
        // Acquire a reference to the system Location Manager
        androidLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        androidLocationManager.requestLocationUpdates(androidLocationManager.GPS_PROVIDER, 90000, 500, listener);
    }

    public void getCurrentPlaceId() {
        mBreadcrumbsFusedLocationProvider = new BreadCrumbsFusedLocationProvider(mContext);
                mBreadcrumbsFusedLocationProvider.GetCurrentPlace(new ResultCallback<PlaceLikelihoodBuffer>() {
                    @Override
                    public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                        String placeId = null;
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
            }
        });
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            DatabaseController dbc = new DatabaseController(mContext);
            if (location.getAccuracy() <= 60) {
                String trailId = PreferenceManager.getDefaultSharedPreferences(mContext).getString("TRAILID", "-1");
                String userId = PreferenceManager.getDefaultSharedPreferences(mContext).getString("USERID", "-1");
                if (!trailId.equals("-1") || !userId.equals("-1")) {
                    // Save our trail point to the db.
                    dbc.saveTrailPoint(trailId, location, userId);
                    int eventId = PreferenceManager.getDefaultSharedPreferences(mContext).getInt("EVENTID", 0);
                    dbc.AddMetadata(eventId,DateTime.now().toString(),location.getLatitude(), location.getLongitude(),trailId, TrailManager.GPS);
                    NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
                    NotificationCompat.Builder noti = new NotificationCompat.Builder(mContext);
                    noti.setContentTitle("BreadCrumbs");
                    noti.setContentText("Provided by " + location.getProvider());
                    noti.setSmallIcon(R.drawable.bc64);
                    notificationManager.notify(1234, noti.build());

                    // We also want to trigger updates to pathsense.
                    // BreadcrumbsLocationProvider breadcrumbsLocationProvider = BreadcrumbsLocationProvider.getInstance(this.getApplicationContext());
                    //breadcrumbsLocationProvider.AddGeofences(location);
                }
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

        }
    };
}
