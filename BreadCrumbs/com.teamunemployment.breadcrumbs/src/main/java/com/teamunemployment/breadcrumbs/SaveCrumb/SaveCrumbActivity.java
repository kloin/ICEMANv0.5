package com.teamunemployment.breadcrumbs.SaveCrumb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
public class SaveCrumbActivity extends AppCompatActivity implements SaveCrumbActivityContract.ICrumbDisplay, TextWatcher, View.OnTouchListener {

    public static final String TAG = "SaveCrumbActivity";
    public int _xDelta = 0;
    public int _yDelta = 0;
    public Point size;
    @Bind(R.id.done_button) FloatingActionButton saveCrumbFab;
    @Bind(R.id.place_name) TextView placeNameTextView;
    @Bind(R.id.media) ImageView mediaView;
    @Bind(R.id.loading_place) ProgressBar loadingPlaceProgress;
    @Bind(R.id.description_floating) EditText floatingDescription;
    @Bind(R.id.root) RelativeLayout root;
    @Bind(R.id.description_floating_cover) TextView floatingDescriptionCover;

    public SaveCrumbModel model;
    public SaveCrumbPresenter presenter;
    private boolean backCameraOpen = false;
    // Contract to load and set the bitmap.
    private SaveCrumbModel.MediaLoader loaderContract = new SaveCrumbModel.MediaLoader() {
        @Override
        public void loadMedia() {
            Bitmap bitmap = model.loadBitmap();
            presenter.setBitmapDisplay(bitmap);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_screen);
        ButterKnife.bind(this);
        backCameraOpen = getIntent().getBooleanExtra("IsBackCameraOpen", true);

        // Create presenter
        presenter = new SaveCrumbPresenter(this);

        Display dm = getWindowManager().getDefaultDisplay();
        size = new Point();
        dm.getSize(size);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) floatingDescription.getLayoutParams();
        _xDelta = (int) (size.x * 0.4);
        _yDelta = (int) (size.y * 0.4);
        layoutParams.leftMargin = (int) (size.x * 0.4);
        layoutParams.topMargin =  (int) (size.y * 0.4);
        layoutParams.rightMargin = -250;
        layoutParams.bottomMargin = -250;
        floatingDescription.setLayoutParams(layoutParams);
        floatingDescriptionCover.setLayoutParams(layoutParams);
        floatingDescription.addTextChangedListener(this);
        floatingDescriptionCover.setOnTouchListener(this);
        floatingDescription.setOnTouchListener(this);
        LoadModel();
    }

    public void LoadModel() {
        CrumbToSaveDetails crumbToSaveDetails = new CrumbToSaveDetails(!backCameraOpen, "L", true);
        SimpleGps simpleGps = new SimpleGps(this);
        model = new SaveCrumbModel(simpleGps, crumbToSaveDetails, presenter);
        model.load(loaderContract);
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
        floatingDescription.setText(description);
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setEditTextEnabled(boolean enabled) {
        if (enabled) {
            floatingDescription.requestFocus();
            showKeyboard();
        } else {
            floatingDescription.clearFocus();
            hideKeyboard();
        }
        floatingDescription.setEnabled(enabled);
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

    @OnClick(R.id.media) void SetReadOnlyMode() {
        if (floatingDescription.isEnabled()) {
            presenter.toggleEditText();
        }
    }

    @OnClick(R.id.set_edit_text) void ToggleEditText() {

        presenter.toggleEditText();
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


    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        // TEST SHIT
//        Log.d("test", "Y : " + view.getY() + ", X :" + view.getX());
//        Log.d("test", "X : " + X + ", Y :" + Y);
//        Log.d("pos", "X : " + (float) X/size.x + "Y" + ": " + (float) Y/size.y);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                _xDelta = X - lParams.leftMargin;
                _yDelta = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                int resultX =  X - _xDelta;
                int resultY = Y - _yDelta;

                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

                layoutParams.leftMargin =resultX;
                layoutParams.topMargin = resultY;
                layoutParams.rightMargin = -250;
                layoutParams.bottomMargin = -250;
                floatingDescription.setLayoutParams(layoutParams);
                floatingDescriptionCover.setLayoutParams(layoutParams);
                float percentX = (float) resultX / size.x;
                float percentY = (float) resultY / size.y;
                model.setDescriptionPosition(percentX, percentY);
                break;
        }
        root.invalidate();

        // If we are using our edit text, we dont want to consume the touch event other wise our keyboard wont appear.
        if (floatingDescription.isEnabled()) {
            return false;
        }
        return true;
    }

    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = getCurrentFocus();
        if (v != null)
            imm.showSoftInput(v, 0);
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = getCurrentFocus();
        if (v != null)
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
