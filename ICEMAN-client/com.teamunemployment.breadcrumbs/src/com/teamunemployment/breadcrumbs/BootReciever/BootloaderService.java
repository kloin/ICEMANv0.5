package com.teamunemployment.breadcrumbs.BootReciever;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.teamunemployment.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;

/**
 * Created by jek40 on 1/03/2016.
 */
public class BootloaderService extends Service{

    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        boolean isTracking = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("TRACKING", false);
        BreadCrumbsFusedLocationProvider breadCrumbsFusedLocationProvider = new BreadCrumbsFusedLocationProvider(this);
        String trailId = PreferenceManager.getDefaultSharedPreferences(this).getString("TRAILID", "-1");
        //if (!trailId.equals("-1") && isTracking) {
            breadCrumbsFusedLocationProvider.StartBackgroundGPSService();
        //}
        // do something when the service is created
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
