package com.teamunemployment.breadcrumbs.Location;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.Circle;
import com.pathsense.android.sdk.location.PathsenseGeofenceEvent;
import com.pathsense.android.sdk.location.PathsenseLocationProviderApi;
import com.teamunemployment.breadcrumbs.Location.PathSense.Activity.PathSenseActivityManager;
import com.teamunemployment.breadcrumbs.Location.PathSense.Geofence.BreadCrumbsPathSenseGeofenceEventReceiver;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.caching.TextCaching;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

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
 *      PathSense
 *
 *      PathSense is an APK from some guys in san diago. They have really shitty support, so good
 *      luck contacting them.
 *      It tracks user movement by understanding user activity (walking, running, driving), and uses
 *      this to figure out speed and travel path. It needs to be along a road however (far as Im
 *      aware) so when off a known road we need to fall back to regular GPS.
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
        Toast.makeText(mContext, "Location found. Attempting to start PathSense",Toast.LENGTH_SHORT).show();
        if (preferences != null && api != null) {
            if (mFindLocation == 1) {
                mFindLocation = 0;
            } else {
                // add 100m geofence around me
                Toast.makeText(mContext, "GeoFence in place",Toast.LENGTH_SHORT).show();
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
                        activity.mApi.removeGeofence("MYGEOFENCE2");
                        activity.mApi.addGeofence("MYGEOFENCE2", location.getLatitude(), location.getLongitude(), 100, BreadCrumbsPathSenseGeofenceEventReceiver.class);
                        activity.ProcessUpdatedLocation(location);

                        // Trigger another api call.
                        Toast.makeText(mActivity.mContext, "Added a new geofence",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void ProcessUpdatedLocation(Location location) {
        // Save to database.
        DatabaseController dbc = new DatabaseController(mContext);
        // I should actually set current trail Id at the end. Having more than one trail at a time does not make sense.
        String trailId = PreferenceManager.getDefaultSharedPreferences(mContext).getString("TRAILID", "-1");
        String userId = PreferenceManager.getDefaultSharedPreferences(mContext).getString("USERID", "-1");
        // If we successfully have retrieved our userId and trailId, we can save this point.
        // double lat = location.getLatitude();
        //  double lon = location.getLongitude();
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
        NotificationCompat.Builder noti = new NotificationCompat.Builder(mContext);
        noti.setContentTitle("BreadCrumbs");
        noti.setContentText(location.getSpeed() + "is Speed , Provider: " + location.getProvider());
        noti.setSmallIcon(R.drawable.bc64);
        notificationManager.notify(1234, noti.build());
        if (!trailId.equals("-1") || !userId.equals("-1")) {
            dbc.saveTrailPoint(trailId, location, userId);
        }
    }

    // Class to run locationListerns onChanged methods.
    static class InternalLocationListenerFilter implements LocationListener
    {
        BreadcrumbsLocationProvider mManager;
        LocationListener mListener;
        //
        InternalLocationListenerFilter(BreadcrumbsLocationProvider manager, LocationListener listener) {
            mManager = manager;
            mListener = listener;
        }

        @Override
        public void onLocationChanged(Location location)
        {
            final BreadcrumbsLocationProvider manager = mManager;
            final LocationListener listener = mListener;

            // Garbage Cleaner might have gobbled the listener, so check that it is not null
            if (manager != null && listener != null) {
                if (manager.validate(location) && manager.removeUpdates(listener)){
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
    private void startListeningToActivityChangesForUser(int durationInSeconds) {
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
                for (int i = 0; i < numProviders; i++)
                {
                    String provider = providers.get(i);
                    InternalLocationListenerFilter filter = new InternalLocationListenerFilter(this, listener);
                    locationManager.requestLocationUpdates(provider, 0, 0, filter);
                    holder.mFilterList.add(filter);
                }
                holders.add(holder);
            }
        }
    }

    /*
        Listen to gps requests from other apps in the background.
     */
    public boolean ListenPassivelyForGPSUpdatesInBackground() {
        Intent passiveGpsIntentService = new Intent(mContext, LocationService.class);
        PendingIntent passiveGpsPendingIntent = PendingIntent.getService(mContext, 1, passiveGpsIntentService, 0);

        androidLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, passiveGpsPendingIntent);
        return true;
    }

    public void AddGeofences(Location location) {
        mApi.removeGeofences();
        mApi.addGeofence("MYGEOFENCE2", location.getLatitude(), location.getLongitude(), 100, BreadCrumbsPathSenseGeofenceEventReceiver.class);
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
    public void StartListening(LocationListener listener) {
        // Acquire a reference to the system Location Manager
        androidLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

// Define a listener that responds to location updates

// Register the listener with the Location Manager to receive location updates
        androidLocationManager.requestLocationUpdates(androidLocationManager.GPS_PROVIDER, 0, 0, listener);
    }
}
