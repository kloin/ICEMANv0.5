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


    /**
     * Unencoded polyline constructor.
     * @param encoded
     * @param polyline
     */
    public BreadcrumbsEncodedPolyline(boolean encoded, String polyline) {
        this.polyline = polyline;
        isEncoded = encoded;
    }

    /**
     * Encoded poyline constructor.
     * @param encoded
     * @param polyline
     * @param baseLat
     * @param baseLong
     * @param headLat
     * @param headLong
     */
    public BreadcrumbsEncodedPolyline(boolean encoded, String polyline, Double baseLat, Double baseLong, Double headLat, Double headLong) {
        this.polyline = polyline;
        isEncoded = encoded;
        this.baseLatitude = baseLat;
        this.baseLongitude = baseLong;
        this.headLatitude = headLat;
        this.headLongitude = headLong;
    }
}
