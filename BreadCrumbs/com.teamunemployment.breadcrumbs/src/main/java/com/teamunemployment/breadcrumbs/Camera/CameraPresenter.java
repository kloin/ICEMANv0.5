package com.teamunemployment.breadcrumbs.Camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.TextureView;

import com.teamunemployment.breadcrumbs.PresenterForActivityContract;

import javax.inject.Inject;

/**
 * @author Josiah Kendall
 *
 * The presenter for our camera page.
 */
public class CameraPresenter implements PresenterForActivityContract {

    private CameraViewObjectContract viewContract;
    private CameraModel model;

    @Inject
    public CameraPresenter(CameraModel model) {
        this.model = model;
    }

    public void SetView(CameraViewObjectContract viewContract) {
        this.viewContract = viewContract;
    }

    public void start(Context context) {
        if (viewContract == null) {
            throw new IllegalStateException("Must call SetView before calling start()");
        }
        TextureView cameraSurface = model.CreateCameraSurface(context);
        viewContract.attachCameraSurface(cameraSurface);
    }

    @Override
    public void stop() {
        model.StopCamera();
    }

    private void initCamera() {
        // Create the camera lazily.

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    }
}
