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

    private static final int NON_EXISTENT = 0;
    private static final int INITIALISED = 1;
    private static final int PLAYING = 2;
    private static final int PAUSED = 3;
    private static final int STOPPED = 4;
    private static final int DESTROYED = 5;
    private static final String TAG = "AlbumPresenter";
    private static final String MP4 = ".mp4";
    private AlbumPresenterViewContract view;
    private Surface videoSurface;
    private AlbumModel model;
    private MediaPlayer mediaPlayer;
    private ArrayList<MimeDetails> mimeDetailsArrayList;
    private ListIterator<MimeDetails> mimeDetailsIterator;
    private Context context;
    private BreadcrumbsTimer timer;
    private ProgressBar progressBar;
    private boolean firstStart = true;
    private MediaPlayerWrapper mediaPlayerWrapper;

    private MimeDetails currentFrame;

    private int mediaPlayerState = 0;
    // If we go backward, it moves the pointer on the iterator so far back so that when we go foreward,
    // it just returns the object that we are lookking at. If we are going backwards, this is desirable, (think skipping back in a song)
    // but we dont want to do this for going foreward.
    private boolean hasGoneBackwardLast = false;

    @Inject
    public AlbumPresenter(AlbumModel model, Context context, MediaPlayerWrapper mediaPlayerWrapper) {
        this.context = context;
        this.model = model;
        this.mediaPlayerWrapper = mediaPlayerWrapper;
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
    public void Start(final String albumId) {

        // Routine pre game checks.
        doPreReqChecks(albumId);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Load the basic details that we need to load / display the info/media.
                mimeDetailsArrayList = model.LoadMimeDetails(albumId);

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
                model.StartDownloadingFrames();
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
                // Request frame. Callback has already been
                model.RequestFrame(next.getId());
            }
        }).start();
    }

    // Not used yet but maybe in the future
    public void Pause() {
        mediaPlayer.pause();
        mediaPlayerState = PAUSED;
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

    // set a users profile pic to the
    public void getProfilePicture(String userId) {
        model.FetchProfilePicture(userId);
    }

    public void getProfileName(String userId) {
        model.FetchUserName(userId);
    }

    @Override
    public void setBuffering(int visibility) {
        if (visibility == View.VISIBLE) {
            view.setBufferingVisible();
        } else {
            view.setBufferingInvisible();
        }
    }

    @Override
    public void setProfilePictureUrl(String url) {
        view.setProfilePicture(url);
    }

    @Override
    public void setUserName(String userName) {
        view.setUserName(userName);
    }

    // Set an image.
    private void setImageFrame(FrameDetails frame) {
        view.setImageVisibility(View.VISIBLE);
        view.setVideoVisibility(View.INVISIBLE);
        Log.d(TAG, "Loading bitmap - are we on the main thread? - " + Utils.WeAreRunningOnTheUIThread());
        String url = context.getExternalCacheDir().getAbsolutePath() + "/"+frame.getId() + frame.getExtension();
        Bitmap bitmap = BitmapFactory.decodeFile(url);
        view.setImageBitmap(bitmap);

        // Set timer
        timer = new BreadcrumbsTimer(5000, this, progressBar);
        timer.setTimerMax(5000);
        timer.Start();
    }

    /**
     * Set up a video frame
     * @param frame The details for the frame we are creating.
     */
    private void setVideoFrame(FrameDetails frame) {
        // TODO find a solution to this. This will almost definitely crash on some devices.
        String url = context.getExternalCacheDir().getAbsolutePath() + "/"+frame.getId() + frame.getExtension();
        mediaPlayer = buildMediaPlayer(videoSurface, url);
        mediaPlayerState = INITIALISED;
        int duration = mediaPlayer.getDuration();
        timer = new BreadcrumbsTimer(duration, this, progressBar);
        timer.setTimerMax(duration);
        timer.Start();
    }

    // Needs moving
    private MediaPlayer buildMediaPlayer(Surface surface, String path) {
        try {
            Log.d(TAG, "Reset Media Player");
            mediaPlayer.reset();
            mediaPlayerState = INITIALISED;
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();

            Log.d(TAG, "Media player datasource set to: " + path);
            mediaPlayer.setSurface(surface);
            Log.d(TAG, "Set Media surface");
            mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            Log.d(TAG, "Prepared mediaPlayer successfully");
        } catch (IOException e) {
            Log.e(TAG, "Failed to prepare music player. ");
            e.printStackTrace();
        }

        return mediaPlayer;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if (surfaceTexture!= null) {
            Log.d(TAG, "New surface texture available now. Will create surface now.");
            videoSurface = new Surface(surfaceTexture);
            mediaPlayer.setSurface(videoSurface);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        // do anything here?
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        // We are not playing shit anymore, so stop downloading.
       // mediaPlayer.pause();
        timer.Stop();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        // Not sure we need this.
    }

    // Needs moving.
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
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            Log.d(TAG, "Media player stopped");
            mediaPlayerState = STOPPED;
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

    public void reverse() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            Log.d(TAG, "Media player stopped");
            mediaPlayerState = STOPPED;
        }
        if (mimeDetailsIterator.hasPrevious()) {
            PlayFrame(mimeDetailsIterator.previous());
            hasGoneBackwardLast = true;
        } else {
            Log.d(TAG, "We are at the first, cannot go back.");
        }
    }

    public void stop() {
        timer.Stop();
        if (mediaPlayerState == PLAYING) {
            mediaPlayer.stop();
            Log.d(TAG, "Media player stopped");
            mediaPlayerState = STOPPED;
        }
    }

    /**
     * Restart the presenter.
     */
    public void restart() {
        // If have started
        if (firstStart) {
            firstStart = false;
        } else {
            if (timer != null) {
                // If its the first time loading it will set us to the start, if its not it will set us to the correct position.
                timer.Start();
                //mediaPlayer.start();
                //mediaPlayer.seekTo(timer.getTimerPosition());

            }
        }
    }

    @Nullable
    public MimeDetails getCurrentFrame() {
      return currentFrame;
    }
}