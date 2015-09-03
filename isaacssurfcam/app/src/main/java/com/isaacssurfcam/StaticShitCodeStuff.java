package com.isaacssurfcam;

/**
 * Created by aDirtyCanvas on 8/18/2015.
 */
public class StaticShitCodeStuff {
    private static StaticShitCodeStuff instance;
    private Main mainActivity;
    private CameraController cameraController;
    private StaticShitCodeStuff() {
        // Private constructor - this is a singleton
    }


    public static StaticShitCodeStuff GetInstance() {
        if (instance == null) {
            instance = new StaticShitCodeStuff();
        }
        return instance;
    }

    public void setCameraInstance(CameraController cameraController) {
        this.cameraController = cameraController;
    }

    public CameraController getCameraController() {
        return  this.cameraController;
    }

    public void setMainActivity(Main mainActivity) {
        this.mainActivity = mainActivity;
    }

    public Main getMainActivity() {
        return this.mainActivity;
    }
}
