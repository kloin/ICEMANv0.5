package com.breadcrumbs.database;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;
import org.neo4j.*;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.breadcrumbs.models.NodeConverter;
import com.breadcrumbs.models.UserService;
import org.json.JSONArray;

/*
 * This class controls the database.
 * 
 * It has a constructor, a get method that returns an instance of
 * the database, and a shutdown method (private)
 * 
 * Written by Josiah Kendall, 1 February 2014
 */
public class DBMaster {
	
	//Our database instance
	private static GraphDatabaseService _db;
	private static DBMaster singletonDBMaster;
	public static String serverAddress = "/var/lib/tomcat7/webapps/images/";
	
	/*
	 * Straight up constructing. This needs to be a singleton because the db can only be shared,
	 * because constructing it is very expensive.
	 */
	private DBMaster() {
		//The current address is localhost - needs to be changed at a later date
		_db = new GraphDatabaseFactory().newEmbeddedDatabase("\\etc\\neo4j\\default.graphdb");
		registerShutdownHook();
	}
	
	public static DBMaster GetAnInstanceOfDBMaster() {
		//If an instance exists, return it, else create a new one.
		//NOTE: I'm not sure what happens when multiple threads access this one instance
		//This could cause bugs
		if (singletonDBMaster == null) {
			singletonDBMaster = new DBMaster();
		}
		
		return singletonDBMaster;
	}
		
	public enum myRelationships implements RelationshipType {
		Controls,
		Friends_With,
		Has_Added,
		Part_Of,
		Linked_To,
		Point_In,
		Has_Pinned,
		Likes
	 }
        
        public enum myActivities {
            Driving,
            Walking,
            Flying
        }

	public enum myLabels implements Label
	{
		Person,
		User,
		Trail,
		Crumb,
                Image,
		Point,
		Comment,
                Event,
                Place,
                Polyline
	}
        
        public interface TransactionCallback {
            void doTransaction();
        }
	//Return an a reference to the instance of the database
	public GraphDatabaseService GetDatabaseInstance() {
		return  _db;
	}
	
	public void Stop() {
		_db.shutdown();
	}
	
	//Shut down the database on exit - Should actually never happen once the db is running
	private static void registerShutdownHook() {
		// Registers a shutdown hook for the Neo4j instance so that it
	    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	    // running application).
	    Runtime.getRuntime().addShutdownHook( new Thread()
	    {
	        @Override
	        public void run()
	        {
	            _db.shutdown();
	        }
	    } );
		
	}

        /**
        * A callback to do work inside a transaction, so that we dont have to 
        * deal with messy transactions every time we want to grab info from a node.
        * @param callback 
        */
 
        public void DoTransaction(TransactionCallback callback) {
            Transaction tx = _db.beginTx();
            try {
                    callback.doTransaction();
                    tx.success();
                } catch (Exception ex) {
			System.out.println("Failed to do callback transaction.");
			ex.printStackTrace();
			tx.failure();
		} finally {
			tx.finish();
                        tx.close();
		}
        }
        
	public Node RetrieveNode(long id) {
		Transaction tx = _db.beginTx();
		Node node = null;
		try {
			node = _db.getNodeById(id);
		} catch (Exception ex) {
			System.out.println("Failed to retrieve node");
			ex.printStackTrace();
			tx.failure();
                        return null;
		} finally {
			tx.finish();
		}

		return node;
	}

	public int AddUser(String firstName, String lastName, int age) {
		int id = 0;
		Transaction tx = _db.beginTx();
		try {
			Node node = _db.createNode();
			node.addLabel(myLabels.Person);
			id = (int) node.getId();
			//Set first name, lastname, any other name.. (to be implemented)
			
			node.setProperty("FirstName", firstName);
			node.setProperty("LastName", lastName);
			node.setProperty("Age", age);
			tx.success();			
		}
		catch (Exception ex){
			tx.failure();
		}
		finally {
			tx.finish();
		}
		
		
		//The successful case;
		return id;
		
	}
	
	public void AddViewToTrail(String TrailId) {
		Node trail = this.RetrieveNode(Integer.parseInt(TrailId));
		int views = 0;
		Transaction tx = _db.beginTx();
		try {
			// If the views has already been set, update it by adding one more			
			views =  Integer.parseInt((String)trail.getProperty("Views"));
			views += 1;
			trail.setProperty("Views", Integer.toString(views));
			tx.success();			
		}
		catch(NotFoundException ex) {
			// Set the property to be 0. This will happen on first count with old trails
			//trail.setProperty("Views", views);
			tx.success();
		}	
		catch (Exception ex){
			tx.failure();
		}
		finally {
			tx.finish();
		}
	}
	
	public String GetTrailViews(String TrailId) {
		Node trail = this.RetrieveNode(Integer.parseInt(TrailId));
		// Get Views property if it exists, else return 0.
		Transaction tx = _db.beginTx();
		try {
			String views = trail.getProperty("Views").toString();	
			tx.success();
			return views;
		} catch (NotFoundException ex) {
			tx.failure();
		} finally {
			tx.finish();
		}
		return "0";
	}
	
	public int SaveNode(Hashtable<String, Object> keysAndItems, com.breadcrumbs.database.DBMaster.myLabels label) {
		
		Transaction tx = _db.beginTx();
		int Id;
		Node node;
		
		try {
			node = _db.createNode(label);
			Id = (int) node.getId();
			Iterator<String> keyIterator = keysAndItems.keySet().iterator();
			while (keyIterator.hasNext()) {
				String key = keyIterator.next();
				node.setProperty(key, keysAndItems.get(key).toString());
			}
			System.out.println("Saved with Id: "+ Id);
			tx.success();
			
		} catch (Exception ex) {
			tx.failure();
			throw ex;
		}
		finally {
			tx.finish();
		}
		return Id;
	}
	/*
	 * Create a relationship between two nodes
	 */
	public void CreateRelationship(Node node1, Node node2, myRelationships relation) {
		Transaction tx = _db.beginTx(); 
		try {
			node1.createRelationshipTo(node2, relation);
			tx.success();
		} catch (Exception ex) {
			tx.failure();
			throw ex;
		} finally {
			tx.finish();
		}
				
	}
	//Add a trail related to a certain user.
	public int AddTrail(String trailName, String userId, String description) {
		int id = 0;
		Transaction tx = _db.beginTx();
		
		try {
			
			//Create trail node
			Label label = myLabels.Trail;
			Node trailNode = _db.createNode();
			
			//Attatch a label to our node
			trailNode.addLabel(label);
			id = (int) trailNode.getId();
			
			//Set properties
			trailNode.setProperty("TrailName", trailName);
			trailNode.setProperty("UserId", userId);
			trailNode.setProperty("Description", description);
			trailNode.setProperty("TrailId", id);
			
			//Get the user node (it must exist as this function can only occur as
			//a result of a user request.
			Node userNode = _db.getNodeById(Integer.parseInt(userId));
			userNode.createRelationshipTo(trailNode, myRelationships.Controls);
			
			System.out.println("Added trail with id: " + id);
			tx.success();			
		}
		catch (Exception ex){
			tx.failure();
		}
		finally {
			tx.finish();
		}
		
		return id;
	}

	public int AddCrumb(String crumbTitle, String description, int userId, String trailId,
			String crumbMedia, String Latitude, String Longitude) {
		
		int id = 0;
		
		Transaction tx = _db.beginTx();
				
			try {
				//Create trail node
				Label label = myLabels.Crumb;
				Node crumb = _db.createNode();
				
				//Attatch a label to our node
				crumb.addLabel(label);
				id = (int) crumb.getId();
				
				//Set properties
				crumb.setProperty("Title", crumbTitle);
				crumb.setProperty("user", userId);
				crumb.setProperty("trailId", trailId);
				crumb.setProperty("Description", description);
				crumb.setProperty("mediaLocation", crumbMedia);
				crumb.setProperty("Longitude", Longitude);
				crumb.setProperty("Latitude", Latitude);
				
				//Set the trail that the crumb belongs to
				Node trail = _db.getNodeById(Integer.parseInt(trailId));
				trail.createRelationshipTo(crumb, myRelationships.Part_Of);
				
				tx.success();			
			} catch (Exception ex) {
				System.err.println("The database has crashed during saving");
				ex.printStackTrace();
				
				//Let the database know its know happening
				tx.failure();
			} finally {
				//Shut the fucking door
				tx.finish();
			}
		
		
		return id;
	}
        
        /**
         * Update a node using a cypher query, as opposed to programatically using
         * the {@link #updateNode(java.lang.String, java.lang.String, java.lang.String)}
         * method. This is useful as it can add new properties to a node, where
         * as the {@link #updateNode(java.lang.String, java.lang.String, java.lang.String)}
         * method fails if the property does not exist on an already existing node.
         * @param nodeId The id of the node that we are updating.
         * @param propertyName The property of the node that we are updating.
         * @param propertyValue The new value we are setting.
         */
        public void UpdateNodeWithCypherQuery(String nodeId, String propertyName, String propertyValue) {
                String cypherQuery = "MATCH (node) "
                                    +"WHERE ID(node) = " + nodeId + " " // appended space like that for readability.
                                    +"SET node." + propertyName + " = '" + propertyValue + "'";
                ExecuteCypherQueryNoReturn(cypherQuery);
            
        }
	
        /**
         * Update an already existing property value.
         * @param nodeId The id of the node to update.
         * @param propertyName The name of the property to update.
         * @param propertyValue The update value.
         */
	public void updateNode(String nodeId, String propertyName, String propertyValue) {
		Node node = RetrieveNode(Integer.parseInt(nodeId));
               
		Transaction tx = _db.beginTx();
		try {
			node.setProperty(propertyName, propertyValue);
			System.out.println("Saved "+propertyName + ": "+propertyValue);
			tx.success();
		} catch(Exception ex) {
			System.out.println("error occured updating a node: " + ex.toString());
			tx.failure();
		} finally {
			tx.close();
		}
	}
	
	public String GetAllFieldsOnANode(String nodeId) {
		
		Node node = RetrieveNode(Integer.parseInt(nodeId));
		
		return "";
	}
	
	/*
	 * This currently does not return anything. It would probably be better to return a boolean if successful etc..
	 */
	public void ExecuteCypherQueryNoReturn(String cypherQuery) {
		ExecutionEngine engine = new ExecutionEngine( _db );
		ExecutionResult result = null;
		Transaction tx = _db.beginTx();
		try {
		    engine.execute(cypherQuery);
		    tx.success();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.print("issues with fetching node and its relations");
			tx.failure();
			//return "failed";
		} finally {
			tx.close();
		}
	}

	/*
	 * Execute a cypher query with a string in JSON format as the return value.
	 */
	public String ExecuteCypherQueryJSONStringReturn(String cypherQuery) {
		String nodeString = "";
		NodeController nc = new NodeController();
		ExecutionEngine engine = new ExecutionEngine( _db );
		ExecutionResult result = null;
		Transaction tx = _db.beginTx();
		try {
		    result = engine.execute(cypherQuery);
		    JSONObject tempNode = new JSONObject(nc.convertIteratorToJSON(result));	
		    nodeString = tempNode.toString();
		    tx.success();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.print("issues with fetching node and its relations");
			tx.failure();
			//return "failed";
		} finally {
			tx.close();
		}
		
		return nodeString;
	}
        
        /*
	 * Execute a cypher query with a string in JSON format as the return value.
	 */
	public String ExecuteCypherQueryJSONListStringReturn(String cypherQuery) {
		
                String resultString = "";
		NodeController nc = new NodeController();
		ExecutionEngine engine = new ExecutionEngine( _db );
		ExecutionResult result = null;
		Transaction tx = _db.beginTx();
		try {
		    result = engine.execute(cypherQuery);
		    resultString = nc.convertIteratorToJSONArray(result);	
                      
		    tx.success();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.print("issues with fetching node and its relations");
			tx.failure();
			//return "failed";
		} finally {
			tx.close();
		}
		
		return resultString;
	}
	
	public String ExecuteCypherQueryReturnPoint(String cypherQuery) {
		String nodeString = "";
		NodeController nc = new NodeController();
		ExecutionEngine engine = new ExecutionEngine( _db );
		ExecutionResult result = null;
		Transaction tx = _db.beginTx();
		try {
		    result = engine.execute(cypherQuery);
		    JSONObject tempNode = new JSONObject(nc.convertTrailPointToJSON(result));	
		    nodeString = tempNode.toString();
		    tx.success();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.print("issues with fetching node and its relations");
			tx.failure();
			//return "failed";
		} finally {
			tx.close();
		}
		
		return nodeString;
	}
	/*
	 * Made to return a single result - such as a count - from an ExecutionResult
	 */
	public String ExecuteCypherQueryReturnCount(String cypherQuery) {
		String nodeString = "";
		NodeController nc = new NodeController();
		ExecutionEngine engine = new ExecutionEngine( _db );
		ExecutionResult result = null;
		Transaction tx = _db.beginTx();
		try {
		    result = engine.execute(cypherQuery);
		    Iterator<Long> iterator = result.columnAs("count(*)");
		    nodeString = Long.toString(iterator.next());
		    tx.success();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.print("issues with fetching node and its relations");
			tx.failure();
			//return "failed";
		} finally {
			tx.close();
		}
		
		return nodeString;
	}
	public String ExecuteCypherQueryJSONStringReturnJustIds(String cypherQuery) {
		String nodeString = "";
		NodeController nc = new NodeController();
		ExecutionEngine engine = new ExecutionEngine( _db );
		ExecutionResult result = null;
		Transaction tx = _db.beginTx();
		try {
		    result = engine.execute(cypherQuery);
		    JSONObject tempNode = new JSONObject(nc.convertIteratorToJSONOfJustIds(result));	
		    nodeString = tempNode.toString();
		    tx.success();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.print("issues with fetching node and its relations");
			tx.failure();
			//return "failed";
		} finally {
			tx.close();
		}
		
		return nodeString;
	}
        
        public String ExecuteCypherQueryReturnCSVString(String cypherQuery) {
		String nodeString = "";
		NodeController nc = new NodeController();
		ExecutionEngine engine = new ExecutionEngine( _db );
		ExecutionResult result = null;
		Transaction tx = _db.beginTx();
		try {
		    result = engine.execute(cypherQuery);
                    String resultCSVString = nc.convertIteratorToCSVString(result);	
		    tx.success();
                    return resultCSVString;
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.print("issues with fetching node and its relations");
			tx.failure();
			//return "failed";
		} finally {
			tx.close();
		}
		
		return nodeString;
	}
        
          // Method to get all the details needed for all the home cards.
    public String ExecuteCypherQueryJSONStringReturnCrumbCardDetails(String cypherQuery) {
        String nodeString = "";
        NodeController nc = new NodeController();
        ExecutionEngine engine = new ExecutionEngine( _db );
        ExecutionResult result = null;
        Transaction tx = _db.beginTx();
        try {
            result = engine.execute(cypherQuery);
            JSONObject tempNode = new JSONObject(nc.convertIteratorToJSONOfHomeCardDetails(result));	
            nodeString = tempNode.toString();
            tx.success();
        } catch (Exception ex) {
                ex.printStackTrace();
                System.out.print("issues with fetching node and its relations");
                tx.failure();
                //return "failed";
        } finally {
                tx.close();
        }

        return nodeString;
    }

	public void SetCoverPhoto(String trailId, String imageId) {
		Node trail = this.RetrieveNode(Integer.parseInt(trailId));
		Transaction tx = _db.beginTx();
		try {
			trail.setProperty("coverPhotoId", imageId);
			tx.success();			
		}
		catch(NotFoundException ex) {
			// Set the property to be 0. This will happen on first count with old trails
			//trail.setProperty("Views", views);
			tx.success();
		}	
		catch (Exception ex){
			tx.failure();
		}
		finally {
			tx.finish();
		}
	}
	/*
	 * @depreciated
	 */
	public void DeleteNode(long NodeId) {
		Node node = this.RetrieveNode(NodeId);
		Transaction tx = _db.beginTx();
		try {
			node.delete();
			
			tx.success();			
		}
		catch(NotFoundException ex) {
			// Set the property to be 0. This will happen on first count with old trails
			//trail.setProperty("Views", views);
			tx.success();
		}	
		catch (Exception ex){
			tx.failure();
		}
		finally {
			tx.finish();
		}

	}

	public String ExecuteCypherQueryReturnTrailDetails(String cypherQuery) {
		// TODO Auto-generated method stub		
		String nodeString = "";
		NodeController nc = new NodeController();	    
	    UserService userService = new UserService();
		ExecutionEngine engine = new ExecutionEngine( _db );
		ExecutionResult result = null;
		Transaction tx = _db.beginTx();
		try {
		    result = engine.execute(cypherQuery);
		    Iterator<Map<String, Object>> it = result.iterator();
		    Map map = it.next();	    
		    // Get objects
		    Object descriptionObject = map.get("trail.Description");
		    Object viewsObject = map.get("trail.Views");
		    Object userIdObject = map.get("trail.UserId");
		    Object trailNameObject = map.get("trail.TrailName");
		    Object coverIdObject = map.get("trail.CoverPhotoId");	
		    System.out.print("Fetched objects from map");
		    String description = "";
		    String views = "";
		    String userId = "";
		    String userName = "";
		    String trailName = "";
		    String coverPhotoId = "";
		    
		    if (descriptionObject != null) {
		    	description = descriptionObject.toString();
		    }
		    if (viewsObject != null) {
		    	views = viewsObject.toString();
		    }
		    
		    if (userIdObject != null) {
		    	userId = userIdObject.toString();
		    }
		    
		    if (trailNameObject != null) {
		    	trailName = trailNameObject.toString();
		    }
		    
		    if (coverIdObject != null) {
		    	coverPhotoId = coverIdObject.toString();
		    }
		    
		    
		    
		    // Put objects
		    JSONObject tempNode = new JSONObject();	
		    tempNode.put("description", description);
		    tempNode.put("views", views);
		    tempNode.put("userId", userId);
		    tempNode.put("userName", userService.FetchUserName(userId));
		    tempNode.put("trailName", trailName);
		    tempNode.put("coverPhotoId", coverPhotoId);			    
		    nodeString = tempNode.toString();
		    tx.success();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.print("issues with fetching node and its relations");
			tx.failure();
		} finally {
			tx.finish();
		}		
		return nodeString;
	}

	public String GetStringPropertyFromNode(String crumbId, String property) {
		Node crumb = this.RetrieveNode(Integer.parseInt(crumbId));
		String propertyValue = "";
		// Get Views property if it exists, else return 0.
		Transaction tx = _db.beginTx();
		try {
			Object stringObject = crumb.getProperty(property);
			if (stringObject!=null){
				propertyValue = stringObject.toString();
			}
			tx.success();
			return propertyValue;
		} catch (NotFoundException ex) {
			System.out.println("An Error Occured trying to get a String property from a node. Could not find the property: " + property);
			tx.failure();
		} finally {
			tx.close();
		}
		return "";
	}

	// Find a node using a cypher query. This is usually done where we do not know the id. Should be very sparse using this because it could 
	// be very expensive.
	public Node ExecuteCypherQueryReturnNode(String query) {
		// TODO Auto-generated method stub  
		ExecutionEngine engine = new ExecutionEngine( _db );
		ExecutionResult result = null;
		Transaction tx = _db.beginTx();
		try {
		    result = engine.execute(query);
		    Iterator<Map<String, Object>> it = result.iterator();
			Map nodeItemMap = it.next();
			Collection valuesCollection = nodeItemMap.values();
			Node tempNode = (Node) valuesCollection.toArray()[0];
			tx.success();
            return tempNode;
		} catch (Exception ex) {
			// Need to do some logging here.
			tx.failure();
		} finally {
			tx.finish();
		}
		return null;
	}	
}
