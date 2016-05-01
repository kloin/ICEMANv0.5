package com.breadcrumbs.database;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.Encoded;

import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.Traversal;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;

import com.breadcrumbs.database.DBMaster.myLabels;
import com.breadcrumbs.database.DBMaster.myRelationships;
import com.breadcrumbs.models.NodeConverter;


/*
 * Generic controller to manage the fetching of nodes and their children. 
 * 
 * This class should be used to get nodes and its relations, and its calling method
 * should be the one to enforce rules/other logic.
 * 
 * Written by Josiah Kendall December 2014
 */
public class NodeController implements INodeController {

	private NodeConverter nodeConverter;
	private DBMaster dbMaster;
	private GraphDatabaseService dbInstance;
	
	public NodeController() {
		dbInstance = DBMaster.GetAnInstanceOfDBMaster().GetDatabaseInstance();
		nodeConverter = new NodeConverter();
	}
	
	@Override
	public String TraversalTest() {
		
		TraversalDescription friendsTraversal = dbInstance.traversalDescription()
    	        .depthFirst()
    	        .relationships( myRelationships.Part_Of );
		
		return friendsTraversal.toString();
		
	}
	
	@Override
	public Node FetchNode(int id) {
		return dbInstance.getNodeById(id);
	}
	
	@Override
	public JSONObject FetchNodeJson(int id) {
		NodeConverter nodeConverter = new NodeConverter();
		
		// Debug log
		System.out.println("Constructing JSON for crumb");
		Transaction tx = dbInstance.beginTx();
		Node node = null;
		try {
			node = dbInstance.getNodeById(id);
			return nodeConverter.ConvertSingleNodeToJSON(node);
		} catch (Exception ex) {
			System.out.println("Failed to retrieve node");
			ex.printStackTrace();
			tx.failure();
			return null;
		} finally {
			tx.finish(); //what to do
			System.out.println("Fetched node...");
		}			
	}

	@Override
	public String FetchNodeAndItsRelations(int id, String relationship) {
		dbInstance = DBMaster.GetAnInstanceOfDBMaster().GetDatabaseInstance();
		ExecutionEngine engine = new ExecutionEngine( dbInstance );
		ExecutionResult result = null;
		Transaction tx = dbInstance.beginTx();
		try {
		    result = engine.execute("START trail=node("+ id + ") MATCH (trail)-[:"+relationship +"]->(crumb) RETURN distinct trail,crumb");
		    tx.success();
		    return convertIteratorToJSON(result);		
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.print("issues with fetching node and its relations");
			tx.failure();
			return "failed";
		} finally {
			tx.close();
		}	    		
	}	
	
	public String convertIteratorToJSON(ExecutionResult result) {
		//Convert our result into json;
		Iterator<Map<String, Object>> it = result.iterator();
		NodeConverter nodeConverter = new NodeConverter();
		JSONObject json = new JSONObject();
		//DOnt know if this index shit is neccessary
		int index = 0;
		while (it.hasNext()) {
			/* Haha im sorry let me explain. We know its an iterator of maps which, each have <KEY, NODE>, so we get that map.
			 * Then, next line we want to get the values, which we know will be always just one node.
			   Then, convert the collection to an array and get the first value (there will always only be one value).*/			 
			Map nodeItemMap = it.next();
			Collection valuesCollection = nodeItemMap.values();
			Node tempNode = (Node) valuesCollection.toArray()[0];
			json.put(Integer.toString(index), nodeConverter.ConvertSingleNodeToJSON(tempNode));
			index+= 1;			
		}
		
		return json.toString();
	}
	
	public String convertTrailPointToJSON(ExecutionResult result) {
		//Convert our result into json;
		Iterator<Map<String, Object>> it = result.iterator();
		NodeConverter nodeConverter = new NodeConverter();
		JSONObject json = new JSONObject();
		//DOnt know if this index shit is neccessary
		int index = 0;
		while (it.hasNext()) {
			/* Haha im sorry let me explain. We know its an iterator of maps which, each have <KEY, NODE>, so we get that map.
			 * Then, next line we want to get the values, which we know will be always just one node.
			   Then, convert the collection to an array and get the first value (there will always only be one value).*/			 
			Map nodeItemMap = it.next();
			Collection valuesCollection = nodeItemMap.values();
			Node tempNode = (Node) valuesCollection.toArray()[0];
			JSONObject tempJson = nodeConverter.ConvertSingleNodeToJSON(tempNode);
			json.put("Node"+ tempJson.getString("index"), tempJson);
			index+= 1;			
		}
		
		return json.toString();
	}
	
	/*
	 * There has to be a better way to do this.... :(
	 * Bassically what I am doing is getting the ids and mapping them into json, sorted by.. Their ids
	 */
	public String convertIteratorToJSONOfJustIds(ExecutionResult result) {
		Iterator<Map<String, Object>> it = result.iterator();
		NodeConverter nodeConverter = new NodeConverter();
		JSONObject json = new JSONObject();
		//DOnt know if this index shit is neccessary
		int index = 0;
		while (it.hasNext()) {
			Map nodeItemMap = it.next();
			Collection valuesCollection = nodeItemMap.values();
			Node tempNode = (Node) valuesCollection.toArray()[0];
			json.put("Node"+ tempNode.getId(), tempNode.getId());
			index+= 1;			
		}
		
		return json.toString();
	}
        
        public String convertIteratorToJSONOfHomeCardDetails(ExecutionResult result) {
		Iterator<Map<String, Object>> it = result.iterator();
		NodeConverter nodeConverter = new NodeConverter();
		JSONObject json = new JSONObject();
		//DOnt know if this index shit is neccessary
		int index = 0;
		while (it.hasNext()) {
			Map nodeItemMap = it.next();
			Collection valuesCollection = nodeItemMap.values();
			Node tempNode = (Node) valuesCollection.toArray()[0];
                        JSONObject details = new JSONObject();
                        details.put ("Id", tempNode.getId());
                        details.put ("DataType", tempNode.getProperty("Extension"));
			json.put("Node"+ tempNode.getId(), details);
			index+= 1;			
		}
		
		return json.toString();
	}

	@Override
	public void DeleteNode(int id) {
		//Transaction tx = dbInstance.beginTx();
		//dbInstance.
		
	}

	@Override
	public void DeleteNodeAndAllItsRelations(int id, String relationship) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String SaveNode(Hashtable<String, Object> keysAndItems, DBMaster.myLabels label) {
		dbInstance = DBMaster.GetAnInstanceOfDBMaster().GetDatabaseInstance();
		Transaction tx = dbInstance.beginTx();
		
		try {
			Node node = dbInstance.createNode(label);
			Long Id = node.getId();
			Iterator keyIterator = keysAndItems.keySet().iterator();
			while (keyIterator.hasNext()) {
				String key = keyIterator.next().toString();
				node.setProperty(key, keysAndItems.get(key).toString());
			}
			//this seems wrong
			//node.setProperty("UserId", Id);
			System.out.println("Saved node with Id: "+ Id);
			tx.success();
			return Id.toString();
		} catch (Exception ex) {
			tx.failure();
			throw ex;
		}
		finally {
			tx.finish();
		}
		
	}

	@Override
	public void SaveNodeWithRelation(int id, String label, String relationship) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void UpdateNode(int id) {
		// TODO Auto-generated method stub
		
	}
	
	public String GetAllRelatedNodesIds(String baseNodeId, myLabels label, String property, String nodeHeader) {
		nodeConverter = new NodeConverter();
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
    	if (dbMaster == null) {
    		System.out.print("There was an issue getting a valid instance of the database");
    	}
    	
    	//Get our trail
    	//Get all the crumbs for our trail
    	//Then format it like : {Trail : {crumb1: {chat : this, talk : this}, crumb2: {you : getit?}}
    	JSONObject trailJsonObject = new JSONObject();
    	
    	// Get all the crumbs
    	Transaction tx = dbMaster.GetDatabaseInstance().beginTx();
    	JSONObject jsonResponse = new JSONObject();
    	ResourceIterable<Node> node = dbMaster.GetDatabaseInstance().findNodesByLabelAndProperty(label, property, baseNodeId);
		Iterator nodeSearcher = node.iterator();

		try {
			int numberOfNodes = 0;
			while (nodeSearcher.hasNext()) {
				//Get the node once, as each time we use "Next()" we move forward on the list
				Node trail = (Node) nodeSearcher.next();
				System.out.println(trail.getId());
				//Construct a jsonString using the node converter for the node.
				//Add the string to the trail object under the crumbs id (? or name?)
				trailJsonObject.put(Integer.toString(numberOfNodes), trail.getId());
				numberOfNodes += 1;
				}
			} catch(JSONException ex) {
				System.out.println("THIS JUST CRASHED FUUUUUUCKKKKK");
				ex.printStackTrace();		
		}
		finally {
			tx.finish();
		}
		System.out.println("Here is the jsonOBject : "+ trailJsonObject.toString());
		return trailJsonObject.toString();
	}
	
	public String GetAllRelatedNodes(String baseNodeId,  myLabels label, String property, String nodeHeader) {
		nodeConverter = new NodeConverter();
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
    	if (dbMaster == null) {
    		System.out.print("There was an issue getting a valid instance of the database");
    	}
    	//Get our trail
    	//Get all the crumbs for our trail
    	//Then format it like : {Trail : {crumb1: {chat : this, talk : this}, crumb2: {you : getit?}}
    	JSONObject trailJsonObject = new JSONObject();
    	
    	// Get all the crumbs
    	Transaction tx = dbMaster.GetDatabaseInstance().beginTx();
    	JSONObject jsonResponse = new JSONObject();
    	ResourceIterable<Node> node = dbMaster.GetDatabaseInstance().findNodesByLabelAndProperty(label, property, baseNodeId);
		Iterator nodeSearcher = node.iterator();

		try {
			int numberOfNodes = 0;
			
			while (nodeSearcher.hasNext()) {
				//Get the node once, as each time we use "Next()" we move forward on the list
				Node trail = (Node) nodeSearcher.next();
				System.out.println(trail.getId());
				//Construct a jsonString using the node converter for the node.
				//Add the string to the trail object under the crumbs id (? or name?)
				JSONObject crumbString = nodeConverter.ConvertSingleNodeToJSON(trail);
				trailJsonObject.append(nodeHeader, crumbString);	
				}
			} catch(JSONException ex) {
				System.out.println("THIS JUST CRASHED FUUUUUUCKKKKK");
				ex.printStackTrace();		
		}
		finally {
			tx.finish();
		}
		System.out.println("Here is the jsonOBject : "+ trailJsonObject.toString());
		return trailJsonObject.toString();
	}

	@Override
	public void CreateNodeToNodeRelationship(String node1Id, String node2Id,
			RelationshipType relationship) {
		// TODO Auto-generated method stub
		
	}

}
