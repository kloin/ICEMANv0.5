package com.breadcrumbs.models;

import java.util.Iterator;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.impl.transaction.xaframework.TxIdGenerator;

import com.breadcrumbs.database.DBMaster;
import com.breadcrumbs.database.NodeController;

import tools.JSONSpecialist;

import com.breadcrumbs.database.DBMaster.myRelationships;
import org.neo4j.graphdb.Relationship;

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
	
	public void UnpinUserForUser(String FollowingUser, String FollowedUser) {
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "start user = node("+FollowingUser+"), "
				+ "followedUser = node("+FollowedUser+") "
				+ "match user-[rel:Has_Pinned]->followedUser "
				+ "delete rel";
		
		dbMaster.ExecuteCypherQueryNoReturn(cypherQuery);
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
		return dbMaster.ExecuteCypherQueryJSONStringReturnJustIds(cypherQuery);
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
        
        public String GetAllPhotoIdsForATrail(String userId) {
            DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
            String cypherQuery = "MATCH (crumb:Crumb) WHERE crumb.UserId = '"+userId+"' AND crumb.Extension = '.jpg' RETURN crumb";	
            return dbMaster.ExecuteCypherQueryJSONStringReturnJustIds(cypherQuery);
	}

	public void SetUserAbout(String userId, String description) {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		Node user = dbMaster.RetrieveNode(Integer.parseInt(userId));
		//dbMaster.updateNode(user, "Description", description);
		//dbMaster.CreateRelationship(user, newContact, myRelationships.Friends_With);	
		
	}
	
	public String GetUserAbout(String userId) {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		Node user = dbMaster.RetrieveNode(Integer.parseInt(userId));
		return dbMaster.GetStringPropertyFromNode(Integer.toString((int) user.getId()), "description");
	}

	public String GetNumberOfTrailsAUserOwns(String userId) {
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "start n =node("+userId+") match (trail:Trail)--(n) Return count(*)";
		return dbMaster.ExecuteCypherQueryReturnCount(cypherQuery);
	}

	// Save an a clients GCMInstanceID to its database
	public String SetGCMClientInstanceID(String userId, String gcmInstanceID) {
		
		return null;
	}

        public boolean IsUserAFollowingUserB(String userAId, String userBId) {
            DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
            
            GraphDatabaseService _db = dbMaster.GetDatabaseInstance();
            Transaction tx = _db.beginTx();
            Node node = null;
            try {
                Node follower = dbMaster.RetrieveNode(Long.parseLong(userAId));
                Node followed = dbMaster.RetrieveNode(Long.parseLong(userBId));
                for (Relationship followedUserIds : follower.getRelationships()) {
                    if (followedUserIds.getOtherNode(follower).equals(followed)) {
                        return true;
                    }
                }
            } catch (Exception ex) {
                    System.out.println("Failed to retrieve node");
                    ex.printStackTrace();
                    tx.failure();
                    return false;

            } finally {
                    tx.finish();
            }
            return false;
        }
        
	public String GetAllPinnedUsers(String userId) {
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "start n = node("+userId+") match n-[:Has_Pinned]->(user:User) return user";	
		return dbMaster.ExecuteCypherQueryJSONStringReturnJustIds(cypherQuery);
	}
        
        public String GetNumberOfUsersThatWeFollow(String userId) {
            DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
            String cypherQuery = "start n = node("+userId+") match n-[:Has_Pinned]->(user:User) return count(*)";	
            return dbMaster.ExecuteCypherQueryReturnCount(cypherQuery);
        }
        
        public String GetAllUsersThatFollowUs(String userId) {
            DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
            String cypherQuery = "start n = node("+userId+") match (user:User)-[:Has_Pinned]->(n) return user";
            return dbMaster.ExecuteCypherQueryJSONStringReturnJustIds(cypherQuery);
        }
        
        public String GetNumberOfUsersThatFollowUs(String userId) {
            DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
            String cypherQuery = "start n = node("+userId+") match (user:User)-[:Has_Pinned]->(n) Return count(*)";
            return dbMaster.ExecuteCypherQueryReturnCount(cypherQuery);
        }
       

	/*
	 * Get all Trails for our pinned users, as well as all the trails that we have pinned.
	 */
	public JSONObject GetAllOurPinnedShit(String userId) {
		UserService userService = new UserService();
		JSONObject userIdsJson = new JSONObject(userService.GetAllPinnedUsers(userId));
		Trail trail = new Trail();
		JSONObject allTrailIds = new JSONObject(trail.FindAllPinnedTrailsForAUser(userId));
		Iterator<String> userIdsKeys = userIdsJson.keys();
		
		// For each followed user, we want to get ALL their trails and add them to the list of trails we are returning.
		while (userIdsKeys.hasNext()) {
			String key = userIdsKeys.next();
			int followedUserId = (int) userIdsJson.get(key);
			
			// get all trails for this users
			String tempUsersTrails = trail.GetAllTrailsForAUser(followedUserId);
			JSONObject tempUserTrailsJson = new JSONObject(tempUsersTrails);
			// Add to the list
			JSONSpecialist jsonSpecialist = new JSONSpecialist();
			jsonSpecialist.JoinTwoJsonObjects(allTrailIds, tempUserTrailsJson);			
		}
		
		return allTrailIds;
	}

	public String CheckForDetailsUsingEmailAddress(String email) {
		Node userNode = FetchUserNodeByEmailAddress(email);
		if (userNode == null) {
			return "404";
		}
		DBMaster db = DBMaster.GetAnInstanceOfDBMaster();
		GraphDatabaseService _db = db.GetDatabaseInstance();
		Transaction tx = _db.beginTx();
		try {
			String Username = userNode.getProperty("Username").toString();
			String Pin = userNode.getProperty("Pin").toString();
			sendEmail(Username, Pin, email);
			tx.success();
		} catch (Exception ex) {
			// caught general exception getting properties
			System.out.println("Found node but failed to get property: " + ex.toString());
			tx.failure();
		} finally {
			tx.finish();
		}
		
		//If we are here, we found the user. Fetch details about them for sending the email.
		return "200";
	}
	
	private void sendEmail(String Username, String Pin, String email) {
		final String testName = "josiahephraim.kendall@gmail.com";
		final String testPass = "surf4u2day";

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(testName, testPass);
			}
	  });

		String from = "josiahephraim.kendall@gmail.com";
		try {
			 MimeMessage message = new MimeMessage(session);

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(from));

	         // Set To: header field of the header.
	         message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));

	         // Set Subject: header field
	         message.setSubject("BreadCrumbs Account Details");

	         // Now set the actual message
	         message.setText("Details for breadcrumbs account: \n"
	         		+ "Username : " + Username + " \n"
     				+ "Pin : "+ Pin);

	         // Send message
	         Transport.send(message);
	         System.out.println("Sent message successfully....");
		} catch(MessagingException mex) {
			mex.printStackTrace();
		}
	}

	private Node FetchUserNodeByEmailAddress(String email) {
		// TODO Auto-generated method stub
		DBMaster db = DBMaster.GetAnInstanceOfDBMaster();
		String query = "MATCH (a:User) WHERE a.Email = '" + email + "' RETURN a";
		Node node = db.ExecuteCypherQueryReturnNode(query);
		return node;
	}
	
	private Node FetchUserNodeByFacebookId(String facebookId) {
		// TODO Auto-generated method stub
		DBMaster db = DBMaster.GetAnInstanceOfDBMaster();
		String query = "MATCH (a:User) WHERE a.FacebookLoginId = '" + facebookId + "' RETURN a";
		Node node = db.ExecuteCypherQueryReturnNode(query);
		return node;
	}

	
	public String CheckForUserExistenceUsingFacebookId(String facebookUserId) {
		Node userNode = FetchUserNodeByFacebookId(facebookUserId);
		if (userNode == null) {
			return "404"; // Not found.
		}
		
		DBMaster db = DBMaster.GetAnInstanceOfDBMaster();
		GraphDatabaseService _db = db.GetDatabaseInstance();
		Transaction tx = _db.beginTx();
		try {
			String UserId = Long.toString(userNode.getId());
			tx.success();
			return UserId;
		} catch (Exception ex) {
			// caught general exception getting properties
			System.out.println("Found node but failed to get property: " + ex.toString());
			tx.failure();
		} finally {
			tx.finish();
		}
		
		return "500"; // Indicating that an error occurred
		
		//If we are here, we found the user. Fetch details about them for sending the email.
		
	}

	public String AttemptToLogInUser(String userName, String pin) {
		Node userNode = FetchUserNodeByUserName(userName);
		if (userNode == null) {
			return "Failed to find User with userName : "+ userName; // Not found.
		}
		DBMaster db = DBMaster.GetAnInstanceOfDBMaster();
		GraphDatabaseService _db = db.GetDatabaseInstance();
		Transaction tx = _db.beginTx();
		try {
			if (!userNode.getProperty("Pin").toString().equals(pin)) {
				return "Pin does not match username";
			}
			String UserId = Long.toString(userNode.getId());
			tx.success();
			return UserId;
		} catch (Exception ex) {
			// caught general exception getting properties
			System.out.println("Found node but failed to get property: " + ex.toString());
			tx.failure();
		} finally {
			tx.finish();
		}
		
		return "E500"; // Indicating that an error occurred
	}

	private Node FetchUserNodeByUserName(String userName) {
		DBMaster db = DBMaster.GetAnInstanceOfDBMaster();
		String query = "MATCH (a:User) WHERE a.Username = '" + userName + "' RETURN a";
		Node node = db.ExecuteCypherQueryReturnNode(query);
		return node;
	}

    public String GetUser(String userId) {
        DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
        GraphDatabaseService _db = dbMaster.GetDatabaseInstance();
        Node userNode = dbMaster.RetrieveNode(Long.parseLong(userId));
        if (userNode == null) {
            return "500";
        }
        
        Transaction tx = _db.beginTx();
        try {
            NodeConverter nodeConverter = new NodeConverter();
            JSONObject result = nodeConverter.ConvertSingleNodeToJSON(userNode);
            return result.toString();
        } catch (Exception ex) {
            System.out.println("Failed to retrieve node");
            ex.printStackTrace();
            tx.failure();
            return "500";
        } finally {
            tx.finish();
        }
    }

}
