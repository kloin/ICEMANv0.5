package com.teamunemployment.breadcrumbs.data;

import com.google.android.gms.maps.model.LatLng;

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

}
