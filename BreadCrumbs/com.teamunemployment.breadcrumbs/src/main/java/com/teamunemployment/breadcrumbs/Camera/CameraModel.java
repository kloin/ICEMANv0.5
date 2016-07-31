package com.teamunemployment.breadcrumbs.Camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.TextureView;

import com.teamunemployment.breadcrumbs.PresenterForActivityContract;

import javax.inject.Inject;

/**
 * Created by jek40 on 30/07/2016.
 */
public class CameraModel implements TextureView.SurfaceTextureListener {

    @Inject
    public CameraModel() {}


    TextureView cameraSurface;
    /**
     * Stop the camera display.
     */
    public void StopCamera() {

    }

    public TextureView CreateCameraSurface(Context context) {
        cameraSurface = new TextureView(context);
        cameraSurface.setSurfaceTextureListener(this);
        return cameraSurface;
    }

    public void StartCamera(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        StartCamera(surfaceTexture, i, i1);
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
