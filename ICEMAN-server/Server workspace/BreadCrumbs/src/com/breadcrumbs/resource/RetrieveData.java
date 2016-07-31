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
import com.breadcrumbs.gcm.GcmSender;
import com.breadcrumbs.heavylifting.Compression.SynchronizedCompressionBacklog;
import com.breadcrumbs.models.*;
import com.breadcrumbs.retrieval.*;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.impl.MultiPartReaderServerSide;
import java.util.concurrent.BlockingQueue;

/*
 * This is the base of the server. This is our rest class that recieves all requests
 * from the app.
 */
@Path("/login")
public class RetrieveData {
    private DBMaster dbMaster;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/Index")
    public String Index() {
        return "/Crumb\n" +
                "/Search\n" +
                "/TrailManager\n" +
                "/User\n" +
                "UserDetail\n";
    }
    
    // Basic "is the service running" test
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String respondAsReady() {
        return "/CompressionQueueSize\n"+
                "/getAllCrumbsForATrail/{id}\n"+
                "/getallCrumbIdsForAUser/{id}\n" +
                "/getallCrumbsForAUser/{id}\n" +
                "/getallTrailsForAUser/{id}\n" +
                "/savecrumb/{chat}/{userId}/{trailId}/{latitude}/{longitude}/{icon}/{extension}/{placeId}/{suburb}/{city}/{country}/{timeStamp}\n" +
                "/saveCrumbWithVideo/{crumbId}\n" +
                "/AttemptToLogInUser/{UserName}/{Pin}\n"+
                "/CreateNewUser/{UserName}/{Pin}/{Age}/{Sex}/{GcmId}/{Email}/{FacebookLoginId}\n" +
                "/DeleteNode/{nodeId}\n" +
                "/GetUser/{userId}\n" +
                "/SaveComment/{UserId}/{EntityId}/{CommentText}\n" +
                "/LoadCommentsForEvent/{EventId}\n" +
                "/DeleteEntityAndAllRelationships/{EntityId}\n" +
                "/GetSpecifiedTrails/{trailString}\n" +
                "/GetAllTrails\n" +
                "/GetPropertyFromNode/{NodeId}/{Property}\n" +
                "/SaveStringPropertyToNode/{NodeId}/{Property}/{PropertyValue}\n" +
                "/GetMultipleParametersForNode/{NodeId}/{Paramaters}\n" +
                "/GetPropertiesForNode/{NodeId}/{Properties}\n" +
                "/ComrpessVideo/{NodeId}\n";
    }
    
    @GET
    @Path("/CompressionQueueSize")
    public String GetCompressionQueueSize() {
        BlockingQueue<String> queue = SynchronizedCompressionBacklog.GetInstance().GetQueue();
        return Integer.toString(queue.size());
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
    @Path("getallCrumbIdsForAUser/{id}")
    public String GetAllCrumbIdsForAUser(@PathParam("id") String id) {
    
    	NodeController nc = new NodeController();
    	System.out.println("working");
        return nc.GetAllRelatedNodesIds(id, myLabels.Crumb, "UserId", "Title").toString();	
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getallCrumbsForAUser/{id}")
    public String GetAllCrumbsForAUser(@PathParam("id") String id) {
    
    	NodeController nc = new NodeController();
		return nc.GetAllRelatedNodes(id, myLabels.Crumb, "UserId", "Title").toString();	
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
    @Path("/savecrumb/{chat}/{userId}/{trailId}/{latitude}/{longitude}/{icon}/{extension}/{placeId}/{suburb}/{city}/{country}/{timeStamp}")
    public String SaveCrumb (@PathParam("chat")String chat,
    					   @PathParam("userId")String userId,
    					   @PathParam("trailId")String trailId,
    					   @PathParam("latitude")String latitude,
    					   @PathParam("longitude")String longitude,
    					   @PathParam("icon")String icon,
    					   @PathParam("extension") String extension,
    					   @PathParam("placeId") String placeId,
    					   @PathParam("suburb") String suburb,
    					   @PathParam("city") String city,
    					   @PathParam("country") String country,
    					   @PathParam("timeStamp") String timeStamp) {
    	Crumb crumb = new Crumb();    	
    	return crumb.AddCrumb(chat, userId, trailId, latitude, longitude, icon, extension, placeId, suburb, city, country, timeStamp); 
    }
    
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/savecrumb/{chat}/{posX}/{posY}/{userId}/{trailId}/{latitude}/{longitude}/{icon}/{extension}/{placeId}/{suburb}/{city}/{country}/{timeStamp}")
    public String SaveCrumbWithFloatingDescription (
            MultiPart data,
            @PathParam("chat")String chat,
            @PathParam("posX") String posX,
            @PathParam("posY") String posY,
            @PathParam("userId")String userId,
            @PathParam("trailId")String trailId,
            @PathParam("latitude")String latitude,
            @PathParam("longitude")String longitude,
            @PathParam("icon")String icon,
            @PathParam("extension") String extension,
            @PathParam("placeId") String placeId,
            @PathParam("suburb") String suburb,
            @PathParam("city") String city,
            @PathParam("country") String country,
            @PathParam("timeStamp") String timeStamp) {
    	Crumb crumb = new Crumb();    	
    	String id = crumb.AddCrumbWithFloatingDescription(chat, posX, posY, userId, trailId, latitude, longitude, icon, extension, placeId, suburb, city, country, timeStamp); 
        
        // Now save the multipart data (our image/video).
        List<BodyPart> parts = data.getBodyParts();
        BodyPartEntity bpe = (BodyPartEntity) parts.get(0).getEntity();
        InputStream stream = bpe.getInputStream();
        
        int savingMediaResult = crumb.saveMedia(stream, extension, id, trailId);
        if (savingMediaResult == 0) {
            return id; 
        } else {
            // Delete crumb metadata that we have saved.
            DeleteNode(id);
            return "Error";
        }
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
    
    /*
     * This is just a method to use for the moment. When we get more trails 
       we will need to start limiting it to 20 or so.
     */
    @GET
    @Path("/GetAllHomeCardDetails")
    public String GetAllHomeCardDetails() {
    	Trail trail = new Trail();
    	return trail.GetAllHomeCardDetails();
    }
    
    @GET
    @Path("GetAllCrumbCardDetailsForATrail/{TrailId}")
    public String GetAllCrumbCardDetailsForATrail(@PathParam("TrailId") String TrailId) {
        Trail trail = new Trail();
        return trail.GetAllCrumbCardDetailsForATrail(TrailId);
    }
   
    @POST
    @Path("/savecrumb/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String uploadFile(MultiPart data, @PathParam("id") String id) {   
    Crumb crumb =new Crumb();
    List<BodyPart> parts = data.getBodyParts();
        BodyPartEntity bpe = (BodyPartEntity) parts.get(0).getEntity();
        InputStream stream = bpe.getInputStream();
            crumb.ConvertAndSaveImage(stream, id);
            return "done"; 
    }
    
    /**
     * Save an image object. We need to pass all the parameters, as well as the 
     * image/video object as a multipartForm. We save the crumb, then the image to
     * disk. If the crumb data fails to save, we delete the metadata we saved for it.
     * 
     * @param data The image or video for this crumb
     * @param chat The description of said media
     * @param userId the user who is saving the crumb
     * @param trailId the trail to save against
     * @param latitude the latitude of the crumb.
     * @param longitude The longitude of the crumb.
     * @param icon The icon to place on the map when viewing. Not used at the moment.
     * @param extension The mime type. Can be .jpg or .mp4
     * @param placeId The place Id that we fetch from the google apis.
     * @param suburb The suburb of the crumbs locality
     * @param city The city of the crumbs locality
     * @param country The country of the crumbs locality
     * @param timeStamp The time the crumb was created.
    */
    @POST
    @Path("/savecrumb/{chat}/{userId}/{trailId}/{latitude}/{longitude}/{icon}/{extension}/{placeId}/{suburb}/{city}/{country}/{timeStamp}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String UploadCrumb(MultiPart data, 
            @PathParam("chat")String chat,
            @PathParam("userId")String userId,
            @PathParam("trailId")String trailId,
            @PathParam("latitude")String latitude,
            @PathParam("longitude")String longitude,
            @PathParam("icon")String icon,
            @PathParam("extension") String extension,
            @PathParam("placeId") String placeId,
            @PathParam("suburb") String suburb,
            @PathParam("city") String city,
            @PathParam("country") String country,
            @PathParam("timeStamp") String timeStamp) {   
        
        Crumb crumb =new Crumb();
        
        // Save the crumb metadata.
        String id = crumb.AddCrumb(chat, userId, trailId, latitude, longitude, icon, extension, placeId, suburb, city, country, timeStamp);
        
        // Now save the multipart data (our image/video).
        List<BodyPart> parts = data.getBodyParts();
        BodyPartEntity bpe = (BodyPartEntity) parts.get(0).getEntity();
        InputStream stream = bpe.getInputStream();
        
        int savingMediaResult = crumb.saveMedia(stream, extension, id, trailId);
        if (savingMediaResult == 0) {
            return id; 
        } else {
            // Delete crumb metadata that we have saved.
            DeleteNode(id);
            return "Error";
        }
        
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
    @Path("/AttemptToLogInUser/{UserName}/{Pin}")
    public String AttemptToLogInUser(@PathParam("UserName") String UserName, @PathParam("Pin") String pin) {
    	UserService userService = new UserService();
    	return userService.AttemptToLogInUser(UserName, pin);
    }
    
    @GET
    @Path("/CreateNewUser/{UserName}/{Pin}/{Age}/{Sex}/{GcmId}/{Email}/{FacebookLoginId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String CreateNewUser (@PathParam("UserName") String UserName, 
									@PathParam("Pin") String Pin,
									@PathParam("Age") String Age,
									@PathParam("Sex") String Sex,
									@PathParam("GcmId") String GcmId,
									@PathParam("Email") String Email,
									@PathParam("FacebookLoginId") String FacebookLoginId) {
    	// Create a node with these fields
    	DBMaster db = DBMaster.GetAnInstanceOfDBMaster();
    	Hashtable<String, Object> keysAndItems = new Hashtable<String, Object>();
    	keysAndItems.put("Username", UserName);
    	keysAndItems.put("Pin", Pin);
    	keysAndItems.put("Age", Age);
    	keysAndItems.put("Sex", Sex);
    	keysAndItems.put("About", ""); // Default on creation, has to be updated later/manually.
    	keysAndItems.put("Nationality", "Kiwi");
    	keysAndItems.put("ProfilePicId", "0");
    	keysAndItems.put("GcmId", GcmId);
    	keysAndItems.put("Email", Email);
    	keysAndItems.put("FacebookLoginId", FacebookLoginId);
    	keysAndItems.put("ActiveTrail", "0");
    	System.out.println("Saved New User");
    	return Integer.toString(db.SaveNode(keysAndItems, com.breadcrumbs.database.DBMaster.myLabels.User));
    }
    // USE THIS BRO
//    @GET
//    @Path("/DeleteAllData")
//    public String Obliterate() {
//    	// USE THIS WITH CAUTION - probably will not be kept around after testing because this would be dumb
//    	Trail trail = new Trail();
//    	trail.Obliterate();
//    	return "go fuck your self";
//    }
    
    @GET
    @Path("/DeleteNode/{nodeId}")
    public String DeleteNode(@PathParam("nodeId") String nodeId) {
    	Trail trail = new Trail();
    	return trail.DeleteNodeAndRelationship(nodeId);
    }
    
    @GET
    @Path("/GetUser/{userId}")
    public String GetUser (@PathParam("UserId") int userId) throws JSONException {
    	NodeController nc = new NodeController();
    	return nc.FetchNodeJson(userId).toString();
    }
    
    /*
     * NOTE THAT THIS IS NOT USED YET BUT PRIOBABLLY WILL BE IN THE FUTURE
     * Creating  a trail.... 
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
	   dbMaster.UpdateNodeWithCypherQuery(nodeId, property, propertyValue);
	   return "200 - ok";
   }
    
   @GET
   @Path("GetMultipleParametersForNode/{NodeId}/{Paramaters}")
   public String GetMultipleParametersForNode(@PathParam("NodeId") String NodeId, @PathParam("Parameters") String Parameters) {
	   NodeManager nodeManager = new NodeManager();
	   String[] params = Parameters.split(",");
	   return nodeManager.GetMutipleParametersFromANode(NodeId, params);
   }
   
   @GET
   @Path("/GetPropertiesForNode/{NodeId}/{Properties}")
   public String GetPropertiesForANode(@PathParam("NodeId") String nodeId,
									   @PathParam("Properties") String properties) {
	   
	   DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
	   return "";
   }
    
 
   @GET
   @Path("/ComrpessVideo/{NodeId}") 
   public String CompressVideo(@PathParam("NodeId") String nodeId) {
       Crumb crumb = new Crumb();
       crumb.compressVideo(nodeId);
       return "Added video. Compression queue now : " + GetCompressionQueueSize();
   }
}
