package com.teamunemployment.breadcrumbs.Location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.teamunemployment.breadcrumbs.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

import java.text.DateFormat;
import java.util.Date;


/**
 * Created by Josiah Kendall August 2015.
 *
 * Breadcrumbs Fused Location Provider is mostly used as a Background tracking service. Has been
 * replaced mostly by the PathSense.
 */
public class BreadCrumbsFusedLocationProvider implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    protected static final String TAG = "location-updates-sample";

    public static boolean isTracking = false;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 900000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected static Location mCurrentLocation;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;
    private Context context;
    private Intent breadcrumbsIntentService;
    private PendingIntent breadcrumbsPendingIntent;
    public BreadCrumbsFusedLocationProvider(Context context) {

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        this.context = context;
        // Update values using data stored in the Bundle.

        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();
        BuildBackgroundService();
    }

    public Location GetLastKnownLocation() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            return LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        }
        return null; // Need to handle this use case
    }


    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /*
     *  The public method to start a service to track a user.
     */
    public void StartBackgroundGPSService() {
        Log.i("GPS", "Background GPS Service starting");
        BuildBackgroundService();
        startLocationUpdates();
    }

    public void StartForegroundGPSService() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() &&
                mLocationRequest != null) {
            Log.d("GPS", "GAC successfully started. Location updates have been requested.");
            // Launch our foreground service. Used while running so that we can easily request gps points.
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    private void BuildBackgroundService() {
        breadcrumbsIntentService = new Intent(context, LocationService.class);
        breadcrumbsPendingIntent = PendingIntent.getService(context, 1, breadcrumbsIntentService, 0);
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setSmallestDisplacement(0);
        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
        mGoogleApiClient.connect();
    }

    // Note really used because it is used in the master class. Keeping this in a seperate class for
    // keeping the other class simpler.
    public static Location GetCurrentLocation() {
        return mCurrentLocation;
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() &&
                mLocationRequest != null && breadcrumbsPendingIntent != null) {
            Log.i("GPS", "Location updates started");
            // Launch our service to listen
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, breadcrumbsPendingIntent);
        } else {

            if (mGoogleApiClient == null) {
                Log.i("GPS", "Error starting location updates. GoogleApiClient was null. Location updates will start " +
                        "when the client is connected.");
            } else {
               Log.i("GPS", "failed to start location updates. GoogleApiClient.isConnected(): " + mGoogleApiClient.isConnected() +". Location updates will start" +
                       "when the client is connected");
            }
            mRequestingLocationUpdates = true;
        }
    }

    /*
     *  Stop the service that we are running in the background.
     */
    public void StopBackgroundGPSSerivce() {
        mRequestingLocationUpdates = false;
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, breadcrumbsPendingIntent);
        }
    }

    protected void startBackgroundLocationTracking() {
        // Begin to fetch location in our background service.
        if (mGoogleApiClient != null && mLocationRequest != null && breadcrumbsPendingIntent != null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, breadcrumbsPendingIntent);
        }
    }

    // Fetch the users current location. We have to provide the callback to do what we want with the data.
    public void GetCurrentPlace(ResultCallback<PlaceLikelihoodBuffer> resultCallback) {
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(resultCallback);
    }

    public void GetPlaceNameFromId(String placeId, ResultCallback<PlaceBuffer> resultCallback) {
        PendingResult<PlaceBuffer> result = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
        result.setResultCallback(resultCallback);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingLocationUpdates) {
            Log.i("GPS", "Location updates was requested before client was connected. Client is now connected and updates will begin");
            startLocationUpdates();
        }
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }
}
