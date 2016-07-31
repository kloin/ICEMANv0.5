package com.teamunemployment.breadcrumbs.Camera;

import android.view.SurfaceHolder;

/**
 * Created by jek40 on 31/07/2016.
 */
public interface CameraSurfaceContract{

    void surfaceCreated(SurfaceHolder holder);
    void surfaceDestroyed(SurfaceHolder holder);
    void surfaceChanged(SurfaceHolder holder, int format, int w, int h);
}
