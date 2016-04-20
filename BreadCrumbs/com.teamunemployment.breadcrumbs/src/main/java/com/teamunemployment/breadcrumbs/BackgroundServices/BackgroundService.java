package com.teamunemployment.breadcrumbs.BackgroundServices;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.teamunemployment.breadcrumbs.Location.BreadcrumbsLocationProvider;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.Weather.WeatherManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Written By Josiah Kendall 2016.
 *
 * Purpose of this service is to manage the activity behaviour and location services.
 */
public class BackgroundService extends Service {
    private SharedPreferences mPreferences;
    private BreadcrumbsLocationProvider locationProvider;
    private final EventBus bus = EventBus.getDefault();
    private PreferencesAPI mPreferencesApi;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mPreferencesApi = PreferencesAPI.GetInstance(this);
        handleServiceStart();
        return START_STICKY;
    }

    // Here we check what we should be doing in terms of starting
    private void handleServiceStart() {
        boolean tracking = mPreferencesApi.isTracking();
        if (tracking) {
            PreferencesAPI.GetInstance(this).DeleteCurrentUserState();
            startListeningForUserActivity();
            if (!PreferencesAPI.GetInstance(this).isDriving()) {
                startPassiveGPS(600, 200);
            }
        }
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
        PreferencesAPI.GetInstance(this).DeleteCurrentUserState();
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

    @Subscribe
    public void onStartListeningForUserActivity(UserActivity event) {
        startListeningForUserActivity();
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

}
