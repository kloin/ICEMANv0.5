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
    
    public Polyline2(String line, boolean isEncoded) {
        this.line = line;
        this.isEncoded = isEncoded;
    }
}
