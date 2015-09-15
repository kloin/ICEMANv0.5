package com.breadcrumbs.models;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;

import com.breadcrumbs.database.DBMaster;
import com.breadcrumbs.database.DBMaster.myLabels;
import com.breadcrumbs.database.NodeController;
import com.breadcrumbs.database.DBMaster.myRelationships;

public class Trail {
		
	private DBMaster dbMaster;
	private GraphDatabaseService dbInstance;
	private NodeConverter nodeConverter;
	public String SaveTrail(String trailName, String userId, String description ) {
		int views = 0;
		Hashtable<String, Object> keysAndItems = new Hashtable<String, Object>();
		keysAndItems.put("TrailName", trailName);
		keysAndItems.put("UserId", userId);
		keysAndItems.put("Description", description);
		keysAndItems.put("Views", views);
		
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		int trailId = dbMaster.SaveNode(keysAndItems, com.breadcrumbs.database.DBMaster.myLabels.Trail);	
		Node trail = dbMaster.RetrieveNode(trailId);
		Node User = dbMaster.RetrieveNode(Integer.parseInt(userId));
		
		dbMaster.CreateRelationship(User, trail, myRelationships.Controls);
		return String.valueOf(trailId);		
	}
	
	public void SavePointFromJSON(JSONObject json, String trailId, String pointId) {
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		Iterator<String> it = json.keys();
		Hashtable<String, Object> keysAndItems = new Hashtable<String, Object>();
		
		// Get all parameters for the json Object
		while (it.hasNext()) {
			String nextVar = it.next();
			Object nextObj = json.get(nextVar);
			keysAndItems.put(nextVar, nextObj);
		}
		keysAndItems.put("pointId", pointId);
		Node trail = dbMaster.RetrieveNode(Integer.parseInt(trailId));
		// Our node already has all the info on it set by the client, so we just need to save it.
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		int pointNodeId = dbMaster.SaveNode(keysAndItems, com.breadcrumbs.database.DBMaster.myLabels.Point);
		// I have to re-fetch the node to create the relationship.
		Node pointNode = dbMaster.RetrieveNode(pointNodeId);
		dbMaster.CreateRelationship(trail, pointNode, myRelationships.Point_In);	
	}
	/*
	 * Record a point along ones trail
	 */
	public String SavePoint(String latitude, String longitude, String lastPointId, String trailId) {
		Hashtable<String, Object> keysAndItems = new Hashtable<String, Object>();
		keysAndItems.put("latitude", latitude);
		keysAndItems.put("longitude", longitude);
		keysAndItems.put("next", lastPointId);
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		Node trail = dbMaster.RetrieveNode(Integer.parseInt(trailId));
		
		// If we are folling on from a previous point,Save our new node, then fetch it and 
		// create the new relationship with it. NOTE that "-1" is what we put when we are not following.
		int pointId = dbMaster.SaveNode(keysAndItems, com.breadcrumbs.database.DBMaster.myLabels.Point);
		// I have to re-fetch the node to create the relationship.
		Node pointNode = dbMaster.RetrieveNode(pointId);
		dbMaster.CreateRelationship(trail, pointNode, myRelationships.Point_In);
		// Next!
		return Integer.toString(pointId);
		
	}
	
	/*
	 * Record a comment for a crumb or trail (or other shit)
	 */
	public String SaveCommentForAnEntity(String UserId, String EntityId, String CommentText) {
		Hashtable<String, Object> keysAndItems = new Hashtable<String, Object>();
		keysAndItems.put("UserId", UserId);
		keysAndItems.put("EntityId", EntityId);
		keysAndItems.put("CommentText", CommentText);
		
		// Get Master instance and save a new node using the hashtable.
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		Node entityNode = dbMaster.RetrieveNode(Integer.parseInt(EntityId));
		
		//This method should possible just return the node? How expensive is this?
		int commentNodeId = dbMaster.SaveNode(keysAndItems, com.breadcrumbs.database.DBMaster.myLabels.Comment);
		Node commentNode = dbMaster.RetrieveNode(commentNodeId);
		dbMaster.CreateRelationship(entityNode, commentNode, myRelationships.Linked_To);
		return Integer.toString(commentNodeId);
	}
	
	public String GetTrailAndCrumbs(String trailId) {
		NodeController nc = new NodeController();
		String crumbs = nc.GetAllRelatedNodes(trailId, myLabels.Crumb, "trailId", "TrailName");
		JSONObject trail = null;
		try {
			//trail = new JSONObject(nc.FetchNodeJson(trailId));
			System.out.println("Crumbs: " + crumbs.toString());
			trail.append("CrumbList", crumbs.toString());
		} catch (JSONException e) {
			System.out.println("An Error occured converting trail to json");
			e.printStackTrace();
		}
		
		return trail.toString();
	}
	
	/* 
	 * return trails and crumbs for given Ids
	 */
	public String FetchTrailsForGivenIds(String trailArrayList) {
		JSONObject trailsAndCrumbs = new JSONObject();
		NodeController nc = new NodeController();
		NodeConverter nodeConverter = new NodeConverter();
		String[] trailIdsArray = trailArrayList.split("T");
		
		// Iterate through building a string.
		for (int index = 0; index < trailIdsArray.length; index+= 1) {
			String trailId = trailIdsArray[index];
			JSONObject trailJson = nc.FetchNodeJson(Integer.parseInt(trailId));
			trailsAndCrumbs.put("Node"+index, trailJson);
		}
		
		return trailsAndCrumbs.toString();
		
	}
	
	public String GetAllRelatedNodes(int baseNodeId,  myLabels label, String property) {
		nodeConverter = new NodeConverter();
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		
    	if (dbMaster == null) {
    		System.out.print("There was an issue getting a valid instance of the database");
    	}
    	//Get our trail
    	//Get all the crumbs for our trail
    	//Then format it like : {Trail : {crumb1: {chat : this, talk : this}, crumb2: {you : getit?}}
    	JSONObject trailJsonObject = new JSONObject();
    	
    	//Get all the crumbs
    	Transaction tx = dbMaster.GetDatabaseInstance().beginTx();
    	JSONObject jsonResponse = new JSONObject();
    	ResourceIterable<Node> node = dbMaster.GetDatabaseInstance().findNodesByLabelAndProperty(label, property, baseNodeId);
		Iterator nodeSearcher = node.iterator();
		System.out.println("Getting all Trails for a user");
		try {
			int numberOfNodes = 0;
			
			while (nodeSearcher.hasNext()) {
				//Get the node once, as each time we use "Next()" we move forward on the list
				Node trail = (Node) nodeSearcher.next();
				
				//Construct a jsonString using the node converter for the node.
				//Add the string to the trail object under the crumbs id (? or name?)
				JSONObject crumbString = nodeConverter.ConvertSingleNodeToJSON(trail);
				trailJsonObject.append(crumbString.getString(""), crumbString.toString());	
				}
			} catch(JSONException ex) {
				System.out.println("THIS JUST CRASHED FUUUUUUCKKKKK");
				ex.printStackTrace();		
		}
		finally {
			tx.finish();
		}
		System.out.println("Here is the jsonOBject");
		return trailJsonObject.toString();
	}
	
	public String GetAllTrailsForAUser(int id) {
		nodeConverter = new NodeConverter();
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		
    	if (dbMaster == null) {
    		System.out.print("There was an issue getting a valid instance of the database");
    	}
    	//Get our trail
    	//Get all the crumbs for our trail
    	//Then format it like : {Trail : {crumb1: {chat : this, talk : this}, crumb2: {you : getit?}}
    	JSONObject trailJsonObject = new JSONObject();
    	
    	//Get all the crumbs
    	Transaction tx = dbMaster.GetDatabaseInstance().beginTx();
    	JSONObject jsonResponse = new JSONObject();
    	ResourceIterable<Node> node = dbMaster.GetDatabaseInstance().findNodesByLabelAndProperty(myLabels.Crumb, "user", 0);
		Iterator nodeSearcher = node.iterator();
		System.out.println("Getting all Trails for a user");
		try {
			int numberOfNodes = 0;
			
			while (nodeSearcher.hasNext()) {
				//Get the node once, as each time we use "Next()" we move forward on the list
				Node trail = (Node) nodeSearcher.next();
				
				//Construct a jsonString using the node converter for the node.
				//Add the string to the trail object under the crumbs id (? or name?)
				JSONObject crumbString = nodeConverter.ConvertSingleNodeToJSON(trail);
				trailJsonObject.append(crumbString.getString("Title"), crumbString.toString());	
				}
			} catch(JSONException ex) {
				System.out.println("THIS JUST CRASHED FUUUUUUCKKKKK");
				ex.printStackTrace();		
		}
		finally {
			tx.finish();
		}
		System.out.println("Here is the jsonOBject");
		return trailJsonObject.toString();
	}
	
	public String FindAllPinnedTrailsForAUser(String userId) {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "start n = node("+userId+") match n-[:Has_Pinned]->(Trail) return Trail";	
		return dbMaster.ExecuteCypherQueryJSONStringReturn(cypherQuery);		
	}
	
	public void PinTrailForUser(String UserId, String TrailId) {
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		Node trail = dbMaster.RetrieveNode(Integer.parseInt(TrailId));
		Node User = dbMaster.RetrieveNode(Integer.parseInt(UserId));
		dbMaster.CreateRelationship(User, trail, myRelationships.Has_Pinned);
	}
	
	/*
	 * A user no longer wants to follow a trail - this is called unpinning - here we remove the association.
	 */
	public void UnpintrailForUser(String UserId, String TrailId) {
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "start user = node("+UserId+"), "
				+ "trail = node("+TrailId+") "
				+ "match user-[rel:Has_Pinned]->trail "
				+ "delete rel";
		
		dbMaster.ExecuteCypherQueryNoReturn(cypherQuery);
	}
	
	/*
	 *  Parse all the json into points, and save them indivdually
	 */
	public String SaveJSONOfTrailPoints(JSONObject trailPointsJSON) {		
		String nextPoint = "0";
		String trailId = null;
		// Get the first one, then loop through as long as "next" != null
		while (nextPoint != null) {
			//Get the next point, send it to the saver to be saved, then get the next and continue.
			JSONObject tempNode = trailPointsJSON.getJSONObject("Index:"+nextPoint); // This is our first point
			if (trailId == null) {
				trailId = tempNode.getString("trailId");
			}
			
			SavePointFromJSON(tempNode, trailId, nextPoint);
			Object nextObj = null;
			try {
				nextObj = tempNode.get("next");
			} catch (JSONException ex) {
				// Not really alot to do here. Will happen every time at the end, but thats not really a big deal i dont think... if it is we can set a -1 by default client side
				nextObj = null;
			}
			
			if (nextObj != null) {
				nextPoint = Integer.toString((int)nextObj);
			} else {
				nextPoint = null;
			}
			
		}
		
		
		// Return the last trail so when we carry on it works all good. we wil store this in shared preferences
		return "200";
	}

	// This converts and saves the point, returning the points id.
	private String convertAndSavePoint(JSONObject tempNode, String lastPointId) {
		double latitude = tempNode.getDouble("latitude");
		double longitude = tempNode.getDouble("longitude");
		String trailId = tempNode.getString("trailId");
		// The end goal is here boys!

		return SavePoint(Double.toString(latitude), Double.toString(longitude), lastPointId, trailId);		
	}

	public String GetAllTrailPointsForATrail(String trailId) {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "start n = node("+trailId+") match n-[rel:Point_In]->(Point) return Point";	
		return dbMaster.ExecuteCypherQueryReturnPoint(cypherQuery);
	}

	public String GetAllTrails() {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "MATCH (n:Trail) RETURN n LIMIT 100";	
		return dbMaster.ExecuteCypherQueryJSONStringReturn(cypherQuery);
	}

	/*
	 * DELETE THIS SOON
	 */
	public void Obliterate() {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE n,r";	
		dbMaster.ExecuteCypherQueryJSONStringReturn(cypherQuery);
		// TODO Auto-generated method stub	
	}

	/*
	 * Sets the cover photo Id on the trail so we can use it to set the trail background.
	 * 
	 * This class is used by the RESTful request handler. THIS NOT GONNA WOrk
	 */
	public void SetCoverPhoto(String TrailId, String ImageId) {
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		dbMaster.SetCoverPhoto(TrailId, ImageId);
	}
	
	public void AddViewForATrail(String TrailId) {
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		dbMaster.AddViewToTrail(TrailId);
	}
	
	/*
	 * Get the number of views for a trail.
	 */
	public String GetNumberOfViewsForATrail(String TrailId) {		
		// Get Trail Node
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		return dbMaster.GetTrailViews(TrailId);	
	}

	public void AddLike(String userId, String entityId) {
		
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		Node trail = dbMaster.RetrieveNode(Integer.parseInt(entityId));
		Node User = dbMaster.RetrieveNode(Integer.parseInt(userId));
		dbMaster.CreateRelationship(User, trail, myRelationships.Likes);
	}
	
	public String GetNumberOfLikesForAnEntity(String entityId) {
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String numberOfLikes = "0";
		String cypherQuery = "start n = node(22) match ()-[rel:Likes]->n return count(*)";	
		numberOfLikes = dbMaster.ExecuteCypherQueryReturnCount(cypherQuery);
		return numberOfLikes;		
	}

	public String GetNumberOfCrumbsForATrail(String trailId) {
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "start n =node("+trailId+") match (crumb:Crumb)--(n) Return count(*)";
		return dbMaster.ExecuteCypherQueryReturnCount(cypherQuery);
	}

	public void setCoverPhotoId(Node trail, int crumbId) {
		// TODO Auto-generated method stub
		
	}

	public String GetAllCrumbIdsForATrail(String trailId) {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "MATCH (crumb:Crumb) WHERE crumb.TrailId = '"+trailId+"' RETURN crumb";	
		return dbMaster.ExecuteCypherQueryJSONStringReturnJustIds(cypherQuery);
	}

	public String GetSimpleDetailsForATrail(String trailId) {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "start trail = node("+trailId+") return trail.UserId, trail.TrailName, trail.Description, trail.Views, trail.CoverId";		
		return dbMaster.ExecuteCypherQueryReturnTrailDetails(cypherQuery);
	}

	public String GetAllTrailIds() {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "MATCH (n:Trail) RETURN n LIMIT 100";	
		return dbMaster.ExecuteCypherQueryJSONStringReturnJustIds(cypherQuery);
	}
	
}