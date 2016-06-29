package com.teamunemployment.breadcrumbs.client.Home;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.ncapdevi.fragnav.FragNavController;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarBadge;
import com.roughike.bottombar.OnMenuTabClickListener;
import com.teamunemployment.breadcrumbs.ActivityRecognition.ActivityController;
import com.teamunemployment.breadcrumbs.ActivityRecognition.ActivityHandler;
import com.teamunemployment.breadcrumbs.Dialogs.IDialogCallback;
import com.teamunemployment.breadcrumbs.Dialogs.SimpleMaterialDesignDialog;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Trails.TrailManagerWorker;
import com.teamunemployment.breadcrumbs.client.NavMenu.Profile.ProfilePageFragment;
import com.teamunemployment.breadcrumbs.client.tabs.ExploreTabFragment;
import com.teamunemployment.breadcrumbs.client.tabs.HomeTabFragment;
import com.teamunemployment.breadcrumbs.client.tabs.subtabs.ExploreTab;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jek40 on 20/05/2016.
 */
public class HomeActivity extends AppCompatActivity {
    private final String TAG = "HomeActivity";
    private BottomBar bottomBar;
    private FragNavController fragNavController;
    private PreferencesAPI preferencesAPI;
    private boolean RESTORED = false;
    private static final int CAMERA_REQUESTED_PERMISSION = 9;
    private static final int REQUESTED_LOCATION_WITH_START_TRAIL_QUEUED = 8;
    private static final int REQUESTED_LOCATION_WITHOUT_START_TRAIL_QUEUED = 7;

    private GoogleApiClient googleApiClient;
    private AppCompatActivity context;

    private int CURRENT_TAB = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_base);
        context = this;
        preferencesAPI = new PreferencesAPI(this);
        initialiseFragHolder();
        setUpBottomBar(savedInstanceState);
        startTrackingIfNeccessary();

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (preferencesAPI == null) {
            preferencesAPI = new PreferencesAPI(context);
        }
        CURRENT_TAB = preferencesAPI.GetCurrentTab();
        if (CURRENT_TAB == -1) {
            CURRENT_TAB = 0;
        }

        //bottomBar.selectTabAtPosition(CURRENT_TAB, false);
        switch (CURRENT_TAB) {
            case FragNavController.TAB1:
                bottomBar.selectTabAtPosition(0, false);
                fragNavController.switchTab(FragNavController.TAB1);
                break;
            case FragNavController.TAB2:
                bottomBar.selectTabAtPosition(1, false);
                fragNavController.switchTab(FragNavController.TAB2);
                break;
            case FragNavController.TAB3:
                bottomBar.selectTabAtPosition(4, false);
                fragNavController.switchTab(FragNavController.TAB3);
                break;
        }
    }

    private void setUpBottomBar(final Bundle savedInstanceState) {
        bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.noTopOffset();
        bottomBar.noNavBarGoodness();
        bottomBar.setItemsFromMenu(R.menu.bottombar_menu, new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                try {
                    if (menuItemId == R.id.bottomBarItemOne) {
                        CURRENT_TAB = FragNavController.TAB1;
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                fragNavController.switchTab(FragNavController.TAB1);
                            }
                        }, 80);
                    } else if(menuItemId == R.id.bottomBarItemTwo) {
                        CURRENT_TAB = FragNavController.TAB2;
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                fragNavController.switchTab(FragNavController.TAB2);
                            }
                        }, 80);
                    } else if(menuItemId == R.id.bottomBarItemThree) {
                        preferencesAPI.SetCurrentTab(CURRENT_TAB);

                        openCamera();
                    } else if(menuItemId == R.id.bottomBarItemFour) {
                        preferencesAPI.SetCurrentTab(CURRENT_TAB);
                        launchMyTripViewer();
                        // Launch my trip
                    } else if(menuItemId == R.id.bottomBarItemFive) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                CURRENT_TAB = FragNavController.TAB3;
                                preferencesAPI.SetCurrentTab(CURRENT_TAB);
                                fragNavController.switchTab(FragNavController.TAB3);
                            }
                        }, 80);

//                    fragNavController.switchTab(FragNavController.TAB5);
                        // should launch profile in fragment here
                    }
                } catch (IllegalStateException ex) {
                    Log.e(TAG, "Failed to change tab. Stack trace follows.");
                    ex.printStackTrace();
                }
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {

            }
        });
    }

    private void launchMyTripViewer() {
        final int localTrail = preferencesAPI.GetLocalTrailId();

        // Show up the create trail dialog.
        if (localTrail == -1) {
            SimpleMaterialDesignDialog.Build(context)
                    .SetTitle("No Trail Found")
                    .SetTextBody("This is where you view your trip progress. Do you wan to create a trip now?")
                    .SetActionWording("Create trail")
                    .UseCancelButton(true)
                    .SetCallBack(createTrailCallback())
                    .Show();
            return;
        }
        else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable(){
                @Override
                public void run() {
                    Intent newIntent = new Intent();
                    String localTrailString = Integer.toString(localTrail) + "L";
                    newIntent.putExtra("TrailId", localTrailString);
                    newIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.LocalMap");
                    startActivity(newIntent);
                }
            }, 60);

        }
    }

    private IDialogCallback createNewTrailWithNoAction() {
        return new IDialogCallback() {
            @Override
            public void DoCallback() {
                TrailManagerWorker worker = new TrailManagerWorker(context);
                worker.StartLocalTrail();
            }
        };
    }

    private IDialogCallback createTrailCallback() {
        return new IDialogCallback() {
            @Override
            public void DoCallback() {
                // If permissions are all good, go ahead and create the trail. If permissions are not all good,
                int coarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
                int fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

                if (coarseLocation == PackageManager.PERMISSION_GRANTED && fineLocation == PackageManager.PERMISSION_GRANTED) {
                    TrailManagerWorker trailManagerWorker = new TrailManagerWorker(context);
                    trailManagerWorker.StartLocalTrail();
                    Intent newIntent = new Intent();
                    int localTrail = preferencesAPI.GetLocalTrailId();
                    String localTrailString = Integer.toString(localTrail) + "L";
                    newIntent.putExtra("TrailId", localTrailString);
                    newIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.LocalMap");
                    startActivity(newIntent);
                } else {
                    if (coarseLocation == PackageManager.PERMISSION_DENIED && fineLocation == PackageManager.PERMISSION_DENIED) {
                        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                        AppCompatActivity appCompatActivity = (AppCompatActivity) context;
                        ActivityCompat.requestPermissions(appCompatActivity, permissions, REQUESTED_LOCATION_WITH_START_TRAIL_QUEUED);
                    } else if(coarseLocation == PackageManager.PERMISSION_DENIED) {
                        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
                        AppCompatActivity appCompatActivity = (AppCompatActivity) context;
                        ActivityCompat.requestPermissions(appCompatActivity, permissions, REQUESTED_LOCATION_WITH_START_TRAIL_QUEUED);
                    } else if (fineLocation == PackageManager.PERMISSION_DENIED) {
                        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
                        AppCompatActivity appCompatActivity = (AppCompatActivity) context;
                        ActivityCompat.requestPermissions(appCompatActivity, permissions, REQUESTED_LOCATION_WITH_START_TRAIL_QUEUED);
                    }
                }
            }
        };
    }

    private void initialiseFragHolder() {
        List<Fragment> fragments = new ArrayList<>(5);
        fragments.add(new HomeTabFragment());
        fragments.add(new ExploreTabFragment());
        fragments.add(new ProfilePageFragment());
        fragNavController = new FragNavController(getSupportFragmentManager(),R.id.fragment_container,fragments);
    }

    private void openCamera() {
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

        if (permissionsArrayList.size() > 0) {
            requestPermissionForCamera( permissionsArrayList.toArray(new String[0]));
        }

        if (permissionsArrayList.size() == 0 ) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable(){
                @Override
                public void run() {
                    Intent newIntent = new Intent();
                    newIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Camera.CameraCapture");
                    startActivity(newIntent);
                }
            }, 60);

        }
    }

    private void requestPermissionForCamera(String[] requiredPermissions) {
            ActivityCompat.requestPermissions(context,
                    requiredPermissions,
                    CAMERA_REQUESTED_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUESTED_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    int permissionAudioCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO);
                    int readAndWritePermissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if(permissionAudioCheck == PackageManager.PERMISSION_GRANTED && readAndWritePermissionCheck == PackageManager.PERMISSION_GRANTED) {
                        Intent newIntent = new Intent();
                        newIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Camera.CameraCapture");
                        startActivity(newIntent);
                    }

                } else {

                    Log.d(TAG, "User refuesed to give camera permission. What a fucken idiot.");
                }
                return;
            }
            case REQUESTED_LOCATION_WITH_START_TRAIL_QUEUED: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent newIntent = new Intent();
                    TrailManagerWorker trailManagerWorker = new TrailManagerWorker(context);
                    trailManagerWorker.StartLocalTrail();
                    int localTrail = preferencesAPI.GetLocalTrailId();
                    String localTrailString = Integer.toString(localTrail) + "L";
                    newIntent.putExtra("TrailId", localTrailString);
                    newIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.LocalMap");
                    startActivity(newIntent);
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * If we were tracking when the phone was turned off, we need to start tracking again.
     */
    private void startTrackingIfNeccessary() {
        if (preferencesAPI == null) {
            preferencesAPI = new PreferencesAPI(this);
        }

        boolean isTracking = preferencesAPI.isTrackingEnabledByUser();
        if (isTracking) {
            ActivityController activityController = new ActivityController(context);
            activityController.StartListenting();
        }
    }
}
