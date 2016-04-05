package com.breadcrumbs.models;

import org.json.JSONObject;

import com.breadcrumbs.database.DBMaster;

public class NodeManager {

	/*
	 * Fetch multiple parameters for one node.
	 * 
	 *  @Returns a jsonString of the parameters requested and their corresponding values.
	 * 	
	 */
	public String GetMutipleParametersFromANode(String nodeId, String[] parameterNames) {
		JSONObject result = new JSONObject();
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		for (String parameter : parameterNames) {
			String value = dbMaster.GetStringPropertyFromNode(nodeId, parameter).toString();
			result.put(parameter, value);
		}
		return result.toString();
	}
}
