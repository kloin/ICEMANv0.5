/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.breadcrumbs.models;

import java.util.ArrayList;

/**
 *
 * @author jek40
 * 
 * A instance to hold all the objects that we get from processing the metadata from a trail.
 * This trail does not hold the media for a photo.
 */
public class TrailMetadata {
    private ArrayList<Event> mEventList;
    private Polyline mOverviewPolyline;
    
    public TrailMetadata(ArrayList<Event> events, Polyline polyline) {
        mEventList = events;
        mOverviewPolyline = polyline;
    }
    
    public ArrayList<Event> GetEvents() {
        return mEventList;
    }
    
    public Polyline GetFullTrailPolyline() {
        return mOverviewPolyline;
    }   
}
