package com.teamunemployment.breadcrumbs.Location.PathSense;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.pathsense.android.sdk.location.PathsenseGeofenceEvent;
import com.pathsense.android.sdk.location.PathsenseGeofenceEventReceiver;

/**
 * Reciever for the events of a geofence being exceeded by the tracking of our user, as according to the PathSense Sensors.
 */
public class BreadCrumbsPathSenseGeofenceEventReceiver extends PathsenseGeofenceEventReceiver{
    static final String TAG = PathsenseGeofenceEventReceiver.class.getName();

    // We intercept the event here, so we know the location etc..
    @Override
    protected void onGeofenceEvent(Context context, PathsenseGeofenceEvent geofenceEvent)
    {
        Log.i(TAG, "geofence = " + geofenceEvent.getGeofenceId() + ", " + geofenceEvent.getLatitude() +
                ", " + geofenceEvent.getLongitude() + ", " + geofenceEvent.getRadius());

        // Not really sure of the difference between ingress and egress.
        if (geofenceEvent.isEgress()) {
            Location location = geofenceEvent.getLocation();
            Log.i(TAG, "geofenceEgress = " + location.getTime() + ", " + location.getProvider() +
                    ", " + location.getLatitude() + ", " + location.getLongitude() + ", " +
                    location.getAltitude() + ", " + location.getSpeed() + ", " + location.getBearing() +
                    ", " + location.getAccuracy());

            // broadcast event
            Intent geofenceEventIntent = new Intent("geofenceEvent");
            geofenceEventIntent.putExtra("geofenceEvent", geofenceEvent);
            LocalBroadcastManager.getInstance(context).sendBroadcast(geofenceEventIntent);
        } else if (geofenceEvent.isIngress()) {
            Location location = geofenceEvent.getLocation();
            Log.i(TAG, "geofenceIngress = " + location.getTime() + ", " + location.getProvider() +
                    ", " + location.getLatitude() + ", " + location.getLongitude() + ", " + location.getAltitude() +
                    ", " + location.getSpeed() + ", " + location.getBearing() + ", " + location.getAccuracy());

            // broadcast event
            Intent geofenceEventIntent = new Intent("geofenceEvent");
            geofenceEventIntent.putExtra("geofenceEvent", geofenceEvent);
            LocalBroadcastManager.getInstance(context).sendBroadcast(geofenceEventIntent);
        }
    }
}