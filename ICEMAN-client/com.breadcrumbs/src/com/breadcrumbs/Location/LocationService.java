package com.breadcrumbs.Location;

/**
 * Created by aDirtyCanvas on 5/14/2015.
 */

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.widget.Toast;

import com.breadcrumbs.caching.GlobalContainer;
import com.breadcrumbs.R;
import com.breadcrumbs.database.DatabaseController;
//import com.google.android.gms.location.LocationClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

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
            // Save to our db.

       // Toast.makeText(this, location.getSpeed()+ "", Toast.LENGTH_SHORT).show();

        /*
         * Base conditions - We must be moving. We must have some level of accuracy. Hopefully this will sop
         */
        if (location.getAccuracy() <= 80) {
            // I should actually set current trail Id at the end. Having more than one trail at a time does not make sense.
            String trailId = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("TRAILID", "-1");
            String userId = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("USERID", "-1");
            // If we successfully have retrieved our userId and trailId, we can save this point.
           // double lat = location.getLatitude();
          //  double lon = location.getLongitude();

            if (!trailId.equals("-1") || !userId.equals("-1")) {
                dbc.saveTrailPoint(trailId, location, userId);
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Builder noti = new NotificationCompat.Builder(this);
                noti.setContentTitle("BreadCrumbs");
                noti.setContentText(location.getSpeed() + "is Speed , Provider: " + location.getProvider());
                noti.setSmallIcon(R.drawable.ic_launcher);
                notificationManager.notify(1234, noti.build());
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();


    }

}
