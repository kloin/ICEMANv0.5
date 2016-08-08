package com.teamunemployment.breadcrumbs.RESTApi;

import com.teamunemployment.breadcrumbs.Album.Frame;
import com.teamunemployment.breadcrumbs.Album.data.FrameDetails;
import com.teamunemployment.breadcrumbs.Album.data.MimeDetails;
import com.teamunemployment.breadcrumbs.Trails.Trip;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * @author Josiah Kendall.
 */
public interface AlbumService {
    @GET("TrailManager/GetAllSavedCrumbIdsForATrail/{TrailId}")
    Call<ResponseBody> GetFrameIdsForAnAlbum(@Path("TrailId") String albumId);

    @GET("Frame/FrameDetails/{FrameId}")
    Call<FrameDetails> GetFrameDetails(@Path("FrameId") String frameId);

    @GET("Frame/LoadFrameMimesForAlbum/{AlbumId}")
    Call<ArrayList<MimeDetails>> GetFrameMimesForAlbum(@Path("AlbumId") String albumId);
}
