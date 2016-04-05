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
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.pathsense.android.sdk.location.PathsenseDetectedActivities;
import com.pathsense.android.sdk.location.PathsenseDetectedActivity;
import com.pathsense.android.sdk.location.PathsenseDeviceHolding;
import com.pathsense.android.sdk.location.PathsenseLocationProviderApi;
import com.teamunemployment.breadcrumbs.BreadcrumbsActivityAPI;
import com.teamunemployment.breadcrumbs.Location.BreadcrumbsLocationProvider;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

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

    // Public constructor for now.
    public PathSenseActivityManager(Context context) {
        mContext = context;
        JodaTimeAndroid.init(context);
        mLocationProvider = BreadcrumbsLocationProvider.getInstance(mContext);
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

    private static class InternalActivityChangeReceiver extends BroadcastReceiver {
        PathSenseActivityManager mActivity;
        //
        InternalActivityChangeReceiver(PathSenseActivityManager activity) {
            mActivity = activity;
        }
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final PathSenseActivityManager activity = mActivity;
            final InternalHandler handler = activity != null ? activity.mHandler : null;
            //
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
        //
        InternalActivityUpdateReceiver(PathSenseActivityManager activity)
        {
            mActivity = activity;
        }
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final PathSenseActivityManager activity = mActivity;
            final InternalHandler handler = activity != null ? activity.mHandler : null;
            //
            if (activity != null && handler != null)
            {
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
        InternalDeviceHoldingReceiver(PathSenseActivityManager activity)
        {
            mActivity = activity;
        }
        @Override
        public void onReceive(Context context, Intent intent)
        {
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

    private static class InternalHandler extends Handler {
        PathSenseActivityManager mActivity;
        //
        InternalHandler(PathSenseActivityManager activity)
        {
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
                    } case MESSAGE_ON_DEVICE_HOLDING: {
                        break;
                    }
                }
            }
        }
    }

    // This basically validates the activity from pathsense, by making sure that it checks it 3 times. pathsense is sometimes wrong.
    private void updateCurrentActivityStatus(String currentActivity) {
        final String SAVED_ACTIVITY_KEY = "RECORDING_ACTIVITY";
        final String PENDING_ACTIVITY_KEY = "PENDING_ACTIVITY";
        final String PENDING_ACTIVITY_COUNT_KEY = "PENDING_ACTIVITY_COUNT";

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        // This is the activity we are currently recording against.
        String savedActivity = preferences.getString(SAVED_ACTIVITY_KEY, null);

        // Basically : If we have the saved activity is curently still or tilting, and so is the detected one, then we want to check for a rest zone.
        if ((savedActivity.equals("TILTING") || savedActivity.equals("STILL")) && (currentActivity.equals("TILTING") || currentActivity.equals("STILL")) ) {
            // This checks to see if we are resting. We currently
            String state = preferences.getString("STATE", null);
            if (state!= null && !state.equals("REST")) {
                checkForRestZone(currentActivity, preferences);
            }
        }

        // First time use case for the app.
        if (savedActivity == null) {
            preferences.edit().putString(SAVED_ACTIVITY_KEY, currentActivity).commit();
            return;
        }

        // If we already have that as our pending activity, don't bother doing anything.
        if (!currentActivity.equals(savedActivity)) {
            // In this case we need to do some work to figure out if we should update
            String pendingActivity = preferences.getString(PENDING_ACTIVITY_KEY, null);

            if (pendingActivity == null) {
                preferences.edit().putString(PENDING_ACTIVITY_KEY, currentActivity).commit();
                preferences.edit().putInt(PENDING_ACTIVITY_COUNT_KEY, 0).commit();
                return;
            }

            // If the current pending activity is different from the last one, we need to wipe the pending stuff and start again
            if (!pendingActivity.equals(currentActivity)) {
                preferences.edit().putInt(PENDING_ACTIVITY_COUNT_KEY, 0).commit();
                preferences.edit().putString(PENDING_ACTIVITY_KEY, currentActivity).commit();
                return;
            }

            // If our pending has been pending last update, we need to check if we need to update our activity that we are recording against
            if (pendingActivity.equals(currentActivity)) {
                int numberOfChecks = preferences.getInt(PENDING_ACTIVITY_COUNT_KEY, 0);
                // If we have checked on 2 previous occasions, we need to update our activity.
                if (numberOfChecks >= 1) {
                    preferences.edit().remove(PENDING_ACTIVITY_KEY).commit();
                    preferences.edit().remove(PENDING_ACTIVITY_COUNT_KEY).commit();
                    preferences.edit().putString(SAVED_ACTIVITY_KEY, currentActivity).commit();

                    NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
                    NotificationCompat.Builder noti = new NotificationCompat.Builder(mContext);
                    noti.setContentTitle("BreadCrumbs");
                    noti.setContentText("Current Activity : "+ currentActivity);
                    noti.setSmallIcon(R.drawable.bc64);
                    notificationManager.notify(1234, noti.build());
                } else {
                    numberOfChecks += 1;
                    preferences.edit().putInt(PENDING_ACTIVITY_COUNT_KEY, numberOfChecks).commit();
                }
            }
        }
    }

    // Purpose of this method is to check whether we are at rest, and if so reduce battery consumption by pausing services.
    private void checkForRestZone(String activity, SharedPreferences preferences) {
        final long minimumTimeInMillis = 1800000;
        LocalTime localTime = LocalTime.now();
        if (localTime.getHourOfDay() > 0 || localTime.getHourOfDay() < 6) {
            int timeOfFirstRest = preferences.getInt("FIRST_REST_MILLIS", 0);

            // This occurs when its our first time resting
            if (timeOfFirstRest == 0) {
                preferences.edit().putInt("FIRST_REST_MILLIS", localTime.getMillisOfDay()).commit();
                return;
            }

            // This is the case when we have been resting for longer than the alloted time (i.e if current time - first recorded rest time is greater than (say 3 hours), then we have been resting for a while
            if (localTime.minusMillis(timeOfFirstRest).getMillisOfDay() > minimumTimeInMillis) {
                preferences.edit().putString("STATE", "REST").commit();

                // Remove repeating updates, because we are at rest now
                mLocationProvider.RemoveGPSUpdates();

                // We need to ensure we are getting a fresh(ish) location
                Location location = mLocationProvider.androidLocationManager.getLastKnownLocation("GPS");
                long age = System.currentTimeMillis() - location.getTime();

                // We want to
                if (age < minimumTimeInMillis) {
                    Criteria criteria = new Criteria();
                    criteria.setAccuracy(30);
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
                    mLocationProvider.androidLocationManager.requestSingleUpdate(criteria, locationListener, null);
                } else {
                    saveRestPoint(preferences, location);
                    // At this point we have successfully recorded a rest zone. We need to post up and wait for geofence to be broken.
                    createGeofence(location);
                }




            }
        }
    }

    private void createGeofence(Location location) {
        mLocationProvider.AddGeofences(location);
    }

    private void saveRestPoint(SharedPreferences preferences, Location location) {
        DatabaseController dbc = new DatabaseController(mContext);
        String trailId = preferences.getString("TRAILID", null);
        if (trailId == null) {
            throw new NullPointerException("Fatal: Trail Id was Null");
        }

        String eventId = preferences.getString("EVENTID", null);
        if (eventId == null) {
            throw new NullPointerException("Fatal: Event Id was null");
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String placeId = "0";
        String timeStamp = DateTime.now().toString();
        dbc.SaveRestZone(trailId, eventId, latitude, longitude, placeId, timeStamp);
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // Save our variables here
            // Save our restzone to the db.
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            saveRestPoint(preferences, location);
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
