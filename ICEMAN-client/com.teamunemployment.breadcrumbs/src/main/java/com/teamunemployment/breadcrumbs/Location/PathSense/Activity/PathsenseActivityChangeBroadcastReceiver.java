package com.teamunemployment.breadcrumbs.Location.PathSense.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.pathsense.android.sdk.location.PathsenseActivityRecognitionReceiver;
import com.pathsense.android.sdk.location.PathsenseDetectedActivities;

/**
 * Basically taken verbatim from the pathsense github : https://github.com/pathsense/pathsense-samples-android/blob/master/pathsense-activitydemo-app/src/main/java/com/pathsense/activitydemo/app/PathsenseActivityChangeBroadcastReceiver.java
   Purpose of this class is to receive events from pathsense when the activity we are requesting changes.
 */
public class PathsenseActivityChangeBroadcastReceiver extends PathsenseActivityRecognitionReceiver
{
    static final String TAG = PathsenseActivityChangeBroadcastReceiver.class.getName();
    //
    @Override
    protected void onDetectedActivities(Context context, PathsenseDetectedActivities detectedActivities) {
        Log.i(TAG, "detectedActivities = " + detectedActivities);
        // broadcast detected activities
        Intent detectedActivitiesIntent = new Intent("activityChange");
        detectedActivitiesIntent.putExtra("detectedActivities", detectedActivities);
        LocalBroadcastManager.getInstance(context).sendBroadcast(detectedActivitiesIntent);
    }
}
