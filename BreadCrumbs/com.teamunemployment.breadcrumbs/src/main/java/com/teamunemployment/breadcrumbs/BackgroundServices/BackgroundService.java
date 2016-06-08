package com.teamunemployment.breadcrumbs.BackgroundServices;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.teamunemployment.breadcrumbs.BackgroundServices.MessageObjects.GPSStartEvent;
import com.teamunemployment.breadcrumbs.BackgroundServices.MessageObjects.GPSStopEvent;
import com.teamunemployment.breadcrumbs.BackgroundServices.MessageObjects.SingleGPSRequest;
import com.teamunemployment.breadcrumbs.BackgroundServices.MessageObjects.TrailObject;
import com.teamunemployment.breadcrumbs.Location.BreadcrumbsLocationProvider;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.Trails.TrailManagerWorker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * @author Josiah Kendall
 *
 * Background service for breadcrumbs. Manages many tasks and activities.
 */
public class BackgroundService extends Service {
    private SharedPreferences mPreferences;
    private BreadcrumbsLocationProvider locationProvider;
    private final EventBus bus = EventBus.getDefault();
    private PreferencesAPI mPreferencesApi;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mPreferencesApi = new PreferencesAPI(this);
        bus.register(this);
      //  handleServiceStart();
        return START_STICKY; // Check if we need to restart the service when we end it.
    }

    // Here we check what we should be doing in terms of starting
    private void handleServiceStart() {
        boolean tracking = mPreferencesApi.isTracking();
        if (tracking) {
            mPreferencesApi.DeleteCurrentUserState();
            startListeningForUserActivity();
            if (!mPreferencesApi.isDriving()) {
                startPassiveGPS(600, 200);
            }
        }

        // If we were uploading when the app got killed, we should resume uploading.
        if (mPreferencesApi.IsUploading()) {
            TrailManagerWorker trailManagerWorker = new TrailManagerWorker(this);
            String traildId = Integer.toString(mPreferencesApi.GetServerTrailId());
            trailManagerWorker.SaveEntireTrail(traildId);
        }
        // Weather not being used
        //FetchWeatherForTheDayService.setLaunchTimeForWeatherRequest(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bus.register(this);
        locationProvider = BreadcrumbsLocationProvider.getInstance(this);
    }

    @Override
    public void onDestroy() {
        Log.d("SERVICE", "THIS IS DEAD");
        bus.unregister(this);
        mPreferencesApi.DeleteCurrentUserState();
        mPreferencesApi.SetTracking(true);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Event listener for starting passiveGPS
    @Subscribe
    public void onStartPassiveGPS(GPSStartEvent event) {
        startPassiveGPS(event.duration, event.minDistance);
    }

    @Subscribe
    public void onGPSStop(GPSStopEvent event) {
        stopPassiveGPS();
        stopListeningForUserActivity();
    }

    @Subscribe
    public void onRequestSingleAccurateGPSUpdate(SingleGPSRequest event) {
        getAccurateFreshGPSPositionNow(event.listener);
    }

    /**
     * Start listening to user activity such as walking, running, driving etc.
     *
     * @param event
     */
    @Subscribe
    public void onStartListeningForUserActivity(UserActivity event) {
        startListeningForUserActivity();
    }

    /**
     * Method to save a trail to the database. This method takes a {@link TrailObject} that contains
     * the trail information, and uploads the information to the server. The actual uploading process
     * occurs in several steps and can take some time, so we need to run this in a background service.
     *
     * @Param trailObject: The object that contains the information about the trail.
     */
    @Subscribe
    public void UploadTrailToDatabase(TrailObject trailObject) {
        SaveTrail(trailObject);
    }

    @Subscribe
    public void onRemoveGeofences(GeofenceRemoval event) {
        removeGeofences();
    }

    private void removeGeofences() {
        locationProvider.RemoveGeofences();
    }

    // Start listening to any gps events on our phone. duration is the interval at which we want gps if none are being called by other apps
    private void startPassiveGPS(int durationInSeconds, int minDistance) {
        locationProvider.StartGPS(durationInSeconds, minDistance);
    }

    public void stopPassiveGPS() {
        // Remove the gps updates.
        mPreferencesApi.SetTracking(false);
        locationProvider.RemoveGPSUpdates();
    }

    private void getAccurateFreshGPSPositionNow(LocationListener listener) {
        locationProvider.requestLocationUpdate(listener);
    }

    private void startListeningForUserActivity() {
        locationProvider.startListeningToActivityChangesForUser();
    }

    public void stopListeningForUserActivity() {
        locationProvider.StopListeningToPathsenseActivityUpdates();
    }

    // This is the actual worker process for saving a trail to the database. see {@link #UploadTrailToDatabase(TrailObject)}
    private void SaveTrail(TrailObject trailObject) {
        TrailManagerWorker trailManagerWorker = new TrailManagerWorker(this);
        trailManagerWorker.SaveEntireTrail(trailObject.ServerTrail);
    }
}
