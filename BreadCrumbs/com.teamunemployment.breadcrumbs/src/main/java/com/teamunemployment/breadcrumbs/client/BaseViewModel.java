package com.teamunemployment.breadcrumbs.client;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.teamunemployment.breadcrumbs.BackgroundServices.BackgroundService;
import com.teamunemployment.breadcrumbs.BreadcrumbsActivityAPI;
import com.teamunemployment.breadcrumbs.BreadcrumbsLocationAPI;
import com.teamunemployment.breadcrumbs.Dialogs.IDialogCallback;
import com.teamunemployment.breadcrumbs.Dialogs.SimpleMaterialDesignDialog;
import com.teamunemployment.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.teamunemployment.breadcrumbs.Location.BreadcrumbsLocationProvider;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.NetworkConnectivityManager;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HomelessNetworkTools;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UpdateViewElementWithProperty;
import com.teamunemployment.breadcrumbs.Trails.*;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.Cards.HomeCardAdapter;
import com.teamunemployment.breadcrumbs.client.NavigationDrawer.DrawerItemCustomAdapter;
import com.teamunemployment.breadcrumbs.client.NavigationDrawer.ObjectDrawerItem;
import com.bumptech.glide.Glide;
import com.teamunemployment.breadcrumbs.client.tabs.ExploreTabFragment;
import com.teamunemployment.breadcrumbs.client.tabs.HomeTabFragment;
import com.teamunemployment.breadcrumbs.client.tabs.MyStuffTab;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/*
 * This is our main page, which hosts the navigation drawer and the
 *
 */
public class BaseViewModel extends AppCompatActivity {

    private static final String TRAIL_CREATED_TITLE = "Trail Created";
    private static final String TRAIL_CREATED_DESCRIPTION = "Your good to go! You can edit, view and publish your trail(s) from the side menu. Use the button below to add content.";
	private BaseViewModel mContext;
	private AsyncDataRetrieval clientRequestProxy;
	private HomeCardAdapter mAdapter;
	private GlobalContainer globalContainer;

    private RecyclerView mRecyclerView;

    private boolean isTracking = false;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mPlanetTitles;
    private Toolbar toolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayout mDrawerView;
    private int backPressedCounter = 0;
    private boolean drawerIsOpen = false;
    private String name;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private BreadcrumbsLocationAPI locationAPI;
    private BreadcrumbsActivityAPI activityAPI;

    private FloatingActionButton rightLowerButton;
    private android.support.design.widget.FloatingActionButton addTrailFab;
    private SubActionButton.Builder rLSubBuilder;
    private SubActionButton trackingButton;
    private ImageView mTrackingIcon;
    private ImageView fabIconNew;
    private FloatingActionMenu rightLowerMenu;

    private View overlay;

    private PreferencesAPI mPreferencesApi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page_with_tabs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.HeaderFont);
        setSupportActionBar(toolbar);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        mContext = this;


        name = PreferenceManager.getDefaultSharedPreferences(this).getString("USERNAME", "");

        // This is going to happen after clearing the cache. Need to work out solution for multiple situations like this.
        if (name == null || name.isEmpty()) {
            // need to fetch name here and add it back to the shared preferences
            HomelessNetworkTools networkTools = new HomelessNetworkTools();
            // networkTools.FetchStringAndSaveToPreferences("USERNAME", );
        }

        mPreferencesApi = new PreferencesAPI(this);
        globalContainer = GlobalContainer.GetContainerInstance();
        setUpTrackingButtons();

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerView= (LinearLayout) findViewById(R.id.drawer_holder);
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerIsOpen = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                drawerIsOpen = false;
                // Code here will execute once drawer is closed
            }
        }; // Drawer Toggle Object Made
        mDrawerLayout.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle

        ObjectDrawerItem[] drawerItem = new ObjectDrawerItem[5];
        drawerItem[0] = new ObjectDrawerItem(R.drawable.ic_perm_identity_black_24dp, "Profile");
        drawerItem[1] = new ObjectDrawerItem(R.drawable.ic_timeline_add_black_24px, "Create Trail");
        drawerItem[2] = new ObjectDrawerItem(R.drawable.ic_timeline_black_24dp, "My Trail");
        drawerItem[3] = new ObjectDrawerItem(R.drawable.ic_camera_alt_black_24dp, "Capture");
        drawerItem[4] = new ObjectDrawerItem(R.drawable.ic_settings_black_24dp, "Settings");

        DrawerItemCustomAdapter adapter = new DrawerItemCustomAdapter(this, R.layout.nav_drawer_item, drawerItem);
        mDrawerList.setAdapter(adapter);
        // Loads trailIds and triggers card creation when done
        RelativeLayout profileHolder = (RelativeLayout) findViewById(R.id.profile_header_nav_menu);
        ViewGroup.LayoutParams layoutParams = profileHolder.getLayoutParams();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        Double width = displaymetrics.widthPixels * 0.8;
        layoutParams.height = width.intValue();
        profileHolder.setLayoutParams(layoutParams);
        setUserNameAndPhotos(name);
        setTrackingButtonsPosition();

        locationAPI = new BreadcrumbsLocationAPI();
        checkServiceStateAndStartIfNeccessary();

        // Create both our action buttons
        setUpFab();
        setTheCreateNewTrailListener();

        // Update to the correct button
        updateFabButtonState();
    }

    private void setUpExitTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide slide = (Slide) TransitionInflater.from(this).inflateTransition(R.transition.activity_slide);
            getWindow().setExitTransition(slide);
        }
    }

    private void initSupportToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.HeaderFont);
        setSupportActionBar(toolbar);
    }

    private void initSupportViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setTheCreateNewTrailListener() {
        addTrailFab = (android.support.design.widget.FloatingActionButton) findViewById(R.id.create_new_trail);
        addTrailFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleAnimations simpleAnimations = new SimpleAnimations();

                showCreateTrailDialog();
            }
        });
    }

    private void setUpFab() {

        rLSubBuilder = new SubActionButton.Builder(this);
        int convertedPixelSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
        rLSubBuilder.setLayoutParams(new FrameLayout.LayoutParams(convertedPixelSize, convertedPixelSize));

        fabIconNew = new ImageView(this);
        fabIconNew.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_new));
        rightLowerButton = new FloatingActionButton.Builder(this)
                .setContentView(fabIconNew)
                .setBackgroundDrawable(R.drawable.button_action_accent)
                    .build();

        rightLowerMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(buildCameraSubActionButton())
                .addSubActionView(buildTrackingSubActionButton())
                .addSubActionView(buildUploadSubActionButton())
                .attachTo(rightLowerButton)
                .build();

        overlay = findViewById(R.id.overlay);
        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // close the view
                closeMenuFab(fabIconNew, v, rightLowerMenu);
                return;
            }
        });

        // Listen menu open and close events to animate the button content view
        rightLowerMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu menu) {
                // Rotate the icon of rightLowerButton 45 degrees clockwise
                openMenuFab(fabIconNew, overlay);

            }

            @Override
            public void onMenuClosed(FloatingActionMenu menu) {
                // Rotate the icon of rightLowerButton 45 degrees counter-clockwise
                closeMenuFab(fabIconNew, overlay, menu);
            }
        });
    }

    private SubActionButton buildUploadSubActionButton() {
        ImageView rlIcon1 = new ImageView(this);
        rlIcon1.setImageDrawable(getResources().getDrawable(R.drawable.ic_backup_black_24dp));
        SubActionButton subActionButton1 = rLSubBuilder.setContentView(rlIcon1).build();
        subActionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rightLowerMenu.close(true);
                Intent intent = new Intent(mContext, TestingVideo.class);
                mContext.startActivity(intent);
                /*
                if (NetworkConnectivityManager.IsNetworkAvailable(mContext)) {
                    Intent intent = new Intent(mContext, UploadTrail.class);
                    mContext.startActivity(intent);
                } else {
                    Toast.makeText(mContext, "Cannot upload - No internet Connection available", Toast.LENGTH_LONG).show();
                }*/
            }
        });

        return subActionButton1;
    }

    private SubActionButton buildCameraSubActionButton() {
        ImageView rlIcon = new ImageView(this);
        rlIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_camera_alt_black_24dp));
        SubActionButton subActionButton1 = rLSubBuilder.setContentView(rlIcon).build();
        subActionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rightLowerMenu.close(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent newIntent = new Intent();
                        newIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Camera.CameraCapture");
                        startActivity(newIntent);
                    }
                }, 350);

            }
        });

        return subActionButton1;
    }

    private SubActionButton buildTrackingSubActionButton() {
        mPreferencesApi = new PreferencesAPI(mContext);
        boolean alreadyTrackingFromLastTime = mPreferencesApi.isTrackingEnabledByUser();
        mTrackingIcon = new ImageView(this);
        if (alreadyTrackingFromLastTime) {
            mTrackingIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_location_black_24dp));
        } else {
            mTrackingIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_location_searching));
        }
        trackingButton = rLSubBuilder.setContentView(mTrackingIcon).build();

        trackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTrackingButtonToggle();
            }
        });

        return trackingButton;
    }

    private void handleTrackingButtonToggle() {
        boolean tracking = mPreferencesApi.isTrackingEnabledByUser();
        if (tracking) {
            SimpleMaterialDesignDialog.Build(mContext)
                    .SetTitle("Stop tracking")
                    .SetTextBody("Do you want to disable tracking?")
                    .SetActionWording("Stop Tracking")
                    .UseCancelButton(true)
                    .SetCallBack(CreateStopTrackingCallback())
                    .Show();
        } else {
            rightLowerMenu.close(true);
            mTrackingIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_location_black_24dp));
            locationAPI.StartLocationService();
            mPreferencesApi.SetUserTracking(true);
        }
    }

    private IDialogCallback CreateStopTrackingCallback() {
        IDialogCallback callback = new IDialogCallback(){

            @Override
            public void DoCallback() {
                rightLowerMenu.close(true);
                mTrackingIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_location_searching));
                locationAPI.StopLocationService();
                mPreferencesApi.SetUserTracking(false);
            }
        };
        return callback;
    }

    private void openMenuFab(ImageView fabIconNew, final View overlay) {
        fabIconNew.setRotation(0);
        PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 45);
        ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(fabIconNew, pvhR);
        animation.start();
        SimpleAnimations.FadeInViewWithSetDuration(overlay, 450);
       /* new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        }, 450);*/

    }

    private void closeMenuFab(ImageView fabIconNew, View overlay, FloatingActionMenu menu) {
        fabIconNew.setRotation(45);
        PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 0);
        ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(fabIconNew, pvhR);
        animation.start();
        menu.close(true);

        overlay.setVisibility(View.GONE);
    }

    private void checkServiceStateAndStartIfNeccessary() {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (BackgroundService.class.getName().equals(service.service.getClassName())) {
                //your service is running
               return;
            }
        }

        // If we get here, the background service is not running.
        Intent breadcrumbsService = new Intent(this, BackgroundService.class);
        startService(breadcrumbsService);
    }

    private void testDB() {
        DatabaseController dbc = new DatabaseController(mContext);
        String userId = PreferenceManager.getDefaultSharedPreferences(mContext).getString("TRAILID", null);
        JSONObject json = dbc.fetchMetadataFromDB(userId, true);
        Log.d("JSON", json.toString());
       // GalleryManager manager = new GalleryManager(mContext);
       // manager.GetGalleryFolders();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HomeTabFragment(), "FEED");
        adapter.addFragment(new ExploreTabFragment(), "DISCOVER");
        adapter.addFragment(new MyStuffTab(), "MY STUFF");
        viewPager.setAdapter(adapter);
    }



    private void setTrackingButtonsPosition() {
        isTracking = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("TRACKING", false);
        if (isTracking) {
            SwitchCompat trackingSwitch = (SwitchCompat) findViewById(R.id.tracking_toggle);
            trackingSwitch.setChecked(true);
        }
    }

    // Set title, styles for the toolbar that we are using.
    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.HeaderFont);
        setSupportActionBar(toolbar);
    }

    // Set up the navigation Drawer and its click listeners and events.
    private void setUpNavDrawer() {
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerView= (LinearLayout) findViewById(R.id.drawer_holder);
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerIsOpen = true;
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                drawerIsOpen = false;
                // Code here will execute once drawer is closed
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle

    }

    // Get our name for using in nav window, and passing to other views.
    private void setUserName() {
        name = PreferenceManager.getDefaultSharedPreferences(this).getString("USERNAME", "");
        // This is going to happen after clearing the cache. Need to work out solution for multiple situations like this.
        if (name == null || name.isEmpty()) {
            // need to fetch name here and add it back to the shared preferences
            HomelessNetworkTools networkTools = new HomelessNetworkTools();
            // networkTools.FetchStringAndSaveToPreferences("USERNAME", );
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateFabButtonState();
    }

    // This method is to correctly decide which floating action button on the home screen should show.
    private void updateFabButtonState() {

        // Check if we have a trailId
        // If we do, show th fab.
        // Else, show the normal.
        int localTrailId =  new PreferencesAPI(mContext).GetLocalTrailId();
        // We have no trailId, so we show the add content fab.
        if (localTrailId == -1) {
            showAddTrailFab();

            return;
        }
        showAddContentFab();
        return;
    }

    private void showAddContentFab() {
        if (rightLowerButton != null && addTrailFab != null) {
            rightLowerButton.setVisibility(View.VISIBLE);
            addTrailFab.setVisibility(View.GONE);
        }
    }

    private void showAddTrailFab() {
        if (rightLowerButton != null && addTrailFab != null) {
            rightLowerButton.setVisibility(View.GONE);
            addTrailFab.setVisibility(View.VISIBLE);
        }
    }
    private void setUpRefreshLayout() {

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refresh();
            }
            public void refresh() {
                // Load all trails here
                reloadTrails();
                onLoaded();
            }

            private void onLoaded() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
        // This checks to see if we now have a trail, and should be displaying the "add content" button.
        setContentButton();
        updateTrailOrientatedButtons();
        setTrackingButtonsPosition();
        // Check that we have the correct switch position.
        if (drawerIsOpen) {
            mDrawerLayout.closeDrawer(mDrawerView);
        }
        updateCoverPhoto();
        backPressedCounter = 0;
    }

    // Update the visibility of the trail buttons - tracking, my trail etc
    private void updateTrailOrientatedButtons() {
        int localTrailId = mPreferencesApi.GetLocalTrailId();
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.tracking_toggle_wrapper);
        if (localTrailId != -1) {
            // Hide shit
            layout.setVisibility(View.GONE);
        } else {
           // layout.setVisibility(View.VISIBLE);
        }
    }

    // Decide which fab to display.
    private void setUpFabDisplay() {
        //if ()

    }

    // Show a snackbar message to the user
    private void displayMessage(String string) {

//        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.test);
//        Snackbar snackbar = Snackbar.make(coordinatorLayout, string, Snackbar.LENGTH_LONG).setAction("UNDO", null);
//
//        // Set text color
//        snackbar.setActionTextColor(Color.RED);
//
//        // Grab actual snackbar and set its color
//        View snackbarView = snackbar.getView();
//        snackbarView.setBackgroundColor(Color.DKGRAY);
//
//        // Grab our text view
//        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
//        textView.setTextColor(getResources().getColor(R.color.accent));
//
//        // Show our work
//        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        if (drawerIsOpen) {
            mDrawerLayout.closeDrawer(mDrawerView);
            drawerIsOpen = false;
        } else {
            if (backPressedCounter > 0) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            } else {
                backPressedCounter += 1;
                displayMessage("Press Back Button Again to Exit");
            }
        }
    }

    // Set up tracking button
    private void setUpTrackingButtons() {

        final SwitchCompat trackingButton = (SwitchCompat) findViewById(R.id.tracking_toggle);
        final RelativeLayout trackingWrapper = (RelativeLayout) findViewById(R.id.tracking_toggle_wrapper);
        RelativeLayout uploadButton = (RelativeLayout) findViewById(R.id.upload);

        // If we do not yet have a trail, we need to set this as ivisible.
        final int localTrailId = mPreferencesApi.GetLocalTrailId();

        if (localTrailId == -1) {
            trackingWrapper.setVisibility(View.GONE);
            return;
        } else {
            // Set visibility true and continue on.
            //trackingWrapper.setVisibility(View.VISIBLE);
            uploadButton.setVisibility(View.VISIBLE);
            uploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkConnectivityManager.IsNetworkAvailable(mContext)) {
                        Intent intent = new Intent(mContext, UploadTrail.class);
                        mContext.startActivity(intent);

                    } else {
                        Toast.makeText(mContext, "Cannot upload - No internet Connection available", Toast.LENGTH_LONG).show();
                    }

                }
            });
        }

        // Grab a fused location provider from our user class so we can track.
        final BreadCrumbsFusedLocationProvider breadCrumbsFusedLocationProvider = new BreadCrumbsFusedLocationProvider(this);
        GlobalContainer gc = GlobalContainer.GetContainerInstance();
        gc.SetBreadCrumbsFusedLocationProvider(breadCrumbsFusedLocationProvider);
        // Check if we were already tracking.
        isTracking = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("TRACKING", false);
        // Our listner for this button. Toggles tracking using fused service, and updates flag.
        assert trackingButton != null;
        trackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop tracking
                if (isTracking) {
                    // Stop and save here.
                    PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean("TRACKING", false).commit();
                    breadCrumbsFusedLocationProvider.StopBackgroundGPSSerivce();
                    BreadcrumbsLocationProvider.getInstance(mContext).StopListeningToPathsenseActivityUpdates();
                    isTracking = false;
                } else {
                    // Start tracking
                    int localTrailId = mPreferencesApi.GetLocalTrailId();
                    if (localTrailId != -1) {
                        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean("TRACKING", true).commit();
                        //breadCrumbsFusedLocationProvider.StartBackgroundGPSService();
                        // BreadcrumbsLocationProvider.getInstance(mContext).StartListeningToPathSense();
                        locationAPI.StartLocationService();
                        activityAPI = new BreadcrumbsActivityAPI();
                        activityAPI.ListenToUserActivityChanges();

                        //BreadcrumbsLocationProvider.getInstance(mContext).ListenPassivelyForGPSUpdatesInBackground();
                        isTracking = true;
                    } else {
                        // Show dialog - cant start tracking
                        showDialog("You need to create a trail to add content");
                    }
                }
            }
        });
    }

    // Set our users name in the drawer. This has some bugs I think
    private void setUserNameAndPhotos(String name) {
        TextView belongsTo = (TextView) findViewById(R.id.belongs_to);
        String userId = PreferenceManager.getDefaultSharedPreferences(this).getString("USERID", globalContainer.GetUserId());
        if (name == null) {
            UpdateViewElementWithProperty updateViewElementWithProperty = new UpdateViewElementWithProperty();
            updateViewElementWithProperty.UpdateTextViewElement(belongsTo, userId, "Username", mContext);
        } else {
            belongsTo.setText(name);
        }
        updateCoverPhoto();
    }

    private void updateCoverPhoto () {
        final ImageView background = (ImageView) findViewById(R.id.drawer_background);
        String coverPhotoId = PreferenceManager.getDefaultSharedPreferences(mContext).getString("COVERPHOTOID", "-1");
        String userId = PreferenceManager.getDefaultSharedPreferences(this).getString("USERID", globalContainer.GetUserId());
        if (coverPhotoId.equals("-1")) {
            String imageIdUrl = LoadBalancer.RequestServerAddress() + "/rest/login/GetPropertyFromNode/"+userId+"/CoverPhotoId";
            AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(imageIdUrl, new AsyncDataRetrieval.RequestListener() {
                @Override
                public void onFinished(String result) {
                    Glide.with(mContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+result + ".jpg").centerCrop().crossFade().into(background);
                }
            }, mContext);
            asyncDataRetrieval.execute();
        } else {
            Glide.with(mContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+coverPhotoId + ".jpg").centerCrop().crossFade().into(background);
        }
    }

    // This is the setup for the  add button on the homepage. If the user has no trails we need
    // have a create trail button there. If not, we just have the video.
    private void setContentButton() {

    }



    public void reloadTrails() {
        // Our url - just gets a json string of all trail ids.
        String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetAllTrailIds";
        url = url.replaceAll(" ", "%20");
        clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                try {
                    // Get our arrayList for the card adapter
                    JSONObject jsonResult = new JSONObject(result);
                    ArrayList<String> allIds = convertJSONToArrayList(jsonResult);
                    ArrayList<String> oldIds = globalContainer.GetTrailIdsCurrentlyDisplayed();

                    // Look through all our old Ids and see if we have already loaded them.
                    if (oldIds == null || allIds.size() == oldIds.size()) {
                        // This should not happen, but it may.
                        displayMessage("Trails up to date");
                        return;
                    }

                    globalContainer.SetTrailIdsCurrentlyDisplayed(allIds);

                    mAdapter = new HomeCardAdapter(allIds, mContext);
                    mRecyclerView.setAdapter(mAdapter);
                    int difference = allIds.size() - oldIds.size();
                    displayMessage(difference + " New Trails");
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.e("Cards", "Failed to reload trails : Stack Trace = " + e.toString());
                }
            }

            // Covert our json result into an arrayList
            private ArrayList<String> convertJSONToArrayList(JSONObject result) {
                ArrayList<String> ids = new ArrayList<String>();
                Iterator<String> keys = result.keys();
                while (keys.hasNext()) {
                    String nextKey = keys.next();
                    try {
                        ids.add(result.getString(nextKey));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return ids;
            }
        }, mContext);
        clientRequestProxy.execute();
    }

    // Need this code but for now I am going to comment it out.
    public void loadTrails() {

        String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetAllTrailIds";
        url = url.replaceAll(" ", "%20");
        clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                try {
                    // Hide loading spinner
                    ProgressBar loadingSpinner = (ProgressBar) mContext.findViewById(R.id.explore_progress_bar);
                    loadingSpinner.setVisibility(View.GONE);
                    // Get our arrayList for the card adapter
                    JSONObject jsonResult = new JSONObject(result);
                    ArrayList<String> ids = convertJSONToArrayList(jsonResult);
                    globalContainer.SetTrailIdsCurrentlyDisplayed(ids);
                    // Create the adapter, and set it to the recyclerView so that it displays
                    mAdapter = new HomeCardAdapter(ids, mContext);
                    mRecyclerView.setAdapter(mAdapter);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.e("Cards", "Failed to convert String to json");
                }
            }

            // Covert our json result into an arrayList
            private ArrayList<String> convertJSONToArrayList(JSONObject result) {
                ArrayList<String> ids = new ArrayList<String>();
                Iterator<String> keys = result.keys();
                while (keys.hasNext()) {
                    String nextKey = keys.next();
                    try {
                        ids.add(result.getString(nextKey));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return ids;
            }
        }, mContext);
        clientRequestProxy.execute();
        Log.i("BASE", "Sending request to construct the cards");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        super.onPrepareOptionsMenu(menu);
        mDrawerLayout.closeDrawer(Gravity.LEFT);
        decideIfCameraOrNewTrailButton(menu);
        menu.getItem(1).setVisible(false);
        menu.getItem(0).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle action buttons
        switch(item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void decideIfCameraOrNewTrailButton(final Menu menu) {
        int localTrailId = mPreferencesApi.GetLocalTrailId();
        if (localTrailId == -1) {
            //Hide it.
        }
        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .main_content);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
            drawerIsOpen = false;
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        final Intent newIntent = new Intent();
        int localTrailId = mPreferencesApi.GetLocalTrailId();
        //Load the correct page.
        switch (position) {
            case 0:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        newIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.NavMenu.Profile.ProfilePageViewer");
                        newIntent.putExtra("userId", PreferenceManager.getDefaultSharedPreferences(mContext).getString("USERID", "-1"));
                        newIntent.putExtra("name", name);
                        startActivity(newIntent);
                    }
                }, 250);
                break;
            case 1:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showCreateTrailDialog();
                    }
                }, 250);
                break;
            case 2:
                if (localTrailId != -1) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            newIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.MapHostBase");
                            startActivity(newIntent);
                        }
                    }, 250);
                } else {
                    // We need to show the user a dialog, and give them the option of creating a new trail
                    showDialog("You have no trail to edit");
                }
                break;
            case 3:
                // Check if we have a trail created. If not we need to tell the user why we cannot take any photos
                if (localTrailId != -1) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            newIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Camera.CameraCapture");
                            startActivity(newIntent);
                        }
                    }, 250);

                } else {
                    // We need to show the user a dialog, and give them the option of creating a new trail
                    showDialog("You need to create a trail to add content");
                }
                break;
            case 4:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        newIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Settings");
                        startActivity(newIntent);
                    }
                }, 250);
                break;
        }

        mDrawerList.setItemChecked(position, true);
        mDrawerList.setSelection(position);
    }

    private void showDialog(String message) {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.no_user_trail_dialog);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.denied_message);
        messageTextView.setText(message);
        TextView dialogButton = (TextView) dialog.findViewById(R.id.dismiss_dialog);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showCreateTrailDialog() {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.create_trail_dialog);
        final TextView createButton = (TextView) dialog.findViewById(R.id.create_dialog);
        // if button is clicked, close the custom dialog
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create local trail
                TrailManagerWorker trailManagerWorker = new TrailManagerWorker(mContext);
                trailManagerWorker.StartLocalTrail();
                // Show post trail orientation dialog.
                showMaterialDesignDialog(TRAIL_CREATED_TITLE, TRAIL_CREATED_DESCRIPTION);
                android.support.design.widget.FloatingActionButton floatingActionButton = (android.support.design.widget.FloatingActionButton) mContext.findViewById(R.id.create_new_trail);
                floatingActionButton.setVisibility(View.GONE);
                mPreferencesApi.SetUserTracking(true);

                // Remove geofences
                locationAPI.RemoveGeofences();
                mTrackingIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_location_black_24dp));

                //Create notification
//                NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
//                NotificationCompat.Builder noti = new NotificationCompat.Builder(mContext);
//                noti.setContentTitle("Trail Created");
//                noti.setContentText("You can name, edit and upload this trail later from the publish screen.");
//                noti.setSmallIcon(R.drawable.ic_launcher);
//                notificationManager.notify(1, noti.build());
                dialog.dismiss();

                SimpleAnimations.shrinkNewTrailFab(addTrailFab, rightLowerButton, mContext);
            }
        });

        TextView dismissButton = (TextView) dialog.findViewById(R.id.cancel_dialog);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void showMaterialDesignDialog(String title, String message) {
        SimpleMaterialDesignDialog.Build(mContext).SetTitle(title).SetTextBody(message).Show();
    }
}

	

