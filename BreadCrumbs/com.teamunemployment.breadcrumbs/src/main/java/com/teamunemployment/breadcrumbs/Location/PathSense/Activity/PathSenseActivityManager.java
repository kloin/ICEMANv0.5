package com.teamunemployment.breadcrumbs.Location.PathSense.Activity;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import com.pathsense.android.sdk.location.PathsenseDetectedActivities;
import com.pathsense.android.sdk.location.PathsenseDetectedActivity;
import com.pathsense.android.sdk.location.PathsenseDeviceHolding;
import com.pathsense.android.sdk.location.PathsenseLocationProviderApi;
import com.teamunemployment.breadcrumbs.Location.BreadcrumbsLocationProvider;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Trails.TrailManagerWorker;
import com.teamunemployment.breadcrumbs.client.TrailManager;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

/**
 * Written by Josiah Kendall 2016.
 *
 * This is a wrapper class around PathSense's Activity API. This is designed to allow our locationManager
 * know what the users current state is, and make decisions based on that information.
 */
public class PathSenseActivityManager {

    private static final String TAG = PathSenseActivityManager.class.getName();
    // Messages
    private static final int MESSAGE_ON_ACTIVITY_CHANGE = 1;
    private static final int MESSAGE_ON_ACTIVITY_UPDATE = 2;
    private static final int MESSAGE_ON_DEVICE_HOLDING = 3;

    public static final String DRIVING = "IN_VEHICLE";
    public static final String WALKING = "ON_FOOT";

    private static final String SAVED_ACTIVITY_KEY = "RECORDING_ACTIVITY";
    private static final String PENDING_ACTIVITY_KEY = "PENDING_ACTIVITY";
    private static final String PENDING_ACTIVITY_COUNT_KEY = "PENDING_ACTIVITY_COUNT";
    //
    private InternalActivityChangeReceiver mActivityChangeReceiver;
    private InternalActivityUpdateReceiver mActivityUpdateReceiver;
    private InternalDeviceHoldingReceiver mDeviceHoldingReceiver;
    private InternalHandler mHandler;
    private PathsenseLocationProviderApi mApi;
    private TextView mTextDetectedActivity0;
    private TextView mTextDetectedActivity1;
    private TextView mTextDeviceHolding;
    private Context mContext;
    private BreadcrumbsLocationProvider mLocationProvider;
    private DatabaseController dbc;
    private int eventId;
    private String trailId;
    private SharedPreferences mPreferences;

    private PreferencesAPI mPreferencesApi;
    // Public constructor for now.
    public PathSenseActivityManager(Context context) {
        mContext = context;

        JodaTimeAndroid.init(context);
        mLocationProvider = BreadcrumbsLocationProvider.getInstance(mContext);
        dbc = new DatabaseController(context);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    /*
        Only for testing - Do not use this for production it will break shit
     */
    public PathSenseActivityManager() {
        //mLocationProvider =
    }

    public void StartPathSenseActivityManager() {
        mHandler = new InternalHandler(this);
        // receivers
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        mActivityChangeReceiver = new InternalActivityChangeReceiver(this);
        localBroadcastManager.registerReceiver(mActivityChangeReceiver, new IntentFilter("activityChange"));
        mActivityUpdateReceiver = new InternalActivityUpdateReceiver(this);
        localBroadcastManager.registerReceiver(mActivityUpdateReceiver, new IntentFilter("activityUpdate"));
        mDeviceHoldingReceiver = new InternalDeviceHoldingReceiver(this);
        localBroadcastManager.registerReceiver(mDeviceHoldingReceiver, new IntentFilter("deviceHolding"));
        // location api
        mApi = PathsenseLocationProviderApi.getInstance(mContext);
        mApi.requestActivityChanges(PathsenseActivityChangeBroadcastReceiver.class);
        mApi.requestActivityUpdates(PathsenseActivityUpdateBroadcastReceiver.class);
        mApi.requestDeviceHolding(PathsenseDeviceHoldingBroadcastReceiver.class);

    }

    // Stop listening to updates from activities.
    public void RemoveActivityUpdates() {
        mApi.removeActivityChanges();
        mApi.removeActivityUpdates();
        mApi.removeDeviceHolding();
    }

    private static class InternalActivityChangeReceiver extends BroadcastReceiver {
        PathSenseActivityManager mActivity;

        InternalActivityChangeReceiver(PathSenseActivityManager activity) {
            mActivity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final PathSenseActivityManager activity = mActivity;
            final InternalHandler handler = activity != null ? activity.mHandler : null;
            if (activity != null && handler != null) {
                PathsenseDetectedActivities detectedActivities = (PathsenseDetectedActivities) intent.getSerializableExtra("detectedActivities");
                Message msg = Message.obtain();
                msg.what = MESSAGE_ON_ACTIVITY_CHANGE;
                msg.obj = detectedActivities;
                handler.sendMessage(msg);
            }
        }
    }

    private static class InternalActivityUpdateReceiver extends BroadcastReceiver {
        PathSenseActivityManager mActivity;

        // Recieve updataes on a users activity
        InternalActivityUpdateReceiver(PathSenseActivityManager activity) {
            mActivity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final PathSenseActivityManager activity = mActivity;
            final InternalHandler handler = activity != null ? activity.mHandler : null;
            //
            if (activity != null && handler != null) {
                PathsenseDetectedActivities detectedActivities = (PathsenseDetectedActivities) intent.getSerializableExtra("detectedActivities");
                Message msg = Message.obtain();
                msg.what = MESSAGE_ON_ACTIVITY_UPDATE;
                msg.obj = detectedActivities;
                handler.sendMessage(msg);
            }
        }
    }

    private static class InternalDeviceHoldingReceiver extends BroadcastReceiver {
        PathSenseActivityManager mActivity;

        //
        InternalDeviceHoldingReceiver(PathSenseActivityManager activity) {
            mActivity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final PathSenseActivityManager activity = mActivity;
            final InternalHandler handler = activity != null ? activity.mHandler : null;

            //
            if (activity != null && handler != null) {
                PathsenseDeviceHolding deviceHolding = (PathsenseDeviceHolding) intent.getSerializableExtra("deviceHolding");
                Message msg = Message.obtain();
                msg.what = MESSAGE_ON_DEVICE_HOLDING;
                msg.obj = deviceHolding;
                handler.sendMessage(msg);
            }
        }
    }

    // Handler for activity changes
    private static class InternalHandler extends Handler {
        PathSenseActivityManager mActivity;

        InternalHandler(PathSenseActivityManager activity) {
            mActivity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            final PathSenseActivityManager activity = mActivity;
            final PathsenseLocationProviderApi api = activity != null ? activity.mApi : null;

            // process the activity so we can make decisions.
            if (activity != null && api != null) {
                switch (msg.what) {
                    case MESSAGE_ON_ACTIVITY_CHANGE: {
                        PathsenseDetectedActivities detectedActivities = (PathsenseDetectedActivities) msg.obj;
                        PathsenseDetectedActivity mostProbableActivity = detectedActivities.getMostProbableActivity();
                        if (mostProbableActivity != null) {
                            activity.updateCurrentActivityStatus(mostProbableActivity.getDetectedActivity().toString());
                        }
                        break;
                    }
                    case MESSAGE_ON_ACTIVITY_UPDATE: {
                        PathsenseDetectedActivities detectedActivities = (PathsenseDetectedActivities) msg.obj;
                        PathsenseDetectedActivity mostProbableActivity = detectedActivities.getMostProbableActivity();
                        if (mostProbableActivity != null) {
                            activity.updateCurrentActivityStatus(mostProbableActivity.getDetectedActivity().toString());
                        }
                        break;
                    }
                    case MESSAGE_ON_DEVICE_HOLDING: {
                        break;
                    }
                }
            }
        }
    }

    // This basically validates the activity from pathsense, by making sure that it checks it 3 times. pathsense is sometimes wrong.
    private void updateCurrentActivityStatus(String currentActivity) {

        Log.d("PS_Activity", "Starting processing of activity of type: " + currentActivity);
        // This is the activity we are currently recording against.
        String savedActivity = mPreferences.getString(SAVED_ACTIVITY_KEY, null);
        Log.d("PS_Activity", "Found savedActivity of type activity of type: " + savedActivity);

        // First time use case for the app. We just set something so that we get going.
        if (savedActivity == null) {
            mPreferences.edit().putString(SAVED_ACTIVITY_KEY, currentActivity).commit();
            return;
        }
        // Basically : If we have the saved activity is curently still or tilting, and so is the detected one, then we want to check for a rest zone.
        if ((savedActivity.equals("TILTING") || savedActivity.equals("STILL")) && (currentActivity.equals("TILTING") || currentActivity.equals("STILL"))) {
            // This checks to see if we are resting. We currently
            String state = mPreferences.getString("STATE", " ");
            Log.d("PS_Activity", "Current state = " + state);
            if (!state.equals("REST")) {
                Log.d("PS_Activity", "Checking for restzone");
                checkForRestZone(currentActivity, mPreferences);
            }
        }

        // If we already have that as our saved activity, don't bother doing anything.
        if (!currentActivity.equals(savedActivity)) {
            // This is our event change
            // In this case we need to do some work to figure out if we should update
            String pendingActivity = mPreferences.getString(PENDING_ACTIVITY_KEY, null);

            if (pendingActivity == null) {
                mPreferences.edit().putString(PENDING_ACTIVITY_KEY, currentActivity).commit();
                mPreferences.edit().putInt(PENDING_ACTIVITY_COUNT_KEY, 0).commit();
                return;
            }

            // If the current pending activity is different from the last one, we need to wipe the pending stuff and start again
            if (!pendingActivity.equals(currentActivity)) {
                mPreferences.edit().putInt(PENDING_ACTIVITY_COUNT_KEY, 0).commit();
                mPreferences.edit().putString(PENDING_ACTIVITY_KEY, currentActivity).commit();
                return;
            }

            // If our pending has been pending last update, we need to check if we need to update our activity that we are recording against
            if (pendingActivity.equals(currentActivity)) {
                int numberOfChecks = mPreferences.getInt(PENDING_ACTIVITY_COUNT_KEY, 0);
                // Currently using quite a few checks to grab a definitive result. Would possibly be better
                // To add filters on the server side as well to not grab events if we just briefly change.
                if (numberOfChecks >= 5) {
                    mPreferences.edit().remove(PENDING_ACTIVITY_KEY).commit();
                    mPreferences.edit().remove(PENDING_ACTIVITY_COUNT_KEY).commit();

                    if (currentActivity == null) {
                        mPreferences.edit().putString(SAVED_ACTIVITY_KEY, "UNKNOWN").commit();
                    }
                    mPreferences.edit().putString(SAVED_ACTIVITY_KEY, currentActivity).commit();
                    //Check to seee if we are changing from driving to walking or vice versa.
                    checkForDrivingEvent(savedActivity, currentActivity);
                } else {
                    numberOfChecks += 1;
                    mPreferences.edit().putInt(PENDING_ACTIVITY_COUNT_KEY, numberOfChecks).commit();
                }
            }
        }
    }

    // Check to see if we are needing to save an event
    private void checkForDrivingEvent(String savedActivity, String currentActivity) {
        // This means we have started driving.
        if (mPreferencesApi == null) {
            mPreferencesApi = PreferencesAPI.GetInstance(mContext);
        }
        if (mPreferencesApi.isTrackingEnabledByUser()) {
            if (currentActivity.equals(DRIVING) && !savedActivity.equals(DRIVING)) {
                // we Are now driving
                mPreferencesApi.SetTransportMethod(TrailManagerWorker.DRIVING);
                mLocationProvider.requestLocationUpdate(activityChangeAndStopEventLocationListener);
                // Driving to not driving use case. We are no longer driving, so we need to start requesting location updates again
            } else if (currentActivity != DRIVING && savedActivity.equals(DRIVING)) { // We were driving but now wer are not
                mPreferencesApi.SetTransportMethod(TrailManagerWorker.ON_FOOT);
                mLocationProvider.StartGPS(600, 200);
            }
        }
    }

    LocationListener activityChangeAndStopEventLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            eventId = mPreferences.getInt("EVENTID", 0);
            trailId = Integer.toString(PreferencesAPI.GetInstance(mContext).GetLocalTrailId());
            // When we save
            dbc.AddMetadata(eventId, DateTime.now().toString(), location.getLatitude(), location.getLongitude(), trailId, TrailManagerWorker.ACTIVITY_CHANGE, TrailManagerWorker.DRIVING);
            eventId += 1;
            mPreferences.edit().putInt("EVENTID",eventId).commit();
            // This was our last location update, so now we stop.
            mLocationProvider.RemoveGPSUpdates();

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



    // Purpose of this method is to check whether we are at rest, and if so reduce battery consumption by pausing services.
    // This may occur quite a few times throughout the day.
    public boolean checkForRestZone(String activity, SharedPreferences preferences) {
        final long minimumTimeInMillis = 300000; // 5 min
        LocalTime localTime = LocalTime.now();
        Log.d("PS_Activity", "Checking for restZone.  ");

        //if (localTime.getHourOfDay() > 0 || ) {
        int timeOfFirstRest = preferences.getInt("FIRST_REST_MILLIS", 0);

        // This occurs when its our first time resting
        if (timeOfFirstRest == 0) {
            preferences.edit().putInt("FIRST_REST_MILLIS", localTime.getMillisOfDay()).commit();
            return false;
        }
        // This is the case when we have been resting for longer than the alloted time (i.e if current time - first recorded rest time is greater than (say 3 hours), then we have been resting for a while
        if (localTime.minusMillis(timeOfFirstRest).getMillisOfDay() > minimumTimeInMillis) {
            preferences.edit().putString("STATE", "REST").commit();

            // We need to ensure we are getting a fresh(ish) location
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

//            Location location = fetchBestRecentLocation(); TODO:NEED TO MOVE TO THIS

            Location location = mLocationProvider.GetBestRecentLocation();
                if (location == null || System.currentTimeMillis() - location.getTime() < minimumTimeInMillis) {
                    singleGpsUpdateWrapper();
                } else {
                    saveRestPoint(preferences, location);
                    preferences.edit().remove("STATE").commit();
                    NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
                    NotificationCompat.Builder noti = new NotificationCompat.Builder(mContext);
                    noti.setContentTitle("RestZone");
                    noti.setContentText("saved");
                    noti.setSmallIcon(R.drawable.bc64);
                    notificationManager.notify(123456789, noti.build());
                    getAndSavePlaceId(location);
                    // Remove repeating updates, because we are at rest now
                    mLocationProvider.RemoveGPSUpdates();
                    mLocationProvider.StopListeningToPathsenseActivityUpdates();
                    // At this point we have successfully recorded a rest zone. We need to post up and wait for geofence to be broken.
                    createGeofence(location);
                    return true;
                }
            }
        //}
        return false;
    }


    private void getAndSavePlaceId(Location location) {

    }

    private void singleGpsUpdateWrapper() {
        Criteria criteria = new Criteria();
        // We need to check what permissions we have before we can start getting any locations. If we cant get the right permissions, we need to just can it
        int fineLocation = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (fineLocation == PackageManager.PERMISSION_GRANTED) {
            criteria.setPowerRequirement(Criteria.ACCURACY_FINE);
        } else if (coarseLocation == PackageManager.PERMISSION_GRANTED) {
            criteria.setPowerRequirement(Criteria.ACCURACY_COARSE);
        } else {
            Log.d("PATHSENSE_ACTIVITY", "User is not allowing location at this point in time, so we are kinda fucked");
            // cant do shit.
            return;
        }
        mLocationProvider.requestLocationUpdate(locationListener);
    }

    private void createGeofence(Location location) {
        mLocationProvider.AddGeofences(location);
    }

    private void saveRestPoint(SharedPreferences preferences, Location location) {

        DatabaseController dbc = new DatabaseController(mContext);
        int trailId = PreferencesAPI.GetInstance(mContext).GetLocalTrailId();
        if (trailId == -1) {
            throw new NullPointerException("Fatal: Trail Id was Null");
        }
        int eventId = preferences.getInt("EVENTID", 0); // BAD IDEA - WE NEED TO SAVE PREFERENCES

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String placeId = "0";
        String timeStamp = DateTime.now().toString();
        dbc.SaveRestZone(Integer.toString(trailId), eventId, latitude, longitude, placeId, timeStamp);

        eventId += 1;
        preferences.edit().putInt("EVENTID", eventId);

    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            saveRestPoint(mPreferences, location);
            mLocationProvider.RemoveGPSUpdates();
            createGeofence(location);
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
