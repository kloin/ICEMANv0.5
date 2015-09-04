package com.breadcrumbs.client.Maps;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by aDirtyCanvas on 6/2/2015.
 */
public class DisplayCrumb implements ClusterItem {
    private final LatLng mPosition;
    private final String extension;
    private final String id;
    private int iconDrawable;

    public DisplayCrumb(double lat, double lng, String extension, String id, int iconDrawable) {
        mPosition = new LatLng(lat, lng);
        this.extension = extension;
        this.id = id;
        this.iconDrawable = iconDrawable;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getExtension() {
        return extension;
    }

    public String getId() {
        return id;
    }

    public int GetCrumbIcon() {
        return iconDrawable;
    }
}

