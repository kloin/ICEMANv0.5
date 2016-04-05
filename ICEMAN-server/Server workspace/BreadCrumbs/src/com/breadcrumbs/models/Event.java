/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.breadcrumbs.models;

/**
 *
 * @author jek40
 */
public class Event {
    
    private int mId;
    private int mEventType;
    private Location mLocation;
    private Polyline mPolyline; // Polyline runs from this event, to the next one
    
    public Event(Location location, int eventType, Polyline polyline, int id) {
        mEventType = eventType;
        mLocation = location;
        mPolyline = polyline;
        mId = id;
    }
    
    public Location GetLocation() {
        return mLocation;
    }
    
    public int GetType() {
        return mEventType;
    }
    
    public Polyline GetPolyline() {
        return mPolyline;
    }
    
    public int GetId() {
        return mId;
    }
}
