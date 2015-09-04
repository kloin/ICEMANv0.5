package com.breadcrumbs.client;

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
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.ServiceProxy.AsyncDataRetrieval;
import com.breadcrumbs.ServiceProxy.UpdateViewElementWithProperty;
import com.breadcrumbs.caching.GlobalContainer;
import com.breadcrumbs.client.Cards.HomeCardAdapter;
import com.breadcrumbs.client.NavigationDrawer.DrawerItemCustomAdapter;
import com.breadcrumbs.client.NavigationDrawer.ObjectDrawerItem;

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
    private LinearLayout mDrawerView;
    private int backPressedCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpager_container);
        context = this;

        globalContainer = GlobalContainer.GetContainerInstance();
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerView= (LinearLayout) findViewById(R.id.drawer_holder);
        mDrawerLayout.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ObjectDrawerItem[] drawerItem = new ObjectDrawerItem[3];
        drawerItem[0] = new ObjectDrawerItem(R.drawable.ic_action_person_dark, "Profile");
        drawerItem[1] = new ObjectDrawerItem(R.drawable.ic_action_new_dark, "Create Trail");
        drawerItem[2] = new ObjectDrawerItem(R.drawable.ic_action_camera_big, "Capture");
        DrawerItemCustomAdapter adapter = new DrawerItemCustomAdapter(this, R.layout.nav_drawer_item, drawerItem);
        mDrawerList.setAdapter(adapter);

        // The array which holds our Objects which manage each drawer item.


        // Set up views, elements
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setUpTrackingButtons();
        setUpDrawerToggle();
        loadTrails();
        setUserName();
        setContentButton();


    }

    // Not sure what I'm going to do with this yet.
    private void setUpDrawerToggle() {
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        }; // Drawer Toggle Object Made
    }

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
        setContentButton();
        backPressedCounter = 0;
    }

    @Override
    public void onBackPressed() {
        if (backPressedCounter > 0) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        } else {
            backPressedCounter += 1;
           displayMessage("Press Back Button Again to Exit");
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

        final Button trackingButton = (Button) findViewById(R.id.tracking_toggle);

        // Grab a fused location provider from our user class so we can track.
        final BreadCrumbsFusedLocationProvider breadCrumbsFusedLocationProvider = new BreadCrumbsFusedLocationProvider(this);

        // Check if we were already tracking.
        isTracking = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("TRACKING", false);

        // If we are already tracking at start of app, we want to set button text as stop.
        if (isTracking) {
            trackingButton.setText("STOP TRACKING");
        }

        // Our listner for this button. Toggles tracking using fused service, and updates flag.
        trackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop tracking
                if (isTracking) {
                    // Stop and save here.
                    trackingButton.setText("START TRACKING");
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("TRACKING", false).commit();
                    isTracking = false;
                }

                // Start tracking
                else {
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("TRACKING", true).commit();
                    breadCrumbsFusedLocationProvider.StartBackgroundGPSService();
                    trackingButton.setText("STOP TRACKING");
                    isTracking = true;
                }
            }
        });
    }

    // Set our users name in the drawer. This has some bugs I think
    private void setUserName() {

        String userId = PreferenceManager.getDefaultSharedPreferences(this).getString("USERID", globalContainer.GetUserId());
        TextView belongsTo = (TextView) findViewById(R.id.belongs_to);
        UpdateViewElementWithProperty updateViewElementWithProperty = new UpdateViewElementWithProperty();
        updateViewElementWithProperty.UpdateTextViewElement(belongsTo, userId, "Username");
    }

    // This is the setup for the  add button on the homepage. If the user has no trails we need
    // have a create trail button there. If not, we just have the video.
    private void setContentButton() {

        FloatingActionButton addCrumb = (FloatingActionButton) findViewById(R.id.new_content);
        String trailId = PreferenceManager.getDefaultSharedPreferences(this).getString("TRAILID", "-1");

        if (trailId.equals("-1")) {
            // Set image to be a + to indicate creating a new trail.
            addCrumb.setImageResource(R.drawable.ic_action_new);
            // Set the click handler to create a new trail. Update the image also.
            addCrumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create new trail
                    Intent newIntent = new Intent();
                    newIntent.setClassName("com.breadcrumbs.client", "com.breadcrumbs.client.EditTrail");
                    startActivity(newIntent);
                }
            });
        } else {
            addCrumb.setImageResource(R.drawable.ic_action_camera);
            addCrumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent cameraIntent = new Intent();
                    cameraIntent.setClassName("com.breadcrumbs.client", "com.breadcrumbs.client.Camera.CameraCapture");
                    startActivity(cameraIntent);
                }
            });
        }
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
                    ArrayList<String> arrayList = convertJSONToArrayList(jsonResult);
                    // Create the adapter, and set it to the recyclerView so that it displays
                    mAdapter = new HomeCardAdapter(arrayList, context);
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
        //mDrawerLayout.closeDrawer(mDrawerView);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        decideIfCameraOrNewTrailButton(menu);
        return super.onPrepareOptionsMenu(menu);
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
        String userId = PreferenceManager.getDefaultSharedPreferences(this).getString("USERID", "-1");
        String url = MessageFormat.format("{0}/rest/User/GetAllEditibleTrailsForAUser/{1}",
                LoadBalancer.RequestServerAddress(),
                userId);
        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .main_content);

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        Intent newIntent = new Intent();

        //Load the correct page.
        switch (position) {
            case 0:
                newIntent.setClassName("com.breadcrumbs.client", "com.breadcrumbs.client.ProfilePageViewer");
                newIntent.putExtra("userId", globalContainer.GetUserId());
                newIntent.putExtra("name", "kloin");
                startActivity(newIntent);
                break;
            case 1:
                newIntent.setClassName("com.breadcrumbs.client", "com.breadcrumbs.client.EditTrail");
                startActivity(newIntent);
                break;
            case 2:
                newIntent.setClassName("com.breadcrumbs.client", "com.breadcrumbs.client.Camera.CameraCapture");
                startActivity(newIntent);
                break;
        }

        mDrawerList.setItemChecked(position, true);
        mDrawerList.setSelection(position);
    }

}

	

