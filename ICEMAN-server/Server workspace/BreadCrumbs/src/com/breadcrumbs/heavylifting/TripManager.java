/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.breadcrumbs.heavylifting;

import com.breadcrumbs.database.DBMaster;
import com.breadcrumbs.models.Event;
import com.breadcrumbs.models.PathNode;
import com.breadcrumbs.models.Polyline2;
import java.util.ArrayList;
import java.util.Hashtable;
import org.json.JSONObject;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Josiah Kendall
 */
public class TripManager {
    private DBMaster dbMaster;
    
    /**
     * Save a path against a trail.
     * @return Success = true, failure = false;
     */
    public boolean SavePath(String trailId, String pathData) {
        JSONObject json = new JSONObject(pathData);
        Path path = new Path(json);
        ArrayList<Polyline2> lines = path.CalculatePolylines();
        for (Polyline2 line : lines) {
            saveEvent(line, Integer.parseInt(trailId));
        }
        
        return true;        
    }
    
    private void saveEvent(Polyline2 event, int trailId) {
        if (dbMaster == null) {
            dbMaster = DBMaster.GetAnInstanceOfDBMaster();
        }
        // Decide if walking or driving
        Hashtable<String, Object> keysAndItems = new Hashtable<String, Object>();
        keysAndItems.put("IsEncoded", event.isEncoded);
        keysAndItems.put("Polyline", event.line);
        keysAndItems.put("Index", event.index);
        // If it is an encoded event, we need to have the head and base points too.
        if (event.isEncoded) {
            keysAndItems.put("BA", event.baseLocation.GetLatitude());
            keysAndItems.put("BO", event.baseLocation.GetLongitude());
            keysAndItems.put("HA", event.headLocation.GetLatitude());
            keysAndItems.put("HO", event.headLocation.GetLongitude());
        }
        
        int eventId = dbMaster.SaveNode(keysAndItems, DBMaster.myLabels.Polyline);
        Node crumb = dbMaster.RetrieveNode(eventId);
        Node trail = dbMaster.RetrieveNode(trailId);
        if (trail != null) {
            dbMaster.CreateRelationship(crumb, trail, DBMaster.myRelationships.Part_Of);	
        }
    }
    
    /**
     * Fetch all the {@link Polyline2} objects that are part of a trip.
     * @param tripId The trip id that we are getting the polylines for.
     * @return The polylines as a JSONObject.
     */
    public JSONObject FetchPathForTrip(String tripId) {
        DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
        String cypherQuery = "start n = node("+tripId+") match n-[rel:Part_Of]-(line:Polyline) return line ORDER BY line.Id";
        String resultString = dbMaster.ExecuteCypherQueryJSONStringReturn(cypherQuery);    
        return new JSONObject(resultString);
    }
    
    /**
     * Calculate the path and return the data in the same format as if we were
     * just loading another trail. However we currently do not save this data.
     * @param tripId The trip we are calculating for
     * @return JSONObject with the data.
     */
    public JSONObject CalculatePath(String tripId, String pathData) {
        JSONObject json = new JSONObject(pathData);
        JSONObject response = new JSONObject();
        Path path = new Path(json);
        ArrayList<Polyline2> lines = path.CalculatePolylines();
        int index = 0;
        for (Polyline2 line : lines) {
            JSONObject node = new JSONObject();
            node.put("IsEncoded", line.isEncoded);
            node.put("Polyline", line.line);
            response.put(Integer.toString(index), node);
            index += 1;
        }
        
        return response;
    }
}
