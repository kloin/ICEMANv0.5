package com.teamunemployment.breadcrumbs.Camera;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import com.teamunemployment.breadcrumbs.R;

import java.io.File;

import butterknife.OnClick;
import butterknife.OnItemLongClick;

/**
 * @author Josiah Kendall.
 */
public class CameraView extends AppCompatActivity implements CameraViewContract{

    CameraPresenter presenter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CameraModel model = new CameraModel();
        presenter = new CameraPresenter(this, model);
        presenter.start();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        presenter.onRestart();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        presenter.onResume();
        super.onResume();
    }


    /**
     * Show a snack bar to the user with the given the message.
     * @param message The message to show.
     */
    @Override
    public void showMessage(String message) {

    }

    /**
     * Set the bitmap we have to our image view and make it visible.
     * @param image
     */
    @Override
    public void setImage(Bitmap image) {

    }

    /**
     * Set the mp4 file we have to our videoview.
     * @param file
     */
    @Override
    public void setVideo(File file) {

    }

    /**
     * Request the permissions required for the camera.
     * @param permissionsArray
     */
    @Override
    public void requestPermissions(String[] permissionsArray) {

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

    }

    /**
     * If we hold down the camera button we should start taking a video, like occurs in
     */
    @OnItemLongClick(R.id.camera_action_button) boolean cameraLongPressHandler() {

        return true;
    }
}
