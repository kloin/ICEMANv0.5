/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.breadcrumbs.models;

/**
 *
 * @author Josiah
 * 
 * Simple location class for handling location data that comes from the app.
 */
public class Location {
    
    private Double mLatitude;
    private Double mLongitude;
    private Double mSpeed;
    private int mAccuracy;
    
    public Location(Double latitude, Double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }
    
    public Location(String latitude, String longitude) {
        mLatitude = Double.parseDouble(latitude);
        mLongitude = Double.parseDouble(longitude);
    }
    
    public Double GetLatitude() {
        return mLatitude;
    }
    
    public Double GetLongitude() {
        return mLongitude;
    }
    
    public void SetSpeed(Double metersPerSecond) {
        mSpeed = metersPerSecond;
    }
    
    public void SetAccuracy(int accuracy) {
        mAccuracy = accuracy;
    }
    
    @Override
    public String toString() {
        return Double.toString(mLatitude) + "," + Double.toString(mLongitude);
    }
}
