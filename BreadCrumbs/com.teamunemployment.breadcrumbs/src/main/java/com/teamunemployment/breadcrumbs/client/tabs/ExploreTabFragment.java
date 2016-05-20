package com.teamunemployment.breadcrumbs.client.tabs;

import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.Cards.HomeCardAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Written By Josiah Kendall, 2016. All Rights reserved
 */
public class ExploreTabFragment extends HomeTabFragment {
    private static final String TAG = "EXPLORE_TAB";

    @Override
    public String ConstructDataUrl() {
        String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetAllTrailIds";
        Log.d(TAG, "Attempting to load Trails with URL: " + url);
        url = url.replaceAll(" ", "%20");
        return url;
    }
}

