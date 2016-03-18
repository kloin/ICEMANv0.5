package com.teamunemployment.breadcrumbs.Location.PathSense;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

import com.pathsense.android.sdk.location.PathsenseLocationProviderApi;
import com.teamunemployment.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;

/**
 * Written by Josiah Kendall.
 *
 * This class is an implementation of the PathSense API.
 * This is an alternative method of finding gps location.
 * PathSense uses an inertial navigation algorithim which will be super effective on the road via
 * any transport method.
 *
 * When we are not on the road and the inertial system does not work we will fall back to regular
 * GPS which will be more battery intensive, so accuracy sacrafices will have to be made.
 */
public class PathSenseLocationProvider {
    private Context mContext;
    private String TAG = "PATHSENSE";
    public PathSenseLocationProvider(Context context) {
        mContext = context;
    }

    public void BeginTracking() {
        // Acquire a reference to the Pathsense location provider.
        final PathsenseLocationProviderApi pathsenseLocationProvider = PathsenseLocationProviderApi.getInstance(mContext);
        // Fetch Location
        BreadCrumbsFusedLocationProvider locationProvider = new BreadCrumbsFusedLocationProvider(mContext);


        // Now we create location using the geofence.
        Toast.makeText(mContext, "recievedLocation, creating GeoFence",Toast.LENGTH_LONG).show();
       // pathsenseLocationProvider.addGeofence("MYGEOFENCE", location.getLatitude(), location.getLongitude(), 100, BreadCrumbsPathSenseGeofenceEventReciever.class);


// Register the listener with the Pathsense location provider to receive location updates

    }




}
