package com.teamunemployment.breadcrumbs.Camera;

import android.graphics.Bitmap;

import java.io.File;

/**
 * @author  JOsiah Kendall
 */
public interface CameraViewContract {

    void showMessage(String message);
    void setImage(Bitmap image);
    void setVideo(File file);
    void requestPermissions(String[] permissionsArray);


}
