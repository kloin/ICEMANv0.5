package com.teamunemployment.breadcrumbs.data.source.LocalRepository;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;
import com.teamunemployment.breadcrumbs.Models;
import com.teamunemployment.breadcrumbs.data.BreadcrumbsEncodedPolyline;
import com.teamunemployment.breadcrumbs.data.BreadcrumbsPolyline;
import com.teamunemployment.breadcrumbs.data.Trip;
import com.teamunemployment.breadcrumbs.data.TripDetails;
import com.teamunemployment.breadcrumbs.data.TripPath;
import com.teamunemployment.breadcrumbs.data.source.TripDataSource;
import com.teamunemployment.breadcrumbs.data.source.TripRepository;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.json.JSONException;
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
        Log.d("TripPath", "Size is: "+tripPath.getTripPolyline().size());

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

    public JSONObject FetchRawTestData() {

        ArrayList<Location> locations = new ArrayList<>();
        Location location1 = new Location("FUCK");
        location1.setLatitude(-36.793208);
        location1.setLongitude(174.709162);
        Location location2 = new Location("FUCK");
        location2.setLatitude(-36.793423);
        location2.setLongitude(174.710310);
        Location location4 = new Location("FUCK");
        location4.setLatitude(-36.795066);
        location4.setLongitude(174.711303);
        Location location6 = new Location("FUCK");
        location6.setLatitude(-36.795952);
        location6.setLongitude(174.712549);
        Location location8 = new Location("FUCK");
        location8.setLatitude(-36.796624);
        location8.setLongitude(174.713522);
        Location location10 =  new Location("FUCK");
        location10.setLatitude(-36.814493);
        location10.setLongitude(174.730864);
        Location location11 =  new Location("FUCK");
        location11.setLatitude(-36.814493);
        location11.setLongitude(174.730864);

        locations.add(location1);
        locations.add(location2);
        locations.add(location4);
        locations.add(location6);
        locations.add(location8);
        locations.add(location10);
        locations.add(location11);

        Iterator<Location> locationIterator = locations.iterator();
        JSONObject metadata = new JSONObject();
        int currentIndex = 0;
        while (locationIterator.hasNext()) {
            JSONObject metadataNode = new JSONObject();

            Location location = locationIterator.next();
            // Data to grab
            Double latitude =location.getLatitude();
            Double longitude = location.getLongitude();
            int activity = DetectedActivity.WALKING;
            int currentActivity = DetectedActivity.WALKING;
            int granularity = 0;

            // Save our single point as a single node, and add it to the overall object that is
            // going to be sent to the server.
            try {
                metadataNode.put(Models.Crumb.LATITUDE, Double.toString(latitude));
                metadataNode.put(Models.Crumb.LONGITUDE, Double.toString(longitude));
                metadataNode.put("LastActivity", Integer.toString(activity));
                metadataNode.put("CurrentActivity", Integer.toString(currentActivity));
                metadataNode.put("Granularity", Integer.toString(granularity));
                // This wraps the object so that the first point is not a gps point, which means that it wont track properly
                metadata.put(Integer.toString(currentIndex), metadataNode);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            currentIndex += 1;

        }

        return metadata;



    }

}
