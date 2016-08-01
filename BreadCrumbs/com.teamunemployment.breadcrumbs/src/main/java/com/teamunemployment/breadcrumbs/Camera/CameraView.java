package com.teamunemployment.breadcrumbs.Camera;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;

import com.teamunemployment.breadcrumbs.App;
import com.teamunemployment.breadcrumbs.AppComponent;
import com.teamunemployment.breadcrumbs.R;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnItemLongClick;
import dagger.internal.DaggerCollections;

/**
 * @author Josiah Kendall.
 */
public class CameraView extends AppCompatActivity implements CameraViewObjectContract {

    @Inject
    CameraPresenter presenter;
    @Bind(R.id.root) CoordinatorLayout root;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.SetView(this);
        presenter.start(this);
    }

    @Override
    protected void onDestroy() {
        presenter.stop();
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        presenter.stop();
        presenter.start(this);
        super.onRestart();
    }

    @Override
    protected void onResume() {
        presenter.start(this);
        super.onResume();
    }


    /**
     * Show a snack bar to the user with the given the message.
     * @param message The message to show.
     */
    @Override
    public void showMessage(String message) {
        Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Request the permissions required for the camera.
     * @param permissionsArray
     */
    @Override
    public void requestPermissions(String[] permissionsArray) {

    }

    @Override
    public void attachCameraSurface(TextureView cameraSurface) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Back button click listener for the camera screen.
     */
    @OnClick(R.id.back_button_capture) void goBack() {
        // clean up resources, stop recording if required
        // finsih activity.
    }

    /**
     * Click handler for the camera button. This has to handle both recording start/stop and photos.
     */
    @OnClick(R.id.camera_action_button) void cameraButtonClickHandler() {
        // presenter.handleCameraButton();

    }

    /**
     * Toggle between front and back cams.
     */
    @OnClick(R.id.font_or_back_camera) void reverseCameraButtonClickHandler() {

    }

    /**
     * Toggle the state of the camera - either photos or videos.
     */
    @OnClick(R.id.video_photo_toggle_button) void toggleVideoOrPhotoClickHandler() {
        showMessage("toggle video button");
    }

    /**
     * If we hold down the camera button we should start taking a video, like occurs in
     */
    @OnItemLongClick(R.id.camera_action_button) boolean cameraLongPressHandler() {
        showMessage("recording video");
        return true;
    }
}
