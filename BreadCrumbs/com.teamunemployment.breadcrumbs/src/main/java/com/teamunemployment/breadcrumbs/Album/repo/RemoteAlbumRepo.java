package com.teamunemployment.breadcrumbs.Album.repo;

import android.support.annotation.Nullable;
import android.util.Log;

import com.teamunemployment.breadcrumbs.Album.AlbumModel;
import com.teamunemployment.breadcrumbs.Album.data.Comment;
import com.teamunemployment.breadcrumbs.Album.data.FrameDetails;
import com.teamunemployment.breadcrumbs.Album.data.MimeDetails;
import com.teamunemployment.breadcrumbs.RESTApi.AlbumService;
import com.teamunemployment.breadcrumbs.RESTApi.CrumbService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @author Josiah Kendall.
 */
public class RemoteAlbumRepo {

    private static final String TAG = "RemoteAlbumRepo";
    private AlbumService albumService;
    private CrumbService crumbService;

    @Inject
    public RemoteAlbumRepo(AlbumService albumService, CrumbService crumbService) {
        this.albumService = albumService;
        this.crumbService = crumbService;
    }

    public ArrayList<MimeDetails> LoadMimeDetailsForAnAlbum(String albumId) {
        Call<ArrayList<MimeDetails>> call = albumService.GetFrameMimesForAlbum(albumId);
        try {
            Response<ArrayList<MimeDetails>> mimesResponse = call.execute();
            return mimesResponse.body();
        } catch (IOException e) {
            Log.d(TAG, "failed to load Mime details for an album");
            e.printStackTrace();
        }

        // Return a blank.
        return new ArrayList<>();
    }

    /**
     * Fetch the details for a frame.
     * @param frameId The Id of the frame we are fetching.
     * @return A Frame object with the details
     */
    public FrameDetails LoadFrameDetails(String frameId) {
        Call<FrameDetails> call = albumService.GetFrameDetails(frameId);
        try {
            Response<FrameDetails> detailsResponse = call.execute();
            return detailsResponse.body();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Return a linked list of all the frame ids for an album.
     * @param albumId
     * @return A Linked list of the ids.
     */
    @Nullable
    public LinkedList<String> LoadFrameIds(String albumId) {
        Call<ResponseBody> call = albumService.GetFrameIdsForAnAlbum(albumId);
        try {
            Response<ResponseBody> response = call.execute();
            if (response != null && response.code() ==200) {
                String commaSeperatedIds = response.body().string();
                String[] ids = commaSeperatedIds.split(",");
                LinkedList<String> idsArray = new LinkedList<>(Arrays.asList(ids));
                return idsArray;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new LinkedList<>();
    }

    /**
     * Download a file from our server.
     * @param id The id of the file to download
     * @param mime The file type (.jpg) (.mp4)
     * @param resolution The resolution of the file to download
     * @return true if successfully downloaded, false if the download failed.
     */
    public boolean DownloadFile(String id, String mime, String resolution) {
        return false;
    }

    /**
     * Save a comment to the remote server.
     * @param comment The comment object to save.
     */
    public String SaveComment(Comment comment) {
        Call<ResponseBody> call = crumbService.saveComment(comment.getUserId(), comment.getEntityId(), comment.getCommentText());
        try {
            Response<ResponseBody> responseBodyResponse = call.execute();
            return responseBodyResponse.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "-1";
    }

    public void DeleteComment(String commentId) {
       // crumbService.deleteComment(commentId);
    }

    /**
     * Get an array list of comments for a frame.
     * @param frameId The id of the frame we are gettign comments for.
     * @return The array of comment objects.
     */
    public ArrayList<Comment> LoadCommentsForFrame(String frameId) {
        Call<ArrayList<Comment>> call = crumbService.getCommentsForEvent(frameId);
        try {Response<ArrayList<Comment>> response = call.execute();
           if (response != null && response.body() != null && response.code() == 200) {
               return response.body();
           }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}