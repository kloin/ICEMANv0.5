package com.teamunemployment.breadcrumbs.data.source;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.teamunemployment.breadcrumbs.data.Trip;
import com.teamunemployment.breadcrumbs.data.source.LocalRepository.TripLocalDataSource;
import com.teamunemployment.breadcrumbs.data.source.RemoteRepository.RemoteTripDataSource;

/**
 * @author Josiah Kendall
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

    @Override
    public void getCurrentTrip(@NonNull LoadCurrentTripCallback callback, String id) {

    }

    @Override
    public void saveTrip(@NonNull Trip trip) {

    }

    @Override
    public void refreshTrips() {

    }

    @Override
    public void deleteTrip(@NonNull String tripId) {

    }
}
