package com.teamunemployment.breadcrumbs.Location;

/*
    DEPRECIATED - USE BreadcrumbsFusedLocationProvider
 */


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncPost;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.client.Maps.MapDisplayManager;
import com.teamunemployment.breadcrumbs.database.DatabaseController;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

public class CanvasLocationManager implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private Context context;
    private MapDisplayManager mapDisplayManager;
    private static Location lastCheckedLocation;
    private GoogleApiClient googleApiClient;
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
        if(resp == ConnectionResult.SUCCESS && googleApiClient == null){
            googleApiClient =  new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        } else {
            Toast.makeText(context, "Google Play Service Error " + resp, Toast.LENGTH_LONG).show();
        }
    }

    public void RemoveLocationListener() {
        if (googleApiClient != null) {
            //locationclient.removeLocationUpdates(mPendingIntent);
            googleApiClient.disconnect();
        }
    }

    public void AddLocationListenerService() {
        if (connected) {
            locationrequest.setInterval(10000);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationrequest, mPendingIntent);
            //locationclient.requestLocationUpdates(locationrequest, mPendingIntent);
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
        //lastCheckedLocation = ();
        locationrequest = LocationRequest.create();
        connected = true;

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastCheckedLocation = location;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


		
}	
