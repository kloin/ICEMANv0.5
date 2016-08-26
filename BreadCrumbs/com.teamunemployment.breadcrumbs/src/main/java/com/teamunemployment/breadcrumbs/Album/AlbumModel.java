package com.teamunemployment.breadcrumbs.Album;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.squareup.picasso.Picasso;
import com.teamunemployment.breadcrumbs.Album.data.Comment;
import com.teamunemployment.breadcrumbs.Album.data.FrameDetails;
import com.teamunemployment.breadcrumbs.Album.data.MimeDetails;
import com.teamunemployment.breadcrumbs.Album.repo.LocalAlbumRepo;
import com.teamunemployment.breadcrumbs.Album.repo.RemoteAlbumRepo;
import com.teamunemployment.breadcrumbs.FileManager.MediaRecordModel;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.NetworkConnectivityManager;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.Profile.data.LocalProfileRepository;
import com.teamunemployment.breadcrumbs.Profile.data.RemoteProfileRepository;
import com.teamunemployment.breadcrumbs.RESTApi.FileManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

/**
 * @author Josiah Kendall.
 */
public class AlbumModel{
    private static final String TAG = "AlbumModel";

    private ArrayList<Comment> commentsArray = new ArrayList<>();
    private RemoteAlbumRepo remoteAlbumRepo;
    private LocalAlbumRepo localAlbumRepo;

    private Context context;
    private String targetId = "0";
    private boolean awaitingFrame = true;
    private AtomicBoolean amDownloading = new AtomicBoolean(false);
    private ArrayList<MimeDetails> mimes;
    private AlbumModelPresenterContract contract;
    private FileManager fileManager;
    private RemoteProfileRepository remoteProfileRepo;
    private LocalProfileRepository localProfileRepo;
    private PreferencesAPI preferencesAPI;

    @Inject
    public AlbumModel(RemoteAlbumRepo remoteAlbumRepo, LocalAlbumRepo localAlbumRepo,
                      Context context, FileManager fileManager, LocalProfileRepository localUserRepo,
                      RemoteProfileRepository remoteProfileRepo, PreferencesAPI preferencesAPI) {
        this.remoteAlbumRepo = remoteAlbumRepo;
        this.localAlbumRepo = localAlbumRepo;
        this.context = context;
        this.fileManager = fileManager;
        this.localProfileRepo = localUserRepo;
        this.remoteProfileRepo = remoteProfileRepo;
        this.preferencesAPI = preferencesAPI;
    }

    /**
     * Synchronously load the profile pic id for a user.
     * @param userId The userId for whoim we are getting the profile pic id.
     * @return The id of the photo that is this users profile pic.
     */
    public String LoadUserProfilePic(String userId) {
        String picId = localProfileRepo.getProfilePictureId(Long.parseLong(userId));
        if (picId == null) {
            picId = remoteProfileRepo.getProfilePictureId(Long.parseLong(userId));
        }
        return picId;
    }

    public interface loadedCommentsCallback {
        void onLoaded(ArrayList<Comment> comments);
    }

    public void setContract(AlbumModelPresenterContract albumModelPresenterContract) {
        this.contract = albumModelPresenterContract;
    }

    /**
     * Load the mime details for an album.
     * @param albumId id of the album we are loading frames for.
     * @return An array of mimeDetails objects for every frame.
     */
    public ArrayList<MimeDetails> LoadMimeDetails(String albumId) {
        mimes = localAlbumRepo.LoadMimeDetailsForAnAlbum(albumId);
        // TODO - do remote check as well, and save it/ update it if neccessary. This is a bug. Not sure how/when this should be fixed. Probably go with callbacks
        if (mimes.size() == 0) {
            mimes = remoteAlbumRepo.LoadMimeDetailsForAnAlbum(albumId);
            localAlbumRepo.SaveFrameMimeData(mimes, albumId);
        }
        return mimes;
    }

    /**
     * Request a frame, and call the presenter setFrame when finished.
     * @param frameId The id of the frame we are loading.
     */
    public void RequestFrame( String frameId) {
        // This checks if we are currently downloading the video we want to display, by matching
        // the id we are currently downloading with the id we want to display. If these do not match,
        // it means that the media with that id has already downloaded.
        String downloadId = getTargetId();
        if (downloadId.equals(frameId) || isAwaitingFrame()) {
            setWaitingFlag(true);
            contract.setBuffering(View.VISIBLE);
            return;
        }

        // If we cannot find the frame details locally, we should return it.
        FrameDetails frameDetails = localAlbumRepo.LoadFrameDetails(frameId);
        if (frameDetails == null || frameDetails.getLongitude().equals("0.0") ) {
            // We are either still loading this one, or we failed.
            frameDetails = remoteAlbumRepo.LoadFrameDetails(frameId);
            localAlbumRepo.SaveFrameDetails(frameDetails);
        }

        if (contract == null) {
            throw new NullPointerException("Model-Presenter contract was null. setContract mus be called before" +
                    "requesting a frame.");
        }
        // Set our frame now it is loaded. This contains the reference to the locally downloaded media.
        contract.setBuffering(View.INVISIBLE);
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
            String res = calculateResolution();
            DownloadFrame(mimeDetails.getId(), mimeDetails.getExtension(), res);
        }
    }

    private String calculateResolution() {
        if (NetworkConnectivityManager.IsOnData(context)) {
            return "280";
        }
        if (isAwaitingFrame()) {
            return "360";
        }
        return "640";
    }

     /**
     * Simple method to trigger the download of a frame.
     * @param frameId The id of a frame.
     * @param extension The extension of said frame - either mp4 or jpeg.
     */
    public void DownloadFrame(String frameId, String extension, String targetRes) {
        // First, get frame details locally if we can. If not, load from remote and save them.
        FrameDetails frameDetails = localAlbumRepo.LoadFrameDetails(frameId);
        if (frameDetails == null) {
            frameDetails = remoteAlbumRepo.LoadFrameDetails(frameId);
            localAlbumRepo.SaveFrameDetails(frameDetails);
        }

        // If we have an mp4 file, we need to do a manual fetch.
        if (extension.equals(".mp4")) {
            if (isAwaitingFrame()) {
                targetRes = "280";
            }
            MediaRecordModel record = localAlbumRepo.FindMediaFileRecord(frameId);
            if (record == null) {
                MediaRecordModel recordModel = fileManager.DownloadAndSaveLocalFile(frameId, targetRes, extension);
                // Save this record to the database.
                if (recordModel == null) {
                    // This means that we did not find the media on the server. This is really bad.

                    return;
                }
                localAlbumRepo.SaveMediaFileRecord(recordModel);
            }
        } else {
            MediaRecordModel record = localAlbumRepo.FindMediaFileRecord(frameId);
            if (record == null) {
                MediaRecordModel recordModel = fileManager.DownloadAndSaveLocalFile(frameId, targetRes, extension);
                // Save this record to the database.
                if (recordModel == null) {
                    // This means that we did not find the media on the server. This is really bad.
                    Log.d(TAG, "Failed to download media. Will now exit.");
                    return;
                }
                localAlbumRepo.SaveMediaFileRecord(recordModel);
            }
        }
        // Kinda a hack. This just means that we are not currently downloading anything.
        setTargetFrameId("-1");
        if (isAwaitingFrame()) {
            if (contract != null) {
                setWaitingFlag(false);
                contract.setBuffering(View.INVISIBLE);
                contract.setFrame(frameDetails);
            }
        }
    }

    /**
     * Set the current id that we are downloading.
     * @param id the frame id of our target to download
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

    /**
     * Set a waiting flag for our next frame.
     * @param state Are we waiting for a frame to download or not.
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

    /**
     * Fetch a prfoile pic url for the specified user.
     * @param userId The id of the user whose profile pic we are fetching.
     */
    public void FetchProfilePicture(final String userId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (userId == null) {
                    return;
                }
                long userIdLong = Long.parseLong(userId);
                String userProfilePicId = localProfileRepo.getProfilePictureId(userIdLong);
                if (userProfilePicId == null) {
                    userProfilePicId = remoteProfileRepo.getProfilePictureId(userIdLong);
                    localProfileRepo.saveProfilePictureId(userProfilePicId, userIdLong);
                }

                String url = LoadBalancer.RequestCurrentDataAddress() + "/images/"+userProfilePicId + "T.jpg";
                contract.setProfilePictureUrl(url);
            }
        }).start();
    }

    /**
     * Fetch the userName for a specified user, and send it to the view.
     * @param userId The id of the user whose name we are fetching.
     */
    public void FetchUserName(final String userId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (userId == null || userId.equals("null") || userId.isEmpty()) {
                    return;
                }
                Long userIdLong = Long.parseLong(userId);
                String userName = localProfileRepo.getUserName(userIdLong);
                if (userName == null) {
                    userName = remoteProfileRepo.getUserName(userIdLong);
                    localProfileRepo.saveUserName(userName,userIdLong);
                }

                contract.setUserName(userName);
            }
        }).start();
    }

    public void RequestLocalFrame(String frameId) {
        FrameDetails frameDetails = localAlbumRepo.LoadFrameDetails(frameId);
        if (contract == null) {
            throw new NullPointerException("Model-Presenter contract was null. setContract mus be called before" +
                    "requesting a frame.");
        }
        // Set our frame now it is loaded. This contains the reference to the locally downloaded media.
        contract.setBuffering(View.INVISIBLE);
        contract.setFrame(frameDetails);
    }

    /**
     * Save a comment for a user
     * @param textToSave The comment to save
     * @param entityId The id of the frame/entity/crumb that we are saving against.
     */
    public void SaveComment(final String textToSave, final String entityId) {

        // Fetch our current users Id
        String currentUserId = preferencesAPI.GetUserId();

        // Build our comment object
        Comment comment = new Comment();
        comment.setCommentText(textToSave);
        comment.setEntityId(entityId);
        comment.setUserId(currentUserId);
        // Save both locally and remotel

        String serverId = remoteAlbumRepo.SaveComment(comment);
        if (!serverId.equals("-1")) {
            comment.setId(serverId);
            localAlbumRepo.SaveComment(comment);
        }
    }

    public void DeleteComment(final String commentId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                localAlbumRepo.DeleteComment(commentId);
                remoteAlbumRepo.DeleteComment(commentId);
            }
        }).start();
    }

    /**
     * Get all the comments for a frame. This puts the result (an array list of the object {@link Comment})
     * on a callback in the presenter which called this method/model. Said callback then does the processing
     * and the displaying of the comments.
     * @param frameId The frame id
     * @param commentsCallback The callback to trigger when we have data to display
     */
    public void GetCommentsForFrame(final String frameId, final loadedCommentsCallback commentsCallback) {
        final ArrayList<Comment> comments = localAlbumRepo.LoadCommentsForAFrame(frameId);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (comments.size() > 0) {
                    commentsCallback.onLoaded(comments);
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Comment> comments1 = remoteAlbumRepo.LoadCommentsForFrame(frameId);
                if (comments1.size() > 0) {
                    commentsCallback.onLoaded(comments1);
                    localAlbumRepo.SaveComments(comments1);
                }
            }
        }).start();
    }
}