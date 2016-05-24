package com.teamunemployment.breadcrumbs.client.Home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.ncapdevi.fragnav.FragNavController;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarBadge;
import com.roughike.bottombar.OnMenuTabClickListener;
import com.teamunemployment.breadcrumbs.Dialogs.IDialogCallback;
import com.teamunemployment.breadcrumbs.Dialogs.SimpleMaterialDesignDialog;
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
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_base);
        context = this;
        preferencesAPI = new PreferencesAPI(this);
        initialiseFragHolder();
        setUpBottomBar(savedInstanceState);
        BottomBarBadge unreadMessages = bottomBar.makeBadgeForTabAt(4, "#FF0000", 5);
        bottomBar.noTopOffset();
    }

    private void setUpBottomBar(final Bundle savedInstanceState) {
        bottomBar = BottomBar.attach(this, savedInstanceState);

        bottomBar.setItemsFromMenu(R.menu.bottombar_menu, new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                try {
                    if (menuItemId == R.id.bottomBarItemOne) {
                        fragNavController.switchTab(FragNavController.TAB1);
                    } else if(menuItemId == R.id.bottomBarItemTwo) {
                        fragNavController.switchTab(FragNavController.TAB2);
                    } else if(menuItemId == R.id.bottomBarItemThree) {
                        openCamera();
                    } else if(menuItemId == R.id.bottomBarItemFour) {
                        launchMyTripViewer();
                        // Launch my trip
                    } else if(menuItemId == R.id.bottomBarItemFive) {
                        fragNavController.switchTab(FragNavController.TAB3);
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
        int localTrail = preferencesAPI.GetLocalTrailId();

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
            Intent newIntent = new Intent();
            String localTrailString = Integer.toString(localTrail) + "L";
            newIntent.putExtra("TrailId", localTrailString);
            newIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.LocalMap");
            startActivity(newIntent);
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
                TrailManagerWorker trailManagerWorker = new TrailManagerWorker(context);
                trailManagerWorker.StartLocalTrail();
                Intent newIntent = new Intent();
                int localTrail = preferencesAPI.GetLocalTrailId();
                String localTrailString = Integer.toString(localTrail) + "L";
                newIntent.putExtra("TrailId", localTrailString);
                newIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.LocalMap");
                startActivity(newIntent);
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
        Intent newIntent = new Intent();
        newIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Camera.CameraCapture");
        startActivity(newIntent);
    }
}
