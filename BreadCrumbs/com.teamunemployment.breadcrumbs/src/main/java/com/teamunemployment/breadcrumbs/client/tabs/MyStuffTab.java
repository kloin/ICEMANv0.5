package com.teamunemployment.breadcrumbs.client.tabs;

import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.Cards.HomeCardAdapter;
import com.teamunemployment.breadcrumbs.client.Cards.MeCardAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by jek40 on 10/05/2016.
 */
public class MyStuffTab extends HomeTabFragment {
    private final String TAG = "MY_STUFF_TAB";
    private final ArrayList<String> ids = new ArrayList<>();
    private PreferencesAPI preferencesAPI;
    private MeCardAdapter meCardAdapter;
    private boolean loaded = false;
    public void loadTrails() {
        preferencesAPI = new PreferencesAPI(context);
        String userId = preferencesAPI.GetUserId();

        /*
            If we have already saved the user id, then we could assume that we have added all the others too
            This is not actually reasonable however - we may load up offline which will add the userId, and the
            local trail ids but not the trails on the server. If we have loaded, we should have a cache so we can look for that.
            The reason this check is here is to stop duplicates being added, because we have a fianl list of
            items. The list doest not get destroyed when the tab gets recycled (i.e when we scroll to the first tab,
            this tab gets recycled.) but the tab does. When we re create this tab, this method will be called again
            by the parent class.
          */
        if (!loaded)  {
            ids.add(userId);
            // Adds our active trail to the list
            fetchMyActiveTrailId();

            // Adds oour saved trails to the list
            fetchMySavedTrailIds();

            // finally, add our inactive trails to the list
            fetchMySavedInactiveTrailIds();

            // FIXME - this is a bug that needs to be sorted - read the speel above.
            loaded = true;
        }
        meCardAdapter = new MeCardAdapter(ids, context);
        mRecyclerView.setAdapter(meCardAdapter);
    }

    // Fetch the active
    private void fetchMyActiveTrailId() {
        // Get active trail from database.
        // add it to list at position 1
        // make sure you prefix id with L
        String currentLocalTrail = Integer.toString(preferencesAPI.GetLocalTrailId());
        if (currentLocalTrail != null) {
            ids.add(currentLocalTrail +"L");
        }
    }

    // Fetch all our previous trail ids that are saaved to the database.
    private void fetchMySavedTrailIds() {
        // fetch forom url, add
    }


    // Fetch all the trails from the database that we once used, but are no longer active. We can activate them from this tab
    private void fetchMySavedInactiveTrailIds() {

    }

}
