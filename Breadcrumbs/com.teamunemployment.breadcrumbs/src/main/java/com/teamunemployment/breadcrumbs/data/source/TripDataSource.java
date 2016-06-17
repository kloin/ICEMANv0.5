package com.teamunemployment.breadcrumbs.data.source;

import android.support.annotation.NonNull;

import com.teamunemployment.breadcrumbs.data.Trip;
import com.teamunemployment.breadcrumbs.data.TripDetails;
import com.teamunemployment.breadcrumbs.data.TripPath;

/**
 * @author Josiah Kendall
 * Entry point for accessing trip data, whether it be local or remote.
 */
public interface TripDataSource {

    // Callbacks for when we complete load of a trip. We split trip path and details because I want to load one first, and then the other.
    interface LoadTripDetailsCallback {
        void onTripDetailsLoaded(TripDetails tripDetails);
    }

    interface LoadTripPathCallback {
        void onTripPathLoaded(TripPath tripPath);
    }

    // Access point for currently active trip.
    interface LoadCurrentTripCallback {
        void onCurrentTripLoaded(Trip trip);
    }

    void getTripDetails(@NonNull LoadTripDetailsCallback callback, String id);
    void getTripPath(@NonNull LoadTripPathCallback callback, String id);
    void getCurrentTrip(@NonNull LoadCurrentTripCallback callback, String id );
    void saveTrip(@NonNull Trip trip);
    void refreshTrips();
    void deleteTrip(@NonNull String tripId);


}
