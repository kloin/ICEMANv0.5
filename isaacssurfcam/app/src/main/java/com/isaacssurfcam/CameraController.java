package com.isaacssurfcam;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class CameraController extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    public Camera mCamera;
    private Boolean isPreviewRunning = false;
    private Activity context;
    private boolean backCameraOpen = true;
    private boolean video = false; // by default
    public MediaRecorder recorder;
    private boolean currentlyFilming = false;
    private String fileName;

    /*
        Default constructors for a custom surfaceView.
     */
    public CameraController(Context context) {
        super(context);
        getHolder().addCallback(this);
        this.context = (Activity) context;
    }

    public CameraController(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        getHolder().addCallback(this);
        this.context = (Activity) context;
    }

    public CameraController(Context context, AttributeSet attrs, int defStyle)  {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
        this.context = (Activity) context;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        mHolder = holder;
        mCamera = Camera.open(0); // Open rear facing by default
        StaticShitCodeStuff.GetInstance().setCameraInstance(this);

        // SetFrontCamera();
        // Get our display to test if it is going to be pushed sideways
        Display display = ((WindowManager)context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();
        int raot = display.getRotation();
        mCamera.setDisplayOrientation(90);
        Camera.Parameters parameters = mCamera.getParameters();

        Camera.Size size = getOptimalPreviewSize(parameters.getSupportedPictureSizes());
        if (size == null) {

        }
        parameters.setPictureSize(size.width, size.height);
        mCamera.setParameters(parameters);

        CheckOrientationIsNotAllFuckingRetarded(parameters, display);
        try {

            mCamera.setPreviewDisplay(holder);
            // mCamera.setDisplayOrientation(90);
            SetupCameraButtonListener();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //StaticShitCodeStuff.GetInstance().getMainActivity().startRepeatingTask();



    }
    /*
    For some reason every phone i have tested this on, android sets the default preview to be sideways.
    Why the fuck this is ever a thing i will never know.
     */
    private void CheckOrientationIsNotAllFuckingRetarded(Camera.Parameters parameters, Display display) {
        if (display.getRotation() == Surface.ROTATION_0) {
            mCamera.setDisplayOrientation(90);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        if (isPreviewRunning) {
            mCamera.stopPreview();
            isPreviewRunning = false;
        }
        mCamera.release();
        mCamera = null; // This could cause issues.
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (!isPreviewRunning) {
            mCamera.startPreview();
            try {
                Display display = ((WindowManager)context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();
                Camera.Parameters parameters = mCamera.getParameters();

                Camera.Size size = getOptimalPreviewSize(parameters.getSupportedPictureSizes());
                parameters.setPictureSize(size.width, size.height);
                mCamera.setParameters(parameters);
                mCamera.setPreviewDisplay(holder);
                CheckOrientationIsNotAllFuckingRetarded(parameters, display);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void OpenBackCamera() {
        if (isPreviewRunning) {
            mCamera.stopPreview();
            isPreviewRunning = false;
        }
        mCamera.release();
        mCamera = null;
        mCamera = Camera.open(0);
        mCamera.startPreview();
        isPreviewRunning = true;
        try {
            Display display = ((WindowManager)context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();
            Camera.Parameters parameters = mCamera.getParameters();
            CheckOrientationIsNotAllFuckingRetarded(parameters, display);
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        backCameraOpen = true;
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes) {
        // Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
        Camera.Size optimalSize = null;

        // final double ASPECT_TOLERANCE = 0.1;
        // double targetRatio = (double) height / width;

        // Try to find a size match which suits the whole screen minus the menu on the left.
        for (Camera.Size size : sizes){
            Log.d("CAM", "Checking size: width = " + size.width + ", Height = "+size.height);
            if (size.height <2000 && size.height >= 360) {
                return size;
            }
            //if (size.height != width) continue;
            //double ratio = (double) size.width / size.height;
            // if (ratio <= targetRatio + ASPECT_TOLERANCE && ratio >= targetRatio - ASPECT_TOLERANCE){
            //     optimalSize = size;
            // }
        }

        // If we cannot find the one that matches the aspect ratio, ignore the requirement.
        if (optimalSize == null) {
            // TODO : Backup in case we don't get a size.
        }

        return optimalSize;
    }

    public void SetFrontCamera() {
        if (isPreviewRunning) {
            mCamera.stopPreview();
            isPreviewRunning = false;
        }
        mCamera.release();
        mCamera = null;
        mCamera = Camera.open(1);
        mCamera.startPreview();
        isPreviewRunning = true;

        try {
            Display display = ((WindowManager)context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();
            Camera.Parameters parameters = mCamera.getParameters();

            CheckOrientationIsNotAllFuckingRetarded(parameters, display);
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        backCameraOpen = false;
    }
    // release the camera and its preview
    private void releaseCameraAndPreview() {
        mCamera.stopPreview();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
    // Set up the camera to record video
    public boolean setUpVideoCamera() {
        mCamera.lock();
        List<Camera.Size> supportedSizes = mCamera.getParameters().getSupportedVideoSizes();
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters.getFocusMode().equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        // If the supported video sizes are the same as the preview sizes, the above call returns null. No idea why anyone ever thought that was a good idea.
        if (supportedSizes == null) {
            supportedSizes = mCamera.getParameters().getSupportedPreviewSizes();
        }
        mCamera.setParameters(parameters);

        mCamera.unlock();
        recorder = new MediaRecorder();
        recorder.setCamera(mCamera);
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);

        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        if (backCameraOpen) {
            recorder.setOrientationHint(90);
        } else {
            recorder.setOrientationHint(270);
        }
        recorder.setVideoEncodingBitRate(6000000);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        Camera.Size size = getOptimalVideoSize(supportedSizes);
        // size = supportedSizes.get(0);
        recorder.setVideoSize(size.width, size.height);

        // Mp4 makes file size significantly smaller.
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        recorder.setVideoFrameRate(30);

        fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

        fileName += "/test.mp4"; //
        File fileTest = new File(fileName);
        if (fileTest.exists()) {
            fileTest.delete();
        }
        recorder.setOutputFile(fileName);
        recorder.setPreviewDisplay(mHolder.getSurface());
        try{
            recorder.prepare();
        } catch (Exception e) {
            Log.d("RECORDER","Recorder failed to prepare");
            e.printStackTrace();
            String message = e.getMessage();
            recorder.release();
            recorder = null;
            return false;
        }
        recorder.start();
        video = true;
        return video;
    }

    private Camera.Size getOptimalVideoSize(List<Camera.Size> sizes) {
        // Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
        Camera.Size optimalSize = null;

        for (Camera.Size size : sizes) {
            Log.d("CAM", "Checking size: width = " + size.width + ", Height = "+size.height);
            if (size.height <=1920) {
                return size;
            }
        }

        if (optimalSize == null) {
            return sizes.get(sizes.size()-2); // random number really
        }

        return optimalSize;
    }

    public boolean disableVideoCamera() {
        //
        recorder.reset();
        recorder.release();
        mCamera.lock();
        mCamera.stopPreview();
        video = false;
        return true;
    }

    /*
        Listener for when the user takes a photo.
     */
    public void SetupCameraButtonListener() {

    }

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            System.out.println("onPictureTaken - raw");
        }
    };

    /** Handles data for jpeg picture */
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize = 0;
            Bitmap bm=BitmapFactory.decodeByteArray(data,0,data.length,options);
            if (backCameraOpen) {

                if (90 != 0 && bm != null) {
                    Matrix m = new Matrix();

                    m.setRotate(90, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
                    try {
                        Bitmap b2 = Bitmap.createBitmap(
                                bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                        if (bm != b2) {
                            bm.recycle();
                            bm = b2;
                        }
                    } catch (OutOfMemoryError ex) {
                        throw ex;
                    }
                }
            }
            // Otherwise its a front cam shot, rotate the other way.
            else {
                if (bm != null) {
                    Matrix m = new Matrix();

                    m.setRotate(270, (float) bm.getWidth()/2, (float) bm.getHeight() / 2);
                    try {
                        Bitmap b2 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                        if (bm != b2) {
                            bm.recycle();
                            bm = b2;
                        }

                        // Our images keep being flipped?
                        Matrix flipHorizontalMatrix = new Matrix();
                        flipHorizontalMatrix.setScale(-1,1);
                        flipHorizontalMatrix.postTranslate(bm.getWidth(),0);
                        bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), flipHorizontalMatrix, true);
                    } catch (OutOfMemoryError ex) {
                        throw ex;
                    }
                }
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            try {
                File image = File.createTempFile(
                        imageFileName,  /* prefix */
                        ".jpg",         /* suffix */
                        storageDir      /* directory */
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
           String url = MediaStore.Images.Media.insertImage(context.getContentResolver(), bm, "titties" , "D");
            // Cache our photo.
          /*  Intent save = new Intent();
            save.setClassName("com.breadcrumbs.client", "com.breadcrumbs.client.tabs.SaveEventFragment");
            context.startActivity(save);*/
        }
    };

    static final int REQUEST_VIDEO_CAPTURE = 1;

/*
    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }*/
}