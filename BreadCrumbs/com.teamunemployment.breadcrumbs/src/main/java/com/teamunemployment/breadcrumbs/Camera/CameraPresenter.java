package com.teamunemployment.breadcrumbs.Camera;

/**
 * @author Josiah Kendall
 *
 * The presenter for our camera page.
 */
public class CameraPresenter {

    CameraViewContract viewContract;
    CameraModel model;

    public CameraPresenter(CameraViewContract viewContract, CameraModel model) {
        this.viewContract = viewContract;
        this.model = model;
    }

    public void start() {
        initCamera();
    }

    private void initCamera() {
        // Create the camera lazily.
    }


    public void onDestroy() {
    }

    public void onRestart() {
    }

    public void onResume() {
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    }
}
