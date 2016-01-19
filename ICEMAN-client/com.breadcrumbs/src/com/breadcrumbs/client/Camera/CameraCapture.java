package com.breadcrumbs.client.Camera;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.breadcrumbs.ServiceProxy.MasterProxy;
import com.breadcrumbs.caching.GlobalContainer;
import com.breadcrumbs.R;

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
        ActionBar actionBar = getActionBar();

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
