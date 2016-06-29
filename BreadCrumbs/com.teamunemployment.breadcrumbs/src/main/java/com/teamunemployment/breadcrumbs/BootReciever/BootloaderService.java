package com.teamunemployment.breadcrumbs.BootReciever;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.teamunemployment.breadcrumbs.ActivityRecognition.ActivityController;
import com.teamunemployment.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.teamunemployment.breadcrumbs.PreferencesAPI;

/**
 *  Bootloader class to check the whether we need to start tracking on device startup. This is a
 *  service that we call from the acutal bootloader.
 */
public class BootloaderService extends Service{

    public void onCreate() {
        super.onCreate();
        PreferencesAPI preferencesAPI = new PreferencesAPI(this);
        boolean isTracking = preferencesAPI.isTrackingEnabledByUser();
        int localTrailId = new PreferencesAPI(this).GetLocalTrailId();
        if (localTrailId != -1 && isTracking) {
            ActivityController activityController = new ActivityController(this);
            activityController.StartListenting();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
