package com.teamunemployment.breadcrumbs.Location.PathSense.Activity;

/**
 * Stolen from pathsense github : https://github.com/pathsense/pathsense-samples-android/blob/master/pathsense-activitydemo-app/src/main/java/com/pathsense/activitydemo/app/PathsenseActivityUpdateBroadcastReceiver.java
 */
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.pathsense.android.sdk.location.PathsenseActivityRecognitionReceiver;
import com.pathsense.android.sdk.location.PathsenseDetectedActivities;
public class PathsenseActivityUpdateBroadcastReceiver extends PathsenseActivityRecognitionReceiver
{
    static final String TAG = PathsenseActivityUpdateBroadcastReceiver.class.getName();
    @Override
    protected void onDetectedActivities(Context context, PathsenseDetectedActivities detectedActivities)
    {
        Log.i(TAG, "detectedActivities = " + detectedActivities);
        // broadcast detected activities
        Intent detectedActivitiesIntent = new Intent("activityUpdate");
        detectedActivitiesIntent.putExtra("detectedActivities", detectedActivities);
        LocalBroadcastManager.getInstance(context).sendBroadcast(detectedActivitiesIntent);
    }
}
