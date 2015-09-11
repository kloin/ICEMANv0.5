package com.breadcrumbs.client;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.ServiceProxy.AsyncDataRetrieval;
import com.breadcrumbs.caching.GlobalContainer;
import com.breadcrumbs.client.Cards.HomeCardAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Josiah Kendall on 9/9/2015.
 *
 * This class creates the "list" page to view all a users current trails, select the active trail,
 * as well as edit/delete.
 */
public class TrailManager extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private AppCompatActivity context;
    //private GlobalContainer globalContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trail_manager);

        //Need to set our context for use inside click handlers etc..
        context = this;

        // Set up our toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.trail_manager_toolbar);
        setSupportActionBar(toolbar);

        // Global container for ids etc if stuff isnt working. Be careful with memory here, because I am creating this on almost every page
        mRecyclerView = (RecyclerView) findViewById(R.id.trail_manager_recycler);
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Load all trails belonging to this user.
        loadTrails();
    }

    public void loadTrails() {

        String userId = PreferenceManager.getDefaultSharedPreferences(this).getString("USERID", "-1");;
        String url = LoadBalancer.RequestServerAddress() + "/rest/User/GetAllEditibleTrailsForAUser/"+userId;
        url = url.replaceAll(" ", "%20");
        AsyncDataRetrieval clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                try {
                    // Get our arrayList for the card adapter
                    JSONObject jsonResult = new JSONObject(result);
                    ArrayList<String> ids = convertJSONToArrayList(jsonResult);
                   HomeCardAdapter mAdapter = new HomeCardAdapter(ids, context);
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
