package com.teamunemployment.breadcrumbs.client.ImageChooser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.Facebook.AccountManager;
import com.teamunemployment.breadcrumbs.Framework.JsonHandler;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.Cards.ImageChooserGridViewAdapter;
import com.teamunemployment.breadcrumbs.R;

import java.util.ArrayList;

/**
 * Created by jek40 on 10/12/2015.
 */
public class GridImageSelector extends Fragment {
    public Context context;
    public ArrayList<String> idsArray;
    private TextView emptyGridInfo;
    public Activity activityContext;
    public View rootView;
    private String userId;

    public void onAttach(Activity activity) {
        activityContext= activity;
        super.onAttach(activity);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
                parent.removeView(rootView);
        }
        userId = PreferenceManager.getDefaultSharedPreferences(activityContext).getString("USERID", "-1");
        rootView = inflater.inflate(R.layout.grid_image_layout_wrapper, container, false);
        context = rootView.getContext();
        setShitUp();
        return rootView;
    }

    public void setShitUp() {
        emptyGridInfo = (TextView) rootView.findViewById(R.id.empty_grid_placeholder);
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
        final String userId = PreferenceManager.getDefaultSharedPreferences(activityContext).getString("USERID", "-1");
        final GridView gridview = (GridView) rootView.findViewById(R.id.gridView1);
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
                        PreferenceManager.getDefaultSharedPreferences(activityContext).edit().putString("COVERPHOTOID", newPicId).commit();
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
