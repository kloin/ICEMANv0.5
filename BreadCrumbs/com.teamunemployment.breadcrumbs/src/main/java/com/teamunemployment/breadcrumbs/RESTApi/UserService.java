package com.teamunemployment.breadcrumbs.RESTApi;

import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.User;

import java.util.ArrayList;

import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * @author Josiah Kendall
 * Retrofit api implementation of the REST user service
 */
public interface UserService {

    @GET("/User/GetTopThreeThreeItems/{UserId}")
    Call<ArrayList<Trip>> GetTopThreeItemsForAUser(@Path("UserId") String nodeId);

    @GET("/User/GetTopThreeUnreadTripIds/{UserId}")
    Call<ResponseBody> GetTopThreeUnreadTripIds(@Path("UserId") String userId);

    @GET("/FetchLocalPlace/{Latitude}/{Longitude}")
    Call<ResponseBody> FetchLocalPlace(@Path("Latitude") double latitude, @Path("Longitude") double longitude);

    @GET("User/GetUser/{UserId}")
    Call<User> GetUser(@Path("UserId") String userId);

    @GET("User/PinTrailForUser/{UserId}/{TrailId}")
    Call<ResponseBody> PinTrailForUser(@Path("UserId") String userId, @Path("TrailId") String trailId);

}
