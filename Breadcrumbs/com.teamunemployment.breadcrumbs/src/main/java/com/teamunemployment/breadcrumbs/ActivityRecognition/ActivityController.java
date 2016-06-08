package com.teamunemployment.breadcrumbs.ActivityRecognition;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

/**
 * Created by jek40 on 25/05/2016.
 */
public class ActivityController implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient googleApiClient;
    private Context context;
    private boolean requestingUpdates;
    @Override
    public void onConnected(Bundle bundle) {
        if (requestingUpdates) {
            Intent intent = new Intent( context, ActivityHandler.class);
            PendingIntent pendingIntent = PendingIntent.getService( context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            int fiveMin = 500000;
            int testTime = 3000;
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(googleApiClient, fiveMin, pendingIntent);
        }

        if (!requestingUpdates) {
            Intent intent = new Intent( context, ActivityHandler.class);
            PendingIntent pendingIntent = PendingIntent.getService( context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(googleApiClient, pendingIntent);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public ActivityController(Context context) {
        this.context = context;
    }

    public void StartListenting() {
        requestingUpdates = true;
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();
    }

    public void StopListening() {
        requestingUpdates = false;
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();
    }
}
