package com.teamunemployment.breadcrumbs.client.Home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ncapdevi.fragnav.FragNavController;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;
import com.teamunemployment.breadcrumbs.R;
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

    private boolean RESTORED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_base);
        initialiseFragHolder();
        setUpBottomBar(savedInstanceState);

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
