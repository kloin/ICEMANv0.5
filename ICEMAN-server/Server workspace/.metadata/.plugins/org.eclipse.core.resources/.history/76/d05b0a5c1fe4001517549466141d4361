package com.breadcrumbs.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.breadcrumbs.database.DBMaster;
import com.breadcrumbs.models.UserService;

@Path("/UserDetail")
public class UserDetail {
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("GetUserName/{UserId}")
	public String GetUserName(@PathParam("UserId") String userId) {
		UserService userService = new UserService();
		return userService.FetchUserName(userId);
	}
	
	
	@GET
	@Path("SaveUserAbout/{UserId}/{Description}") 
	public String SaveUserAbout(@PathParam("UserId") String userId, @PathParam("Description") String description) {
		UserService userService = new UserService();
		userService.SetUserAbout(userId, description);
		return "Success";
	}
	
	@GET
	@Path("StoreGCMClientInstanceID/{UserId}/{GCMInstanceID}")
	public String SaveGCMClientID(@PathParam("UserId") String userId, @PathParam("GCMInstaceID") String gcmInstanceID) {
		String result = "";
		
		UserService userService = new UserService();
		//result = userService.SetGCMClientInstanceID(userId, gcmInstanceID);
		return result;
	}
}
