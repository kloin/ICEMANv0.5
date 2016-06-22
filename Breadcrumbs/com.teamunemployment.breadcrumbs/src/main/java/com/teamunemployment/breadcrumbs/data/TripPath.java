package com.teamunemployment.breadcrumbs.data;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Model for the trip path. Only models the path, nothing else.
 */
public class TripPath {

    private ArrayList<BreadcrumbsEncodedPolyline> polylines = new ArrayList<>();

    public TripPath(ArrayList<BreadcrumbsEncodedPolyline> points) {
        polylines = points;
    }

    public ArrayList<BreadcrumbsEncodedPolyline> getTripPolyline() {
        return polylines;
    }

    public void AddLocationToLine(Location location) {

        // Grab our last polyline as we need to draw from the end of that.
        int size = polylines.size();
        if (size == 0) {
            return;
        }

        BreadcrumbsEncodedPolyline endOfBreadcrumbsPolylinesList = polylines.get(size-1);
        Location locationNode = grabEndOfLastSavedPolyline(endOfBreadcrumbsPolylinesList);

        // Now I need to create a new unencoded polyline to match mine with the
        String unencodedPolyline = locationNode.getLatitude() + "," + locationNode.getLongitude() + "|" + location.getLatitude() + "," + location.getLongitude();
        BreadcrumbsEncodedPolyline encodedPolyline = new BreadcrumbsEncodedPolyline(false, unencodedPolyline);

        polylines.add(encodedPolyline);
    }

    private Location grabEndOfLastSavedPolyline(BreadcrumbsEncodedPolyline breadcrumbsEncodedPolyline) {
        // If we are encoded, we know that we are going to have a head latitude and longitude passed down.
        Location lastLocation = new Location("Fake");
        if (breadcrumbsEncodedPolyline.isEncoded) {
            lastLocation.setLatitude(breadcrumbsEncodedPolyline.headLatitude);
            lastLocation.setLongitude(breadcrumbsEncodedPolyline.headLongitude);
        }
        // If we are not encoded, we are endocded as a base/head. unencoded is only ever a base and a head.
        else {
            // split into base and head lat/lngs
            String[] locations = breadcrumbsEncodedPolyline.polyline.split("\\|");
            // split into latitude and longitude doubles.
            if (locations.length > 0) {
                String headLocation = locations[1];
                String[] latLngsOfHead = headLocation.split(",");
                double lat = Double.parseDouble(latLngsOfHead[0]);
                double lon = Double.parseDouble(latLngsOfHead[1]);
                lastLocation.setLongitude(lon);
                lastLocation.setLatitude(lat);
            }
        }
        return lastLocation;
    }



}
