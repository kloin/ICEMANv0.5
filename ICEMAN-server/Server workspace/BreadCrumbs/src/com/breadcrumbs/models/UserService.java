package com.breadcrumbs.models;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.breadcrumbs.database.DBMaster;
import com.breadcrumbs.database.NodeController;
import com.breadcrumbs.database.DBMaster.myRelationships;

public class UserService {
	private DBMaster dbMaster;
	/*
	 * Fetch just the users name
	 */
	public String FetchUserName(String UserId) {
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		GraphDatabaseService db = dbMaster.GetDatabaseInstance();
		Transaction tx = db.beginTx();
		Node node = null;
		try {
			node = db.getNodeById(Integer.parseInt(UserId));
			String name = node.getProperty("Username").toString();
			tx.success();
			return name;
		} catch (Exception ex) {
			System.out.println("Failed to retrieve node");
			ex.printStackTrace();
			tx.failure();
		} finally {
			tx.finish();
		}
		return "Error";
	}

	// Add a user to their contacts. Later this will need a bunch of validation/ waiting for acceptance etc..
	public void PinUserForUser(String UserIdA, String UserIdB) {
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		Node user1 = dbMaster.RetrieveNode(Integer.parseInt(UserIdA));
		Node user2 = dbMaster.RetrieveNode(Integer.parseInt(UserIdB));
		dbMaster.CreateRelationship(user1, user2, myRelationships.Has_Pinned);		
	}
	
	// This is the method that deletes a user from their contacts.
	public void UnPinUserForAUser(String UserIdA, String UserIdB) {
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "start user = node("+UserIdA+"), "
				+ "loser = node("+UserIdB+") "
				+ "match user-[rel:Has_Pinned]->loser "
				+ "delete rel";		
		dbMaster.ExecuteCypherQueryNoReturn(cypherQuery);
	}

	/*
	 * Get a list of all trails that a user can edit. This populates the list when adding crumbs etc..
	 */
	public String GetAllEditibleTrailsForAUser(String userId) {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "start n = node("+userId+") match n-[rel:Controls]->(Trail) return Trail";	
		return dbMaster.ExecuteCypherQueryJSONStringReturn(cypherQuery);
	}

	//TODO: all this shit.
	public String GetAllContactsForAUser(String userId) {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "start n = node("+userId+")  match n-[rel:Friends_With]->(User) return User";	
		return dbMaster.ExecuteCypherQueryJSONStringReturn(cypherQuery);	
	}
	/* When a user sends a friend request, this is called. When a user logs in they are notified of their pending request*/
	public String SendFriendRequest(String UserId, String ContactsId) {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		GraphDatabaseService db = dbMaster.GetDatabaseInstance();
		Node user = dbMaster.RetrieveNode(Integer.parseInt(UserId));
		Node contactAdded = dbMaster.RetrieveNode(Integer.parseInt(ContactsId));
		
		// Add connection. Also need to do something about notifications.
		dbMaster.CreateRelationship(user, contactAdded, myRelationships.Has_Added);
		return "Done";
	}
	/* This is when a user accepts a friend request. This makes them contacts.*/
	public String AddContactForAUser(String UserId, String ContactsId) {	
		
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		Node user = dbMaster.RetrieveNode(Integer.parseInt(UserId));
		Node newContact = dbMaster.RetrieveNode(Integer.parseInt(ContactsId));
		
		dbMaster.CreateRelationship(user, newContact, myRelationships.Friends_With);	
		return "Done";
	}
	
	
	public String DeleteContactForAUser(String UserId, String ContactsId) {
		
		return "Done";
	}

	public String GetAllCrumbIdsForAUser(String userId) {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "MATCH (crumb:Crumb) WHERE crumb.UserId = '"+userId+"' RETURN crumb";	
		return dbMaster.ExecuteCypherQueryJSONStringReturnJustIds(cypherQuery);
	}

	public void SetUserAbout(String userId, String description) {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		Node user = dbMaster.RetrieveNode(Integer.parseInt(userId));
		dbMaster.updateNode(user, "Description", description);
		
		//dbMaster.CreateRelationship(user, newContact, myRelationships.Friends_With);	
		
	}
	
	public String GetUserAbout(String userId) {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		Node user = dbMaster.RetrieveNode(Integer.parseInt(userId));
		return dbMaster.GetStringPropertyFromNode(Integer.toString((int) user.getId()), "description");
	}

}
