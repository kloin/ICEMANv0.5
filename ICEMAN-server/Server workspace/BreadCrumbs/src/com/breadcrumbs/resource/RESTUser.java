package com.breadcrumbs.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.json.JSONObject;

import com.breadcrumbs.database.DBMaster;
import com.breadcrumbs.gcm.GcmMessages;
import com.breadcrumbs.models.Trail;
import com.breadcrumbs.models.UserService;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/User")
public class RESTUser {
	
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String respondAsReady() {
            return "/GetAllPinnedTrailsForAUser/{UserId}\n" +
                    "/PinTrailForUser/{UserId}/{TrailId}\n" +
                    "/FindUserByFacebookId/{FacebookUserId}\n" +
                    "/FetchUserDetails/{Email}\n" +
                    "/GetAllPinnedUsersForAUser/{UserId}\n" +
                    "/GetNumberOfFollowersForAUser/{UserId}\n" +
                    "/GetAllHomePageTrailIdsForAUser/{UserId}\n" +
                    "/UnpintrailForUser/{UserId}/{TrailId}\n" +
                    "/UnPinUserForUser/{UserId}/{UserId2}\n" +
                    "/IsUserFollowingOtherUser/{BroadcastUserId}/{VisitorId}\n" +
                    "/DeleteAccount/{UserId}\n" +
                    "/PinUserForUser/{FollowingUser}/{FollowedUser}\n" +
                    "/GetAllEditibleTrailsForAUser/{UserId}\n" +
                    "/GetUserName/{UserId}\n" +
                    "/GetContactsForAUser/{UserId}\n" +
                    "/SendFriendRequest/{UserId}/{UserId2}\n" +
                    "/AddContact/{UserId}/{UserId2}\n" +
                    "/GetAllSavedCrumbIdsForAUser/{UserId}\n" +
                    "/GetAllSavedPhotoIdsForAUser/{UserId}\n" +
                    "/GetUser/{UserId}";
        }
        
        @GET
        @Path("GetUser/{UserId}")
        public String GetUser(@PathParam("UserId") String userId) {
            UserService user = new UserService();
            return user.GetUser(userId);
        }
        
        @GET
        @Path("GetTopThreeUnreadTripIds/{UserId}")
        public String GetTopThreeUnreadTripIds(@PathParam("UserId") String userId) {
            Trail trail = new Trail();
            return trail.FindTopThreeTripIdsForAUser(userId);
        }
        
	@GET
	@Path("GetAllPinnedTrailsForAUser/{UserId}")
	public String GetAllPinnedTrailsForAUser(@PathParam("UserId") String UserId) {
		Trail trail = new Trail();
		return trail.FindAllPinnedTrailsForAUser(UserId);
	}
	
	@GET
	@Path("PinTrailForUser/{UserId}/{TrailId}")
	public String PinTrailForUser(@PathParam("UserId") String UserId,
								@PathParam("TrailId") String TrailId) {
		Trail trail = new Trail();
		trail.PinTrailForUser(UserId, TrailId);
		DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		String ownerId = dbMaster.GetStringPropertyFromNode(TrailId, "UserId");
		GcmMessages messageSender = new GcmMessages();
		messageSender.SendUserNotificationOfTrailBeingFollowed(TrailId, ownerId, UserId);
		return "Success";
	}
	
	@GET
	@Path("FindUserByFacebookId/{FacebookUserId}")
	public String FindUserByFacebookId(@PathParam("FacebookUserId") String FacebookUserId) {
		UserService userService = new UserService();
		return userService.CheckForUserExistenceUsingFacebookId(FacebookUserId);
	}
        
	@GET
	@Path("FetchUserDetails/{Email}")
	public String FetchUserDetails(@PathParam("Email") String Email) {
		UserService userService = new UserService();
		return userService.CheckForDetailsUsingEmailAddress(Email);
	}
        
        @GET
        @Path("/IsUserFollowingOtherUser/{BroadcastUserId}/{VisitorId}")
        public String IsUserFollowingOtherUser(@PathParam("BroadcastUserId") String broadCastUserId, 
                @PathParam("VisitorId") String visitorId) {
            UserService userService = new UserService();
            boolean isFollowing = userService.IsUserAFollowingUserB(visitorId, broadCastUserId);
            return Boolean.toString(isFollowing);
        }
        
	@GET
	@Path("GetAllPinnedUsersForAUser/{UserId}") 
	public String GetAllPinnedUsersForAUser(@PathParam("UserId") String UserId) {

		UserService userService = new UserService();
		return userService.GetAllPinnedUsers(UserId);
	}
	
        @GET
        @Path("GetNumberOfFollowersForAUser/{UserId}")
        public String GetNumberOfFollowersForAUser(@PathParam("UserId") String UserId) {
            UserService userService = new UserService();
            String numberOfFollowers = userService.GetNumberOfUsersThatFollowUs(UserId);
            return numberOfFollowers;
        }
        
	@GET
	@Path("GetAllHomePageTrailIdsForAUser/{UserId}")
	public String GetAllHomePageTrailIdsForAUser(@PathParam("UserId") String UserId) {
		UserService userService = new UserService();
		return userService.GetAllOurPinnedShit(UserId).toString();
	}
	
	@GET
	@Path("UnpintrailForUser/{UserId}/{TrailId}")
	public String UnpintrailForUser(@PathParam("UserId") String UserId,
									@PathParam("TrailId") String TrailId) {
		Trail trail = new Trail();
		trail.UnpintrailForUser(UserId, TrailId);
		return "Success";
	}
	
	@GET
	@Path("UnPinUserForUser/{UserId}/{UserId2}")
	public String UnPinUserForUser(@PathParam("UserId") String UserId, @PathParam("UserId2") String UserId2) {
		UserService user = new UserService();
		user.UnPinUserForAUser(UserId, UserId2);
		return "200";
	}
	
	@GET
	@Path("DeleteAccount/{UserId}")
	public String DeleteAccount(@PathParam("UserId") String UserId) {
		Trail trail = new Trail();
		return trail.DeleteNodeAndRelationship(UserId);	
	}
	
	@GET
	@Path("PinUserForUser/{FollowingUser}/{FollowedUser}")
	public String PinUserForUser(@PathParam("FollowingUser") String UserIdA,
			@PathParam("FollowedUser") String UserIdB) {
		UserService user = new UserService();
		user.PinUserForUser(UserIdA, UserIdB);
		//GcmMessages gcm = new GcmMessages();
		//gcm.SendUserNoficationWhenFollowed(UserIdB, UserIdA);
		return "200";
	}
	
	
	@GET
	@Path("GetAllEditibleTrailsForAUser/{UserId}")
	public String GetAllEditibleTrailsForAUser(@PathParam("UserId") String UserId) {		
		UserService userService = new UserService();
		return userService.GetAllEditibleTrailsForAUser(UserId);
	}
	
	@GET
	@Path("GetUserName/{UserId}")
	public String GetUserName(@PathParam("UserId") String UserId) {
		UserService userService = new UserService();
		return userService.FetchUserName(UserId);
	}	
	
	@GET
	@Path("GetContactsForAUser/{UserId}")
	public String GetAllContactsForAUser(@PathParam("UserId") String UserId) {
		UserService userService = new UserService();
		return userService.GetAllContactsForAUser(UserId);
	}	
	
	@GET
	@Path("SendFriendRequest/{UserId}/{UserId2}") 
	public String SendFriendRequest(@PathParam("UserId") String UserId,
									@PathParam("UserId2") String UserId2) {
		UserService userService = new UserService();
		return userService.SendFriendRequest(UserId, UserId2);
	}
	
	@GET
	@Path("AddContact/{UserId}/{UserId2}") 
	public String AddFriend(@PathParam("UserId") String UserId, @PathParam("UserId2") String UserId2) {
		UserService userService = new UserService();
		return userService.AddContactForAUser(UserId, UserId2);
	}
	
	@GET
	@Path("GetAllSavedCrumbIdsForAUser/{UserId}")
	public String GetAllCrumbIdsForAUser(@PathParam("UserId") String UserId) {
		UserService userService = new UserService();
		return userService.GetAllCrumbIdsForAUser(UserId);
	}
        
        @GET
        @Path("GetAllSavedPhotoIdsForAUser/{UserId}")
        public String GetAllSavedPhotoIdsForAUser(@PathParam("UserId") String userId) {
            UserService userService = new UserService();
            return userService.GetAllPhotoIdsForATrail(userId);
        }
}
