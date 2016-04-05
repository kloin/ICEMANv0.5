package com.teamunemployment.breadcrumbs.client.Maps;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by aDirtyCanvas on 6/2/2015.
 */
public class TrailDrawer {

    private GoogleMap map;
    private Context context;
    public TrailDrawer(GoogleMap map, Context context) {
        this.context = context;
        this.map = map;
    }

    public void DrawPointOnMap(LatLng location) {
       /* map.addCircle(new CircleOptions()
                .center(location)
                .radius(30)
                .strokeColor(Color.parseColor("#9C27B0"))
                .fillColor(Color.parseColor("#9C27B0")));*/
    }

    public void DrawLineBetweenPoints(Location point1, Location point2) {

    }
}
