package com.breadcrumbs.Location;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.ServiceProxy.AsyncPost;
import com.breadcrumbs.ServiceProxy.HTTPRequestHandler;
import com.breadcrumbs.client.Maps.MapDisplayManager;
import com.breadcrumbs.client.RunOnUiThreads;
import com.breadcrumbs.database.DatabaseController;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONObject;

public class CanvasLocationManager implements GooglePlayServicesClient.ConnectionCallbacks,GooglePlayServicesClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private Context context;
    private MapDisplayManager mapDisplayManager;
    private static Location lastCheckedLocation;
    private LocationClient locationclient;
    private LocationRequest locationrequest;
    private Intent mIntentService;
    private PendingIntent mPendingIntent;
    private boolean connected = false;
    public CanvasLocationManager(Context context) {
        this.context = context;
        SetUpLocationListener();
    }

    public void SetUpLocationListener() {
        mIntentService = new Intent(context, LocationService.class);
        mPendingIntent = PendingIntent.getService(context, 1, mIntentService, 0);
        int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if(resp == ConnectionResult.SUCCESS){
            locationclient = new LocationClient(context,this,this);
            locationclient.connect();
        } else {
            Toast.makeText(context, "Google Play Service Error " + resp, Toast.LENGTH_LONG).show();
        }
    }

    public void RemoveLocationListener() {
        if (locationclient != null) {
            locationclient.removeLocationUpdates(mPendingIntent);
        }
    }

    public void AddLocationListenerService() {
        if (connected) {
            locationrequest.setInterval(10000);
            locationclient.requestLocationUpdates(locationrequest, mPendingIntent);
        }
        else {
            // We need to let the user know why we cannot connect.
            //THis will happen because a) they have no ability to get location - ie gps is off,
            // or we have programed wrong and need to make changes
        }
    }

    public static Location getLastCheckedLocation() {
        if(lastCheckedLocation != null){
            return lastCheckedLocation;
        }
        // Elses
        return null;

    }

    public void fetchTrailPointDataAndSaveItToTheServer() {
        final String trailPointsId;
        // HACZ - cos im just testing.
        final DatabaseController dbc = new DatabaseController(context);
        // But how will I actually know this?
        final String trailId = PreferenceManager.getDefaultSharedPreferences(context).getString("TRAILID", "-1");
        // Dont want to be saving a non existent trail - I will do other shit with it first
        if (!trailId.equals("-1")) {
            JSONObject jsonObject = dbc.getAllSavedTrailPoints(trailId);
            HTTPRequestHandler saver = new HTTPRequestHandler();

            // Save our trails.
            String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/SaveTrailPoints/";
            AsyncPost post = new AsyncPost(url, new AsyncPost.RequestListener() {
                @Override
                public void onFinished(String result) {
                    dbc.DeleteAllSavedTrailPoints(trailId);
                }
            }, jsonObject);

            post.execute();
        }
    }



    @Override
    public void onConnected(Bundle bundle) {
        lastCheckedLocation = locationclient.getLastLocation();
        locationrequest = LocationRequest.create();
        connected = true;

    }

    @Override
    public void onDisconnected() {
        // Close?
    }

    @Override
    public void onLocationChanged(Location location) {
        lastCheckedLocation = location;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


		
}	
