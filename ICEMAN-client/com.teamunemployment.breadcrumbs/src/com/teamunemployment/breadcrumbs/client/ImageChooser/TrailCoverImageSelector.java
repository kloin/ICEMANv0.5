package com.teamunemployment.breadcrumbs.client.ImageChooser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.Framework.JsonHandler;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.Cards.ImageChooserGridViewAdapter;

import java.util.ArrayList;

/**
 * Written by Josiah Kendall on 22/01/2016.
 */
public class TrailCoverImageSelector  extends Activity{
    private TextView emptyGridInfo;
    public ArrayList<String> idsArray;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_image_layout_wrapper);
        context = this;
        setShitUp();
    }

    public void setShitUp() {
        emptyGridInfo = (TextView) findViewById(R.id.empty_grid_placeholder);
        /*ImageButton backButton = (ImageButton) rootView.findViewById(R.id.backButtonCapture);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityContext.finish();
            }
        });*/
        setUpGridAndListeners();
        //Get all trails then set the adapter.
    }

    public void setUpGridAndListeners() {
        final String userId = PreferenceManager.getDefaultSharedPreferences(this).getString("USERID", "-1");
        emptyGridInfo = (TextView) findViewById(R.id.empty_grid_placeholder);
        final GridView gridview = (GridView)  findViewById(R.id.gridView1);
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
                ImageChooserGridViewAdapter adapter = new ImageChooserGridViewAdapter(idsArray, context);
                gridview.setAdapter(adapter);
                gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        // Save our profile header pic.
                        String newPicId = idsArray.get(position);

                        // Save our new selection locally too.
                        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("TRAILCOVERPHOTO", newPicId).commit();
                        Intent returnIntent = new Intent();
                        Activity activityContext = (Activity) context;
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
