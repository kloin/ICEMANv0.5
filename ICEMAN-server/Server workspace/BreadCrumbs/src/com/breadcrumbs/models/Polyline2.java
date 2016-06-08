/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.breadcrumbs.models;

/**
 * A more lightweight version of the {@link Polyline} class.
 * @author Josiah Kendall
 */
public class Polyline2 {
    
    
    public String line;
    public boolean isEncoded;
    public int index;
    public Location baseLocation;
    public Location headLocation;
    
    public Polyline2(String line, boolean isEncoded, int index) {
        this.line = line;
        this.isEncoded = isEncoded;
        this.index = index;
    }
    
    public Polyline2(String line, boolean isEncoded, int index, Location baseLocation, Location headLocation) {
        this.line = line;
        this.isEncoded = isEncoded;
        this.index = index;
        this.baseLocation = baseLocation;
        this.headLocation = headLocation;
    }
}
