package com.teamunemployment.breadcrumbs.RESTApi;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * @author Josiah Kendall
 * Retrofilt implemetation of a bunch of dfferent parts of our api that allow us to easilly interact
 * with node properties.
 */
public interface NodeService {

    @GET("login/GetPropertyFromNode/{NodeId}/{Property}")
    String getStringProperty(@Path("NodeId") String nodeId, @Path("Property") String property);

    @GET("login/SetStringPropertyToNode/{NodeId}/{Property}/{PropertyValue}")
    Void setStringProperty(@Path("NodeId") String nodeId, @Path("Property") String property, @Path("PropertyValue") String propertyValue);


}
