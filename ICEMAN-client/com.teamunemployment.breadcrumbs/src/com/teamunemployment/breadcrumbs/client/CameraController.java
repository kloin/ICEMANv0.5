package com.teamunemployment.breadcrumbs.client;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.Releasable;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraController extends SurfaceView implements SurfaceHolder.Callback {
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
        // Set the height of the camera to be the same as the width so we get a square.

        mHolder = holder;
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

        // Store this for later reference
        cameraHeight = size.height;
        cameraWidth = size.width;
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
                cameraHeight = size.height;
                cameraWidth = size.width;
                parameters.setPictureSize(size.width, size.height);
                mCamera.setParameters(parameters);
                mCamera.setPreviewDisplay(holder);
                CheckOrientationIsNotAllFuckingRetarded(parameters, display);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes)
    {
        // Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
        Camera.Size optimalSize = null;

       // final double ASPECT_TOLERANCE = 0.1;
       // double targetRatio = (double) height / width;

        // Try to find a size match which suits the whole screen minus the menu on the left.
        for (Camera.Size size : sizes){
            Log.d("CAM", "Checking size: width = " + size.width + ", Height = "+size.height);
            if (size.height <1000 && size.height >= 720) {
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
        parameters.setPictureSize(size.width, size.height);
        mCamera.setParameters(parameters);
        // parameters.setPictureSize(1280, 720);
        //parameters.setPictureFormat(format);
        mCamera.setParameters(parameters);
        isPreviewRunning = true;

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
        Camera.Size size = getOptimalPreviewSize(sizes);
        //parameters.setJpegQuality(50);
        parameters.setPreviewSize(size.width,size.height);
        parameters.setPictureSize(size.width,size.height);
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

    // release the camera and its preview
    private void releaseCameraAndPreview() {
        mCamera.stopPreview();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    // Set up the camera to record video
    private boolean setUpVideoCamera() {
        List<Camera.Size> supportedSizes = mCamera.getParameters().getSupportedVideoSizes();
        recorder = new MediaRecorder();
        mCamera.unlock();
        recorder.setCamera(mCamera);

        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        //recorder.setVideoSize(640, 480);

        recorder.setVideoEncodingBitRate(3000000);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setVideoFrameRate(16); //might be auto-determined due to lighting
        recorder.setVideoSize(supportedSizes.get(8).width, supportedSizes.get(8).height);

        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);

        fileName =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        fileName += "/test.mp4";
        recorder.setOutputFile(fileName);

        recorder.setOrientationHint(90);
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

        final ImageButton cameraButton = (ImageButton) context.findViewById(R.id.captureButton);
        cameraButton.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                //currentlyFilming = true;
                // Set up our video camera. This is different from taking a photo
                //setUpVideoCamera();
                //recorder.start();
                Toast.makeText(context, "Video recording currently disabled",Toast.LENGTH_LONG ).show();
                return true;
            }
        });

       cameraButton.setOnClickListener(new OnClickListener() {
           @Override
           public void onClick(View v) {
               // Take a photo

               mCamera.takePicture(null, rawCallback, jpegCallback);
               //mCamera.stopPreview();
           }

       });

        cameraButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && currentlyFilming) {
                    recorder.stop();
                    disableVideoCamera();
                    currentlyFilming = false;

                    // Launch video intent to save the video.
                    Intent save = new Intent();
                    save.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.tabs.SaveVideoActivity");
                    save.putExtra("videoUrl", fileName); //The uri to our address
                    context.startActivity(save);
                }
                // return false so that we are not consuming the event and our onclick still works
                return false;
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

            //ByteArrayOutputStream os = new ByteArrayOutputStream();
            //bm.compress(Bitmap.CompressFormat.JPEG, 100, os);
           // byte[] array = os.toByteArray();
           // bm = BitmapFactory.decodeByteArray(array, 0, array.length);
             //if (bm.getWidth() > 720 && bm.getHeight() > 1100 && backCameraOpen) {
            int difference = cameraWidth -cameraHeight;
            Log.d("CAM", "Creating bitmap. Width: " + cameraWidth + " Height: " + cameraHeight + " Difference: " + difference);
            bm = Bitmap.createBitmap(bm, 0, 0, 720, 720);
           // bm = Bitmap.createScaledBitmap(bm, 400, 400, true);
            // }
            // Cache our photo.
            GlobalContainer.GetContainerInstance().SetBitMap(bm);
            Intent save = new Intent();
            save.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.tabs.SaveEventFragment");
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

/*
    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }*/
}
