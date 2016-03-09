package com.teamunemployment.breadcrumbs.BootReciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.teamunemployment.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.teamunemployment.breadcrumbs.client.Main;

/**
 * Class which recieves the boot notification from android.
 * Need to check if the phone was turned off while tracking - if so resume tracking.
 */
public class BootReceiver extends BroadcastReceiver {

    BreadCrumbsFusedLocationProvider breadCrumbsFusedLocationProvider;
    @Override
    public void onReceive(Context context, Intent intent) {
       /* Toast.makeText(context, "WORKING", Toast.LENGTH_LONG).show();
        boolean isTracking = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("TRACKING", false);
        breadCrumbsFusedLocationProvider = new BreadCrumbsFusedLocationProvider(context);
        String trailId = PreferenceManager.getDefaultSharedPreferences(context).getString("TRAILID", "-1");
        if (!trailId.equals("-1") && isTracking) {
            breadCrumbsFusedLocationProvider.StartBackgroundGPSService();
        }*/
        Intent serviceIntent = new Intent(context, BootloaderService.class);
        context.startService(serviceIntent);
    }
}
