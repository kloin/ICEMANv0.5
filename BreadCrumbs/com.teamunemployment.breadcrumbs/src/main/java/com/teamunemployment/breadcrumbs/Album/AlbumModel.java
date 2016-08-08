package com.teamunemployment.breadcrumbs.Album;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Environment;
import android.view.Surface;
import android.view.TextureView;

import com.squareup.picasso.Picasso;
import com.teamunemployment.breadcrumbs.Album.data.FrameDetails;
import com.teamunemployment.breadcrumbs.Album.data.MimeDetails;
import com.teamunemployment.breadcrumbs.Album.data.VideoFrame;
import com.teamunemployment.breadcrumbs.Album.repo.LocalAlbumRepo;
import com.teamunemployment.breadcrumbs.Album.repo.RemoteAlbumRepo;
import com.teamunemployment.breadcrumbs.FileManager.MediaRecordModel;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.RESTApi.FileManager;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Josiah Kendall.
 */
public class AlbumModel{
    private static final String TAG = "AlbumModel";

    private RemoteAlbumRepo remoteAlbumRepo;
    private LocalAlbumRepo localAlbumRepo;

    private Context context;
    private String targetId = "0";
    private boolean awaitingFrame = false;
    private AtomicBoolean amDownloading = new AtomicBoolean(false);
    private LinkedList<String> frameIds;
    private ArrayList<MimeDetails> mimes;
    private AlbumModelPresenterContract contract;
    private FileManager fileManager;
    private Surface surface;

    public AlbumModel(RemoteAlbumRepo remoteAlbumRepo, LocalAlbumRepo localAlbumRepo,
                      Context context, AlbumModelPresenterContract contract,
                      FileManager fileManager) {
        this.remoteAlbumRepo = remoteAlbumRepo;
        this.localAlbumRepo = localAlbumRepo;
        this.context = context;
        this.contract = contract;
        this.fileManager = fileManager;
    }

    /**
     * Load the media for a frame.
     * @param id The id of the media we are loading
     * @param mimeType
     * @return True if loaded successfully, false if it failed.
     */
    public boolean LoadFrameMedia(String id, String mimeType) {
        if (mimeType.equals(".jpg")) {
            return loadImage(id);
        } else {
            return loadVideo(id);
        }
    }

    public boolean loadImage(String id) {
        // do load
        // Go next
        return false;
    }

    public boolean loadVideo(String id) {
        // do load. Load next.
        return false;
    }

    /**
     * Load the mime details for an album.
     * @param albumId id of the album we are loading frames for.
     * @return An array of mimeDetails objects for every frame.
     */
    public ArrayList<MimeDetails> LoadMimeDetails(String albumId) {
        mimes = localAlbumRepo.LoadMimeDetailsForAnAlbum(albumId);
        // TODO - do remote check as well, and save it/ update it if neccessary.
        if (mimes.size() == 0) {
            mimes = remoteAlbumRepo.LoadMimeDetailsForAnAlbum(albumId);
            localAlbumRepo.SaveFrameMimeData(mimes);
        }
        return mimes;
    }

    /**
     * Request a frame, and call the presenter setFrame when finished.
     * @param frameId The id of the frame we are loading.
     * @param extension The extension (.jpg, .mp4)
     */
    public void RequestFrame( String frameId, String extension) {
        // This checks if we are currently downloading the video we want to display, by matching
        // the id we are currently downloading with the id we want to display. If these do not match,
        // it means that the media with that id has already downloaded.
        String downloadId = getTargetId();
        if (downloadId.equals(frameId)) {
            setWaitingFlag(true);
            return;
        }

        // If we cannot find the frame details locally, we should return it.
        FrameDetails frameDetails = localAlbumRepo.LoadFrameDetails(frameId);
        if (frameDetails == null) {
            // We are either still loading this one, or we failed.
            frameDetails = remoteAlbumRepo.LoadFrameDetails(frameId);
        }
        
        // Set our frame now it is loaded. This contains the reference to the locally downloaded media.
        contract.setFrame(frameDetails);
    }

    /**
     * Start downloading all our frames.
     */
    public void StartDownloadingFrames() {
        Iterator<MimeDetails> mimeDetailsIterator = mimes.iterator();
        amDownloading.set(true);
        while (mimeDetailsIterator.hasNext() && amDownloading.get()) {
            MimeDetails mimeDetails = mimeDetailsIterator.next();
            setTargetFrameId(mimeDetails.getId());
            DownloadFrame(mimeDetails.getId(), mimeDetails.getExtension());
        }
    }

    public void StartPlayingMedia() {

    }

    /**
     * Simple method to trigger the download of a frame.
     * @param frameId The id of a frame
     * @param extension The extension of said frame - either mp4 or jpeg.
     */
    public void DownloadFrame(String frameId, String extension) {

        FrameDetails frameDetails = localAlbumRepo.LoadFrameDetails(frameId);
        if (frameDetails == null) {
            frameDetails = remoteAlbumRepo.LoadFrameDetails(frameId);
            localAlbumRepo.SaveFrameDetails(frameDetails);
        }
        if (extension.equals(".mp4")) {
            MediaRecordModel recordModel = fileManager.DownloadAndSaveLocalFile(frameId);
            // Save this record to the database.
            localAlbumRepo.SaveMediaFileRecord(recordModel);
        } else {
            // TODO mock this and make it testable.
            Picasso.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+frameId + ".jpg");
        }

        if (isAwaitingFrame()) {
            if (contract != null) {
                setWaitingFlag(false);
                contract.setFrame(frameDetails);
            }
        }
    }

//    /**
//     * Download all the video files for an album. This runs all downloads on the same thread.
//     * @return
//     */
//    private boolean DownloadAllVideoFiles() {
//        Iterator<MimeDetails> frameMimesIterator = mimes.iterator();
//
//        while (frameMimesIterator.hasNext()) {
//            // download media.
//            MimeDetails mimeDetails =  frameMimesIterator.next();
//            String mime = mimeDetails.getExtension();
//            String id = mimeDetails.getId();
//
//            // Load media
//            fileManager.DownloadFileFromServer(id, mime);
//        }
//
//        return true;
//    }

    /**
     * Set the current id that we are downloading.
     * @param id
     */
    private synchronized void setTargetFrameId(String id) {
        targetId = id;
    }

    /**
     * @return the frame that we are currently downloading.
     */
    private synchronized String getTargetId() {
        return targetId;
    }

    private synchronized void setDownloadingState(boolean state) {

    }
    /**
     * Set a waiting flag for our next frame.
     * @param state
     */
    private synchronized void setWaitingFlag(boolean state) {
        awaitingFrame = state;
    }

    /**
     * @return Whether we are waiting for a frame or not.
     */
    private synchronized boolean isAwaitingFrame() {
        return awaitingFrame;
    }

}
