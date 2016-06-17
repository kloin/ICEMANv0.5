package com.teamunemployment.breadcrumbs.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by jek40 on 15/06/2016.
 */
public class BreadcrumbsPolyline {
    public boolean isEncoded;
    public ArrayList<LatLng> points = new ArrayList<>();

    public BreadcrumbsPolyline(boolean encoded, ArrayList<LatLng> points) {
        this.points = points;
        isEncoded = encoded;
    }
}
