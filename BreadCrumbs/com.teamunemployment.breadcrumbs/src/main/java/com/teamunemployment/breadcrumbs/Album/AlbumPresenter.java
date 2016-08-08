package com.teamunemployment.breadcrumbs.Album;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Environment;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.teamunemployment.breadcrumbs.Album.data.FrameDetails;
import com.teamunemployment.breadcrumbs.Album.data.MimeDetails;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * @author Josiah Kendall
 */
public class AlbumPresenter implements AlbumModelPresenterContract, MediaPlayer.OnPreparedListener, TextureView.SurfaceTextureListener {
    private static final String MP4 = ".mp4";
    private static final String JPG = ".jpg";
    private AlbumPresenterViewContract view;
    private Surface videoSurface;
    private AlbumModel model;
    private MediaPlayer mediaPlayer;
    private ArrayList<MimeDetails> mimeDetailsArrayList;
    private ListIterator<MimeDetails> mimeDetailsIterator;

    public AlbumPresenter(AlbumModel model) {
        this.model = model;
        this.mediaPlayer  = new MediaPlayer();
    }


    /**
     * Start loading an album
     * @param albumId
     */
    public void Start(final String albumId) {
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
                model.StartDownloadingFrames();
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
     */
    public void Play() {
        if (mimeDetailsIterator == null) {
            mimeDetailsIterator = mimeDetailsArrayList.listIterator();
        }

        MimeDetails nextObject = mimeDetailsIterator.next();

        // Request frame. Callback has already been
        model.RequestFrame(nextObject.getId(), nextObject.getExtension());
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

    }

    private void setVideoFrame(FrameDetails frame) {
        view.setVideoVisibility(View.VISIBLE);
        view.setImageVisibility(View.INVISIBLE);
        String url = "";
        mediaPlayer = buildMediaPlayer(videoSurface, url);
    }



    private MediaPlayer playVideo(Surface surface, String filePath) {

        try {
            mediaPlayer.setDataSource(filePath);
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
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer = mp;
        mp.start();
    }

    private MediaPlayer buildMediaPlayer(Surface surface, String path) {
        final MediaPlayer mediaPlayer = new MediaPlayer();
        String filePath = Environment.getExternalStorageDirectory()
                + File.separator + "Video/Sample.mp4";
        try {
            mediaPlayer.setDataSource(filePath);
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
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        // Not sure we need this.
    }
}