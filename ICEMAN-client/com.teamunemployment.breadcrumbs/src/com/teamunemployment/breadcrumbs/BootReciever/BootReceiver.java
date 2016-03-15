package com.teamunemployment.breadcrumbs.BootReciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.teamunemployment.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.teamunemployment.breadcrumbs.client.Main;

/**
 * The actual bootloader.
 * Need to check if the phone was turned off while tracking - if so resume tracking.
 */
public class BootReceiver extends BroadcastReceiver {

    BreadCrumbsFusedLocationProvider breadCrumbsFusedLocationProvider;
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, BootloaderService.class);
        context.startService(serviceIntent);
    }
}
