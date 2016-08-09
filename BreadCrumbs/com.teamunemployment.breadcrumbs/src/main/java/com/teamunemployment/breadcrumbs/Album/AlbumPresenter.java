package com.teamunemployment.breadcrumbs.Album;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.teamunemployment.breadcrumbs.Album.data.FrameDetails;
import com.teamunemployment.breadcrumbs.Album.data.MimeDetails;
import com.teamunemployment.breadcrumbs.BreadcrumbsTimer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.inject.Inject;

/**
 * @author Josiah Kendall
 */
public class AlbumPresenter implements AlbumModelPresenterContract, MediaPlayer.OnPreparedListener,
        TextureView.SurfaceTextureListener, BreadcrumbsTimer.TimerCompleteListener {
    private static final String MP4 = ".mp4";
    private static final String JPG = ".jpg";
    private AlbumPresenterViewContract view;
    private Surface videoSurface;
    private AlbumModel model;
    private MediaPlayer mediaPlayer;
    private ArrayList<MimeDetails> mimeDetailsArrayList;
    private ListIterator<MimeDetails> mimeDetailsIterator;
    private TextureView videoTextureView;
    private Context context;

    @Inject
    public AlbumPresenter(AlbumModel model, Context context) {
        this.context = context;
        this.model = model;
        this.mediaPlayer  = new MediaPlayer();
    }

    public void setVideoSurface(TextureView surface) {
        videoTextureView = surface;
        videoTextureView.setSurfaceTextureListener(this);
    }
    /**
     * Start loading an album
     * @param albumId
     */
    public void Start(final String albumId) {
        model.setContract(this);
        if (view == null) {
            throw new NullPointerException("Cannot start presenter before view has been set. Use SetView() to set");
        }

        if (albumId == null) {
            throw new NullPointerException("Album Id must not be null");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                mimeDetailsArrayList = model.LoadMimeDetails(albumId);
                if (mimeDetailsIterator == null) {
                    if (mimeDetailsArrayList == null || mimeDetailsArrayList.size() == 0) {
                        view.showMessage("Unable to play.");
                        return;
                    }
                    mimeDetailsIterator = mimeDetailsArrayList.listIterator();


                }
                // If i play without calling next
                if (!mimeDetailsIterator.hasNext()) {
                    // mimeDetailsIterator.next();
                    return;
                }

                Play(mimeDetailsIterator.next());

                model.StartDownloadingFrames();
                //Play();
            }
        }).start();
    }
    /**
     * Set the view contract for our presenter
     * @param view
     */
    public void SetView(AlbumPresenterViewContract view) {
        this.view = view;
    }

    /**
     * Start the play of the album.
     * @param next
     */
    public void Play(MimeDetails next) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                // We play the first one by default, without calling next, so this just gets us past that first one.
                if (mimeDetailsIterator.hasNext()) {

                    MimeDetails nextObject = mimeDetailsIterator.next();

                    // Request frame. Callback has already been
                    model.RequestFrame(nextObject.getId(), nextObject.getExtension());
                } else {
                    view.showMessage("Finished");
                }
            }
        }).start();
    }

    public void Pause() {
        mediaPlayer.pause();
    }

    public void Stop() {

    }

    @Override
    public void setFrame(FrameDetails frame) {
        if (frame.getExtension().equals(MP4)) {
            setVideoFrame(frame);
        } else {
            setImageFrame(frame);
        }
    }

    private void setImageFrame(FrameDetails frame) {

        view.setImageVisibility(View.VISIBLE);
        view.setVideoVisibility(View.INVISIBLE);
        view.setImageUrl(frame.getId());

        // Set timer
        BreadcrumbsTimer timer = new BreadcrumbsTimer(5000, this);
        timer.Start();
    }

    private void setVideoFrame(FrameDetails frame) {
        view.setVideoVisibility(View.VISIBLE);
        view.setImageVisibility(View.INVISIBLE);
        String url = context.getExternalCacheDir().getAbsolutePath() + "/"+frame.getId() + frame.getExtension();
        mediaPlayer = buildMediaPlayer(videoSurface, url);
        int duration = mediaPlayer.getDuration();
        BreadcrumbsTimer timer = new BreadcrumbsTimer(duration, this);
        timer.Start();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer = mp;
        mp.start();
    }

    private MediaPlayer buildMediaPlayer(Surface surface, String path) {
        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.setSurface(surface);
            mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mediaPlayer;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if (surfaceTexture!= null) {
            videoSurface = new Surface(surfaceTexture);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        // do anything here?
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        // We are not playing shit anymore, so stop downloading.
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        // Not sure we need this.
    }

    @Override
    public void onCompleted() {
        // request the next frame.
        Play(null);
    }
}