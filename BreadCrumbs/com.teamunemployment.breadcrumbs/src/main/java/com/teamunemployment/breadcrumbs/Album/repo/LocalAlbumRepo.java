package com.teamunemployment.breadcrumbs.Album.repo;

import android.support.annotation.Nullable;

import com.teamunemployment.breadcrumbs.Album.data.Comment;
import com.teamunemployment.breadcrumbs.Album.data.FrameDetails;
import com.teamunemployment.breadcrumbs.Album.data.MimeDetails;
import com.teamunemployment.breadcrumbs.FileManager.MediaRecordModel;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.inject.Inject;

/**
 * @author Josiah Kendall
 */
public class LocalAlbumRepo {

    private DatabaseController databaseController;

    @Inject
    public LocalAlbumRepo(DatabaseController databaseController) {
        this.databaseController = databaseController;
    }

    /**
     * Load mimes from the local server
     * @param albumId
     * @return
     */
    public ArrayList<MimeDetails> LoadMimeDetailsForAnAlbum(String albumId) {
        return databaseController.LoadMimeDetails(albumId);
    }

    /**
     * Fetch the details for a frame.
     * @param frameId The Id of the frame we are fetching.
     * @return A Frame object with the details
     */
    @Nullable
    public FrameDetails LoadFrameDetails(String frameId) {

        return databaseController.GetFrameDetails(frameId);
    }

    /**
     * Return a linked list of all the frame ids for an album.
     * @param albumId
     * @return A Linked list of the ids.
     */
    public LinkedList<String> LoadFrameIds(String albumId) {

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
        // TODO : Create file management system.

        return false;
    }

    public void SaveMimeDetailsToFrame(MimeDetails mimeDetails, String albumId) {
        // If A Frame exists
        FrameDetails frameDetails = databaseController.GetFrameDetails(mimeDetails.getId());

        if (frameDetails== null) {
            frameDetails = new FrameDetails();
            frameDetails.setId(mimeDetails.getId());
            frameDetails.setExtension(mimeDetails.getExtension());
            frameDetails.setTrailId(albumId);
            databaseController.SaveFrameDetails(frameDetails);
        }
    }

    public void SaveFrameMimeData(ArrayList<MimeDetails> mimeDetailsArray, String albumId) {
        Iterator<MimeDetails> frameDetailsIterator = mimeDetailsArray.iterator();
        while (frameDetailsIterator.hasNext()) {
            MimeDetails details = frameDetailsIterator.next();
            SaveMimeDetailsToFrame(details, albumId);
        }
    }

    /**
     * Save a crumb
     * @param newFrameDetail
     */
    public void SaveFrameDetails(FrameDetails newFrameDetail) {
        if (newFrameDetail != null) {
            final FrameDetails frameDetails = databaseController.GetFrameDetails(newFrameDetail.getId());
            if (frameDetails == null) {
                databaseController.SaveFrameDetails(newFrameDetail);
            } else {
                databaseController.UpdateFrameDetails(newFrameDetail);
            }
        }
    }

    public void SaveMediaFileRecord(MediaRecordModel recordModel) {
        if (recordModel != null) {
            databaseController.SaveMediaFileRecord(recordModel);
        }
    }

    public MediaRecordModel FindMediaFileRecord(String frameId) {
        return databaseController.GetMediaFileRecord(frameId);
    }

    /**
     * Save a comment for a frame.
     * @param comment The comment to save
     */
    public void SaveComment(Comment comment) {
        // TODO save a comment object to the database.
        if (comment != null) {
            databaseController.SaveComment(comment);
        }
    }

    public void DeleteComment(String commentId) {
        databaseController.DeleteComment(commentId);
    }

    public ArrayList<Comment> LoadCommentsForAnAlbum(String frameId) {
        return databaseController.GetAllCommentsForAnAlbum(frameId);
    }

    /**
     * Save all the comments in an array.
     */
    public void SaveComments(ArrayList<Comment> comments) {
        Iterator<Comment> commentIterator = comments.iterator();
        while (commentIterator.hasNext()) {
            Comment next = commentIterator.next();
            SaveComment(next);
        }
    }
}
