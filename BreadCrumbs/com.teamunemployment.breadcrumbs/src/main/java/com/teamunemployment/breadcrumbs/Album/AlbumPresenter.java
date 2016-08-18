package com.teamunemployment.breadcrumbs.Album;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ProgressBar;

import com.teamunemployment.breadcrumbs.Album.data.FrameDetails;
import com.teamunemployment.breadcrumbs.Album.data.MimeDetails;
import com.teamunemployment.breadcrumbs.AlbumDataSource;
import com.teamunemployment.breadcrumbs.BreadcrumbsTimer;
import com.teamunemployment.breadcrumbs.MediaPlayerWrapper;
import com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.inject.Inject;

/**
 * @author Josiah Kendall
 */
public class AlbumPresenter implements AlbumModelPresenterContract, TextureView.SurfaceTextureListener,
        BreadcrumbsTimer.TimerCompleteListener {

    private static final String TAG = "AlbumPresenter";
    private static final String MP4 = ".mp4";
    private AlbumPresenterViewContract view;
    private Surface videoSurface;
    private AlbumModel model;
    private ArrayList<MimeDetails> mimeDetailsArrayList;
    private ListIterator<MimeDetails> mimeDetailsIterator;
    private Context context;
    private BreadcrumbsTimer timer;
    private ProgressBar progressBar;
    private boolean firstStart = true;
    private MediaPlayerWrapper mediaPlayerWrapper;

    private MimeDetails currentFrame;
    private AlbumDataSource albumDataSource;

    // If we go backward, it moves the pointer on the iterator so far back so that when we go foreward,
    // it just returns the object that we are lookking at. If we are going backwards, this is desirable, (think skipping back in a song)
    // but we dont want to do this for going foreward.
    private boolean hasGoneBackwardLast = false;
    private boolean requestPrepareAndPlayWhenSurfaceReady = false;

    @Inject
    public AlbumPresenter(AlbumModel model, Context context, MediaPlayerWrapper mediaPlayerWrapper, AlbumDataSource albumDataSource) {
        this.context = context;
        this.model = model;
        this.mediaPlayerWrapper = mediaPlayerWrapper;
        this.albumDataSource = albumDataSource;

        // Set our communication contract between the model and the presenter
        model.setContract(this);
    }

    /**
     * Set the Texture surface required to play videos.
     * @param surface Our texture view.
     */
    public void setVideoSurface(TextureView surface) {
        surface.setSurfaceTextureListener(this);
    }

    /**
     * Set the progress bar we need.
     * @param pb The progress bar for each individual frame.
     */
    public void setProgressBar(ProgressBar pb) {
        this.progressBar = pb;
    }

    /**
     * Start loading an album
     * @param albumId The id of the album that we are displaying.
     */
    public void Start(String albumId) {
        albumDataSource.SetAlbumId(albumId);

        // Routine pre game checks.
        doPreReqChecks(albumDataSource.GetAlbumId());

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Load the basic details that we need to load / display the info/media.
                mimeDetailsArrayList = model.LoadMimeDetails(albumDataSource.GetAlbumId());

                // If we have nothing to display, we cant really do shit. This shouldn't happen in theory, but need safety.
                if (mimeDetailsArrayList == null || mimeDetailsArrayList.size() == 0) {
                    view.showMessage("Unable to play.");
                    return;
                }

                // initialise our frames iterator
                if (mimeDetailsIterator == null) {
                    mimeDetailsIterator = mimeDetailsArrayList.listIterator();
                }

                // If i play without calling next
                if (!mimeDetailsIterator.hasNext()) {
                    // If the iterator has nothing, we have to exit.
                    stop();
                    view.finishUp();
                }

                PlayFrame(mimeDetailsIterator.next());
                if (!albumDataSource.getIsLocal()) {
                    model.StartDownloadingFrames();
                }
            }
        }).start();
    }

    /**
     * Check that we all good to start.
     * @param albumId album id.
     */
    private void doPreReqChecks(String albumId) {
        // Check we have a view to display shit on.
        if (view == null) {
            throw new NullPointerException("Cannot start presenter before view has been set. Use SetView() to set");
        }

        // We need the album id to fetch any frames.
        if (albumId == null) {
            throw new NullPointerException("Album Id must not be null");
        }
    }

    /**
     * Set the view contract for our presenter.
     * @param view The album view for the presenter to use.
     */
    public void SetView(AlbumPresenterViewContract view) {
        this.view = view;
    }

    /**
     * Play an object. If we are already playing, this method should stop and play a new one.
     * @param next The object which we are about to display.
     */
    public void PlayFrame(final MimeDetails next) {
        currentFrame = next;
        if (timer != null) {
            timer.Stop();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (albumDataSource.getIsLocal()) {
                    model.RequestLocalFrame(next.getId());
                    return;
                }
                // Request frame. This returns the frame to the setFrame callback.
                model.RequestFrame(next.getId());
            }
        }).start();
    }

    // Not used yet but maybe in the future
    public void Pause() {
        if (currentFrame.getExtension().equals(MP4)) {
            mediaPlayerWrapper.Pause();
        }
        timer.Pause();
    }

    // Resume playback. Happens when the app is closed and reopened on this page, or when we move to another
    // activity and then back again.
    public void Resume() {

        // Resume gets called on a start too. Therefore we have this prereq check.
        if (firstStart) {
            firstStart = false;
            return;
        }

        // Current frame must not be null. We also only want to resume here if it is a photo. The video
        // resume callback is handled by the TextureView lifecycle.
        if (currentFrame != null && !currentFrame.getExtension().equals(MP4)) {
            timer.Resume();
            timer.Start();
        }
    }

    /**
     * Resume playback of video.
     */
    private void resumeMediaPlayback() {
        mediaPlayerWrapper.Play();
        timer.Resume();
        timer.Start();
    }

    @Override
    public void setFrame(FrameDetails frame) {
        if (frame.getExtension().equals(MP4)) {
            setVideoFrame(frame);
        } else {
            setImageFrame(frame);
        }

        getProfilePicture(frame.getUserId());
        getProfileName(frame.getUserId());
        
        String xPosString = frame.getDescPosX();
        String yPosString = frame.getDescPosY();
        String description = frame.getChat();
        if (xPosString != null && yPosString != null && description!= null && !description.equals("null")) {
            float x = Float.parseFloat(xPosString);
            float y = Float.parseFloat(yPosString);
            view.setScreenMessage(frame.getChat(),x, y);
        } else {
            view.setScreenMessage("", 0, 0);
        }
    }

    /**
     * Set the owner profile pic.
     * @param userId The owner id.
     */
    public void getProfilePicture(String userId) {
        model.FetchProfilePicture(userId);
    }

    /**
     * Get the users name.
     * @param userId The owner Id.
     */
    public void getProfileName(String userId) {
        model.FetchUserName(userId);
    }

    /**
     * Set buffering of video. This sets the visibility of the indeterminate progress circle
     * @param visibility The visibility to set the progress to.
     */
    @Override
    public void setBuffering(int visibility) {
        if (visibility == View.VISIBLE) {
            view.setBufferingVisible();
        } else {
            view.setBufferingInvisible();
        }
    }

    /**
     * Set the profile picture url. This triggers the load of the profile pic using {@link com.squareup.picasso.Picasso}
     * @param url The target url.
     */
    @Override
    public void setProfilePictureUrl(String url) {
        view.setProfilePicture(url);
    }

    /**
     * Set the users name
     * @param userName the user name to set.
     */
    @Override
    public void setUserName(String userName) {
        view.setUserName(userName);
    }

    /**
     * Set and display an image frame
     * @param frame The object representing the frame. This contains the information needed to display
     *              all our details about the frame. see {@link FrameDetails}.
     */
    private void setImageFrame(FrameDetails frame) {

        // Show our image, hide our video.
        view.setImageVisibility(View.VISIBLE);
        view.setVideoVisibility(View.INVISIBLE);

        // debug stuff. Should never be true.
        Log.d(TAG, "Loading bitmap - are we on the main thread? - " + Utils.WeAreRunningOnTheUIThread());

        // TODO - need to fix this potential npe. knowing android this will occur on some random ass phones
        String url = albumDataSource.getDataSource() + "/"+frame.getId() + frame.getExtension();
        Bitmap bitmap = BitmapFactory.decodeFile(url);
        view.setImageBitmap(bitmap);

        // reset/set timer.
        timer = new BreadcrumbsTimer(5000, this, progressBar);
        timer.setTimerMax(5000);
        timer.Start();
    }

    /**
     * Set up a video frame
     * @param frame The details for the frame we are creating.
     */
    private void setVideoFrame(FrameDetails frame) {
        view.setImageVisibility(View.INVISIBLE);
        view.setVideoVisibility(View.VISIBLE);
        // TODO find a solution to this. This will almost definitely crash on some devices.
        String url = albumDataSource.getDataSource() + "/"+frame.getId() + frame.getExtension();

        // Reset the media player back to an idle state.
        mediaPlayerWrapper.Reset();

        // Set the datasource
        mediaPlayerWrapper.SetTrack(url);

        // TODO - tidy this.
        int prepareResult = mediaPlayerWrapper.Prepare();
        if (prepareResult == MediaPlayerWrapper.PREPARED) {
            mediaPlayerWrapper.Play();
            int duration = mediaPlayerWrapper.getDuration();
            timer = new BreadcrumbsTimer(duration, this, progressBar);
            timer.setTimerMax(duration);
            timer.Start();
        } else if (prepareResult == MediaPlayerWrapper.INITIALIZED) {
            requestPrepareAndPlayWhenSurfaceReady = true;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if (surfaceTexture!= null) {
            Log.d(TAG, "New surface texture available now. Will create surface now.");
            videoSurface = new Surface(surfaceTexture);
            mediaPlayerWrapper.setSurfaceView(videoSurface);
            if (mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PAUSED) {
                resumeMediaPlayback();
            } else if (requestPrepareAndPlayWhenSurfaceReady) {
                mediaPlayerWrapper.Prepare();
                mediaPlayerWrapper.Play();

                int duration = mediaPlayerWrapper.getDuration();
                timer = new BreadcrumbsTimer(duration, this, progressBar);
                timer.setTimerMax(duration);
                timer.Start();
                requestPrepareAndPlayWhenSurfaceReady = false;
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        // Currently do not support changing of orientation or any of that shit, as we want to allways
        // be full screen to maintain immersive experience. This should never be called.
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        // If we press back before we have loaded the first item, all this stuff is not iniailized so it npe
        if (timer != null) {
            timer.Stop();
            mediaPlayerWrapper.setSurfaceView(null);

            if (currentFrame.getExtension().equals(MP4)) {
                // Pause, as we may want to resume at a later date.
                mediaPlayerWrapper.Pause();
            }
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    /**
     * Listener for the completed timer.
     */
    @Override
    public void onCompleted() {
        // request the next frame.
        if (hasGoneBackwardLast) {
            mimeDetailsIterator.next();
        }

        if (mimeDetailsIterator.hasNext()) {
            PlayFrame(mimeDetailsIterator.next());
            hasGoneBackwardLast = false;
        } else {
            Log.d(TAG, "Cannot go foreward, finishing now.");
            view.showMessage("Finished");
            view.finishUp();
        }
    }

    /**
     * Go foreward to the next item in the list
     */
    public void foreward() {
       mediaPlayerWrapper.Stop();
        if (timer != null) {
            timer.Stop();
        }
        if (hasGoneBackwardLast) {
            mimeDetailsIterator.next();
        }

        if (mimeDetailsIterator.hasNext()) {
            PlayFrame(mimeDetailsIterator.next());
            hasGoneBackwardLast = false;
        } else {
            view.finishUp();
            Log.d(TAG, "We are at the Last, cannot go foreward.");
        }
    }

    /**
     * Go back to the previous item.
     */
    public void reverse() {
        mediaPlayerWrapper.Stop();
        if (mimeDetailsIterator.hasPrevious()) {
            PlayFrame(mimeDetailsIterator.previous());
            hasGoneBackwardLast = true;
        } else {
            Log.d(TAG, "We are at the first, cannot go back.");
        }
    }

    /**
     * Stop playback.
     */
    public void stop() {
        if (timer != null) {
            timer.Stop();
        }
        mediaPlayerWrapper.Stop();
    }

    @Nullable
    public MimeDetails getCurrentFrame() {
      return currentFrame;
    }
}