package com.teamunemployment.breadcrumbs.Location;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.pathsense.android.sdk.location.PathsenseGeofenceEvent;
import com.pathsense.android.sdk.location.PathsenseLocationProviderApi;
import com.teamunemployment.breadcrumbs.Location.PathSense.BreadCrumbsPathSenseGeofenceEventReceiver;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Written by Josiah Kendall.
 *
 * This is the master class for gps access. All gps calls should eventually go through here. This
 * is the main interface.
 */
public class BreadcrumbsLocationProvider implements LocationListener {

    public static boolean isPathSenseRunning = false;
    public static LocationManager androidLocationManager;

    // Messages
    static final int MESSAGE_ON_GEOFENCE_EVENT = 1;
    static final String TAG = BreadcrumbsLocationProvider.class.getName();
    static BreadcrumbsLocationProvider sInstance;

    // Manages fetching location from androids Location

    private PathsenseLocationProviderApi mPathsenseLocationProviderApi;
    public InternalHandler mHandler;
    private Geofence geofence;
    private Context mContext;
    Circle mGeofence;
    Circle mGeofenceEgress;
    Circle mGeofenceIngress;
    private InternalLocalGeofenceEventReceiver mLocalGeofenceEventReceiver;
    private PathsenseLocationProviderApi mApi;
    private SharedPreferences mPreferences;
    private int mFindLocation;

    /*
        Singleton Entry point
     */
    public static synchronized BreadcrumbsLocationProvider getInstance(Context context)
    {
        if (sInstance == null)
        {
            sInstance = new BreadcrumbsLocationProvider(context);
        }
        return sInstance;
    }
    // ---------------------- Instance Fields
    Queue<InternalHolder> mHolders = new ConcurrentLinkedQueue<InternalHolder>();
    // ---------------------- Instance Methods
    private BreadcrumbsLocationProvider(Context context) {
        mContext = context;
        androidLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mPathsenseLocationProviderApi = PathsenseLocationProviderApi.getInstance(context);
    }

    /*
        Location Listener Overrides Start
     *******************************************************************************
      */
    @Override
    public void onLocationChanged(Location location) {

        final SharedPreferences preferences = mPreferences;
        final PathsenseLocationProviderApi api = mApi;

        if (preferences != null && api != null)
        {
            if (mFindLocation == 1) {
                mFindLocation = 0;
            } else {
                // add 100m geofence around me
                api.addGeofence("MYGEOFENCE", location.getLatitude(), location.getLongitude(), 100, BreadCrumbsPathSenseGeofenceEventReceiver.class);
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
        requestLocationUpdate(this);
    }

    // Just sort shit out before we start listening to the pathsense, incase of previous instances.
    private void cleanUp() {
        if (mGeofence != null)
        {
            mGeofence.remove();
            mGeofence = null;
        }
        if (mGeofenceEgress != null)
        {
            mGeofenceEgress.remove();
            mGeofenceEgress = null;
        }
        if (mGeofenceIngress != null)
        {
            mGeofenceIngress.remove();
            mGeofenceIngress = null;
        }
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
    static class InternalHolder
    {
        LocationListener mListener;
        List<InternalLocationListenerFilter> mFilterList;
    }

    /*
        The InternalLocalGeoFenceEventReviever is where the updates on what our position is when the geofence is broken.
     */
    static class InternalLocalGeofenceEventReceiver extends BroadcastReceiver
    {
        BreadcrumbsLocationProvider mActivity;
        //
        InternalLocalGeofenceEventReceiver(BreadcrumbsLocationProvider activity) {
            mActivity = activity;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            final BreadcrumbsLocationProvider activity = mActivity;
            final InternalHandler handler = activity != null ? activity.mHandler : null;
            //
            if (activity != null && handler != null)
            {
                // local broadcast from PathsenseGeofenceEventBroadcastReceiver
                PathsenseGeofenceEvent geofenceEvent = (PathsenseGeofenceEvent) intent.getSerializableExtra("geofenceEvent");
                Message msg = Message.obtain();
                msg.what = MESSAGE_ON_GEOFENCE_EVENT;
                msg.obj = geofenceEvent;
                handler.sendMessage(msg);
            }
        }
    }

    /*
            This class is where we handle the location / Perimeter breach events.
     */
    static class InternalHandler extends Handler
    {
        BreadcrumbsLocationProvider mActivity;
        //
        int mZIndex;
        //
        InternalHandler(BreadcrumbsLocationProvider activity) {
            mActivity = activity;
        }
        @Override
        public void handleMessage(Message msg) {
            final BreadcrumbsLocationProvider activity = mActivity;
            //
            if (activity != null) {
                switch (msg.what) {
                    case MESSAGE_ON_GEOFENCE_EVENT: {
                        PathsenseGeofenceEvent geofenceEvent = (PathsenseGeofenceEvent) msg.obj;
                        Location location = geofenceEvent.getLocation();
                        activity.ProcessUpdatedLocation(location);

                        // Trigger another api call.
                        activity.mApi.addGeofence("MYGEOFENCE", location.getLatitude(), location.getLongitude(), 100, BreadCrumbsPathSenseGeofenceEventReceiver.class);

                        }
                    }
                }
            }
        }


    private void ProcessUpdatedLocation(Location location) {
        // Save to database.
        DatabaseController dbc = new DatabaseController(mContext);
        // Save to our db.

        // Toast.makeText(this, location.getSpeed()+ "", Toast.LENGTH_SHORT).show();

        /*
         * Base conditions - We must be moving. We must have some level of accuracy. Hopefully this will sop
         */
        //if (location.getAccuracy() <= 60) {
        // I should actually set current trail Id at the end. Having more than one trail at a time does not make sense.
        String trailId = PreferenceManager.getDefaultSharedPreferences(mContext).getString("TRAILID", "-1");
        String userId = PreferenceManager.getDefaultSharedPreferences(mContext).getString("USERID", "-1");
        // If we successfully have retrieved our userId and trailId, we can save this point.
        // double lat = location.getLatitude();
        //  double lon = location.getLongitude();

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
    boolean validate(Location location)
    {
        if (location != null)
        {

            String provider = location.getProvider();
            if (androidLocationManager.NETWORK_PROVIDER.equals(provider))
            {
                double accuracy = location.getAccuracy();
                long age = System.currentTimeMillis() - location.getTime();
                Log.i(TAG, "provider=" + provider + ",accuracy=" + accuracy + ",age=" + age);
                if (location.getAccuracy() <= 100.0 && age <= 20000)
                {
                    return true;
                }
            } else if (androidLocationManager.GPS_PROVIDER.equals(provider))
            {
                if (location.getAccuracy() <= 50.0 && (System.currentTimeMillis() - location.getTime()) <= 5000)
                {
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
