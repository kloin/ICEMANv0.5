package com.teamunemployment.breadcrumbs.Camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.TextureView;
import android.view.WindowManager;

import com.teamunemployment.breadcrumbs.PresenterForActivityContract;

import javax.inject.Inject;


/**
 * @author Josiah Kendall
 */
public class CameraModel implements TextureView.SurfaceTextureListener {
    private static final String TAG = "CameraModel";

    // Camera identifiers.
    public static final int BACK_CAMERA = 0;
    public static final int FRONT_CAMERA = 1;

    private Camera camera;
    private CameraPresenter presenter;

    @Inject
    public CameraModel(CameraPresenter presenter) {
        this.presenter = presenter;
    }

    TextureView cameraSurface;
    /**
     * Stop the camera display.
     */
    public void StopCamera() {
        if (camera != null) {
            Log.d(TAG, "Stopping camera preview");
            camera.stopPreview();
            Log.d(TAG, "Releasing camera");
            camera.release();
        }
    }

    public TextureView CreateCameraSurface(Context context) {
        Log.d(TAG, "Begin create textureView for camera");
        cameraSurface = new TextureView(context);
        cameraSurface.setSurfaceTextureListener(this);
        return cameraSurface;
    }

    // StartCamera
    public void StartCamera(SurfaceTexture surfaceTexture, int cameraType) {
        int numberOfCameras = Camera.getNumberOfCameras();
        if (numberOfCameras <= 0) {
            Log.d(TAG, "no available cameras. Cannot start camera");
            return;
        }

        camera = Camera.open(cameraType);


    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        StartCamera(surfaceTexture, 0);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        // Camera handles this, dont need to worry about it.
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        StopCamera();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        // Not sure what to do here.
    }
}
