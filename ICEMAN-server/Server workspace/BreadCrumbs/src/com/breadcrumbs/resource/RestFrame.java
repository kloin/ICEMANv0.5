/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.breadcrumbs.resource;

import com.breadcrumbs.models.Crumb;
import com.breadcrumbs.models.Trail;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * 
 * @author jek40
 */
@Path("/Frame")
public class RestFrame {
    
    /**
     * Fetch a frame (also known as a crumb) from the server to display.
     * @param frameId
     * @return 
     */
    @GET
    @Path("/FrameDetails/{FrameId}")
    public String FetchFrameDetails(@PathParam("FrameId") String frameId) {
        Crumb crumb = new Crumb();
        return crumb.GetFrame(frameId);
    }
    
    @GET
    @Path("/LoadFrameMimesForAlbum/{AlbumId}")
    public String FetchFrameMimesForAnAlbum(@PathParam("AlbumId") String albumId) {
        Trail trailManager = new Trail();
        return trailManager.LoadFrameMimesForAnAlbum(albumId); 
    }
}
