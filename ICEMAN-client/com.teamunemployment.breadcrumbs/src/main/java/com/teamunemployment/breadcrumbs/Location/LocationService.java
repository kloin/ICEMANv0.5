package com.teamunemployment.breadcrumbs.Location;

/**
 * Created by aDirtyCanvas on 5/14/2015.
 */

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.teamunemployment.breadcrumbs.Preferences.Preferences;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.database.DatabaseController;
//import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.FusedLocationProviderApi;

public class LocationService extends IntentService {

    private double MIN_SPEED = 0.233;
    private int count = 0;
    private String TAG = this.getClass().getSimpleName();
    public LocationService() {
        super("Fused Location");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        count += 1;
        Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
        if(location !=null){
            ProcessUpdatedLocation(location);
            Log.i(TAG, "onHandleIntent " + location.getLatitude() + "," + location.getLongitude());
        }
    }

    private void ProcessUpdatedLocation(Location location) {
        // Save to database.
        DatabaseController dbc = new DatabaseController(this.getApplicationContext());
        if (location.getAccuracy() <= 40) {
            String trailId = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("TRAILID", "-1");
            String userId = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("USERID", "-1");
            if (!trailId.equals("-1") || !userId.equals("-1")) {
                // Save our trail point to the db.
                dbc.saveTrailPoint(trailId, location, userId);
                // This is mostly debug. When we are tracking in production we will be showing an unremovable notification as per google terms etc.
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Builder noti = new NotificationCompat.Builder(this);
                noti.setContentTitle("BreadCrumbs");
                noti.setContentText("Provided by " + location.getProvider());
                noti.setSmallIcon(R.drawable.bc64);
                notificationManager.notify(1234, noti.build());

                // We also want to trigger updates to pathsense.
               // BreadcrumbsLocationProvider breadcrumbsLocationProvider = BreadcrumbsLocationProvider.getInstance(this.getApplicationContext());
                //breadcrumbsLocationProvider.AddGeofences(location);
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();


    }

}
