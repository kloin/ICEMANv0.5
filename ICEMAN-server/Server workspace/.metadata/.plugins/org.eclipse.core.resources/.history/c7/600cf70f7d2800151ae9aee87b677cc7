package com.breadcrumbs.resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.json.JSONObject;

import com.breadcrumbs.models.Crumb;
import com.breadcrumbs.search.Search;

@Path("/Crumb")
public class RESTCrumb {

	@GET
	@Path("GetLatitudeAndLogitudeForCrumb/{CrumbId}")
	public String GetLatitudeAndLongitudeForACrumb(@PathParam("CrumbId") String CrumbId) {		
		Crumb crumb = new Crumb();
		return crumb.GetLatitudeAndLongitude(CrumbId);
	}
	
	@GET
	@Path("GetPropertyFromCrumb/{CrumbId}/{Property}") 
	public String GetPropertyFromCrumb(@PathParam("CrumbId") String CrumbId,
									   @PathParam("Property") String Property) {
		return "";
	}
}
