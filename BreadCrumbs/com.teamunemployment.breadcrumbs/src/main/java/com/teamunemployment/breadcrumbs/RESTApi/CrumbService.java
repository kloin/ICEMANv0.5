package com.teamunemployment.breadcrumbs.RESTApi;

import com.teamunemployment.breadcrumbs.Album.data.Comment;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Retrofit api for working with crumbs/events.
 */
public interface CrumbService {

    @GET("TrailManager/AddCommentToAlbum/{AlbumId}/{UserId}/{CommentText}")
    Call<ResponseBody> addCommentToAlbum(@Path("AlbumId") String albumId, @Path("UserId") String userId, @Path("CommentText") String commentText);

    @GET("login/LoadCommentsForEvent/{EventId}")
    Call<ArrayList<Comment>> getCommentsForEvent(@Path("EventId") String eventId);

}
