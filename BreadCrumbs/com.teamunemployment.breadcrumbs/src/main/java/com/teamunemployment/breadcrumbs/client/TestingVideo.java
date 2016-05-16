package com.teamunemployment.breadcrumbs.client;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;

import com.devbrackets.android.exomedia.EMVideoView;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.util.DebugTextViewHelper;
import com.google.android.exoplayer.util.Util;
import com.teamunemployment.breadcrumbs.BreadcrumbsExoPlayer.BreadcrumbsExoPlayer;
import com.teamunemployment.breadcrumbs.BreadcrumbsExoPlayer.ExtractorRendererBuilder;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;

import java.util.List;

/**
 * Created by jek40 on 1/05/2016.
 */
public class TestingVideo  extends Activity implements SurfaceHolder.Callback,
        BreadcrumbsExoPlayer.Listener, BreadcrumbsExoPlayer.CaptionListener,
        AudioCapabilitiesReceiver.Listener{

    private boolean playerNeedsPrepare;
    private BreadcrumbsExoPlayer player;
    private Context context;
    private SurfaceTexture surfaceTexture;
    private MediaController mediaController;
    private SurfaceView surfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_test);
        View root = findViewById(R.id.root);
        context = this;
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);
        mediaController = new KeyCompatibleMediaController(this);
        mediaController.setAnchorView(root);
        preparePlayer(true);
    }

    private void preparePlayer(boolean playWhenReady) {
        if (player == null) {
            String userAgent = Util.getUserAgent(this, "BreadcrumbsExoplayer");
            Uri uri = Uri.parse(LoadBalancer.RequestCurrentDataAddress() + "/images/5073.mp4");
            player = new BreadcrumbsExoPlayer(new ExtractorRendererBuilder(context,userAgent, uri, false));
            player.addListener(this);
            player.setCaptionListener(this);
            player.seekTo(0);
            playerNeedsPrepare = true;
            mediaController.setMediaPlayer(player.getPlayerControl());
            mediaController.setEnabled(true);
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
        }
       // player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(playWhenReady);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (player != null) {
            player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Not going to happen I dont think.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null) {
            player.blockingClearSurface();
        }
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

    }

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {

    }

    @Override
    public void onCues(List<Cue> cues) {

    }

    private static final class KeyCompatibleMediaController extends MediaController {

        private MediaController.MediaPlayerControl playerControl;
        public KeyCompatibleMediaController(Context context) {
            super(context);
        }

        @Override
        public void setMediaPlayer(MediaController.MediaPlayerControl playerControl) {
            super.setMediaPlayer(playerControl);
            this.playerControl = playerControl;
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            int keyCode = event.getKeyCode();
            if (playerControl.canSeekForward() && keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    playerControl.seekTo(playerControl.getCurrentPosition() + 15000); // milliseconds
                    show();
                }
                return true;
            } else if (playerControl.canSeekBackward() && keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    playerControl.seekTo(playerControl.getCurrentPosition() - 5000); // milliseconds
                    show();
                }
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
    }
}
