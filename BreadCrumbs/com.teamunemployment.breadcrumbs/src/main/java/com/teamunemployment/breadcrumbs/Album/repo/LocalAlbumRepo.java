package com.teamunemployment.breadcrumbs.Album.repo;

import android.support.annotation.Nullable;

import com.teamunemployment.breadcrumbs.Album.Frame;
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

    public void SaveMimeDetailsToFrame(MimeDetails mimeDetails) {
        // If A Frame exists
        FrameDetails frameDetails = databaseController.GetFrameDetails(mimeDetails.getId());
        if (frameDetails== null) {
            frameDetails = new FrameDetails();
            frameDetails.setId(mimeDetails.getId());
            frameDetails.setExtension(mimeDetails.getExtension());
            databaseController.SaveFrameDetails(frameDetails);
        }
    }

    public void SaveFrameMimeData(ArrayList<MimeDetails> mimeDetailsArray) {
        Iterator<MimeDetails> frameDetailsIterator = mimeDetailsArray.iterator();
        while (frameDetailsIterator.hasNext()) {
            MimeDetails details = frameDetailsIterator.next();
            SaveMimeDetailsToFrame(details);
        }
    }

    /**
     * Save a crumb
     * @param frameDetail
     */
    public void SaveFrameDetails(FrameDetails frameDetail) {
        FrameDetails frameDetails = databaseController.GetFrameDetails(frameDetail.getId());
        if (frameDetail == null) {
            databaseController.SaveFrameDetails(frameDetails);
        } else {
            databaseController.UpdateFrameDetails(frameDetails);
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
}
