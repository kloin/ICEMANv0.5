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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.teamunemployment.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.CameraController;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraCapture extends ActionBarActivity {
	private View rootView;
    private GlobalContainer globalContainer;
	private Camera mCamera;
	private Context context;
	BreadCrumbsFusedLocationProvider breadCrumbsFusedLocationProvider;
	private LruCache<String, Bitmap> mMemoryCache;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);

		// ********** POTENTIAL BATTERY ISSUE *******************************
		// I start the foreground service but I never manually stop it. Does it get destroyed with the intent?
		breadCrumbsFusedLocationProvider = new BreadCrumbsFusedLocationProvider(this);
		breadCrumbsFusedLocationProvider.StartForegroundGPSService();

        // Construction purposes.
		setBackButtonListener();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Make sure that the progress bar is back at 0.
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.video_progress);
		progressBar.setProgress(0);
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
