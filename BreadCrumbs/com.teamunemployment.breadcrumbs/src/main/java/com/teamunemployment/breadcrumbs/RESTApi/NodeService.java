package com.teamunemployment.breadcrumbs.RESTApi;

import com.teamunemployment.breadcrumbs.Album.data.Comment;
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

    @GET("login/DeleteNode/{NodeId}")
    Call<ResponseBody> deleteNode(@Path("NodeId") String nodeId);

    @GET("login/GetPropertyFromNode/{NodeId}/{Property}")
    Call<ResponseBody> getStringProperty(@Path("NodeId") String nodeId, @Path("Property") String property);

    @GET("login/SaveStringPropertyToNode/{NodeId}/{Property}/{PropertyValue}")
    Call<ResponseBody> setStringProperty(@Path("NodeId") String nodeId, @Path("Property") String property, @Path("PropertyValue") String propertyValue);

    @GET("TrailManager/GetThreePublishedTrips/{NodeId}")
    Call<ArrayList<Trip>> getTopThreeTripsForAUser(@Path("NodeId") String nodeId);

    @GET("TrailManager/GetTwentyTrips")
    Call<ArrayList<Trip>> getTwentyTrips();

    @GET("TrailManager/GetTwentyTripIds")
    Call<ResponseBody> getTwentyTripIds();

    @GET("TrailManager/GetAllAlbumsFromFollowedUser/{UserId}")
    Call<ArrayList<Trip>> getAllAlbumsFromFollowedUsers(@Path("UserId") String userId);

    @GET("TrailManager/GetTrip/{TripId}")
    Call<Trip> getTrip(@Path("TripId") String tripId);

    @GET("TrailManager/GetFavouritedTripsForAUser/{UserId}/{MaxCount}")
    Call<ResponseBody> getFavouritedTripsForAUser(@Path("UserId") String userId, @Path("MaxCount") String maxCount);

    @GET("TrailManager/GetIdsOfMostPopularTrips/{MaxCount}")
    Call<ResponseBody> getIdsOfMostPopularTrips(@Path("MaxCount") String maxCount);

    @GET("TrailManager/GetDurationOfTrailInDays/{TrailId}")
    Call<ResponseBody> getDurationOfTrailInDays(@Path("TrailId") String trailId);

    @GET("TrailManager/SetCoverPhotoForTrail/{TrailId}/{ImageId}")
    Call<ResponseBody> setCoverPhotoForTrail(@Path("TrailId") String trailId, @Path("ImageId") String imageId);

    @GET("TrailManager/AddTrailView/{TrailId}")
    Call<ResponseBody> addTrailView(@Path("TrailId") String trailId);

    @GET("TrailManager/GetTrailViews/{TrailId}")
    Call<ResponseBody> getTrailViews(@Path("TrailId") String trailId);

    @GET("TrailManager/UserLikesTrail/{UserId}/{TrailId}")
    Call<ResponseBody> addLikeToTrail(@Path("UserId") String userId, @Path("TrailId") String trailId);

    @GET("TrailManager/GetLIkesForTrail/{TrailId}")
    Call<ResponseBody> getNumberOfLikesForATrail(@Path("TrailId") String trailId);

    @GET("TrailManager/GetNumberOfCrumbsForATrail/{TrailId}")
    Call<ResponseBody> getNumberOfCrumbsForATrail(@Path("TrailId") String trailId);

    @GET("TrailManager/GetAllCommentsForAnAlbum/{AlbumId}")
    Call<ArrayList<Comment>> getAllCommentsForAnAlbum(@Path("AlbumId") String albumId);

    @GET("TrailManager/AddCommentToAlbum/{AlbumId}/{UserId}/{CommentText}")
    Call<ResponseBody> addCommentToAlbum(@Path("AlbumId") String albumId, @Path("UserId") String userId, @Path("CommentText") String commentText);

    @GET("TrailManager/GetAllAlbumIdsFromFollowedUsers/{UserId}")
    Call<ResponseBody> getAllAlbumIdsFromFollowedUsers(@Path("UserId") String userId);
}
