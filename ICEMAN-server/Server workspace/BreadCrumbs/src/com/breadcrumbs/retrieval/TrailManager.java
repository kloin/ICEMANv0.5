//package com.breadcrumbs.retrieval;
//
//import java.awt.Image;
//import java.awt.image.BufferedImage;
//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.IOException;
//import java.lang.reflect.Array;
//import java.net.Socket;
//import java.util.Arrays;
//import java.util.Hashtable;
//import java.util.Iterator;
//import java.util.List;
//
//import javax.imageio.ImageIO;
//import javax.servlet.ServletOutputStream;
//import javax.servlet.http.HttpServletRequest;
//
//import org.apache.catalina.util.Base64;
//import org.neo4j.graphdb.GraphDatabaseService;
//import org.neo4j.graphdb.Node;
//import org.neo4j.graphdb.NotFoundException;
//import org.neo4j.graphdb.PropertyContainer;
//import org.neo4j.graphdb.ResourceIterable;
//import org.neo4j.graphdb.Transaction;
//import org.neo4j.kernel.api.exceptions.TransactionalException;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import com.breadcrumbs.database.DBMaster;
//import com.breadcrumbs.database.DBMaster.myRelationships;
//import com.breadcrumbs.database.NodeController;
//import com.breadcrumbs.models.NodeConverter;
//import com.breadcrumbs.models.Trail;
//import com.breadcrumbs.test.CrumbTests.myLabels;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//
//public class TrailManager extends IDataManager {
//
//	private Trail trail;
//	private DBMaster dbMaster;
//	private JSONObject jsonResponse;
//	HttpServletRequest request;
//	
//	public TrailManager(HttpServletRequest request) {
//		
//		this.request = request;
//		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
//		jsonResponse = new JSONObject();
//	}
//	public TrailManager() {
//		dbMaster = DBMaster.GetAnInstancaeOfDBMaster();
//		jsonResponse = new JSONObject();
//	}
//	/*
//	 * Hahah trailers is good man.
//	 */
//	public void SaveTrailAndTrailers(String trailName, String idArray) {
//		List<String> userIdList = Arrays.asList(idArray.split(","));
//		
//		// Get the fields that we need to save to the trail set up in the hashtable.
//		Hashtable<String, Object> nodeFields = new Hashtable<String, Object>();
//		nodeFields.put("TrailName", trailName);
//		
//		//Save our trail
//		NodeController nc = new NodeController();
//		String trailId = nc.SaveNode(nodeFields, com.breadcrumbs.database.DBMaster.myLabels.Trail);
//
//		// If we get green light from trail,start linking.
//		if (trailId !=null) {
//			Node trail = nc.FetchNode(Integer.parseInt(trailId));
//			// Get an iterator cos they are clean to work with.
//			Iterator<String> iterator = userIdList.iterator();
//			while (iterator.hasNext()) {
//				// Get linking
//				String userId = iterator.next();
//				Node user = nc.FetchNode(Integer.parseInt(userId));				
//				trail.createRelationshipTo(user, myRelationships.Linked_To);				
//			}
//		}
//		
//		
//	}
//	
//	/*
//	 * PRE REFACTOR BELOW THIS LINE
//	 * ============================================================================================
//	 */
//	/* requestString: 
//	 * 	- 'x1x' : get crumb
//	 * ------------------------
//	 * ------------------------
//	 *  - 'x0x' : get trial
//	 * 
//	 * @see Retrieval.IDataManager#GetData()
//	 */
//	public JSONObject GetData() {
//		String requestString = request.getParameter("requestString");
//		System.out.println("Fetching data in TrailManager()");
//		//1 is a reqest for a crumb (e.g 710).. Sorry about the nested I was high
//		if (requestString.charAt(1) == '1') {
//			if(requestString.charAt(2) == '0') {
//				System.out.println("Crumb requested");
//			}			
//		}
//		
//		else if(requestString.charAt(1) == '9') {
//			//get all trails for a user.
//			if (requestString.charAt(2) == '1') {
//				getAllTrailsForUser();
//			}
//			else if (requestString.charAt(2) == '2') {
//				GetAllCrumbsForUser();
//			}
//			else if (requestString.charAt(2)== '0') {
//				System.out.println("Photo Requested");
//				FetchImage(Integer.parseInt(request.getParameter("crumbId")));
//			}
//		}
//		
//		//0 Is a request for a trail
//		else if (requestString.charAt(1) == '9') {
//			System.out.println("Trail requested");
//			GetTrail(0);
//		}
//		System.out.println("returning this json data :" + jsonResponse);
//		return jsonResponse;
//	}
//	
//	/*
//	 * Retrieves a node using the given Id, and with the nodes data it
//	 * creates a Trail.
//	 * 
//	 * PI: How big can a trail be? For massive trails, we may have to grab only 
//	 * a few crumbs, e.g the top 50.
//	 */
//
//	public String GetTrail(int id) {
//		Transaction tx = dbMaster.GetDatabaseInstance().beginTx();
//		NodeConverter nodeConverter = new NodeConverter();
//		//Debug log
//		String Title = "";
//		String Description = "";
//		String TrailName = "";
//		String UserId = "";
//		System.out.println("Constructing JSON for trail");
//		try {
//		//Fetch our trail node
//			Node trail = dbMaster.RetrieveNode(id);
//			return nodeConverter.ConvertSingleNodeToJSON(trail).toString();
//		} catch (NotFoundException ex)	{
//			ex.printStackTrace();
//			tx.failure();
//			return "{}";
//		} finally {
//			tx.finish();
//		}
//	}
//	
//	
//	
//	/*
//	 * Return all the trails for a certain user.
//	 * 
//	 * PI: This could cause issues with users with a lots of related trails.
//	 * It should return an appropriate amount of trails at one time (think continuous 
//	 * scrolling). The reason for this is that trails will be pretty large, so we don't 
//	 * want to be fetching 30 trails, all ~5 MB. The user will be waiting for ever.
//	 */
//	private void getAllTrailsForUser() {
//		ResourceIterable<Node> node = dbMaster.GetDatabaseInstance().findNodesByLabelAndProperty(myLabels.Trail, "userId", 0);
//		Iterator nodeSearcher = node.iterator();
//		System.out.println("Getting all Trails for a user");
//		try {
//			int numberOfNodes = 0;
//			
//			while (nodeSearcher.hasNext()) {
//				//Get the node once, as each time we use "Next()" we move forward on the list
//				Node trail = (Node) nodeSearcher.next();
//				//Temp node to store objects then add it to the string
//				JSONObject temporaryNode = new JSONObject();
//				System.out.println("adding data:");
//				
//				//Now get the deets
//				String TrailName = trail.getProperty("TrailName").toString();	
//				System.out.println("added trailname");
//				String Description = trail.getProperty("Description").toString();
//				System.out.println("added description");
//				String userId = trail.getProperty("userId").toString();
//				System.out.println("Added userId");
//				
//				//the actual neo4j id of the node
//				int node_index_id = (int) trail.getId();
//				
//				System.out.println("Adding crumb Count");
//				//Get all the crumbs attatched to this and add them up.
//				
//				//MATCH (m:Movie {title:"The Matrix"})<-[:ACTS_IN]-(actor)
//				//RETURN count(*);
//				ResourceIterable<Node> crumbs = dbMaster.GetDatabaseInstance().findNodesByLabelAndProperty(myLabels.Crumb, "trail", request.getParameter("userId"));
//				Iterator crumbsIterator = crumbs.iterator();
//				int numberOfCrumbs = 0;
//				while(crumbsIterator.hasNext()) {
//					crumbsIterator.next();
//					numberOfCrumbs += 1;
//				}
//					
//				//Debug
//				System.out.println("Fetched Description: "+ Description + " Title: " + TrailName);
//	
//				//now add the deets
//				temporaryNode.put("TrailName", TrailName);
//				temporaryNode.put("Description", Description);
//				temporaryNode.put("NumberOfCrumbs", numberOfCrumbs);
//				temporaryNode.put("UserId", userId);
//				temporaryNode.put("trailId", node_index_id);
//				//Now add the json object into our main json - we need this because i dont know how to iterate a json String
//				jsonResponse.put("Node"+numberOfNodes, temporaryNode);
//				System.out.println("Added test successfully");
//				//System.out.println("Trails Description: " + ((PropertyContainer) nodeSearcher.next()).getProperty("Description"));
//				JSONObject object2 = (JSONObject) jsonResponse.get("Node"+numberOfNodes);
//				numberOfNodes += 1;
//				} 
//				
//			} catch(JSONException ex) {
//				System.out.println(ex);
//		}
//	}
//
//
//	   private byte[] convertFileToArray(String file)
//	    {
//	    	FileInputStream fileInputStream=null;
//	 
//	 
//	        byte[] bFile = new byte[100000];
//	 
//	        try {
//	            //convert file into array of bytes
//		    fileInputStream = new FileInputStream(file);
//		    fileInputStream.read(bFile);
//		    fileInputStream.close();
//	 
//		    for (int i = 0; i < bFile.length; i++) {
//		       	System.out.print((char)bFile[i]);
//	        }
//	 
//		    System.out.println("Done");
//	        }catch(Exception e){
//	        	e.printStackTrace();
//	        }
//			return bFile;
//	    }
//	
//	   //Just jamming some shit here. Lets hope it works
//	   public void FetchImage(int Id) {
//		  // ServletOutputStream out; 
//		   try {
//		   BufferedOutputStream out;
//		   Socket s = new Socket("localhost", 8080);
//		    out = (BufferedOutputStream) s.getOutputStream();  
//		    FileInputStream fin = new FileInputStream("C:\\Users\\Josiah\\Desktop\\BreadCrumbsMedia\\"+Id+".png");  
//		      
//		    BufferedInputStream bin = new BufferedInputStream(fin);  
//		    BufferedOutputStream bout = new BufferedOutputStream(out);  
//		    int ch =0; ;  
//		    while((ch=bin.read())!=-1) {  
//		    	bout.write(ch);  
//	    	}  
//		      
//		    bin.close();  
//		  
//				fin.close();
//		
//		    bout.close();  
//		    out.close();  
//			} catch (IOException e) {
//			// TODO Auto-generated catch block
//				System.out.println("server has failed to return Image: ");
//				e.printStackTrace();
//			}
//
//	    }  
//	   
//	   
//	public void GetAllCrumbsForUser() {
//
//		System.out.println(myLabels.Crumb);
//		System.out.println(request.getAttribute("trailId"));
//		
//		ResourceIterable<Node> node = dbMaster.GetDatabaseInstance().findNodesByLabelAndProperty(myLabels.Crumb, "trailId", request.getParameter("trailId"));
//		Iterator nodeSearcher = node.iterator();
//		System.out.println("Getting all crumbs for an entity: " + node.toString());
//		try {
//			
//			int numberOfNodes = 0;			
//			while (nodeSearcher.hasNext()) {
//				//Get the node once, as each time we use "Next()" we move forward on the list
//				System.out.println(numberOfNodes);
//				Node trail = (Node) nodeSearcher.next();
//				//Temp node to store objects then add it to the string
//				JSONObject temporaryNode = new JSONObject();
//				
//				//Now get the deets
//				String Title = trail.getProperty("Title").toString();				
//				String Description = trail.getProperty("Description").toString();
//				int crumbId = (int) trail.getId();
//				String Latitude = trail.getProperty("Latitude").toString();
//				String Longitude = trail.getProperty("Longitude").toString();
//				
//				//Debug
//				System.out.println("Fetched Description: "+ Description + " Title: " + Title + "id" + crumbId);
//				
//				//now add the deets
//				temporaryNode.put("Title", Title);
//				temporaryNode.put("Description", Description);
//				temporaryNode.put("crumbId", crumbId);
//				temporaryNode.put("Latitude", Latitude);
//				temporaryNode.put("Longitude", Longitude);
//				
//				//Now add the json object into our main json:
//				jsonResponse.put("Node"+numberOfNodes, temporaryNode);
//				System.out.println("Added test successfully");
//				//System.out.println("Trails Description: " + ((PropertyContainer) nodeSearcher.next()).getProperty("Description"));
//				JSONObject object2 = (JSONObject) jsonResponse.get("Node"+numberOfNodes);
//				numberOfNodes += 1;
//				} 
//				
//			} catch(JSONException ex) {
//				System.out.println(ex);
//		}
//
//	}
//
//	/*
//	 * Convert the crumb we've got to a json String
//	 */
//	
//	
//	
//}
