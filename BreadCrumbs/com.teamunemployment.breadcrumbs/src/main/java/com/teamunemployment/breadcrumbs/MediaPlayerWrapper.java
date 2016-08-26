package com.teamunemployment.breadcrumbs;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils;

import java.io.IOException;

/**
 * @author Josiah Kendall.
 *
 * Wrapper for the {@link android.media.MediaPlayer}. Handles mediaplayer operations and manages
 * its state.
 */
public class MediaPlayerWrapper implements MediaPlayer.OnPreparedListener{
    private static final String TAG = "MediaPlayerWrapper";
    public static final int NON_EXISTANT = -1;
    public static final int IDLE = 0;
    public static final int INITIALIZED = 1;
    public static final int PREPARED = 2;
    public static final int STARTED = 3;
    public static final int STOPPED = 4;
    public static final int PLAYBACK_COMPLETED = 5;
    public static final int PAUSED = 6;
    public static final int PREPARING = 7;

    private MediaPlayer mediaPlayer;
    private int mediaPlayerState = -1;

    private int stoppageSeekPoint = 0;

    // A String path to our datasource.
    private String datasource;

    private Surface videoSurface;
    // It is possible (?) that we may call play when the media player is not ready, so we need this
    // flag to indicate that we wish to play when it is ready.
    private boolean setPlayWhenPrepared = false;
    private boolean setPrepareAndPlayWhenSurfaceSet = false;

    public MediaPlayerWrapper(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        mediaPlayerState = 0;
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void setSurfaceView(Surface videoSurface) {
        this.videoSurface = videoSurface;
        mediaPlayer.setSurface(videoSurface);
        if (setPrepareAndPlayWhenSurfaceSet) {
            Prepare();
            Play();
            setPrepareAndPlayWhenSurfaceSet = false;
        }
    }

    /**
     * @return The media player duration.
     */
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    /**
     * Getter for the current media player state.
     * @return The current state.
     */
    public int getCurrentState() {
        return mediaPlayerState;
    }

    /**
     * Returns the current dataSource
     * @return our datasource.
     */
    public String getDataSource() {
        return datasource;
    }

    /**
     * Reset the media player.
     * @return The new state of the media player
     */
    public int Reset() {
    //    Log.d(TAG, "About to reset mediaplayer");
        mediaPlayer.reset();
    //    Log.d(TAG, "Reset successfull");
        mediaPlayerState = IDLE;
        return mediaPlayerState;
    }

    /**
     * Set the datasource.
     * @param path The path to set as the dataSource.
     * @return The current state.
     */
    public int SetTrack(String path) {
        try {
            mediaPlayer.setDataSource(path);
            datasource = path;
            mediaPlayerState = INITIALIZED;
        } catch (IOException e) {
       //     Log.d(TAG, "Failed to set dataSource. Media player state set to idle.");
            e.printStackTrace();
            datasource = null;
            this.Reset();
        }
        return mediaPlayerState;
    }

    /**
     * Starts playback of the specified datasource. Must be used when the user is in a PREPARED state.
     * @return The current state of the player.
     */
    public int Play() {
       // Log.d(TAG, "About to start mediaplayer");
        if (mediaPlayerState == PREPARING) {
           // Log.d(TAG, "Media player not prepared. Will prepare now");
            //This means that we are still preparing.
            setPlayWhenPrepared = true;
        }
        mediaPlayer.start();
      //  Log.d(TAG, "Successfully started mediaplayer");
        mediaPlayerState = STARTED;
        return mediaPlayerState;
    }

    /**
     * Pause the media player. Media player must be PLAYING before this can be called.
     * @return The media player current state.
     */
    public int Pause() {
        mediaPlayer.pause();
       // Log.d(TAG, "Media player successfully paused");
        mediaPlayerState = PAUSED;
        return mediaPlayerState;
    }

    public int Stop() {
        stoppageSeekPoint = mediaPlayer.getCurrentPosition();
        if (mediaPlayerState == STARTED) {
            mediaPlayer.stop();
        }
      //  Log.d(TAG, "Media player stopped");
        mediaPlayerState = STOPPED;
        return mediaPlayerState;
    }

    /**
     * Prepare our mediaplayer for playback.
     * @return the current state of the mediaplayer.
     */
    public synchronized int Prepare() {
        if (videoSurface == null) {
            setPrepareAndPlayWhenSurfaceSet = true;
            return mediaPlayerState;
        }

        if (datasource == null) {
            mediaPlayer.reset();
            mediaPlayerState = IDLE;
            return mediaPlayerState;
        }

        try {
       //     Log.d(TAG, "Preparing media player");
            Log.d(TAG, "Preparing video - are we on the main thread? - " + Utils.WeAreRunningOnTheUIThread());
            mediaPlayer.reset();
            mediaPlayer.setDataSource(datasource);

            mediaPlayer.prepare();
            mediaPlayerState = PREPARED;
        } catch (IOException e) {
        //    Log.e(TAG, "Failed to prepare media player.");
            e.printStackTrace();
            mediaPlayer.reset();
            mediaPlayerState = IDLE;
        }

        return mediaPlayerState;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
      //  this.mediaPlayer = mediaPlayer;
        Log.d(TAG, "PREPARED MEDIA PLAYER");
        mediaPlayerState = PREPARED;
        if (setPlayWhenPrepared) {
            Play();
        }
    }
}
