package com.teamunemployment.breadcrumbs.ActivityRecognition;

import android.Manifest;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.teamunemployment.breadcrumbs.Location.SimpleGps;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.caching.TextCaching;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import java.util.List;

/**
 * @author Josiah Kendall.
 *
 * Class to handle the activity recognition events that get thrown, such as walking, cycling, driving etc.
 *
 * This should be in a service, not an intent service, as
 */
public class ActivityHandler extends Service {
    private DatabaseController dbc;
    private PreferencesAPI preferencesAPI;
    private SimpleGps simpleGps;
    private Context context;
    private static final String TAG = "ActivityHandler";
    private static final float MINIMUM_DISTANCE = 100; // Meters

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        simpleGps = new SimpleGps(this);
        dbc = new DatabaseController(this);
        preferencesAPI = new PreferencesAPI(this);

        // if we have a result in the intent, do our work.
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivity(result);
        }
        return START_STICKY;
    }

    /**
     * The handler for our detected activity
     * @param probableActivities our activity result.
     */
    private void handleDetectedActivity(ActivityRecognitionResult probableActivities) {
        DetectedActivity probableActivity = probableActivities.getMostProbableActivity();
        Log.d(TAG, "Detected Activity : "+ probableActivity.getType() + " Confidence: " + probableActivity.getConfidence());
        TextCaching textCaching = new TextCaching(context);
        String text = "";
        text += textCaching.FetchCachedText("activityStuff");
        text += "\n Activity: " + probableActivity.getType() + " Confidence: " + probableActivity.getConfidence();
        textCaching.CacheText("activityStuff", text);
        if (probableActivity.getConfidence() > 90) {
            handleMostProbablyActivity(probableActivity, probableActivities);
        }
    }

    private void handleMostProbablyActivity(DetectedActivity detectedActivity, ActivityRecognitionResult result) {
        int mostProbableActivity = detectedActivity.getType();
        switch (mostProbableActivity) {
            case DetectedActivity.IN_VEHICLE: {
                handleDrivingActivity();
                Log.d(TAG, "Driving");
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                handleDrivingActivity();
                Log.d(TAG, "On Bycycle");
                break;
            }
            case DetectedActivity.ON_FOOT: {
                Log.d(TAG, "On Foot");
                int inCarProb = result.getActivityConfidence(0);
                if (detectedActivity.getConfidence()> 95 && inCarProb < 50) {
                    handleWalkingActivity();
                }
                break;
            }
            case DetectedActivity.RUNNING: {
                int inCarProb = result.getActivityConfidence(0);
                if (detectedActivity.getConfidence()> 95 && inCarProb < 50) {
                    handleWalkingActivity();
                }
                Log.d(TAG, "RUNNING");
                break;
            }
            case DetectedActivity.STILL: {
                Log.d(TAG,"User at rest, so we are not going to do anything.");
                int inCarProb = result.getActivityConfidence(0);
                if (detectedActivity.getConfidence()> 95 && inCarProb < 50) {
                    handleWalkingActivity();
                }
                Log.d(TAG, "Still");
                break;
            }

            case DetectedActivity.TILTING: {
                int inCarProb = result.getActivityConfidence(0);
                if (detectedActivity.getConfidence()> 95 && inCarProb < 50) {
                    handleWalkingActivity();
                }
                Log.d(TAG, "TITLING");
                break;
            }

            case DetectedActivity.WALKING: {
                int inCarProb = result.getActivityConfidence(0);
                if (detectedActivity.getConfidence()> 95 && inCarProb < 50) {
                    handleWalkingActivity();
                }
                Log.d(TAG, "WALKING");
                break;
            }
            case DetectedActivity.UNKNOWN: {
                //Dont do shit.
                Log.d(TAG, "FATHERS DAY");
                break;
            }
        }
    }

    /**
     * Handle a driving event. These are pretty simple, we just get a netwok event to figure out
     * a rough indication of where we are. (i.e suburb sort of granularity).
     */
    private void handleDrivingActivity() {
        if (preferencesAPI == null) {
            preferencesAPI = new PreferencesAPI(context);
        }
        final int lastActivity = preferencesAPI.GetLastActivity();

        // Fetch a location
        if (simpleGps == null) {
            simpleGps = new SimpleGps(context);
        }

        Location lccation = simpleGps.GetInstantLocation();
        boolean youngEnough = validateLocationTimestamp(lccation);
        if (youngEnough) {
            dbc.SaveActivityPoint(DetectedActivity.IN_VEHICLE, lastActivity, lccation.getLatitude(), lccation.getLongitude(), 1);
            stopSelf();
        }
    }

    /**
     * Method to handle the walking use case. If we are walking we handle location differently
     * than if we are driving.
     */
    private void handleWalkingActivity() {
        // Fetch our last activity for saving.
        int lastActivity = preferencesAPI.GetLastActivity();
        if (lastActivity == -1) {
            // This means that we have not recorded anything yet. We should save this point
            Location location = simpleGps.GetInstantLocation();
            if (location != null) {
                dbc.SaveActivityPoint(DetectedActivity.WALKING, lastActivity,location.getLatitude(), location.getLongitude(), 0);
            } else {
                simpleGps.FetchFineLocation(callbackGenerator(DetectedActivity.WALKING, lastActivity, 0));
            }

        }
        // Fetch last know location first. If that is recent enough, and far enough away from our
        // previous target, we can just same that
        Location location = simpleGps.GetInstantLocation();

        // We need to validate the locations age.
        boolean youngEnough = validateLocationTimestamp(location);
        if (youngEnough) {

            // Get the last location saved.
            Location previousLocation = dbc.GetLastSavedLocation();

            // Ensure that we have moved more than 500 meters. Remember that we may get false positives
            // and vice versa here, because accuracy may be pretty poor. Prepared to incurr this atm.
            boolean movedFarEnough = validateLocationDistance(location, previousLocation);

            // If our point has pretty decent accuracy there is no reason we cannot just use this point that we have and be done with it.
            if(movedFarEnough && location.getAccuracy() < 30 && location.getAccuracy() > 0) {
                Log.d(TAG, "We have moved far enough and we have a fine enough accuracy. We will save a point here.");
                float distanceInMeters = previousLocation.distanceTo(location);
                Log.d(TAG, "We have moved : " + distanceInMeters + " meters");
                if (distanceInMeters < 500) {
                    dbc.SaveActivityPoint(DetectedActivity.WALKING, lastActivity,location.getLatitude(), location.getLongitude(), 0);
                    preferencesAPI.SetLastActivity(DetectedActivity.WALKING);
                    stopSelf();
                } else {
                    dbc.SaveActivityPoint(DetectedActivity.IN_VEHICLE, lastActivity,location.getLatitude(), location.getLongitude(), 0);
                    preferencesAPI.SetLastActivity(DetectedActivity.IN_VEHICLE);
                    stopSelf();
                }

                return;
            } else if (movedFarEnough) {
                Log.d(TAG, "We have moved far enough but there we dont have the accuracy required for a walking point." +
                        " Fetching new fine locaiton now.");
                // Fetch an accurate position and save that.
                simpleGps.FetchFineLocation(callbackGenerator(DetectedActivity.WALKING, lastActivity, 0));

            } else if (previousLocation == null){
                // Save first event.
                dbc.SaveActivityPoint(DetectedActivity.WALKING, DetectedActivity.WALKING, location.getLatitude(), location.getLongitude(), 0);
                stopSelf();
            }
            // If we go right through this loop and we have no reason to save a point beacause
            // a) we havent moved far enough
            // b) we havent got the accuracy required for a point
        } else {
            // Location not recent enough - do checks using a coarse callback. If we have moved enough we will get a more accurate location.
            simpleGps.FetchCoarseLocation(getCoarseDistanceCheckCallback(DetectedActivity.WALKING, lastActivity));
        }
        // Fetch a coarse location. If the coarse location is further


    }

    /**
     * Callback for responding to a coarse location request. This is used to check if we have used a
     */
    private SimpleGps.Callback getCoarseDistanceCheckCallback(final int currentActivity, final int previousActivity) {
        return new SimpleGps.Callback() {
            @Override
            public void doCallback(Location location) {
                Location previousLocation = dbc.GetLastSavedLocation();
                boolean movedFarEnough = validateLocationDistance(location, previousLocation);
                if (!movedFarEnough) {
                    return;
                }
                // Else, we want to do a check to see if we are allowed to get fine location.
                // Check permission
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // WE shouldnt request permissions here, because we are in the background.
                    Log.e(TAG, "User is a dumb cunt and they are not allowing us to use Fine accuracy, so lets just save this shitty point for them");
                    dbc.SaveActivityPoint(currentActivity, previousActivity, location.getLatitude(), location.getLongitude(), 1);
                    stopSelf();
                    return;
                }

                // If we reach here, we have moved far enough and we have fine location enabled
                simpleGps.FetchFineLocation(callbackGenerator(currentActivity, previousActivity, 0));
            }
        };
    }


    private SimpleGps.Callback callbackGenerator(final int currentActivity, final int pastActivity, final int granularity) {
        return new SimpleGps.Callback() {
            @Override
            public void doCallback(Location location) {
                Location previousLocation = dbc.GetLastSavedLocation();

                // If we have been further than 500 meters we dont want to save this as a walking line, but
                // a driving line.
                if (previousLocation.distanceTo(location) > 500) {
                    preferencesAPI.SetLastActivity(DetectedActivity.IN_VEHICLE);
                    dbc.SaveActivityPoint(DetectedActivity.IN_VEHICLE, pastActivity, location.getLatitude(), location.getLongitude(), granularity);
                    stopSelf();
                } else {
                    preferencesAPI.SetLastActivity(currentActivity);
                    dbc.SaveActivityPoint(currentActivity, pastActivity, location.getLatitude(), location.getLongitude(), granularity);
                    stopSelf();
                }

            }
        };
    }

    private boolean validateLocationDistance(Location location, Location previousLocation) {
        if (previousLocation == null) {
            Log.e(TAG, "Failed to find a previous location");
            return false;
        }

        if (location == null) {
            Log.e(TAG, "Failed to find current location");
            return false;
        }
        float distanceInMeters = previousLocation.distanceTo(location);
        return distanceInMeters > MINIMUM_DISTANCE;
    }


    private boolean validateLocationTimestamp(Location location) {
        if (location == null) {
            return false;
        }
        long time= System.currentTimeMillis();
        time -= 240000; // two minute ago
        return time < location.getTime();
    }

    private Criteria getCoarseCriteria() {
        return null;
    }

    private LocationListener getListenerForCoarseRequest() {
        throw new IllegalStateException();
    }
}
