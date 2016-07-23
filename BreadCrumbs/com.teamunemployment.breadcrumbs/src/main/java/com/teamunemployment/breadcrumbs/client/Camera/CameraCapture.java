package com.teamunemployment.breadcrumbs.client.Camera;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.teamunemployment.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.CameraController;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CameraCapture extends AppCompatActivity {

	private static final int CAMERA_REQUESTED_PERMISSION = 9;
	private LocationManager locationManager;
	private ArrayList<Location> locations = new ArrayList<>();

	@Bind(R.id.camera_holder)
	RelativeLayout cameraHolder;

	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_activity);
		ButterKnife.bind(this);
		context = this;
		setBackButtonListener();
		String [] permissionsRequired = checkCameraPermissions();
		startCamera(permissionsRequired);
	}

	private String[] checkCameraPermissions() {
		//int permissionCheckAudio = ContextCompat.checkSelfPermission()
		int permissionCheckCam = ContextCompat.checkSelfPermission(context,
				Manifest.permission.CAMERA);
		int permissionCheckAudio = ContextCompat.checkSelfPermission(context,
				Manifest.permission.RECORD_AUDIO);
		int permissionCheckWriteToExternalStorage = ContextCompat.checkSelfPermission(context,
				Manifest.permission.WRITE_EXTERNAL_STORAGE);

		ArrayList<String> permissionsArrayList = new ArrayList<String>();
		if (permissionCheckCam == PackageManager.PERMISSION_DENIED) {
			permissionsArrayList.add(Manifest.permission.CAMERA);
		}
		if (permissionCheckAudio == PackageManager.PERMISSION_DENIED) {
			permissionsArrayList.add(Manifest.permission.RECORD_AUDIO);
		}
		if (permissionCheckWriteToExternalStorage == PackageManager.PERMISSION_DENIED) {
			permissionsArrayList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		}


		return permissionsArrayList.toArray(new String[0]);

	}

	private void requestPermissionForCamera(String[] requiredPermissions) {
		ActivityCompat.requestPermissions((Activity) context,
				requiredPermissions,
				CAMERA_REQUESTED_PERMISSION);
	}
	private void startCamera(String[] requiredPermissions) {

		if (requiredPermissions.length > 0 ) {
			requestPermissionForCamera(requiredPermissions);
		} else {
			addCameraToView();
		}
	}

	private void addCameraToView() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				CameraController cameraController = new CameraController(context);
				cameraHolder.addView(cameraController);
			}
		}, 500);
	}



	@Override
	protected void onResume() {
		super.onResume();
		// Make sure that the progress bar is back at 0.
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.video_progress);
		progressBar.setProgress(0);
	}
	//


	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case 1: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					// permission was granted, yay! Do the
					// contacts-related task you need to do.

				} else {

					// permission denied, boo! Disable the
					// functionality that depends on this permission.
				}
				return;
			}
			case CAMERA_REQUESTED_PERMISSION : {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					addCameraToView();
				}
			}

			// other 'case' lines to check for other
			// permissions this app might request
		}
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
}
