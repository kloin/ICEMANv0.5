package com.teamunemployment.breadcrumbs.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Model for the trip path. Only models the path, nothing else.
 */
public class TripPath {

    private ArrayList<BreadcrumbsPolyline> polylines = new ArrayList<>();

    public TripPath(String polylinePoints, boolean isEncoded) {
        // Do the parsing here.

    }

    public TripPath(ArrayList<BreadcrumbsPolyline> points) {
        polylines = points;
    }

    public ArrayList<BreadcrumbsPolyline> getTripPolyline() {
        return polylines;
    }

}
