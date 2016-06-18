package com.teamunemployment.breadcrumbs.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by jek40 on 18/06/2016.
 */
public class BreadcrumbsEncodedPolyline {

    public boolean isEncoded;
    public String polyline;
    public Double headLatitude;
    public Double headLongitude;
    public Double baseLongitude;
    public Double baseLatitude;

    public BreadcrumbsEncodedPolyline(boolean encoded, String polyline) {
        this.polyline = polyline;
        isEncoded = encoded;
    }

    // This is used for encoded polylines where we need to join the hanging lines using base/head lat/longs
    public BreadcrumbsEncodedPolyline(boolean encoded, String polyline, Double baseLat, Double baseLong, Double headLat, Double headLong) {
        this.polyline = polyline;
        isEncoded = encoded;
        this.baseLatitude = baseLat;
        this.baseLongitude = baseLong;
        this.headLatitude = headLat;
        this.headLongitude = headLong;
    }
}
