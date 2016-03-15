package com.teamunemployment.breadcrumbs.client.tabs;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.Cards.HomeCardAdapter;
import com.teamunemployment.breadcrumbs.client.TrailActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Written By Josiah Kendall, 2016. All Rights reserved
 */
public class ExploreTabFragment extends HomeTabFragment {
    private static final String TAG = "EXPLORE_TAB";
    public void reloadTrails() {
        // Our url - just gets a json string of all trail ids.
        String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetAllTrailIds";
        Log.d(TAG, "Explore tab load start with url: " + url);
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

        String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetAllTrailIds";
        url = url.replaceAll(" ", "%20");
        clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                try {
                    // Hide loading spinner
                    ProgressBar loadingSpinner = (ProgressBar) rootView.findViewById(R.id.explore_progress_bar);
                    loadingSpinner.setVisibility(View.GONE);
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
}
