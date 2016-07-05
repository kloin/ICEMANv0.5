package com.teamunemployment.breadcrumbs.BreadcrumbsExoPlayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;

import com.google.android.exoplayer.TimeRange;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.util.Util;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A Wrapper for the exoplayer and its surface view that it plays on. Allows us to manage and interact
 * with multiple players on the same activity.
 */
public class BreadcrumbsExoPlayerWrapper implements BreadcrumbsExoPlayer.Listener, BreadcrumbsExoPlayer.CaptionListener,
        AudioCapabilitiesReceiver.Listener {

    protected MatrixManager matrixManager = new MatrixManager();

    private final static int PLAYER_IDLE_STATE = 0;
    private final static int PLAYER_LOADING_STATE = 1;
    private final static int PLAYER_LOADED_STATE = 2;
    private final static int PLAYER_PLAYING_STATE = 3;
    private final static int PLAYER_FINISHED_STATE = 4;

    // By default we are idling
    private final static int CURRENT_PLAYER_STATE = 0;

    // Id to identify every Exoplayer instance that we have. This needs a refactor into useing just one player and multiple tracks/datasources
    private int id;
    private boolean isLoading = false;
    public TextureView VideoSurface;
    public BreadcrumbsExoPlayer player;
    public View root;
    public MediaController mediaController;
    public ExtractorRendererBuilder dataSource;

    private BreadcrumbsExoPlayer.InfoListener infoListener;
    private WrapperInterface wrapperInterface;
    private final Context context;
    private boolean playerNeedsPrepare = false;

    protected final ReentrantLock globalLayoutMatrixListenerLock = new ReentrantLock(true);

    public interface WrapperInterface {
        void stateChangedListener(boolean playWhenReady, int state, int id);
    }

    public BreadcrumbsExoPlayerWrapper(TextureView surfaceView, Context context, int id) {
        this.context = context;
        Activity tempActivity = (Activity) context;
        VideoSurface = surfaceView;
        root = tempActivity.findViewById(R.id.root);
        this.id = id;
    }

    // Entry point.
    public void StartPlaying() {
        if (player == null) {
            throw new NullPointerException("Player has not been created. You need to call BuildPlayer() before you can start playing");
        }
        player.getPlayerControl().start();
    }

    public int GetId() {
        return id;
    }

    public void StopPlaying() {
        player.getPlayerControl().pause();
    }
    public void BeginLoading(String url) {
        BuildDatasource(url);
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isPlaying() {
        return player.getPlayerControl().isPlaying();
    }

    // This is required to create the extractorRenderer so that we load the mp4.
    // WE have this in a serperate method so that we can load before we need to show the video.
    public void BuildDatasource(String url) {
        //VideoSurface.getHolder().addCallback(this);
        mediaController = new KeyCompatibleMediaController(context);
        mediaController.setAnchorView(root);

        // Build up the datasource/extractor renderer
        String userAgent = Util.getUserAgent(context, "BreadcrumbsExoplayer");
        Uri uri = Uri.parse(url);
        dataSource = new ExtractorRendererBuilder(context,userAgent, uri, false);
    }

    public void BuildLocalDatasource(String url) {
        //VideoSurface.getHolder().addCallback(this);
        mediaController = new KeyCompatibleMediaController(context);
        mediaController.setAnchorView(root);

        // Build up the datasource/extractor renderer
        String userAgent = Util.getUserAgent(context, "BreadcrumbsExoplayer");
        Uri uri = Uri.parse(url);
        dataSource = new ExtractorRendererBuilder(context,userAgent, uri, true);
    }

    public void BuildPlayerAndSeek(boolean playWhenReady, int time) {
        if (dataSource == null) {
            throw new NullPointerException("Cannot build player because the datasource was not yet built. Build a datasource with BeginLoading()");
        }

        if (player == null) {
            player = new BreadcrumbsExoPlayer(dataSource, this);
            if (infoListener != null) {
                player.setInfoListener(infoListener);
            }
            player.addListener(this);
            player.setCaptionListener(this);
            player.seekTo(time);
            playerNeedsPrepare = true;
            mediaController.setMediaPlayer(player.getPlayerControl());
            mediaController.setEnabled(true);
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
        }
        player.seekTo(time);
        player.setSurface(new Surface(VideoSurface.getSurfaceTexture()));
        // player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(playWhenReady);
    }

    // We are just preparing here. We dont start until it is requested
    public void buildPlayer(boolean playWhenReady) {

        // We have to have a datasource before we can begin playing
        if (dataSource == null) {
            throw new NullPointerException("Cannot build player because the datasource was not yet built. Build a datasource with BeginLoading()");
        }

        if (player == null) {
            VideoSurface.setVisibility(View.VISIBLE);
            player = new BreadcrumbsExoPlayer(dataSource, this);
            if (infoListener != null) {
                player.setInfoListener(infoListener);
            }
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
        if (VideoSurface.getSurfaceTexture() == null) {
            Log.d("Exoplayer", "Video surface was null for some reason. This might be getting caused by opening a video as we close.");
            return;
        }
        player.setSurface(new Surface(VideoSurface.getSurfaceTexture()));
        // player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(playWhenReady);
    }

    public void setInfoListenerforLoading(BreadcrumbsExoPlayer.InfoListener infoListener) {
        this.infoListener = infoListener;
    }

    public void setWrapperInterface(WrapperInterface wrapperInterface) {
        this.wrapperInterface = wrapperInterface;
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (wrapperInterface!= null) {
            wrapperInterface.stateChangedListener(playWhenReady, playbackState, id);
        }
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
       // VideoSurface.setRotation(unappliedRotationDegrees);
        if (unappliedRotationDegrees == 0) {
            return;
        }
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        String TAG = "VideoView.Size()";
        Point size = new Point();
        display.getSize(size);
        final Matrix mtx=new Matrix();
        RectF src = new RectF(0, 0, width, height);
        RectF dst = new RectF(0, 0, VideoSurface.getWidth(), VideoSurface.getHeight());
        RectF screen = new RectF(dst);
        mtx.postRotate(unappliedRotationDegrees, screen.centerX(), screen.centerY());
        mtx.mapRect(dst);

        mtx.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
        mtx.mapRect(src);

        mtx.setRectToRect(screen, src, Matrix.ScaleToFit.FILL);
        mtx.postRotate(unappliedRotationDegrees, screen.centerX(), screen.centerY());
//        String TAG = "VideoView.Size()";
//        Point size = new Point();
//        display.getSize(size);
//        final Matrix mtx=new Matrix();
////        RectF drawableRect = new RectF(0, 0, size.x, size.y);
////        RectF viewRect = new RectF(0, 0, size.x, size.y);
////        mtx.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.START);
//        float scalex = (float) width / (float) height;
//        float scaley = (float) height / (float) width;
//        //mtx.preTranslate(-1, 1);
//        mtx.postRotate(unappliedRotationDegrees,size.y/2, size.x/2);
//        mtx.postScale(scaley, scalex, 0, size.y);
        Activity activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                VideoSurface.setTransform(mtx);
            }
        });
        //setScaleType(ScaleType.CENTER_CROP);
//
//        Matrix matrix = new Matrix();
//        matrix.setRotate(unappliedRotationDegrees);
//        VideoSurface.setTransform(matrix);
////
//
//        params.height = 1820;
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        display.getRealMetrics(displayMetrics);
//        Log.d(TAG, "Screen Width:" + size.x);
//        Log.d(TAG, "Screen WIdth:" + size.y);
//        Log.d(TAG, "densityDpi" + displayMetrics.densityDpi);
//        Log.d(TAG, "density" + displayMetrics.density);
//        Log.d(TAG, displayMetrics.xdpi + ": xdpi");
//        Log.d(TAG, displayMetrics.ydpi + ": ydpi");
//        Log.d(TAG, "widthPixels" + displayMetrics.widthPixels);
//        Log.d(TAG, "heightPixels" + displayMetrics.heightPixels);
//        Log.d(TAG, "scaledDensity" + displayMetrics.scaledDensity);
//
//        VideoSurface.setMinimumHeight(size.y);
//
//        params.width = size.x;
//        params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
//        Log.d(TAG, "VideoView wisth: "+ width *5);
//        Log.d(TAG, "VideoView height: "+ height *5);
////        params.height = height*4;
////        params.width = width*4;
//        VideoSurface.setLayoutParams(params);

//        VideoSurface.setMinimumHeight(size.x);
//        VideoSurface.setMinimumWidth(size.y);
    }

    private void updateMatrixOnLayout() {
        GlobalLayoutMatrixListener globalLayoutMatrixListener = new GlobalLayoutMatrixListener();
        globalLayoutMatrixListenerLock.lock();

        VideoSurface.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutMatrixListener);


        globalLayoutMatrixListenerLock.unlock();
    }

    @Override
    public void onCues(List<Cue> cues) {

    }

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {

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

    public void setScaleType(@NonNull ScaleType scaleType) {
        matrixManager.scale(VideoSurface, scaleType);
    }

    private class GlobalLayoutMatrixListener implements ViewTreeObserver.OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {
            // Updates the scale to make sure one is applied
            setScaleType(matrixManager.getCurrentScaleType());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                VideoSurface.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            } else {
                //noinspection deprecation
                VideoSurface.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        }
    }
}
