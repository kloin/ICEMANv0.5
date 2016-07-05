package com.teamunemployment.breadcrumbs.RESTApi;

import com.teamunemployment.breadcrumbs.Trails.Trip;

import java.util.ArrayList;

import okhttp3.ResponseBody;
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
    Call<ResponseBody> getStringProperty(@Path("NodeId") String nodeId, @Path("Property") String property);

    @GET("login/SaveStringPropertyToNode/{NodeId}/{Property}/{PropertyValue}")
    Call<ResponseBody> setStringProperty(@Path("NodeId") String nodeId, @Path("Property") String property, @Path("PropertyValue") String propertyValue);

    @GET("TrailManager/GetThreePublishedTrips/{NodeId}")
    Call<ArrayList<Trip>> getTopThreeTripsForAUser(@Path("NodeId") String nodeId);
}
