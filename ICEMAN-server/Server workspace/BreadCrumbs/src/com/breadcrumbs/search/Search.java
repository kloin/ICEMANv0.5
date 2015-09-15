package com.breadcrumbs.search;

import org.json.JSONObject;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import com.breadcrumbs.database.DBMaster;
import com.breadcrumbs.database.NodeController;

/*
 * This is the class that handles search functions on the database.
 */
public class Search {
	private GraphDatabaseService dbInstance; 
	private NodeController nc;
	/*
	 * This is a class that searches the name of a trail, the name of a collection, or a users name.
	 * It will return matching instances of all three in a JSONObject.*/
	public String SearchAllNodesWithLabelByGivenProperty(String searchtext) {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "MATCH (n:Trail) WHERE n.TrailName =~ '"+searchtext+".*' RETURN n";	
		return dbMaster.ExecuteCypherQueryJSONStringReturn(cypherQuery);				
	}
	
	public String SearchAllTrailsByGivenText(String searchText) {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "MATCH (n:Trail) WHERE n.TrailName =~ '"+searchText+".*' RETURN n";	
		return dbMaster.ExecuteCypherQueryJSONStringReturn(cypherQuery);
	}

	public String SearchAllUsersByGivenText(String searchText) {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "MATCH (n:User) WHERE n.FirstName =~'"+searchText+".*' OR n.LastName=~'"+searchText+".*' RETURN n";
		String result = dbMaster.ExecuteCypherQueryJSONStringReturn(cypherQuery);
		//String cypherQuery2 = "MATCH (n:User) WHERE n.LastName =~'"+searchText+".*' RETURN n";
		//result += dbMaster.ExecuteCypherQueryJSONStringReturn(cypherQuery2);
		return result;
	}
	
}
