package com.teamunemployment.breadcrumbs.client.ImageChooser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.Framework.JsonHandler;
import com.teamunemployment.breadcrumbs.Models;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.Cards.ImageChooserGridViewAdapter;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Written by Josiah Kendall on 22/01/2016.
 */
public class TrailCoverImageSelector extends Activity {
    public TextView emptyGridInfo;
    public ArrayList<String> idsArray;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_image_layout_wrapper);
        findViewById(R.id.select_profile_cover_photo_toolbar).setVisibility(View.VISIBLE);
        idsArray = new ArrayList<>();
        context = this;
        setShitUp();
    }

    public void setShitUp() {
        emptyGridInfo = (TextView) findViewById(R.id.empty_grid_placeholder);
        SetUpGridView();
        goBackListener();
    }

    /**
     * Set up our gird view with the items it needs, and the way to handle clicks.
     */
    public void SetUpGridView() {
        int trailId = new PreferencesAPI(context).GetServerTrailId();
        emptyGridInfo = (TextView) findViewById(R.id.empty_grid_placeholder);
        final GridView gridview = (GridView) findViewById(R.id.gridView1);
        String allImagesUrl = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetAllPhotoIdsForATrail/" + trailId;

        fetchLocalImages();
        if (trailId != -1) {
            fetchRemoteImages(allImagesUrl, gridview);
            return;
        }
        if (idsArray.size() < 1) {
            // We want to show the placeholder.
            emptyGridInfo.setVisibility(View.VISIBLE);
            SimpleAnimations simpleAnimations = new SimpleAnimations();
            simpleAnimations.FadeInView(emptyGridInfo);
        }
        ImageChooserGridViewAdapter adapter = new ImageChooserGridViewAdapter(idsArray, context);
        gridview.setAdapter(adapter);
        setGridViewItemClickHandler(gridview);
    }

    private void fetchRemoteImages(String url, final GridView gridView) {
        // This is the netork reqeust that sends out the request for the all our photo ids.
        AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                // Convert the json result to an array;
                JsonHandler jsonHandler = new JsonHandler();
                idsArray.addAll(jsonHandler.ConvertJSONStringToArrayList(result));
                // Error use case.
                if (idsArray.size() < 1) {
                    // We want to show the placeholder.
                    emptyGridInfo.setVisibility(View.VISIBLE);
                    SimpleAnimations simpleAnimations = new SimpleAnimations();
                    simpleAnimations.FadeInView(emptyGridInfo);
                }
                if (idsArray.size() == 0) {
                    return;
                }
                //Create our image chooser adapter.
                ImageChooserGridViewAdapter adapter = new ImageChooserGridViewAdapter(idsArray, context);
                gridView.setAdapter(adapter);
                setGridViewItemClickHandler(gridView);
            }
        }, context);
        asyncDataRetrieval.execute();
    }

    /**
     * Method that adds our locally saved images to the class scope of ids we want to load {@link #idsArray}.
     */
    private void fetchLocalImages() {
        // Fetch local ids.
        DatabaseController databaseController = new DatabaseController(context);
        JSONObject crumbs = databaseController.GetAllCrumbs(null);
        // add them all to the list with and "L" attatched to the back of the ids so that we know to load from the local db.
        appendLocalImages(crumbs);

    }

    private void goBackListener() {
        ImageView goBackImageView = (ImageView) findViewById(R.id.trail_cover_photo_back_button);
        goBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // Append the crumbs to our json.
    private void appendLocalImages(JSONObject crumbs) {
        Iterator<String> keys = crumbs.keys();

        // Iterate through the keys and add each one of them to our list.
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                JSONObject tempObject = crumbs.getJSONObject(key);
                String id = tempObject.getString(Models.Crumb.EVENT_ID);
                // Filter out videos.
                if (tempObject.getString(Models.Crumb.EXTENSION).endsWith("g")) {
                    /// Have to add a local flag so we know its local.
                    idsArray.add(id + "L");
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setGridViewItemClickHandler(final GridView gridView) {
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // Save our profile header pic.
                String newPicId = idsArray.get(position); // Bug should occur here. This is a sketchy way to grab id.

                // Save our new selection locally too.
                new PreferencesAPI(context).SetCurrentTrailCoverPhoto(newPicId);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("Id", newPicId );

                Activity activityContext = (Activity) context;
                activityContext.setResult(Activity.RESULT_OK, returnIntent);

                // Quiting on select for now.
                activityContext.finish();
            }
        });
    }
}
