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

@Path("/Crumb")
public class RESTCrumb {
	private DBMaster dbm;

	@GET
	@Path("GetLatitudeAndLogitudeForCrumb/{CrumbId}")
	public String GetLatitudeAndLongitudeForACrumb(@PathParam("CrumbId") String CrumbId) {		
		Crumb crumb = new Crumb();
		return crumb.GetLatitudeAndLongitude(CrumbId);
	}
	
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
	
	@GET
	@Path("GetPropertyFromCrumb/{CrumbId}/{Property}") 
	public String GetPropertyFromCrumb(@PathParam("CrumbId") String CrumbId,
									   @PathParam("Property") String Property) {
		return "";
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
}
