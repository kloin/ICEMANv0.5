package com.breadcrumbs.models;

import org.json.JSONObject;

/**
 * Class to manage the ugly dealings with JSONObjects. EAch instance of this
 * class represents a point on a users path, with associated activity and location,
 * as well as the previous activity.
 * @author jek40
 */
public class PathNode {
    
    
    private JSONObject jsonNode;
    
    public PathNode(JSONObject jsonNode) {
        this.jsonNode = jsonNode;
    }
    
    public Double GetLatitude() {
        return jsonNode.getDouble("Latitude");
    }
    
    public Double GetLongitude() {
        return jsonNode.getDouble("Longitude");
    }
    
    public int GetActivity() {
        return jsonNode.getInt("CurrentActivity");
    }
    
    public int GetPreviousActivity() {
        return jsonNode.getInt("LastActivity");
    }
    
    public JSONObject GetJSON() {
        return jsonNode;
    }
}
