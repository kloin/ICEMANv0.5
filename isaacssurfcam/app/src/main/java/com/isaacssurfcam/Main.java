package com.isaacssurfcam;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;

/**
 * Written by Josiah Kendall. \
 *
 * THis is a simple camera app that allows you to take a photo every X amount of seconds.
 */
public class Main  extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private boolean cameraIsRunning = false;
    private static final String TAG = "BaseDriveActivity";
    private DriveId driveId;
    private DriveContents driveContents;
    private Main context;
    //private int mInterval = 60000; // 5 seconds by default, can be changed later
    private int progress = 0;
    private int duration = 0;
    private Handler mHandler;
    /**
     * DriveId of an existing folder to be used as a parent folder in
     * folder operations samples.
     */
    public static final String EXISTING_FOLDER_ID = "0B2EEtIjPUdX6MERsWlYxN3J6RU0";

    /**
     * DriveId of an existing file to be used in file operation samples..
     */
    public static final String EXISTING_FILE_ID = "0ByfSjdPVs9MZTHBmMVdSeWxaNTg";

    /**
     * Extra for account name.
     */
    protected static final String EXTRA_ACCOUNT_NAME = "account_name";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * Next available request code.
     */
    protected static final int NEXT_AVAILABLE_REQUEST_CODE = 2;


    private Bitmap bm;
    private GoogleApiClient mGoogleApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        setContentView(R.layout.main);
context=this;
        String resourceId = PreferenceManager.getDefaultSharedPreferences(context).getString("ID", null);
        if (resourceId != null) {
            Toast.makeText(context, "Resource is not Null: " + resourceId, Toast.LENGTH_SHORT).show();
        }
        FloatingActionButton captureButton = (FloatingActionButton) findViewById(R.id.startstopbutton);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When  clicked we want to toggle

                startRepeatingTask();
            }

        });
        SetUpSeekBarListeners();
    }

    private void startRepeatingTask() {
        mStatusChecker.run();
    }

    private void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    final Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            startCamera();
            mHandler.postDelayed(mStatusChecker, progress*1000);
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    private void SetUpSeekBarListeners() {
        final TextView qualityDisplay = (TextView) findViewById(R.id.photoQualitySliderDisplay);
        final TextView frequencyDisplay = (TextView) findViewById(R.id.photoFrequencyText);

        SeekBar quality = (SeekBar) findViewById(R.id.photoQualitySlider);
        final SeekBar frequency = (SeekBar) findViewById(R.id.DurationSlider);
        qualityDisplay.setText("Quality: (" + quality.getProgress() + "%)");

        quality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                qualityDisplay.setText("Quality :" + progress+"%");
            }
        });
        frequency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                duration = progressValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                frequencyDisplay.setText("interval: " + duration + " seconds");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Called when activity gets invisible. Connection to Drive service needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");

    }

    /**
     * Called when {@code mGoogleApiClient} is disconnected.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution is
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }
    /**
     * Getter for the {@code GoogleApiClient}.
     */
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }



    private void startCamera() {
        // We need to start up the camera and capture every x amount of seconds.
        // Need to run this in an asyc thread.
        //Camera mCamera = Camera.open();
        //mCamera.setDisplayOrientation(90);
        Camera mCamera = StaticShitCodeStuff.GetInstance().getCameraController().mCamera;
        mCamera.takePicture(null, rawCallback, jpegCallback);
    }

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            System.out.println("onPictureTaken - raw");
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize = 0;
            bm=BitmapFactory.decodeByteArray(data,0,data.length,options);
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
            saveToInternalSorage(bm);
        }
    };

    private void saveToInternalSorage(Bitmap bitmapImage){
        bm = bitmapImage;
        Drive.DriveApi.newDriveContents(getGoogleApiClient())
                .setResultCallback(driveContentsCallback);
    }

    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(final DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        return;
                    }
                    new Thread() {
                        @Override
                        public void run() {
                            String resourceId = PreferenceManager.getDefaultSharedPreferences(context).getString("ID", null);

                            if (resourceId != null) {
                                driveId = DriveId.decodeFromString(resourceId);
                                DriveFile file = Drive.DriveApi.getFile(getGoogleApiClient(),driveId);

                                file.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                                    @Override
                                    public void onResult(DriveApi.DriveContentsResult result) {


                                        if (!result.getStatus().isSuccess()) {
                                            // Handle error
                                            return;
                                        }

                                        // get the contents of the drive we want to update.
                                        driveContents = result.getDriveContents();
                                        final OutputStream outputStream2 =  driveContents.getOutputStream();
                                        ParcelFileDescriptor parcelFileDescriptor = driveContents.getParcelFileDescriptor();
                                        // Use the parcel description to get the input
                                        FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
                                        // Read to the end of the file.
                                        // Append to the file.

                                        // Here I need to write the image to the outputStream

                                        ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
                                        bm.compress(Bitmap.CompressFormat.JPEG, progress, stream1);

                                        try {
                                            outputStream2.write(stream1.toByteArray());
                                            // fileOutputStream.write(stream1.toByteArray());
                                        } catch (IOException e1) {
                                            Log.i(TAG, "Unable to write file contents.");
                                        }
                                        try {
                                            fileInputStream.read(new byte[fileInputStream.available()]);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                                .setTitle("surfcam")
                                                .setMimeType("image/jpg")
                                                .setStarred(true)
                                                .setLastViewedByMeDate(new Date()).build();

                                        driveContents.commit(mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<Status>() {
                                            @Override
                                            public void onResult(Status result) {
                                                // Handle the response status
                                            }
                                        });
                                    }
                                });
                            } else {
                                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                        .setTitle("surfcam")
                                        .setMimeType("image/jpg")
                                        .setStarred(true)
                                        .setLastViewedByMeDate(new Date()).build();

                                Drive.DriveApi.getRootFolder(getGoogleApiClient())
                                        .createFile(getGoogleApiClient(), changeSet, null).setResultCallback(fileCallback);
                                // Create new file
                            }

                        }
                    }.start();
                }
            };

    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {;
                        return;
                    }
                    driveId = result.getDriveFile().getDriveId();
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putString("ID", driveId.encodeToString()).commit();
                }
            };
}