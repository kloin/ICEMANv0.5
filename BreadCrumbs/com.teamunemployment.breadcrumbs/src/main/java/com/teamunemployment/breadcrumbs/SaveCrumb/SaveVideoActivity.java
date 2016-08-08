package com.teamunemployment.breadcrumbs.SaveCrumb;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.teamunemployment.breadcrumbs.Location.SimpleGps;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Josiah Kendall.
 */
public class SaveVideoActivity extends SaveCrumbActivity implements SaveCrumbActivityContract.ICrumbDisplay,
        TextWatcher, TextureView.SurfaceTextureListener {


    @Bind(R.id.media_container) RelativeLayout mediaContainer;
    private String filePath;
    private TextureView videoContainer;

    // Contract to load and set the bitmap.
    private SaveCrumbModel.MediaLoader loaderContract = new SaveCrumbModel.MediaLoader() {
        @Override
        public void loadMedia() {
            // Do nothing - not loading a bitmap.
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filePath = getIntent().getExtras().getString("videoUrl");
        setUpTextureView();
    }

    @Override
    public void LoadModel() {
        CrumbToSaveDetails crumbToSaveDetails = new CrumbToSaveDetails(false, "L", false);
        SimpleGps simpleGps = new SimpleGps(this);
        model = new SaveCrumbModel(simpleGps, crumbToSaveDetails, presenter);
        model.load(loaderContract);
    }

    private void setUpTextureView() {
        videoContainer = new TextureView(this);
        videoContainer.setSurfaceTextureListener(this);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mediaContainer.addView(videoContainer);
            }
        }, 100);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if (surfaceTexture!= null) {
            Surface surface = new Surface(surfaceTexture);
            buildMediaPlayer(surface);
        }
    }

    private void buildMediaPlayer(Surface surface) {
        final MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.setSurface(surface);
            mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            mediaPlayer.prepare();
            MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    mp.setLooping(true);

                }
            };

            mediaPlayer.setOnPreparedListener(onPreparedListener);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        surfaceTexture.release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }
}
