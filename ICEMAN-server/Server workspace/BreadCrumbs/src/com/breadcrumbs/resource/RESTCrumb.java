package com.breadcrumbs.resource;
import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.json.JSONObject;

import com.breadcrumbs.database.DBMaster;
import com.breadcrumbs.models.Crumb;
import com.breadcrumbs.search.Search;

import Statics.StaticValues;
import com.breadcrumbs.database.NodeController;
import com.breadcrumbs.models.Trail;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.MultiPart;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.neo4j.graphdb.Node;

@Path("/Crumb")
public class RESTCrumb {
	private DBMaster dbm;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String respondAsReady() {
        return "/GetLatitudeAndLogitudeForCrumb/{CrumbId}\n" +
                "/DeleteCrumb/{CrumbId}\n" +
                "/UploadProfileImage/{UserId}\n" +
                "/UploadProfilePictureForUser/{UserId}\n"+
                "/GetPropertyFromCrumb/{CrumbId}/{Property}\n" +
                "/GetAllImageCrumbIdsForAUser/{id}\n" +
                "/UserLikesCrumb/{CrumbId}/{UserId}\n" +
                "/GetNumberOfLikesForCrumb/{CrumbId}\n" +
                "/SaveImageToDatabase/{FileName}\n";
    }
        /*
            Get the latitude and longitude for a crumb. Pretty self explainatory.
        */
	@GET
	@Path("GetLatitudeAndLogitudeForCrumb/{CrumbId}")
	public String GetLatitudeAndLongitudeForACrumb(@PathParam("CrumbId") String CrumbId) {		
		Crumb crumb = new Crumb();
		return crumb.GetLatitudeAndLongitude(CrumbId);
	}
	
        /*
            Delete a crumb by its Id. It also does a search on the disk for 
            any files related to that crumb (.jpg, .mp4) and deletes them from storage.
        */
	@GET
	@Path("DeleteCrumb/{CrumbId}")
	public String DeleteCrumb(@PathParam("CrumbId") String CrumbId) {
		dbm = DBMaster.GetAnInstanceOfDBMaster();
		String cypherQuery = "MATCH (n) WHERE ID(n) = " + CrumbId + " optional Match(n)-[r]-() DELETE r,n";
		dbm.ExecuteCypherQueryNoReturn(cypherQuery);
		
		// Now delete any media from disk
		File tempFile = new File(StaticValues.serverAddress+CrumbId+".jpg");
		if (tempFile.exists()) {
			tempFile.delete();
		} else {
			tempFile = new File(StaticValues.serverAddress+CrumbId+".mp4");
			if (tempFile.exists()) {
				tempFile.delete();
			}
		}
		
		return "success";
	}
	
        /*
            Upload a porfile image. This is for uploading images that we have not
            currently got stored in the databse - i.e images that are stored locally
            on the users phone ( think instagram etc.).
        */
        @GET
        @Path("UploadProfileImage/{UserId}")
        public String UploadProfileImage(@PathParam("UserId") String userId) {
            Hashtable<String, Object> keysAndItems = new Hashtable<String, Object>();
            keysAndItems.put("UserId", userId);
            keysAndItems.put("public", false);
            
            Trail trailManager = new Trail();
            dbm = DBMaster.GetAnInstanceOfDBMaster();
            int crumbId = dbm.SaveNode(keysAndItems, com.breadcrumbs.database.DBMaster.myLabels.Image);	
            Node crumb = dbm.RetrieveNode(crumbId);
            Node trail = dbm.RetrieveNode(Integer.parseInt(userId));

            dbm.CreateRelationship(crumb, trail, DBMaster.myRelationships.Part_Of);	
            return String.valueOf(crumbId);
        }
        
        @POST
        @Path("UploadProfilePictureForUser/{UserId}")
        public String UploadProfilePictureForAUser(MultiPart data, @PathParam("UserId") String userId) {
            Hashtable<String, Object> keysAndItems = new Hashtable<String, Object>();
            keysAndItems.put("UserId", userId);
            keysAndItems.put("public", false);
            
            Trail trailManager = new Trail();
            dbm = DBMaster.GetAnInstanceOfDBMaster();
            int crumbId = dbm.SaveNode(keysAndItems, com.breadcrumbs.database.DBMaster.myLabels.Image);	
            Node crumb = dbm.RetrieveNode(crumbId);
            Node user = dbm.RetrieveNode(Integer.parseInt(userId));

            Crumb crumbModel = new Crumb();
            dbm.CreateRelationship(crumb, user, DBMaster.myRelationships.Part_Of);
            List<BodyPart> parts = data.getBodyParts();
            BodyPartEntity bpe = (BodyPartEntity) parts.get(0).getEntity();
            InputStream stream = bpe.getInputStream();
            String newNodeIdString = Integer.toString(crumbId);
            crumbModel.ConvertAndSaveImage(stream, newNodeIdString);
            DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
            dbMaster.UpdateNodeWithCypherQuery(userId, "ProfilePicId", Integer.toString(crumbId));
            return newNodeIdString;
        }
        
        /*
            Get a propery from a crumb, using the crumb Id and the property name.
        */
	@GET
	@Path("GetPropertyFromCrumb/{CrumbId}/{Property}") 
	public String GetPropertyFromCrumb(@PathParam("CrumbId") String CrumbId,
                                           @PathParam("Property") String Property) {
		return "";
	}
        
     
        /*
            Get all the crumb ids for a user that have corresponding images.
        */
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Path("GetAllImageCrumbIdsForAUser/{id}")
        public String GetAllCrumbIdsForAUser(@PathParam("id") String id) {
            dbm = DBMaster.GetAnInstanceOfDBMaster();
            System.out.println("Fetching all crumbs ids that are photos, for user with ID: " + id);
            String cypherQuery = "MATCH (n:Crumb) WHERE n.UserId = '"+id+" AND n.Extension = '.jpg' RETURN n";
            return dbm.ExecuteCypherQueryJSONStringReturnJustIds(cypherQuery);
        }
	
	@GET
	@Path("UserLikesCrumb/{CrumbId}/{UserId}")
	public String UserLikesCrumb(@PathParam("CrumbId") String CrumbId, @PathParam("UserId") String UserId) {
		Crumb crumb = new Crumb();
		crumb.UserLikesCrumb(UserId, CrumbId);
		return "";
	}
	
	@GET
	@Path("GetNumberOfLikesForCrumb/{CrumbId}")
	public String GetNumberOfLikesForCrumb(@PathParam("CrumbId") String CrumbId) {
		Crumb crumb = new Crumb();
		return crumb.GetNumberOfLikesForACrumb(CrumbId);
	}
        
        @POST
        @Path("/SaveImageToDatabase/{FileName}")
            public String SaveImage(MultiPart data, @PathParam("FileName") String fileName) {
                // Save an image
                // Now save the multipart data (our image/video).
            List<BodyPart> parts = data.getBodyParts();
            BodyPartEntity bpe = (BodyPartEntity) parts.get(0).getEntity();
            InputStream stream = bpe.getInputStream();
            Crumb crumb = new Crumb();
            crumb.ConvertAndSaveImage(stream, fileName);
        return "200";
        }
        
        
        
}
