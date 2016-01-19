package com.breadcrumbs.client;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.R;
import com.breadcrumbs.ServiceProxy.AsyncDataRetrieval;
import com.breadcrumbs.ServiceProxy.HomelessNetworkTools;
import com.breadcrumbs.ServiceProxy.UpdateViewElementWithProperty;
import com.breadcrumbs.caching.GlobalContainer;
import com.breadcrumbs.client.Cards.HomeCardAdapter;
import com.breadcrumbs.client.NavigationDrawer.DrawerItemCustomAdapter;
import com.breadcrumbs.client.NavigationDrawer.ObjectDrawerItem;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;

/*
 * This is our main page, which hosts the navigation drawer and the
 *
 */
public class BaseViewModel extends AppCompatActivity {

	private BaseViewModel context;
	private AsyncDataRetrieval clientRequestProxy;
	private HomeCardAdapter mAdapter;
	private GlobalContainer globalContainer;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpager_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.HeaderFont);
        setSupportActionBar(toolbar);
        context = this;
        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();
        name = PreferenceManager.getDefaultSharedPreferences(this).getString("USERNAME", "");

        // This is going to happen after clearing the cache. Need to work out solution for multiple situations like this.
        if (name == null || name.isEmpty()) {
            // need to fetch name here and add it back to the shared preferences
            HomelessNetworkTools networkTools = new HomelessNetworkTools();
           // networkTools.FetchStringAndSaveToPreferences("USERNAME", );
        }
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
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                drawerIsOpen = false;
                // Code here will execute once drawer is closed
            }
        }; // Drawer Toggle Object Made
        mDrawerLayout.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshWrapper);
        setUpRefreshLayout();
                // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ObjectDrawerItem[] drawerItem = new ObjectDrawerItem[4];

        drawerItem[0] = new ObjectDrawerItem(R.drawable.ic_perm_identity_black_24dp, "Profile");
        drawerItem[1] = new ObjectDrawerItem(R.drawable.ic_add_black_24dp, "Create Trail");
        drawerItem[2] = new ObjectDrawerItem(R.drawable.ic_settings_black_24dp, "My Trails");
        drawerItem[3] = new ObjectDrawerItem(R.drawable.ic_camera_alt_black_24dp, "Capture");
        DrawerItemCustomAdapter adapter = new DrawerItemCustomAdapter(this, R.layout.nav_drawer_item, drawerItem);
        mDrawerList.setAdapter(adapter);
        // Loads trailIds and triggers card creation when done
        loadTrails();
        RelativeLayout profileHolder = (RelativeLayout) findViewById(R.id.profile_header_nav_menu);
        ViewGroup.LayoutParams layoutParams = profileHolder.getLayoutParams();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        Double width = displaymetrics.widthPixels * 0.8;
        layoutParams.height = width.intValue();
        profileHolder.setLayoutParams(layoutParams);
        setUserNameAndPhotos(name);
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
        setContentButton();
        if (drawerIsOpen) {
            mDrawerLayout.closeDrawer(mDrawerView);
        }
        updateCoverPhoto();

        backPressedCounter = 0;
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

    // Show a snackbar message to the user
    private void displayMessage(String string) {

        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_content);
        Snackbar snackbar = Snackbar.make(coordinatorLayout, string, Snackbar.LENGTH_LONG).setAction("UNDO", null);

        // Set text color
        snackbar.setActionTextColor(Color.RED);

        // Grab actual snackbar and set its color
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.DKGRAY);

        // Grab our text view
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(R.color.accent));

        // Show our work
        snackbar.show();
    }

    // Set up tracking button
    private void setUpTrackingButtons() {

        final SwitchCompat trackingButton = (SwitchCompat) findViewById(R.id.tracking_toggle);

        // If we do not yet have a trail, we need to set this as ivisible.
        if (PreferenceManager.getDefaultSharedPreferences(this).getString("TRAILID", "0").equals("0")) {
            trackingButton.setVisibility(View.GONE);
            return;
        } else {
            // Set visibility true and continue on.
            trackingButton.setVisibility(View.VISIBLE);
        }


        // Grab a fused location provider from our user class so we can track.
        final BreadCrumbsFusedLocationProvider breadCrumbsFusedLocationProvider = new BreadCrumbsFusedLocationProvider(this);

        // Check if we were already tracking.
        isTracking = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("TRACKING", false);

        // Our listner for this button. Toggles tracking using fused service, and updates flag.
        trackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop tracking

                if (isTracking) {
                    // Stop and save here.
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("TRACKING", false).commit();
                    breadCrumbsFusedLocationProvider.StopBackgroundGPSSerivce();
                    isTracking = false;
            }
                // Start tracking
                else {
                    String trailId = PreferenceManager.getDefaultSharedPreferences(context).getString("TRAILID", "-1");
                    if (!trailId.equals("-1")) {
                        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("TRACKING", true).commit();
                        breadCrumbsFusedLocationProvider.StartBackgroundGPSService();
                        isTracking = true;
                    } else {
                        // Show dialog - cant start tracking
                        showNewTrailDialog();
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
            updateViewElementWithProperty.UpdateTextViewElement(belongsTo, userId, "Username");
        } else {
            belongsTo.setText(name);
        }
        updateCoverPhoto();


    }

    private void updateCoverPhoto () {
        final ImageView background = (ImageView) findViewById(R.id.drawer_background);
        String coverPhotoId = PreferenceManager.getDefaultSharedPreferences(context).getString("COVERPHOTOID", "-1");
        String userId = PreferenceManager.getDefaultSharedPreferences(this).getString("USERID", globalContainer.GetUserId());
        if (coverPhotoId.equals("-1")) {
            String imageIdUrl = LoadBalancer.RequestServerAddress() + "/rest/login/GetPropertyFromNode/"+userId+"/CoverPhotoId";
            AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(imageIdUrl, new AsyncDataRetrieval.RequestListener() {
                @Override
                public void onFinished(String result) {
                    Glide.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+result + ".jpg").centerCrop().crossFade().into(background);
                }
            });
            asyncDataRetrieval.execute();
        } else {
            Glide.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+coverPhotoId + ".jpg").centerCrop().crossFade().into(background);
        }
    }

    // This is the setup for the  add button on the homepage. If the user has no trails we need
    // have a create trail button there. If not, we just have the video.
    private void setContentButton() {

        FloatingActionButton addCrumb = (FloatingActionButton) findViewById(R.id.new_content);
        String trailId = PreferenceManager.getDefaultSharedPreferences(this).getString("TRAILID", "-1");

        if (trailId.equals("-1")) {
            // Set image to be a + to indicate creating a new trail.
            addCrumb.setVisibility(View.GONE);
        } else {
            addCrumb.setImageResource(R.drawable.ic_action_camera);
            addCrumb.setVisibility(View.VISIBLE);
            addCrumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent cameraIntent = new Intent();
                    cameraIntent.setClassName("com.breadcrumbs", "com.breadcrumbs.client.Camera.CameraCapture");
                    startActivity(cameraIntent);
                }
            });
        }
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
                    ArrayList<String> newIds = new ArrayList<>();
                    ArrayList<String> oldIds = globalContainer.GetTrailIdsCurrentlyDisplayed();

                    // Look through all our old Ids and see if we have already loaded them.
                    if (oldIds == null || allIds.size() == oldIds.size()) {
                        // This should not happen, but it may.
                        displayMessage("Trails up to date");
                        return;
                    }
                    // This is for efficiency. However in the mean time it is only text so I am just going to reload all.
                  /*  for (String item:allIds) {
                        if (!oldIds.contains(item)){
                            // Then we have a new Id, so add it to the end.
                            newIds.add(item);
                        }
                    }*/

                    globalContainer.SetTrailIdsCurrentlyDisplayed(allIds);

                    mAdapter = new HomeCardAdapter(allIds, context);
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
        });
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
                    // Get our arrayList for the card adapter
                    JSONObject jsonResult = new JSONObject(result);
                    ArrayList<String> ids = convertJSONToArrayList(jsonResult);
                    globalContainer.SetTrailIdsCurrentlyDisplayed(ids);
                    // Create the adapter, and set it to the recyclerView so that it displays
                    mAdapter = new HomeCardAdapter(ids, context);
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
        });
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
        String trailid = PreferenceManager.getDefaultSharedPreferences(this).getString("TRAILID", "-1");
        if (trailid.equals("-1")) {
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
        Intent newIntent = new Intent();

        //Load the correct page.
        switch (position) {
            case 0:
                newIntent.setClassName("com.breadcrumbs", "com.breadcrumbs.client.ProfilePageViewer");
                newIntent.putExtra("userId", globalContainer.GetUserId());
                newIntent.putExtra("name", name);
                startActivity(newIntent);
                break;
            case 1:
                newIntent.setClassName("com.breadcrumbs", "com.breadcrumbs.client.EditTrail");
                startActivity(newIntent);
                break;

            case 2:
                newIntent.setClassName("com.breadcrumbs", "com.breadcrumbs.client.TrailManager");
                startActivity(newIntent);
                break;
            case 3:
                // Check if we have a trail created. If not we need to tell the user why we cannot take any photos
                String trailId = PreferenceManager.getDefaultSharedPreferences(context).getString("TRAILID", "-1");
                if (!trailId.equals("-1")) {
                    newIntent.setClassName("com.breadcrumbs", "com.breadcrumbs.client.Camera.CameraCapture");
                    startActivity(newIntent);
                } else {
                    // We need to show the user a dialog, and give them the option of creating a new trail
                    showNewTrailDialog();
                }
                break;
        }

        mDrawerList.setItemChecked(position, true);
        mDrawerList.setSelection(position);
    }

    private void showNewTrailDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.no_user_trail_dialog);

       /* TextView createTrailButton = (TextView) dialog.findViewById(R.id.no_trail_dialog_create_trail_button);
        createTrailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent newIntent = new Intent();
                newIntent.setClassName("com.breadcrumbs", "com.breadcrumbs.client.EditTrail");
                startActivity(newIntent);

            }
        });*/
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

}

	

