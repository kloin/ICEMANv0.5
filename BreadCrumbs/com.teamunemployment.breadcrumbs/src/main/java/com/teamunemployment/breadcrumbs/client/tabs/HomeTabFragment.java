package com.teamunemployment.breadcrumbs.client.tabs;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.teamunemployment.breadcrumbs.Dialogs.IDialogCallback;
import com.teamunemployment.breadcrumbs.Dialogs.SimpleMaterialDesignDialog;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UpdateViewElementWithProperty;
import com.teamunemployment.breadcrumbs.Preferences.Preferences;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Trails.TrailManagerWorker;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.Cards.HomeCardAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by jek40 on 3/03/2016.
 */
public class HomeTabFragment extends Fragment {
    public RecyclerView mRecyclerView;
    public RecyclerView.LayoutManager mLayoutManager;
    public HomeCardAdapter mAdapter;
    public SwipeRefreshLayout mSwipeRefreshLayout;
    public AsyncDataRetrieval clientRequestProxy;
    public GlobalContainer globalContainer;
    public Context context;
    public View rootView;
    public Activity activityContext;
    public String userId;
    private final String TAG = "HOME_TAB";

    private PreferencesAPI preferencesAPI;
    private static final int REQUESTED_LOCATION_WITH_START_TRAIL_QUEUED = 8;

    private final ArrayList<String> ids = new ArrayList<>();
    public void onAttach(Activity activity) {
        activityContext= activity;
        super.onAttach(activity);
    }

    final SimpleAnimations simpleAnimations = new SimpleAnimations();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
                parent.removeView(rootView);
        }
        userId = PreferenceManager.getDefaultSharedPreferences(activityContext).getString("USERID", "-1");
        rootView = inflater.inflate(R.layout.home_tab_fragment, container, false);
        context = rootView.getContext();
        preferencesAPI = new PreferencesAPI(context);
        setShitUp();
        return rootView;
    }

    public void setShitUp() {
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshWrapper);
        globalContainer = GlobalContainer.GetContainerInstance();
        setUpRefreshLayout();
        //    use this setting to improve performance if you know that changes
        //   in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        //    use a linear layout manager
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        TextView heading = (TextView) rootView.findViewById(R.id.heading);
        heading.setText("Home");
        loadTrails();
        setUpListenerForNewTrailButton();
    }

    private void setUpListenerForNewTrailButton() {
        rootView.findViewById(R.id.create_new_trail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleMaterialDesignDialog.Build(context)
                        .SetTitle("New Trip")
                        .SetTextBody("Are you sure you want to create a new trip?")
                        .SetActionWording("Create Trip")
                        .UseCancelButton(true)
                        .SetCallBack(createTrailCallback())
                        .Show();
            }
        });
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

    public void setUpRefreshLayout() {
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

    public void reloadTrails() {
        // Our url - just gets a json string of all trail ids.
        String url = LoadBalancer.RequestServerAddress() + "/rest/User/GetAllHomePageTrailIdsForAUser/"+userId;
        url = url.replaceAll(" ", "%20");
        clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                try {
                    //Toast.makeText(context, "Working",Toast.LENGTH_LONG).show();
                    // Get our arrayList for the card adapter
                    CardView networkIssueCard = (CardView) rootView.findViewById(R.id.network_issue_placeholder);
                    if (result.equals("NE1")) {

                        simpleAnimations.FadeInView(networkIssueCard);
                        networkIssueCard.setVisibility(View.VISIBLE);
                        mAdapter = new HomeCardAdapter(ids, context);
                        mRecyclerView.setAdapter(mAdapter);
                        return;
                    }
                    networkIssueCard.setVisibility(View.GONE);
                    JSONObject jsonResult = new JSONObject(result);
                    ArrayList<String> allIds = convertJSONToArrayList(jsonResult);
                    ArrayList<String> newIds = new ArrayList<>();
                    ArrayList<String> oldIds = globalContainer.GetTrailIdsCurrentlyDisplayed();

                    // Look through all our old Ids and see if we have already loaded them.
                    //if (oldIds == null || allIds.size() == oldIds.size()) {
                        // This should not happen, but it may.
                    //    displayMessage("Trails up to date");
                    //    return;
                   // }
                    // This is for efficiency. However in the mean time it is only text so I am just going to reload all.
                  /*  for (String item:allIds) {
                        if (!oldIds.contains(item)){
                            // Then we have a new Id, so add it to the end.
                            newIds.add(item);
                        }
                    }*/
                    if (allIds.size() == 0) {
                        return;
                    }
                    globalContainer.SetTrailIdsCurrentlyDisplayed(allIds);

                    mAdapter = new HomeCardAdapter(allIds, context);
                    mRecyclerView.setAdapter(mAdapter);
                    // Hide the placeholder
                    TextView placeholder = (TextView) rootView.findViewById(R.id.no_favs_placeholder);
                    placeholder.setVisibility(View.GONE);
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
        }, context);
        clientRequestProxy.execute();
    }

    public String ConstructDataUrl() {
        String url = LoadBalancer.RequestServerAddress() + "/rest/User/GetAllHomePageTrailIdsForAUser/"+userId;
        Log.d(TAG, "Attempting to load Trails with URL: " + url);
        url = url.replaceAll(" ", "%20");
        return url;
    }

    private void hideLoadingSpinner() {
        // Hide loading spinner
        ProgressBar loadingSpinner = (ProgressBar) rootView.findViewById(R.id.explore_progress_bar);
        loadingSpinner.setVisibility(View.GONE);
    }

    public void loadTrails() {
        if (ids.isEmpty()) {
            String url = ConstructDataUrl();
            load(url);

            return;
        }
        hideLoadingSpinner();
        setAdapter();
    }

    public void load(String url) {
        clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                resolveNetworkResponse(result);
            }
        }, context);
        clientRequestProxy.execute();
        Log.i("BASE", "Sending request to construct the cards");
    }

    private void resolveNetworkResponse(String result) {
        try {
            Log.d(TAG, "Finished loading trails. Result : " + result);


            hideLoadingSpinner();

            // Check for network error
            CardView networkIssueCard = (CardView) rootView.findViewById(R.id.network_issue_placeholder);
            if (result.equals("NE1")) {
                simpleAnimations.FadeInView(networkIssueCard);
                networkIssueCard.setVisibility(View.VISIBLE);
                return;
            }

            // otherwise we want to ensure that the network issue card is not visible.
            networkIssueCard.setVisibility(View.GONE);

            // Add our json array to ids
            JSONObject jsonResult = new JSONObject(result);
            convertJSONToArrayList(jsonResult);

            if (ids.size() == 0) {
                Log.d(TAG, "Found some data, setting up placeholder visibility");
                // Hide placeholder.
                TextView cardPlaceholder = (TextView) rootView.findViewById(R.id.no_favs_placeholder);
                simpleAnimations.FadeInView(cardPlaceholder);
                cardPlaceholder.setVisibility(View.VISIBLE);
            }
            setAdapter();

        } catch (JSONException e) {
            // simpleAnimations.FadeInView(noDataPlaceholder);
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.e("Cards", "Failed to convert String to json");
            //simpleAnimations.FadeInView(noDataPlaceholder);
        }
    }

    private void setAdapter() {
        // Create the adapter, and set it to the recyclerView so that it displays
        mAdapter = new HomeCardAdapter(ids, context);
        mRecyclerView.setAdapter(mAdapter);
    }

    // CAN TAKE THIS TO THE MODEL
    // Covert our json result into an arrayList
    private ArrayList<String> convertJSONToArrayList(JSONObject result) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
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
}
