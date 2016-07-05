package com.teamunemployment.breadcrumbs.client;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.Releasable;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Policy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CameraController extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Boolean isPreviewRunning = false;
    private Activity context;
    private boolean backCameraOpen = true;
    private boolean video = false; // by default
    private MediaRecorder recorder;
    private boolean currentlyFilming = false;
    private String fileName;
    private int cameraWidth;
    private int cameraHeight;

    private int videoTimer = 0;
    private final static int FRONT_FACING_CAM = 0;
    private final static int SELFIE_CAM = 1;
    private final static int VIDEO = 1;
    private final static int PHOTO = 0;
    private int CAMERA_MODE = VIDEO;
    private boolean TAKING_PHOTO = false;
    private LocationManager locationManager;
    private ArrayList<Location> locations = new ArrayList<>();
    private Timer t;

    private float aspectRatio = 0.000000f;
    /*
        Default constructors for a custom surfaceView.
     */
    public CameraController(Context context) {
        super(context);
        getHolder().addCallback(this);
        this.context = (Activity) context;
        videoTimer= 0;
    }

    public CameraController(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        getHolder().addCallback(this);
        this.context = (Activity) context;
        videoTimer= 0;
    }

    public CameraController(Context context, AttributeSet attrs, int defStyle)  {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
        this.context = (Activity) context;
        videoTimer= 0;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        float width = size.x;
        float height = size.y;

        aspectRatio = width/height;
        Log.d("CAM", "Aspect ratio is : " + aspectRatio);
        setUpCamera(holder);
    }

    private void setUpCamera(SurfaceHolder holder) {
        TAKING_PHOTO = false;

        // Set the height of the camera to be the same as the width so we get a square.
        mHolder = holder;
        videoTimer = 0;
        if (mCamera != null) {
            mCamera.release();
        }
        if (backCameraOpen) {
            mCamera = Camera.open(0); // Open rear facing by default
        }
        else {
            mCamera = Camera.open(1);
        }
        Display display = ((WindowManager)context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();
        int raot = display.getRotation();
        mCamera.setDisplayOrientation(90);
        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size size = getOptimalPreviewSize(parameters.getSupportedPictureSizes());
        Camera.Size preview = getOptimalPreviewSize(parameters.getSupportedPreviewSizes());
        if (parameters.getFocusMode() == null || parameters.getFocusMode().equals("auto")) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        // Store this for later reference
        cameraHeight = size.height;
        cameraWidth = size.width;
        parameters.setPreviewSize(size.width, size.height);
        parameters.setPictureSize(size.width, size.height);
        mCamera.setParameters(parameters);
        CheckOrientationIsNotAllFuckingRetarded(parameters, display);
        try {
            mCamera.setPreviewDisplay(holder);
            // mCamera.setDisplayOrientation(90);
            SetupCameraButtonListener();
            setUpSwitchCameraListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    For some reason every phone i have tested this on, android sets the default preview to be sideways.
    Even when activity is limited to portrait.
    Why the fuck this is ever a thing i will never know.

    This method seems really dodgey, but its been tested on a lot of phones and it seems pretty sweet.
     */
    private void CheckOrientationIsNotAllFuckingRetarded(Camera.Parameters parameters, Display display) {
        if (display.getRotation() == Surface.ROTATION_0) {
            mCamera.setDisplayOrientation(90);
        }
    }

    private void setUpSwitchCameraListener() {
        //If we are back camera, change to front camera.
        ImageButton switchButton = (ImageButton) context.findViewById(R.id.font_or_back_camera);
        switchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (backCameraOpen) {
                    SetFrontCamera();
                } else {
                    OpenBackCamera();
                }
            }
        });

        //otherwise, if we are front camera change to back
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        if (mCamera != null) {
            Log.d("Camera", "Shutting down camera");
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            isPreviewRunning = false;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (!isPreviewRunning) {
            mCamera.startPreview();
            try {
                Display display = ((WindowManager)context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();
                Camera.Parameters parameters = mCamera.getParameters();
                Camera.Size size = getOptimalPreviewSize(parameters.getSupportedPictureSizes());
                cameraHeight = size.height;
                cameraWidth = size.width;
                if (parameters.getFocusMode() == null) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }

                parameters.setPictureSize(size.width, size.height);
                mCamera.setParameters(parameters);
                mCamera.setPreviewDisplay(holder);
                CheckOrientationIsNotAllFuckingRetarded(parameters, display);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes) {
        // Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
        Camera.Size optimalSize = null;

        // Try to find a size match which suits the whole screen minus the menu on the left.
        for (Camera.Size size : sizes) {
            // TODO : Make camera work for phones that have different orientations.
            float height = size.height;
            float width = size.width;
            float sizeAR = height / width;
            Log.d("CAM", "Checking size: width = " + size.width + ", Height = "+size.height);
            if (size.height <1000 && size.height >= 720 && sizeAR == aspectRatio) {

                // Logging in relation to the aspect ratio
                Log.d("Camera Size", "Chosen size is - Height: " + size.height + " Width: " + size.width );
                return size;
            }
        }

        // If we cannot find the one that matches the aspect ratio, ignore the requirement.
        if (optimalSize == null) {
            // grab the biggest size that we have then
            return sizes.get(0);
        }

        return optimalSize;
    }

    private Camera.Size getOptimalVideoSize(List<Camera.Size> sizes) {
        // Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
        Camera.Size optimalSize = null;

        for (Camera.Size size : sizes) {
            Log.d("CAM", "Checking size: width = " + size.width + ", Height = "+size.height);
            if (size.height <500) {
                return size;
            }
        }

        if (optimalSize == null) {
            // TODO : Backup in case we don't get a size.
            return sizes.get(sizes.size()-2); // random number really
        }

        return optimalSize;
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
        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size size = getOptimalPreviewSize(parameters.getSupportedPictureSizes());
        cameraHeight = size.height;
        cameraWidth = size.width;
        if (parameters.getFocusMode() == null) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        parameters.setPreviewSize(size.width, size.height);
        parameters.setPictureSize(size.width, size.height);
        mCamera.setParameters(parameters);
        isPreviewRunning = true;
        mCamera.autoFocus(this);

        try {
            Display display = ((WindowManager)context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();
            CheckOrientationIsNotAllFuckingRetarded(parameters, display);
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        backCameraOpen = true;
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
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size size = getOptimalPreviewSize(sizes);
        Camera.Size previewSize = getOptimalPreviewSize(previewSizes);
        //parameters.setJpegQuality(50);
        parameters.setPreviewSize(previewSize.width,previewSize.height);
        parameters.setPictureSize(size.width, size.height);
        mCamera.setParameters(parameters);
        isPreviewRunning = true;

        try {
            Display display = ((WindowManager)context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();
            Camera.Parameters parameters2 = mCamera.getParameters();
            CheckOrientationIsNotAllFuckingRetarded(parameters2, display);
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        backCameraOpen = false;
    }

    // Set up the camera to record video
    private boolean setUpVideoCamera() {
        // Some phones do dumb shit if the camera isnt locked. No wonder this class is depreciated.
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
        recorder = new MediaRecorder();
        mCamera.unlock();
        recorder.setCamera(mCamera);
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);

        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        if (backCameraOpen) {
            recorder.setOrientationHint(90);
        } else {
            recorder.setOrientationHint(270);
        }
        recorder.setVideoEncodingBitRate(3200000);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        Camera.Size size = getOptimalVideoSize(supportedSizes);
        recorder.setVideoSize(size.width, size.height);


        // Mp4 makes file size significantly smaller.
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        recorder.setVideoFrameRate(30);

        //recorder.setProfile(cpHigh);

        fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        int eventId = new PreferencesAPI(context).GetEventId();
        //eventId += 1;
        if (eventId == -1) {
            eventId = 0;
        }
        // I Add the +1 because later in the piece(before saving, I increment the event Id. This entire class needs a rework.
        fileName += "/"+ eventId+ ".mp4"; //
        recorder.setOutputFile(fileName);
        recorder.setPreviewDisplay(mHolder.getSurface());
        try{
            recorder.prepare();
        } catch (Exception e) {
            String message = e.getMessage();
            recorder.release();
            recorder = null;
            return false;
        }
        video = true;
        return video;
    }

    private boolean disableVideoCamera() {
        recorder.reset();
        recorder.release();
        mCamera.lock();
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        video = false;
        return true;
    }

    /*
        Listener for when the user takes a photo.
     */
    public void SetupCameraButtonListener() {
        final FloatingActionButton cameraButton = (FloatingActionButton) context.findViewById(R.id.captureButton);
        final FloatingActionButton videoButton = (FloatingActionButton) context.findViewById(R.id.videoButton);

        videoButton.setOnClickListener(new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CAMERA_MODE == VIDEO) {
                    if (currentlyFilming) {
                        Toast.makeText(context, "Cannot toggle while filming", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    CAMERA_MODE = PHOTO;
                    // Change to red
                    cameraButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
                    SimpleAnimations.ShrinkUnshrinkStandardFab(cameraButton);
                    cameraButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_camera));
                    videoButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_videocam_black_24dp));
                } else {
                    CAMERA_MODE = VIDEO;
                    // Set back to blue.
                    cameraButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));
                    SimpleAnimations.ShrinkUnshrinkStandardFab(cameraButton);
                    cameraButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_videocam_white_24dp));
                    videoButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_camera_alt_black_24dp));
                }
            }
        });

       cameraButton.setOnClickListener(new OnClickListener() {
           @Override
           public void onClick(View v) {
               // Take a photo
               if (CAMERA_MODE == PHOTO && !TAKING_PHOTO) {
                   TAKING_PHOTO = true;
                   final View flashoverlay = (View) context.findViewById(R.id.flash_overlay);
                   SimpleAnimations.FlashView(flashoverlay);
                   mCamera.takePicture(null, rawCallback, jpegCallback);
               } else if (CAMERA_MODE == VIDEO){
                   toggleVideo(cameraButton);
                   // set the color to a pressed blue color.
               }
           }

       });

        ImageButton imageButton = (ImageButton) context.findViewById(R.id.backButtonCapture);
        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                context.finish();
            }
        });
    }

    private void toggleVideo(FloatingActionButton cameraButton) {
        if (currentlyFilming) {
            t.cancel();
            cameraButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.ColorPrimary)));
            recorder.stop();
            disableVideoCamera();
            currentlyFilming = false;
            // Launch video intent to save the video.
            Intent save = new Intent();
            save.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.SaveCrumb.SaveVideoActivity");
            Bundle extras = new Bundle();
            extras.putBoolean("IsBackCameraOpen", backCameraOpen);
            save.putExtras(extras);
            save.putExtra("videoUrl", fileName); //The uri to our address
            context.startActivity(save);
        } else {
            cameraButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BBDEFB")));
            currentlyFilming = true;
            //doProgressShit(50);
            startTimer();
            // Set up our video camera. This is different from taking a photo
            setUpVideoCamera();
           // videoButton.setBackgroundColor(getResources().getColor(R.color.accent));
            recorder.start();
        }
    }

    private void startTimer() {
        t=new Timer();
        final ProgressBar progressBar = (ProgressBar) context.findViewById(R.id.video_progress);
        //progressBar.getProgressDrawable().set(Color.parseColor("#C0D000"), android.graphics.PorterDuff.Mode.SRC_ATOP);
        //progressBar.setScaleY(4f);z
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                videoTimer += 50;
                if (videoTimer < 15000) {
                    progressBar.setProgress(videoTimer);
                } else {
                    // Stop video, and go to the next page
                    stopRecorderAndOpenViewScreen();
                    t.cancel();
                }
            }
        }, 50, 50);
    }

    private void stopRecorderAndOpenViewScreen() {
        if (recorder!= null) {
            recorder.stop();
            disableVideoCamera();
            currentlyFilming = false;

            // Launch video intent to save the video.
            Intent save = new Intent();
            save.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.SaveCrumb.SaveVideoActivity");
            Bundle extras = new Bundle();
            extras.putBoolean("IsBackCameraOpen", backCameraOpen);
            save.putExtras(extras);
            save.putExtra("videoUrl", fileName);
            context.startActivity(save);
        }
    }

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            System.out.println( "onPictureTaken - raw");
        }
    };

    /** Handles data for jpeg picture */
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize = 0;
            Bitmap bm=BitmapFactory.decodeByteArray(data,0,data.length,options);

            int difference = cameraWidth -cameraHeight;
            Log.d("CAM", "Creating bitmap. Width: " + cameraWidth + " Height: " + cameraHeight + " Difference: " + difference);
            // Cache our photo.
            GlobalContainer.GetContainerInstance().SetBitMap(bm);
            Intent save = new Intent();
            Bundle extras = new Bundle();
            extras.putBoolean("IsBackCameraOpen", backCameraOpen);
            // If our location isnt null we want to pass this in as the save location.

            save.putExtras(extras);
            save.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.SaveCrumb.SaveCrumbActivity");
            context.startActivity(save);
        }
    };

    static final int REQUEST_VIDEO_CAPTURE = 1;
    private String saveToInternalStorage(Bitmap bitmap) throws IOException {
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        // Create imageDir
        File mypath=new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES.toString()+ "/profile.jpg")));

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fos.close();
        }
        return mypath.getPath();
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }
}
