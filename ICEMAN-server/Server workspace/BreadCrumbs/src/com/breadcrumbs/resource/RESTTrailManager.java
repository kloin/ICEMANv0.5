package com.breadcrumbs.resource;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.json.JSONObject;

import com.breadcrumbs.database.DBMaster;
import com.breadcrumbs.heavylifting.TrailManager20;
import com.breadcrumbs.heavylifting.TripManager;
import com.breadcrumbs.models.Polyline2;
import com.breadcrumbs.models.Trail;
import com.breadcrumbs.models.TrailMetadata;
import com.breadcrumbs.models.UserService;
import java.util.ArrayList;
import javax.ws.rs.Produces;

@Path("/TrailManager")
public class RESTTrailManager {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String respondAsReady() {
            return "/SaveTrailPoints/{(Consumes JSON)}\n" +
                "/GetDurationOfTrailInDays/{TrailId}\n" +
                "/GetThreePublishedTrips/{UserId}\n" +
                    "/GetAllTrailsForAUser/{UserId}\n" +
                    "/GetAllTrailPoints/{TrailId}\n" +
                    "/SetCoverPhotoForTrail/{TrailId}/{ImageId}\n" +
                    "/AddTrailView/{TrailId}\n" +
                    "/GetTrailViews/{TrailId}\n" +
                    "/UserLikesTrail/{UserId}/{TrailId}\n" +
                    "/GetLikesForTrail/{TrailId}\n" +
                    "/GetNumberOfCrumbsForATrail/{TrailId}\n" +
                    "/GetNumberOfPhotosInATrail/{TrailId}\n" +
                    "/GetAllPhotoIdsForATrail/{TrailId}\n" +
                    "/GetNumberOfTrailsAUserOwns/{UserId}\n" +
                    "/GetAllSavedCrumbIdsForATrail/{TrailId}\n" +
                    "/GetAllTrailIds\n" +
                    "/GetBaseDetailsForATrail/{TrailId}\n" +
                    "/GetDisplayVariablesForATrail/{TrailId}\n" +
                    "/GetNumberOfVideosInATrail/{TrailId}\n" +
                    "/DeleteAllTrails\n" +
                    "/FollowTrail/{TrailId}/{UserId}\n" +
                    "/GetNumberOfFollowersForATrail/{TrailId}\n" +
                    "/SaveMetaData/{trailId}\n" +
                    "/ProcessMetadataWithNoSave\n" +
                    "/SaveMetadataAndReturnIt/{TrailId}\n" +
                    "/FetchMetadata/{TrailId}\n" +
                    "/SaveRestZones/{zones}/{trailId}\n" +
                    "/SavePath/{TrailId}\n" +
                    "/CalculatePath/{TrailId}/(consumes JSON)\n" +
                    "/GetSavedPath/{TrailId}\n" +
                    "/GetMapDetails/{TripId}\n" +
                    "/GetAllTripIdsForAUser/{UserId}" +
                    "/GetTrip/{TripId}" +
                    "/GetFavouritedTripsForAUser/{UserId}/{MaxCount}" +
                    "/GetIdsOfMostPopularTrips/{MaxCount}";
        }
        
        @GET
        @Path("/GetTwentyTripIds") 
        public String GetTwentyTripIds() {
            Trail trail = new Trail();
            return trail.GetIdsOfTwentyMostPopularTrips();
        }
        
        @GET
        @Path("/GetIdsOfMostPopularTrips/{MaxCount}")
        public String GetIdsOfMostPopularTrips(@PathParam("MaxCount") String max) {
            Trail trail = new Trail();
            return trail.GetIdsOfPopularTrips(max);
        }
        
        @GET
        @Path("/GetFavouritedTripsForAUser/{UserId}/{MaxCount}")
        public String GetFavouritedTripsForAUser(@PathParam("UserId") String userId, @PathParam("MaxCount") String count) {
            Trail trail = new Trail();
            return trail.GetPinnedTripsForAUser(userId, count);
        }
        
        @GET
	@Path("/GetTrip/{TripId}")
        public String GetTrip(@PathParam("TripId") String tripId) {
            Trail trail = new Trail();
            return trail.GetTrip(tripId);
        }
        /*
            Get the number of days this trail took.
        */
	@GET
	@Path("/GetDurationOfTrailInDays/{TrailId}")
	public String GetDurationOfTrailInDays(@PathParam("TrailId") String TrailId) {
            DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
            String startDate = dbMaster.GetStringPropertyFromNode(TrailId, "StartDate").toString();

            Date date = new Date();
            SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
            String currentDate = dt1.format(date);

            LocalDate currentLocalDate = new LocalDate(currentDate);
            LocalDate startLocalDate = new LocalDate(startDate);
            int days = Days.daysBetween(startLocalDate, currentLocalDate).getDays();
            return Integer.toString(days);
	}
        
        @GET
        @Path("/GetThreePublishedTrips/{UserId}")
        public String GetThreePublishedTrips(@PathParam("UserId") String userId) {
            Trail trail = new Trail();
            return trail.GetTopThreeTripsForAUser(userId);
        }
	
        /*
            Fetch a list of all the trails for a user. Pretty sure this returns more than ids.
        */
	@GET
	@Path("/GetAllTrailsForAUser/{UserId}")
	public String GetAllTrailsForAUser(@PathParam("UserId") String UserId) {
		Trail trail = new Trail();
		return trail.GetAllTrailsForAUser(Integer.parseInt(UserId));
	}
	
	@GET
	@Path("/GetAllTrailPoints/{TrailId}")
	public String GetAllTrailPoints(@PathParam("TrailId") String TrailId) {
            Trail trail = new Trail();
            return trail.GetAllTrailPointsForATrail(TrailId);
	}
	
	// Returns path to the trail we want to load.
	/**
	 * @param TrailId The trail we want to give a cover photo
	 * @param ImageId The Image we are going to display as a cover photo. 
	 * 		  The image a user selects to display as a cover photo must be part of their trail,
	 * 		  So it will always exist. This means we can just save the reference to the image 
	 * 		  and find it that way.
	 * @return
	 */
	@GET
	@Path("/SetCoverPhotoForTrail/{TrailId}/{ImageId}")
	public String SetCoverPhotoForTrailByGivingItThePhotoId(@PathParam("TrailId") String TrailId,
                                                                @PathParam("ImageId") String ImageId) {
		Trail trail = new Trail();
		trail.SetCoverPhoto(TrailId, ImageId);
		return "200";
	}
	
	// Add a view to a trail.
	@GET
	@Path("/AddTrailView/{TrailId}") 
	public String AddTrailView(@PathParam("TrailId") String TrailId) {
		Trail trail = new Trail();
		trail.AddViewForATrail(TrailId);
		return "Success";
	}
	
	//Get the number of views for a trail
	@GET
	@Path("/GetTrailViews/{TrailId}")
	public String GetTrailViews(@PathParam("TrailId") String TrailId) {		
		Trail trail = new Trail();
		return trail.GetNumberOfViewsForATrail(TrailId);
	}
	
	@GET
	@Path("/UserLikesTrail/{UserId}/{TrailId}") 
	public String UserLikesTrail(@PathParam("UserId") String UserId,
                                     @PathParam("TrailId") String TrailId) {
		
		Trail trail = new Trail();
		trail.AddLike(UserId, TrailId);
		return "Success";
	}
	
	@GET
	@Path("/GetLikesForTrail/{TrailId}") 
	public String GetNumberOfLikesForATrail(@PathParam("TrailId") String TrailId) {
		Trail trail = new Trail();
		return trail.GetNumberOfLikesForAnEntity(TrailId);
	}
	
	@GET
	@Path("/GetNumberOfCrumbsForATrail/{TrailId}")
	public String GetNumberOfCrumbsForATrail(@PathParam("TrailId") String TrailId) {
		Trail trail = new Trail();
		return trail.GetNumberOfCrumbsForATrail(TrailId);
	}
        
        @GET
        @Path("/GetNumberOfPhotosInATrail/{TrailId}")
        public String GetNumberOfPhotosInATrail(@PathParam("TrailId") String TrailId) {
            Trail trail = new Trail();
            return Integer.toString(trail.GetNumberOfPhotoCrumbsForATrail(TrailId));
        }
        
        @GET
        @Path("/GetAllPhotoIdsForATrail/{TrailId}")
        public String GetAllPhotoIdsForATrail(@PathParam("TrailId") String TrailId) {
            Trail trail = new Trail();
            return trail.GetAllPhotoIdsForATrail(TrailId);
        }
	
	@GET
	@Path("/GetNumberOfTrailsAUserOwns/{UserId}") 
	public String GetNumberOfTrailsAUserOwns(@PathParam("UserId") String UserId) {
		UserService user = new UserService();
		return user.GetNumberOfTrailsAUserOwns(UserId);
	}
	
	@GET
	@Path("/GetAllSavedCrumbIdsForATrail/{TrailId}")
	public String GetAllCrumbIdsForAUser(@PathParam("TrailId") String TrailId) {
		UserService userService = new UserService();
		Trail trailService = new Trail();
		return trailService.GetAllCrumbIdsForATrail(TrailId);
	}
	
	/*
	 * This needs to be all trail ids for a user
	 */
	@GET
	@Path("GetAllTrailIds") 
	public String GetAllTrailIds() {
		Trail trail = new Trail();
		return trail.GetAllTrailIds();
	}
	
	@GET 
	@Path("GetBaseDetailsForATrail/{TrailId}")
	public String GetBaseDetailsForATrail(@PathParam("TrailId") String TrailId) {
		// Fetch each individual detail for the trail
		Trail trail = new Trail();
		return trail.GetSimpleDetailsForATrail(TrailId);
	}
        
        @GET
        @Path("/GetDisplayVariablesForATrail/{TrailId}")
        public String GetDisplayVariablesForATrail(@PathParam("TrailId") String TrailId) {
            Trail trail = new Trail();
            return trail.GetDisplayVariablesForATrail(TrailId);
        }
	
        @GET
        @Path("/GetNumberOfVideosInATrail/{TrailId}")
        public String GetNumberOfVideosInATrail(@PathParam("TrailId") String TrailId) {
            Trail trail = new Trail();
            return trail.GetNumberOfVideosInATrail(TrailId);
        }
        
	@GET
	@Path("DeleteAllTrails") 
	public void DeleteAllTrails() {
		//USE WITH CAUTION _ THIS SHOULD BE REMOVED BEFORE A PROPER RELEASE
	}
        
    @GET
    @Path("FollowTrail/{TrailId}/{UserId}")
    public void FollowTrail(@PathParam("TrailId") String trailId, @PathParam("UserId") String userId) {
        Trail trail = new Trail();
        trail.AddFollowerForTrail(trailId, userId);
    }
    
    @GET
    @Path("/GetNumberOfFollowersForATrail/{TrailId}")
    public String GetNumberOfFollowersForATrail(@PathParam("TrailId") String trailId) {
         Trail trail = new Trail();
         return trail.GetNumberOfFollowersForATrail(trailId);
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/SaveMetaData/{trailId}")
    public String SaveMetadataForTrail(String metadataJSON, @PathParam("trailId") String trailId) {
    	// I think every time that we need the metadata, we will be fetching all of it, so I may just save the file to disk and when it is requested
    	// I can retrieve that file, and respond with the string.
    	TrailManager20 trailManager = new TrailManager20();
        JSONObject jsonObject = new JSONObject(metadataJSON);
        int startingIndex = jsonObject.getInt("StartingIndex");
    	TrailMetadata metadata = trailManager.ProcessMetadata(jsonObject.getJSONObject("Events"), startingIndex, trailId);
        trailManager.SaveMetadata(metadata, Integer.parseInt(trailId));
     	return "200";
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/ProcessMetadataWithNoSave")
    public String ProcessMetadataWithNoSave(String metadataJSON) {
        TrailManager20 trailManager20 = new TrailManager20();
        JSONObject metaJSONObject = new JSONObject(metadataJSON);
        int startingIndex = metaJSONObject.getInt("StartingIndex");
        TrailMetadata metaData = trailManager20.ProcessMetadata(metaJSONObject, startingIndex, "-1"); // -1 here is a hack. This needs to be fixed
        // Need to implement this method.
        return metaData.toString();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/SaveMetadataAndReturnIt/{TrailId}")
    public String SaveMetadataAndReturnIt(String metadataJSON, @PathParam("TrailId") String trailId) {
        TrailManager20 trailManager = new TrailManager20();
        JSONObject jsonObject = new JSONObject(metadataJSON);
        int startingIndex = jsonObject.getInt("StartingIndex");
    	TrailMetadata metadata = trailManager.ProcessMetadata(jsonObject.getJSONObject("Events"), startingIndex, trailId);
        trailManager.SaveMetadata(metadata, Integer.parseInt(trailId));
        return trailManager.FetchMetadataFromTrail(trailId);
    }
    
    @GET
    @Path("/FetchMetadata/{TrailId}")
    public String FetchMetadata(@PathParam("TrailId") String trailId) {
        TrailManager20 tm = new TrailManager20();
        return tm.FetchMetadataFromTrail(trailId);
    }
    
    @GET
    @Path("/SaveRestZones/{zones}/{trailId}")
    public String SaveRestZonesForTrail(@PathParam("zones") String zones, @PathParam("trailId") String trailId) {
    	return "200";
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/SavePath/{TrailId}")
    public String SavePath( String pathData,
                            @PathParam("TrailId") String trailId) {
        
        TripManager tripManager = new TripManager();
        tripManager.SavePath(trailId, pathData);
        return "200";
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/CalculatePath/{TrailId}")
    public String CalculatePath(String pathData, @PathParam("TrailId") String trailId) {
        TripManager tripManager = new TripManager();
        JSONObject result = tripManager.CalculatePath(trailId, pathData);
        return result.toString();
    }
    
    @GET
    @Path("/GetSavedPath/{TrailId}")
    public String GetSavedPathForTrail(@PathParam("TrailId") String trailId) {
        TripManager tripManager = new TripManager();
        JSONObject jsonObject = tripManager.FetchPathForTrip(trailId);
        return jsonObject.toString();
    }
    
    @GET
    @Path("/GetMapDetails/{TripId}")
    public String GetMapDetails(@PathParam("TripId") String tripId) {
        Trail trail =  new Trail();
         DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
        String views = trail.GetNumberOfViewsForATrail(tripId);
        String duration = GetDurationOfTrailInDays(tripId);
        String trailName = dbMaster.GetStringPropertyFromNode(tripId, "TrailName").toString();
        return views + ","+duration + ","+trailName;
    }
    
    @GET
    @Path("/GetAllTripIdsForAUser/{UserId}")
    public String GetAllTripIdsForAUser(@PathParam("UserId") String userId) {
        Trail trail = new Trail();
        return trail.FindAllPinnedTrailsForAUser(userId);
    }
    
    
    
}
