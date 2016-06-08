/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.breadcrumbs.models;

import Statics.StaticValues;
import com.breadcrumbs.heavylifting.TrailManager20;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jek40
 */
public class Path {
   
    private ArrayList<PathNode> pathNodes;
    public Path(JSONObject pathData) {
        pathNodes = processDataIntoNodes(pathData);
    }
    
    private ArrayList<PathNode> processDataIntoNodes(JSONObject pathData) {
        ArrayList<PathNode> nodes = new ArrayList<>();
        Iterator<String> keys = pathData.keys();
        int id = 1;
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                JSONObject tempObject = pathData.getJSONObject(Integer.toString(id)); 
                PathNode node = new PathNode(tempObject);
                nodes.add(id-1,node);
            } catch (JSONException ex) {
                /*Log the error - might be important. If this happens its a sign
                of bad data, so we need to know about it. It would be cool if
                we could raise an alert here.
                */
                
            }
            id += 1;
        }
        
        return nodes;
    }
    /**
     * This is the entry point for where we enter the algorithm to calculate
     * the polyline(s) for a users path. The algorithm works as such:
     * 
     * ==========Polyline types==============
     * 
     *  - Walking
     * These are simple straight lines that we draw from point to point. They
     * are saved in a string format like 173.222,-43.445|174.234,-43.223.
     * The '|' character defines the break between points, while the ',' defines
     * the break between latitude and longitude for a single point.
     * 
     * - Driving
     * These are a bit mode complex. They hold the polyline points for a driving 
     * event. They are calculated from a base event to a 
     * @return 
     */
    public ArrayList<Polyline2> CalculatePolylines() {
        ArrayList<Polyline2> lineList = new ArrayList<>();
        boolean haveBeenDriving = false;
        int currentActivity = 7; // walking
        PathNode baseNode = null;
        PathNode currentNode = null;
        PathNode previousNode = null;
        
        // Get our iterator for searching through the javascript nodes.
        Iterator<PathNode> nodeIterator = pathNodes.iterator();
        boolean started = false;
        while (nodeIterator.hasNext()) {
            System.out.println("About to start");
            
            if (!started) {
                // First time through use case.
               // Grab the next node off the iterator. Assign our currentNode to
                // our base node, as we will be changing the currentnode more than the base node.
                baseNode = nodeIterator.next();
                currentNode = baseNode;
                System.out.println("Started");
            } else {                
                // If we have an old node, we want to move foreward in the list.
                 previousNode = recycleNode(currentNode);
                 currentNode = nodeIterator.next();
            }
            // This is first time use. No need to do any checking.
            if (previousNode != null) {
                // Check If both current and previous events are walking events, we want to save the event to the database
                if (currentNode.GetActivity() == StaticValues.ON_FOOT && previousNode.GetActivity()== StaticValues.ON_FOOT) {
                    if (haveBeenDriving) {
                        // We want to calculate a driving event.
                        Polyline2 polyline = calculateDrivingEvent(baseNode, previousNode);
                        lineList.add(polyline);
                    } 
                    
                    Polyline2 polyline = calculateWalkingEvent(baseNode, previousNode);
                    lineList.add(polyline);
                    
                } else if (currentNode.GetActivity() == StaticValues.IN_VEHICLE) {
                    // do next
                   haveBeenDriving = true; 
                }
            }

            started =true;
        }
     
        return lineList;
    }
    public ArrayList<Polyline2> CalculatePolylines(boolean test) {
        ArrayList<Polyline2> lineList = new ArrayList<>();
        boolean haveBeenDriving = false;
        int currentActivity = 7; // walking
        PathNode baseNode = null;
        PathNode currentNode = null;
        PathNode previousNode = null;
        
        // Get our iterator for searching through the javascript nodes.
        Iterator<PathNode> nodeIterator = pathNodes.iterator();
        boolean started = false;
        int id = 1;
        while (nodeIterator.hasNext()) {
            System.out.println("About to start");
            if (!started) {
                // First time through use case.
               // Grab the next node off the iterator. Assign our currentNode to
                // our base node, as we will be changing the currentnode more than the base node.
                baseNode = nodeIterator.next();
                currentNode = baseNode;
                System.out.println("Started");
            } else {                
                // If we have an old node, we want to move foreward in the list.
                 previousNode = recycleNode(currentNode);
                 currentNode = nodeIterator.next();
                 
            }
            // This is first time use. No need to do any checking.
            if (previousNode != null) {
                // Check If both current and previous events are walking events, we want to save the event to the database
                if (currentNode.GetActivity() == StaticValues.ON_FOOT && previousNode.GetActivity()== StaticValues.ON_FOOT) {
                    if (haveBeenDriving) {
                        // We want to calculate a driving event.
                        Polyline2 polyline = getMockDrivingEvent();//calculateDrivingEvent(baseNode, previousNode);
                        lineList.add(polyline);
                        haveBeenDriving = false;
                    } 
                    
                    Polyline2 polyline = getMockWalkingEvent();//calculateWalkingEvent(baseNode, previousNode);
                    lineList.add(polyline);
                    
                } else if (currentNode.GetActivity() == StaticValues.IN_VEHICLE) {
                    // do next
                   haveBeenDriving = true; 
                }
            }
            
            // IF this is the last node, we may need to manually add 
            if (!nodeIterator.hasNext()) {
                if (haveBeenDriving) {
                    // We want to calculate a driving event.
                        Polyline2 polyline = getMockDrivingEvent();//calculateDrivingEvent(baseNode, previousNode);
                        lineList.add(polyline);
                }
            }
            id += 1;
            started =true;
        }
     
        return lineList;
    }
    
    private Polyline2 getMockDrivingEvent() {
        return new Polyline2("gfd43tgfr60gfd03#$%^%", true);
    }
    
    private Polyline2 getMockWalkingEvent() {
        return new Polyline2("MOck", false);
    }
    
    /**
     * Class used to deep copy a node and break the reference.
     * @param oldNode The node that we are copying.
     * @return The new node with the old node values.
     */
    private PathNode recycleNode(PathNode oldNode) {
        return new PathNode(oldNode.GetJSON());
    }

    /**
     * Calculate the {@link Polyline2} between two points for a driving 
     * @param baseNode The node that we are drawing from.
     * @param destinationNode The node that we are drawing to.
     * @return A {@link Polyline2} instance with the encoded polyline.
     */
    private Polyline2 calculateDrivingEvent(PathNode baseNode, PathNode destinationNode) {

        Location baseLocation = new Location(baseNode.GetLatitude(), baseNode.GetLongitude());
        Location headLocation = new Location(destinationNode.GetLatitude(), destinationNode.GetLongitude());
        String encodedPolyline = getGoogleDirectionsWithNoWaypoints(baseLocation, headLocation);
        return new Polyline2(encodedPolyline, true);
    }
    
    
    private String getGoogleDirectionsWithNoWaypoints(Location location1, Location location2) {
            ArrayList<Location> locationList = new ArrayList();
            
            String urlString = buildUrl(locationList, location1, location2);
            JSONObject jsonResponse = fetchDirectionsFromGoogle(urlString);
            if (jsonResponse.getJSONArray("routes") == null || jsonResponse.getJSONArray("routes").length() == 0) {
                return null;
            }
            
            JSONObject routes = jsonResponse.getJSONArray("routes").getJSONObject(0);
            // Uodate the trails total distance.
            JSONObject overviewPolyline = routes.getJSONObject("overview_polyline");
            String encodedPolyline = overviewPolyline.getString("points");
            
            return encodedPolyline;
        }
   
    private JSONObject fetchDirectionsFromGoogle(String urlString) {
            try {
                // Create the connection
                URL url = new URL(urlString);
                URLConnection yc = url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                String inputLine;

                StringBuilder response = new StringBuilder(); 
                while ((inputLine = in.readLine()) != null)  {
                    System.out.println(inputLine);
                    response.append(inputLine);
                    response.append('\r');                    
                }
                
                in.close();
                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse;

                
            } catch (MalformedURLException ex) {
                Logger.getLogger(TrailManager20.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(TrailManager20.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return null;
        }
    
    private String buildUrl(List<Location> locationList, Location origin, Location destination) {
            String baseUrl = "https://maps.googleapis.com/maps/api/directions/json?origin=";//-44.9439635,168.8379247&destination=-43.4721,170.017685&key=AIzaSyC3zYs82SyMaMlj2Xbss9b51FuoWJEVF-E"
            
            // Fetch the destination and origin of our trail.
            
            String oLatString = Double.toString(origin.GetLatitude());
            String oLonString = Double.toString(origin.GetLongitude());
            String dLatString = Double.toString(destination.GetLatitude());
            String dLonString = Double.toString(destination.GetLongitude());

            baseUrl = baseUrl.concat(oLatString + "," + oLonString + "&destination="+dLatString + "," + dLonString);
          
            // Iterate through each of the points. Make the first the origin,
            // the last the destination and the rest of them the waypoints.
            if (locationList.size() > 0) {
                boolean first = true;
                baseUrl = baseUrl.concat("&waypoints=");
                Iterator<Location> locationIterator = locationList.iterator();
                while (locationIterator.hasNext()) {
                    Location location = locationIterator.next();
                    Double latitude = location.GetLatitude();
                    Double longitude = location.GetLongitude();
                    String latitudeString = Double.toString(latitude);
                    String longitudeString = Double.toString(longitude);
                    if (!first) {
                    	baseUrl = baseUrl.concat("|"+latitudeString + "," + longitudeString);
                    } else {
                        baseUrl = baseUrl.concat(latitudeString + "," + longitudeString);
                    }
                   
                    first = false;
                }
            }
            baseUrl = baseUrl.concat("&key=AIzaSyC3zYs82SyMaMlj2Xbss9b51FuoWJEVF-E");
            return baseUrl;
        }
    
    /**
     * Calculate the walking directions to 
     * @param baseNode Where we are driving from.
     * @param headNode Where we are driving to.
     * @return The {@link Polyline2} object with the walking line.
     */
    private Polyline2 calculateWalkingEvent(PathNode baseNode, PathNode headNode) {
        String lat1 = Double.toString(baseNode.GetLatitude());
        String lat2 = Double.toString(headNode.GetLatitude());
        String lon1 = Double.toString(baseNode.GetLongitude());
        String lon2 = Double.toString(headNode.GetLongitude());
        String unEncodedPath =  lat1+","+lon1+"|"+lat2+","+lon2;
        return new Polyline2(unEncodedPath, false);
    }
    
}
