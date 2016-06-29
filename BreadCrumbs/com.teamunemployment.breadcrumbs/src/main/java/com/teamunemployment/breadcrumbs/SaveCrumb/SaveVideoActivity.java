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
 * Created by jek40 on 29/06/2016.
 */
public class SaveVideoActivity extends AppCompatActivity implements SaveCrumbActivityContract.ICrumbDisplay,
        TextWatcher, TextureView.SurfaceTextureListener {

    @Bind(R.id.done_button)
    FloatingActionButton saveCrumbFab;

    @Bind(R.id.place_name)
    TextView placeNameTextView;

    @Bind(R.id.media)
    ImageView mediaView;

    @Bind(R.id.loading_place)
    ProgressBar loadingPlaceProgress;

    @Bind(R.id.description_view)
    TextView descriptionTextView;

    @Bind(R.id.description)
    EditText descriptionEditText;

    @Bind(R.id.media_container)
    RelativeLayout mediaContainer;

    private String filePath;
    private Surface videoSurface;
    private TextureView videoContainer;
    private SaveCrumbModel model;
    private SaveCrumbPresenter presenter;

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

        // Initialise
        setContentView(R.layout.add_screen);
        ButterKnife.bind(this);

        filePath = getIntent().getExtras().getString("videoUrl");

        // Create crumb saving details object
        CrumbToSaveDetails crumbToSaveDetails = new CrumbToSaveDetails(false, "L", false);

        // Create gps
        SimpleGps simpleGps = new SimpleGps(this);

        // Create presenter
        SaveCrumbPresenter presenter = new SaveCrumbPresenter(this);

        // Create model with dependencies
        model = new SaveCrumbModel(simpleGps, crumbToSaveDetails, presenter);

        // Do load
        model.load(loaderContract);

        // Add watcher for the description edit text view.
        descriptionEditText.addTextChangedListener(this);
        setMaxWidthForDescriptionCard();
        setUpTextureView();
    }

    private void setMaxWidthForDescriptionCard() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int oneFifthMaxWidth = displayMetrics.widthPixels/10;
        descriptionEditText.setWidth(oneFifthMaxWidth*7);
        descriptionTextView.setMaxWidth(oneFifthMaxWidth*7);
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
    public void setBitmap(Bitmap bitmap) {
        // Not required - we are setting a video not a bitmap
    }

    @Override
    public void setPlaceName(final String placeName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingPlaceProgress.setVisibility(View.GONE);
                placeNameTextView.setText(placeName);
            }
        });
    }

    @Override
    public void setDescription(String description) {
        if (description == null || description.isEmpty()) {
            description = "Tap to add a description.";
            descriptionTextView.setText(description);
        }
        descriptionTextView.setText(description);
        descriptionEditText.setText(description);
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

    }

    @Override
    public void setEditTextDescriptionVisibility(int visibility) {
        descriptionEditText.setVisibility(visibility);
    }

    @Override
    public void setTextViewDescriptionVisibility(int visibility) {
        descriptionTextView.setVisibility(visibility);
    }

    @OnClick(R.id.done_button) void SaveCrumb() {
        PreferencesAPI preferencesAPI = new PreferencesAPI(this);
        model.SaveCrumb(this, preferencesAPI);
        finish();
    }

    //
    @OnClick(R.id.backAddScreen) void GoBack() {
        model.CleanUp();
        finish();
    }

    @OnClick(R.id.description_view) void SetEditTextVisibileAndHideTextView() {
        model.setEditDescription();
        descriptionEditText.setEnabled(true);
    }

    @OnClick(R.id.media) void SetReadOnlyMode() {
        model.setReadOnlyDescription();
        descriptionEditText.setEnabled(false);
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        model.setDescription(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
