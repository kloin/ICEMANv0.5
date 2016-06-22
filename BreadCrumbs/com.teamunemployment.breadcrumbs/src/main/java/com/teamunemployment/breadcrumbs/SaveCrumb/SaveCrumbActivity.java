package com.teamunemployment.breadcrumbs.SaveCrumb;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.teamunemployment.breadcrumbs.Location.SimpleGps;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Josiah Kendall
 */
public class SaveCrumbActivity extends AppCompatActivity implements SaveCrumbActivityContract.ICrumbDisplay, TextWatcher {

    private static final String TAG = "SaveCrumbActivity";
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

    private SaveCrumbModel model;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_screen);
        ButterKnife.bind(this);
        boolean backCameraOpen = getIntent().getBooleanExtra("IsBackCameraOpen", true);
        CrumbToSaveDetails crumbToSaveDetails = new CrumbToSaveDetails(!backCameraOpen, "L", true);

        // Create gps
        SimpleGps simpleGps = new SimpleGps(this);

        // Create presenter
        SaveCrumbPresenter presenter = new SaveCrumbPresenter(this);

        // Create model and start load.
        model = new SaveCrumbModel(simpleGps, crumbToSaveDetails, presenter);
        model.load();

        descriptionEditText.addTextChangedListener(this);
        setMaxWidthForDescriptionCard();
    }

    private void setMaxWidthForDescriptionCard() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int oneFifthMaxWidth = displayMetrics.widthPixels/10;
        descriptionEditText.setWidth(oneFifthMaxWidth*7);
        descriptionTextView.setWidth(oneFifthMaxWidth*7);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void setBitmap(final Bitmap bitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mediaView.setImageBitmap(bitmap);
            }
        });
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
        if (description.isEmpty()) {
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
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        Log.d(TAG, "beforeTextChanged called with char sequence: " + s);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.d(TAG, "OnTextChanged called with char sequence: "+ s.toString());
        model.setDescription(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.d(TAG, "AfterTextChanged called with char sequence: " + s);
    }
}
