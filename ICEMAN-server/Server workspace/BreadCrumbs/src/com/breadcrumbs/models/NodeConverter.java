package com.breadcrumbs.models;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.graphdb.Node;

public class NodeConverter {
	
	/*
	 * Give this classes method a node, and it will output json of the values
	 */
	public JSONObject ConvertSingleNodeToJSON(Node node) {
		
		//Take node, get all keys, add to all values
		JSONObject jsonResponse = new JSONObject();
		
		Iterable<String> keys = node.getPropertyKeys();
		Iterator<String> keysIterator = keys.iterator();
		while (keysIterator.hasNext()) {
			String key = keysIterator.next();
			String value = (String) node.getProperty(key).toString();
			
			/* Here i surround just the code that is doing the work, rather than the entire block.
			 * This has been done because I want the iteration to carry on through and exit as normal 
			 * if one value fails.
			 */		
			try {
				jsonResponse.put(key, value);
			} catch (JSONException e) {
				System.out.println("Iteration failed to save value : " + key + ": " + value + 
						" to JSONOBject. Iteration Continuing.");
				e.printStackTrace();
			}
		}
		int idToCast = (int) node.getId();
		String id = Integer.toString(idToCast);
		try {
			jsonResponse.put("Id", id);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jsonResponse;
				
		
	}

    public JSONObject ConvertSingleNodeToJSONWithSelectedParams(Node tempNode, ArrayList<String> ids) {
        //Take node, get all keys, add to all values
		JSONObject jsonResponse = new JSONObject();
		
		Iterable<String> keys = tempNode.getPropertyKeys();
		Iterator<String> keysIterator = ids.iterator();
		while (keysIterator.hasNext()) {
			String key = keysIterator.next();
			String value = (String) tempNode.getProperty(key).toString();
			
			/* Here i surround just the code that is doing the work, rather than the entire block.
			 * This has been done because I want the iteration to carry on through and exit as normal 
			 * if one value fails.
			 */		
			try {
				jsonResponse.put(key, value);
			} catch (JSONException e) {
				System.out.println("Iteration failed to save value : " + key + ": " + value + 
						" to JSONOBject. Iteration Continuing.");
				e.printStackTrace();
			}
		}
		int idToCast = (int) tempNode.getId();
		String id = Integer.toString(idToCast);
		try {
			jsonResponse.put("Id", id);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jsonResponse;
    }
}
