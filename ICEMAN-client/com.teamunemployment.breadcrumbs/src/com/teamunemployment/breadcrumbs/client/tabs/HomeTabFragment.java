package com.teamunemployment.breadcrumbs.client.tabs;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UpdateViewElementWithProperty;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Trails.MyCurrentTrailManager;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.Cards.HomeCardAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

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
        loadTrails();
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

    // Show a snackbar message to the user
    public void displayMessage(String string) {

        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.test);
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

    public void reloadTrails() {
        // Our url - just gets a json string of all trail ids.
        String url = LoadBalancer.RequestServerAddress() + "/rest/User/GetAllHomePageTrailIdsForAUser/"+userId;
        url = url.replaceAll(" ", "%20");
        clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                try {
                    // Get our arrayList for the card adapter
                    CardView networkIssueCard = (CardView) rootView.findViewById(R.id.network_issue_placeholder);
                    if (result.equals("NE1")) {

                        simpleAnimations.FadeInView(networkIssueCard);
                        networkIssueCard.setVisibility(View.VISIBLE);
                        ArrayList<String> ids = new ArrayList<>();
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
                    CardView placeholder = (CardView) rootView.findViewById(R.id.card_view_placeholder);
                    placeholder.setVisibility(View.GONE);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.e("Cards", "Failed to reload trails : Stack Trace = " + e.toString());
                }
            }

           /* private ArrayList<HomeCardDataObject> convertHomeCardJSONToArrayList(JSONObject result) {
                ArrayList<HomeCardDataObject> details = new ArrayList<HomeCardDataObject>();
                Iterator<String> keys = result.keys();
                while (keys.hasNext()) {
                    String nextKey = keys.next();
                    try {
                        HomeCardDataObject dataObject
                        ids.add(result.getString(nextKey));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return ids;
            }*/

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
        //final TextView noDataPlaceholder = (TextView) rootView.findViewById(R.id.no_data_placeholder);
        String url = LoadBalancer.RequestServerAddress() + "/rest/User/GetAllHomePageTrailIdsForAUser/"+userId;
        Log.d(TAG, "Attempting to load Trails with URL: " + url);
        url = url.replaceAll(" ", "%20");
        clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                try {
                    Log.d(TAG, "Finished loading trails. Result : " + result);
                    // Hide loading spinner
                    ProgressBar loadingSpinner = (ProgressBar) rootView.findViewById(R.id.explore_progress_bar);
                    loadingSpinner.setVisibility(View.GONE);
                    CardView networkIssueCard = (CardView) rootView.findViewById(R.id.network_issue_placeholder);
                    if (result.equals("NE1")) {

                        simpleAnimations.FadeInView(networkIssueCard);
                        networkIssueCard.setVisibility(View.VISIBLE);
                        return;
                    }
                    networkIssueCard.setVisibility(View.GONE);
                    // Get our arrayList for the card adapter
                    JSONObject jsonResult = new JSONObject(result);
                    ArrayList<String> ids = convertJSONToArrayList(jsonResult);
                    if (ids.size() == 0) {
                    Log.d(TAG, "Found some data, setting up placeholder visibility");
                        // Hide placeholder.
                        CardView cardPlaceholder = (CardView) rootView.findViewById(R.id.card_view_placeholder);
                        simpleAnimations.FadeInView(cardPlaceholder);
                        cardPlaceholder.setVisibility(View.VISIBLE);
                       // simpleAnimations.FadeInView(noDataPlaceholder);
                        //noDataPlaceholder.setVisibility(View.VISIBLE);
                    }
                    globalContainer.SetTrailIdsCurrentlyDisplayed(ids);
                    // Create the adapter, and set it to the recyclerView so that it displays
                    mAdapter = new HomeCardAdapter(ids, context);
                    mRecyclerView.setAdapter(mAdapter);
                } catch (JSONException e) {
                   // simpleAnimations.FadeInView(noDataPlaceholder);
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.e("Cards", "Failed to convert String to json");
                    //simpleAnimations.FadeInView(noDataPlaceholder);
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
}
