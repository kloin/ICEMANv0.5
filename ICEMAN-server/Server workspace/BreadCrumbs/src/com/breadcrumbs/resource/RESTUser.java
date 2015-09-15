package com.breadcrumbs.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.breadcrumbs.models.Trail;
import com.breadcrumbs.models.UserService;

@Path("/User")
public class RESTUser {
	
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
		return "Success";
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
	@Path("PinUserForUser/{UserIdA}/{UserIdB}")
	public String PinUserForUser(@PathParam("UserIdA") String UserIdA,
			@PathParam("UserIdB") String UserIdB) {
		UserService user = new UserService();
		user.PinUserForUser(UserIdA, UserIdB);
		return "Success";
		
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
	public String AddFriend(@PathParam("UserId") String UserId,
									@PathParam("UserId2") String UserId2) {
		UserService userService = new UserService();
		return userService.AddContactForAUser(UserId, UserId2);
	}
	
	@GET
	@Path("GetAllSavedCrumbIdsForAUser/{UserId}")
	public String GetAllCrumbIdsForAUser(@PathParam("UserId") String UserId) {
		UserService userService = new UserService();
		return userService.GetAllCrumbIdsForAUser(UserId);
	}
	
	
}