package com.teamunemployment.breadcrumbs;

import android.media.MediaPlayer;
import android.util.Log;

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

    // A String path to our datasource.
    private String datasource;

    // It is possible (?) that we may call play when the media player is not ready, so we need this
    // flag to indicate that we wish to play when it is ready.
    private boolean setPlayWhenPrepared = false;

    public MediaPlayerWrapper(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        mediaPlayerState = 0;
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
        if (mediaPlayerState != PREPARED) {
           // Log.d(TAG, "Media player not prepared. Will prepare now");
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
        mediaPlayer.stop();
      //  Log.d(TAG, "Media player stopped");
        mediaPlayerState = STOPPED;
        return mediaPlayerState;
    }

    /**
     * Prepare our mediaplayer for playback.
     * @return the current state of the mediaplayer.
     */
    public int Prepare() {
        try {
       //     Log.d(TAG, "Preparing media player");
            mediaPlayer.prepare();
            // in case the callback happens really fast, we need this check here so we dont set ourselves in an incorrect state.
            if (mediaPlayerState != PREPARED) {
                mediaPlayerState = PREPARING;
            }
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
        this.mediaPlayer = mediaPlayer;
        mediaPlayerState = PREPARED;
        if (setPlayWhenPrepared) {
            Play();
        }
    }
}
