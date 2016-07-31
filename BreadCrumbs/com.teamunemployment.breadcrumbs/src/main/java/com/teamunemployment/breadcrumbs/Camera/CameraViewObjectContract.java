package com.teamunemployment.breadcrumbs.Camera;

import android.graphics.Bitmap;
import android.view.TextureView;

import java.io.File;

/**
 * @author  Josiah Kendall
 */
public interface CameraViewObjectContract {

    void showMessage(String message);
    void requestPermissions(String[] permissionsArray);
    void attachCameraSurface(TextureView cameraSurface);
}
