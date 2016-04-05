package com.teamunemployment.breadcrumbs.Location.PathSense.Activity;

/**
 * stolen from pathsense github : https://github.com/pathsense/pathsense-samples-android/blob/master/pathsense-activitydemo-app/src/main/java/com/pathsense/activitydemo/app/PathsenseDeviceHoldingBroadcastReceiver.java
 */
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.pathsense.android.sdk.location.PathsenseDeviceHolding;
import com.pathsense.android.sdk.location.PathsenseDeviceHoldingReceiver;
public class PathsenseDeviceHoldingBroadcastReceiver extends PathsenseDeviceHoldingReceiver
{
    static final String TAG = PathsenseActivityChangeBroadcastReceiver.class.getName();
    //
    @Override
    protected void onDeviceHolding(Context context, PathsenseDeviceHolding deviceHolding) {
        Log.i(TAG, "deviceHolding = " + deviceHolding);
        // broadcast device holding
        Intent deviceHoldingIntent = new Intent("deviceHolding");
        deviceHoldingIntent.putExtra("deviceHolding", deviceHolding);
        LocalBroadcastManager.getInstance(context).sendBroadcast(deviceHoldingIntent);
    }
}