package com.teamunemployment.breadcrumbs.data.source.LocalRepository;

import android.content.Context;
import android.support.annotation.NonNull;

import com.teamunemployment.breadcrumbs.data.BreadcrumbsEncodedPolyline;
import com.teamunemployment.breadcrumbs.data.BreadcrumbsPolyline;
import com.teamunemployment.breadcrumbs.data.Trip;
import com.teamunemployment.breadcrumbs.data.TripDetails;
import com.teamunemployment.breadcrumbs.data.TripPath;
import com.teamunemployment.breadcrumbs.data.source.TripDataSource;
import com.teamunemployment.breadcrumbs.data.source.TripRepository;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Josiah Kendall.
 *
 */
public class TripLocalDataSource implements TripDataSource {

    private DatabaseController databaseController;

    public TripLocalDataSource(DatabaseController databaseController) {
        this.databaseController = databaseController;
    }

    @Override
    public void getTripDetails(@NonNull LoadTripDetailsCallback callback, String id) {
        // This should be a run off the thread then call the trip callback on the UI Thread.
        TripDetails tripDetails = databaseController.GetTripDetails(id);
        callback.onTripDetailsLoaded(tripDetails);
    }

    @Override
    public void getTripPath(@NonNull LoadTripPathCallback callback, String id) {
        TripPath tripPath = databaseController.FetchTripPath(id);
        callback.onTripPathLoaded(tripPath);
    }

    @Override
    public void getCurrentTrip(@NonNull LoadCurrentTripCallback callback, String id) {

    }

    @Override
    public void saveTrip(@NonNull Trip trip) {

    }

    /**
     * Save trip details to the local database.
     * @param tripPath
     */
    @Override
    public void saveTripPath(@NonNull TripPath tripPath, String id) {
        ArrayList<BreadcrumbsEncodedPolyline> lines = tripPath.getTripPolyline();
        // save each polyline to the database
        for (BreadcrumbsEncodedPolyline polyline : lines) {
            databaseController.SavePolyline(polyline, id);
        }
    }

    @Override
    public void refreshTrips() {

    }

    @Override
    public void deleteTrip(@NonNull String tripId) {

    }

    public JSONObject FetchRawTrackingData(int id, int index) {
        return databaseController.GetAllActivityData(id, index);
    }

}
