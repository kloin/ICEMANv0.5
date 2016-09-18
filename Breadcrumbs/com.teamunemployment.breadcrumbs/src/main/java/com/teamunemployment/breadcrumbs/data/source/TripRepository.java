package com.teamunemployment.breadcrumbs.data.source;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.data.TripPath;
import com.teamunemployment.breadcrumbs.data.source.LocalRepository.TripLocalDataSource;
import com.teamunemployment.breadcrumbs.data.source.RemoteRepository.RemoteTripDataSource;

import org.json.JSONObject;

/**
 * @author Josiah Kendall
 *
 * Entry point for fetching / syncing data related to trips.
 */
public class TripRepository implements TripDataSource {
    private final static String TAG = "TripRepository";
    private TripLocalDataSource tripLocalDataSource;
    private RemoteTripDataSource remoteTripDataSource;

    // Not using a singleton at the moment. Will probably move to dependency injection with Dagger for tesability.
    public TripRepository(@NonNull TripLocalDataSource localDataSource,
                          @NonNull RemoteTripDataSource remoteTripDataSource) {

        this.remoteTripDataSource = remoteTripDataSource;
        this.tripLocalDataSource = localDataSource;
    }

    @Override
    public void getTripDetails(@NonNull LoadTripDetailsCallback callback, String id) {
        if (callback == null) {
            Log.d(TAG, "Load callback was null. Cannot fetch trip details");
            return;
        }

        if (id == null) {
            Log.d(TAG, "Trip id was null. Cannot fetch Trip details");
            return;
        }

        // This means that we are fetching a local trail - probably looking at the MyTrip screen.
        if (id.endsWith("L")) {
            tripLocalDataSource.getTripDetails(callback, id);
        } else {
            remoteTripDataSource.getTripDetails(callback, id);
        }
    }

    @Override
    public void getTripPath(@NonNull LoadTripPathCallback callback, @NonNull String id) {
        if (callback == null) {
            Log.d(TAG, "Load callback was null. Cannot fetch trip path");
            return;
        }
        if (id == null) {
            Log.d(TAG, "Trip id was null. Cannot fetch Trip path");
            return;
        }

        // Decide if the trip we are fetching comes from the local db or the remote db.
        if (id.endsWith("L")) {
            tripLocalDataSource.getTripPath(callback, id);
        } else {
            remoteTripDataSource.getTripPath(callback, id);
        }
    }

    /**
     * Grab the current trip data from the database i.e the Trip that the user is currently recording.
     * This data is not presentable to the user, but should be used to fetch raw data from the database
     * which we will process server side.
     * @param callback
     * @param id
     */
    @Override
    public void getCurrentTrip(@NonNull LoadCurrentTripCallback callback, String id) {

    }

    @Override
    public void saveTrip(@NonNull Trip trip) {

    }

    @Override
    public void saveTripPath(@NonNull TripPath tripPath, String id) {

    }

    @Override
    public void refreshTrips() {

    }

    @Override
    public void deleteTrip(@NonNull String tripId) {

    }

}
