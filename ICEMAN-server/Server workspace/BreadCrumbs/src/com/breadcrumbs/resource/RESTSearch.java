package com.breadcrumbs.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.json.JSONObject;

import com.breadcrumbs.search.Search;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/Search")
public class RESTSearch {
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String respondAsReady() {
        return "/GeneralSearch/{SearchText}\n" +
                "/TrailSearch/{SearchText}\n" +
                "/UserSearch/{SearchText}\n";
    }
    
	//This is a method that will search a users name, a trail name, and a collection (collections todo)
	@GET
	@Path("GeneralSearch/{SearchText}")
	public String GeneralSearch(@PathParam("SearchText") String SearchText) {
		Search search = new Search();
		String searchResult = search.SearchAllNodesWithLabelByGivenProperty(SearchText);
		return searchResult;
	}
	
	@GET
	@Path("TrailSearch/{SearchText}")
	public String TrailSearch(@PathParam("SearchText") String SearchText) {
		Search search = new Search();
		String searchResult = search.SearchAllTrailsByGivenText(SearchText);
		return searchResult;	
	}
	
	@GET
	@Path("UserSearch/{SearchText}") 
	public String UserSearch(@PathParam("SearchText") String SearchText) {
		Search search = new Search();
		String searchResult = search.SearchAllUsersByGivenText(SearchText);
		return searchResult;
	}
}
