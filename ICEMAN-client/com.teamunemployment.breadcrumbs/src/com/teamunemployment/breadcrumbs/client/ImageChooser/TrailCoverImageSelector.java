package com.teamunemployment.breadcrumbs.client.ImageChooser;

import android.app.Activity;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.Framework.JsonHandler;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.Cards.ImageChooserGridViewAdapter;

/**
 * Written by Josiah Kendall on 22/01/2016.
 */
public class TrailCoverImageSelector  extends GridImageSelector{
    private TextView emptyGridInfo;
    @Override
    public void setUpGridAndListeners() {
        final String userId = PreferenceManager.getDefaultSharedPreferences(activityContext).getString("USERID", "-1");
        emptyGridInfo = (TextView) rootView.findViewById(R.id.empty_grid_placeholder);
        final GridView gridview = (GridView)  rootView.findViewById(R.id.gridView1);
        String allImagesUrl = LoadBalancer.RequestServerAddress() + "/rest/login/getallCrumbIdsForAUser/"+userId;
        AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(allImagesUrl, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                // Convert the json result to an array;
                JsonHandler jsonHandler = new JsonHandler();
                idsArray = jsonHandler.ConvertJSONStringToArrayList(result);
                if (idsArray.size() < 1) {
                    // We want to show the placeholder.
                    emptyGridInfo.setVisibility(View.VISIBLE);
                    SimpleAnimations simpleAnimations = new SimpleAnimations();
                    simpleAnimations.FadeInView(emptyGridInfo);
                }
                ImageChooserGridViewAdapter adapter = new ImageChooserGridViewAdapter(idsArray, activityContext);
                gridview.setAdapter(adapter);
                gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        // Save our profile header pic.
                        String newPicId = idsArray.get(position);

                        // Save our new selection locally too.
                        PreferenceManager.getDefaultSharedPreferences(activityContext).edit().putString("TRAILCOVERPHOTO", newPicId).commit();
                        Intent returnIntent = new Intent();
                        activityContext.setResult(Activity.RESULT_OK,returnIntent);

                        // Quiting on select for now.
                        activityContext.finish();
                    }
                });
            }
        });
        asyncDataRetrieval.execute();
    }
}
