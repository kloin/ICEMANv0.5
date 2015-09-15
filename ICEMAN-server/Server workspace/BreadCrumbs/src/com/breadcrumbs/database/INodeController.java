package com.breadcrumbs.database;

import java.util.Hashtable;

import org.json.JSONObject;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

/*
 * Interface to methods that manage fetching, saving, deleting and updating of nodes
 * why the fuck did i create an interface for this it will never be inherited.
 * Josiah Kendall 2014
 */
public interface INodeController {

	public Node FetchNode(int id);
	public JSONObject FetchNodeJson(int id);
	public String FetchNodeAndItsRelations(int id, String relationship);
	public void DeleteNode(int id);
	public void DeleteNodeAndAllItsRelations(int id, String relationship);
	public String SaveNode(Hashtable<String, Object> itemsAndKeys, com.breadcrumbs.database.DBMaster.myLabels label);
	public void CreateNodeToNodeRelationship(String node1Id, String node2Id, RelationshipType relationship);
	public void SaveNodeWithRelation(int id, String label, String relationship);
	public void UpdateNode(int id);
	public String TraversalTest();
}
