package com.teamunemployment.breadcrumbs.client.Camera;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.teamunemployment.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.MasterProxy;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.CameraController;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraCapture extends ActionBarActivity {
	private View rootView;
	private MasterProxy clientProxyService;
    private GlobalContainer globalContainer;
	private Camera mCamera;
	private Context context;
	BreadCrumbsFusedLocationProvider breadCrumbsFusedLocationProvider;
	private LruCache<String, Bitmap> mMemoryCache;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);

        globalContainer = GlobalContainer.GetContainerInstance();
        ImageButton cameraButton = (ImageButton) findViewById(R.id.captureButton);
        globalContainer.SetCaptureButton(cameraButton);

		// ********** POTENTIAL BATTERY ISSUE *******************************
		// I start the foreground service but I never manually stop it. Does it get destroyed with the intent?
		breadCrumbsFusedLocationProvider = new BreadCrumbsFusedLocationProvider(this);
		breadCrumbsFusedLocationProvider.StartForegroundGPSService();

        // Construction purposes.
		setBackButtonListener();
        clientProxyService = MasterProxy.GetProxyInstance();
		FrameLayout overlay = (FrameLayout) findViewById(R.id.camera_overlay);
		ViewGroup.LayoutParams overlayParams = overlay.getLayoutParams();
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		overlayParams.height = displaymetrics.widthPixels;
		overlay.setLayoutParams(overlayParams);
	}

	private void setBackButtonListener() {
		ImageView backButton = (ImageView) findViewById(R.id.backButtonCapture);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Close this activity, which will exit the screen
				finish();
			}
		});
	}

	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
            c.release();
	    	System.out.println("Fuck the camera is not letting us use it");
 	    }
	    return c; // returns null if camera is unavailable
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	//The single click to take a photo. Video not currently supported
	public void TakePhotoWithCamera() {
		//Our button via tag
        final Camera camera = getCameraInstance();
		SurfaceView photoclick = (SurfaceView) rootView.findViewById(R.id.camera_preview);
		//Camera camera = Camera
		//Listener for click
		photoclick.setOnClickListener(new OnClickListener() {
			@Override
		    public void onClick(View v) {
				//camera.takePicture(null, rawCallback, jpegCallback);
		    }
		});
		
		
	}
	
	String mCurrentPhotoPath;

	private File createImageFile() throws IOException {
	    // Create an image file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = "JPEG_" + timeStamp + "_";
	    File storageDir = Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES);
	    File image = File.createTempFile(
	        imageFileName,  /* prefix */
	        ".jpg",         /* suffix */
	        storageDir      /* directory */
	    );

	    // Save a file: path for use with ACTION_VIEW intents
	    mCurrentPhotoPath = "file:" + image.getAbsolutePath();
	    
	    return image;
	}

}
