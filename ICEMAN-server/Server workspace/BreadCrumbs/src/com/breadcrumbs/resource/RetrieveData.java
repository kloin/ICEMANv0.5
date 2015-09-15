package com.breadcrumbs.resource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Request;

import org.apache.lucene.util.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.Traversal;
import org.omg.CORBA.Any;
import org.omg.CORBA.NameValuePair;
import org.omg.CORBA.NamedValue;

import com.breadcrumbs.database.*;
import com.breadcrumbs.database.*;
import com.breadcrumbs.database.DBMaster.myLabels;
import com.breadcrumbs.database.DBMaster.myRelationships;
import com.breadcrumbs.models.*;
import com.breadcrumbs.retrieval.*;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.impl.MultiPartReaderServerSide;

/*
 * This is the base of the server. This is our rest class that recieves all requests
 * from the app.
 */
@Path("/login")
public class RetrieveData {
	
	private DBMaster dbMaster;
	
	// Basic "is the service running" test
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String respondAsReady() {
        return "Welcome to B C SERVER. ";
    }
        
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/json")
    public String GetMyJSON() {
    	return "{object2: 'object'}";
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getAllCrumbsForATrail/{id}")
    public String GetAllCrumbsForATrail(@PathParam("id") String id) {
    	NodeController nc = new NodeController();
		return nc.GetAllRelatedNodes(id, myLabels.Crumb, "TrailId", "Title");	
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getallCrumbsForAUser/{id}")
    public String GetAllCrumbsForAUser(@PathParam("id") String id) {
    
    	NodeController nc = new NodeController();
		return nc.GetAllRelatedNodes(id, myLabels.Crumb, "user", "Title").toString();	
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getallTrailsForAUser/{id}")
    public String GetAllTrailsForAUser(@PathParam("id") String id) {
    
    	NodeController nc = new NodeController();
    	System.out.println("Working on trails");
		return nc.GetAllRelatedNodes(id, myLabels.Trail, "UserId", "Trails" );
    }
    
    @GET
    @Path("/savecrumb/{chat}/{userId}/{trailId}/{latitude}/{longitude}/{icon}/{extension}")
    public String SaveCrumb (@PathParam("chat")String chat,
    					   @PathParam("userId")String userId,
    					   @PathParam("trailId")String trailId,
    					   @PathParam("latitude")String latitude,
    					   @PathParam("longitude")String longitude,
    					   @PathParam("icon")String icon,
    					   @PathParam("extension") String extension) {
    	Crumb crumb = new Crumb();    	
    	return crumb.AddCrumb(chat, userId, trailId, latitude, longitude, icon, extension); 
    }
        
    @GET
    @Path("/saveTrail/{title}/{description}/{userId}")
    public String SaveTrail (@PathParam("title") String title,
    						@PathParam("description") String description,
    						@PathParam("userId") String userId) {
    	//Construct TrailManager really - not a trail
    	Trail trail = new Trail();
    	System.out.println("Saving trail");
    	return trail.SaveTrail(title, userId, description);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/loadtrailwithcrumbs/{id}") 
    public String LoadTrailWithCrumbs(@PathParam("id") String id) {
    	Trail trail = new Trail();
    	return trail.GetTrailAndCrumbs(id);  	
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/loadcrumb/{id}")
    public String LoadCrumb(@PathParam("id") int id) {
    	NodeController nodeController = new NodeController();
    	return nodeController.FetchNodeJson(id).toString();
    }    
   
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/savecrumb/{id}")
	public String uploadFile(String test, @PathParam("id") String id) {   
    	Crumb crumb =new Crumb();
		crumb.ConvertAndSaveImage(test, id);
		return "done"; 
	}
    
    @POST
    @Path("/saveCrumbWithVideo/{crumbId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String create(MultiPart data, @PathParam("crumbId") String crumbId) {
    	List<BodyPart> parts = data.getBodyParts();
        BodyPartEntity bpe = (BodyPartEntity) parts.get(0).getEntity();
        InputStream stream = bpe.getInputStream();
        
        // Save our crumb
        Crumb crumb = new Crumb();
        crumb.ConvertAndSaveVideo(stream, crumbId);        
           
        return "200";
    }
    
    @GET
    @Path("/CreateNewUser/{UserName}/{Pin}/{Age}/{Sex}")
    @Produces(MediaType.APPLICATION_JSON)
    public String CreateNewUser (@PathParam("UserName") String UserName, 
									@PathParam("Pin") String Pin,
									@PathParam("Age") String Age,
									@PathParam("Sex") String Sex) {
    	// Create a node with these fields
    	DBMaster db = DBMaster.GetAnInstanceOfDBMaster();
    	
    	Hashtable<String, Object> keysAndItems = new Hashtable<String, Object>();
    	keysAndItems.put("Username", UserName);
    	keysAndItems.put("Pin", Pin);
    	keysAndItems.put("Age", Age);
    	keysAndItems.put("Sex", Sex);
    	keysAndItems.put("About", "User has not saved details"); // Default on creation, has to be updated later/manually.
    	System.out.println("Saved New User");

    	return Integer.toString(db.SaveNode(keysAndItems, com.breadcrumbs.database.DBMaster.myLabels.User));
    }
    
   /* @GET
    @Path("/DeleteAllData")
    public String Obliterate() {
    	// USE THIS WITH CAUTION - probably will not be kept around after testing because this would be dumb
    	Trail trail = new Trail();
    	trail.Obliterate();
    	return "";
    }*/
    
    @GET
    @Path("/GetUser/{userId}")
    public String GetUser (@PathParam("UserId") int userId) throws JSONException {
    	NodeController nc = new NodeController();
    	return nc.FetchNodeJson(userId).toString();
    }
    
    /*
     * NOTE THAT THIS IS NOT USED YET BUT PRIOBABL:Y WILL BE IN THE FUTURE
     * Creating  a trail.... 
     * 
     * This is a bit more complex than normal because we want to invite users when we create the trail.
     * Create trail and then go back to invite users in a new workflow == BAD.
     * SO this means we need to send through a list, or call two different URLS (rest methods) from the
     * client. Then, save the new trial and link the users to the trail (e.g invited to/subscribed to).
     * This will require a couple of different methods at a layer between this and the DB Master class.
     */
    @GET
    @Path("/CreateNewTrail/{TrailName}/{UserIds}")
    public void CreateNewTrail (@PathParam("TrailName") String TrailName, 
									@PathParam("UserIds") String UserIds) {
    	//TrailManager tm = new TrailManager();
    	//tm.SaveTrailAndTrailers(TrailName, UserIds);
    }  
    
	/*
	 * Record a comment for a crumb or trail
	 */
	@GET
	@Path("/SaveComment/{UserId}/{EntityId}/{CommentText}")
	public String SaveCommentForAnEntity(@PathParam("UserId") String UserId,
										 @PathParam("EntityId") String EntityId,
										 @PathParam("CommentText") String CommentText) {
		
		Trail trail = new Trail();
		return trail.SaveCommentForAnEntity(UserId, EntityId, CommentText);		
	}
	
	@GET
	@Path("/LoadCommentsForEvent/{EventId}") 
	public String LoadCommentsForEvent(@PathParam("EventId") String EventId) {
		NodeController nc = new NodeController();
		return nc.FetchNodeAndItsRelations(Integer.parseInt(EventId), "Linked_To");
	}
    
    @GET
    @Path("/SaveTrailPoint/{latitude}/{longitude}/{lastPointId}/{trailId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String SaveTrailPoint(@PathParam("latitude") String latitude,
    							@PathParam("longitude") String longitude,
    							@PathParam("lastPointId") String lastPointId,
    							@PathParam("trailId") String trailId) {
    	
    	//Save our point to our trail.
    	Trail trail = new Trail();
    	System.out.println("TrailId is : " + trailId);
    	String newPointId = trail.SavePoint(latitude, longitude, lastPointId, trailId);
    	return newPointId;
    }
    
   @POST
   @Path("/DeleteEntityAndAllRelationships/{EntityId}")
   public String DeleteEntityAndAllRelationships(@PathParam("EntityId") String EntityId) {
	   //ToDo
	   
	   return "";
   }
   
   /*
    * Purpose of this class is to get all the saved trails for a user.
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/GetSpecifiedTrails/{trailString}")
   public String ReturnAllSpecifiedTrails(@PathParam("trailString") String trailString) {
   	// Each trailId is split by a "T"
	   Trail trail = new Trail();
	   return trail.FetchTrailsForGivenIds(trailString);
   }
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/GetAllTrails")
   public String GetAllTrails() {
	   Trail trail = new Trail();
	   return trail.GetAllTrails();
	   
   }
   
   @GET
   @Path("/GetPropertyFromNode/{NodeId}/{Property}")
   public String GetPropertyFromNode(@PathParam("NodeId") String nodeId, @PathParam("Property") String property) {
	   DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
	   return dbMaster.GetStringPropertyFromNode(nodeId, property).toString();
   }
   
   @GET
   @Path("/SaveStringPropertyToNode/{NodeId}/{Property}/{PropertyValue}")
   public String SaveStringPropertyToNode(@PathParam("NodeId") String nodeId, @PathParam("Property") String property, @PathParam("PropertyValue") String propertyValue) {
	   DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
	   Node node = dbMaster.RetrieveNode(Integer.parseInt(nodeId));
	   dbMaster.updateNode(node, property, propertyValue);
	   return "200 - ok";
   }
    
   
 /*  @GET
   @Path("/GetPropertiesForNode/{NodeId}/{Properties}")
   public String GetPropertiesForANode(@PathParam("NodeId") String nodeId,
									   @PathParam("Properties") String properties) {
	   JSONObject jsonResponse = new JSONObject();
	   String[] propertiesArray = properties.split(",");
	   DBMaster dbm = DBMaster.GetAnInstanceOfDBMaster();
	   for (int index =0; index < propertiesArray.length-1; index += 1) {
		   
	   }
   }*/
    
 
}