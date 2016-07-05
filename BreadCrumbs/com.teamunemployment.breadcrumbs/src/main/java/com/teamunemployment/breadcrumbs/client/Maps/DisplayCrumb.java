package com.teamunemployment.breadcrumbs.client.Maps;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by aDirtyCanvas on 6/2/2015.
 */
public class DisplayCrumb implements ClusterItem {
    private final LatLng mPosition;
    private final String extension;
    private final String id;
    private final String placeId;
    private final String suburb;
    private final String city;
    private final String country;
    private final String timeStamp;
    private final String description;
    private final Bitmap thumbNail;
    private int iconDrawable;
    private int isLocal;
    private float descriptionXPos;
    private float descriptionYPos;

    public DisplayCrumb(double lat, double lng, String extension, String id, int iconDrawable, String placeId, String suburb, String city, String country, String timeStamp, String description, Bitmap thumbNail, int isLocal, float x, float y) {
        mPosition = new LatLng(lat, lng);
        this.extension = extension;
        this.id = id;
        this.iconDrawable = iconDrawable;
        this.placeId = placeId;
        this.suburb = suburb;
        this.city = city;
        this.country = country;
        this.timeStamp = timeStamp;
        this.description = description;
        this.thumbNail = thumbNail;
        this.isLocal = isLocal;
        this.descriptionXPos = x;
        this.descriptionYPos = y;
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

    public String getPlaceId() {
        return placeId;
    }

    public String getSuburb() {
        return suburb;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getDescription() {
        return description;
    }

    public int GetCrumbIcon() {
        return iconDrawable;
    }

    public Bitmap getThumbNail() {
        return thumbNail;
    }

    public int GetIsLocal() {return isLocal;}

    public float getDescriptionPosX() {
        return descriptionXPos;
    }

    public float getDescriptionPosY() {
        return descriptionYPos;
    }
}

