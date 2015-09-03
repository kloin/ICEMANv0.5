package com.breadcrumbs.client;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.ServiceProxy.AsyncDataRetrieval;
import com.breadcrumbs.ServiceProxy.AsyncRetrieveImage;
import com.breadcrumbs.ServiceProxy.HTTPRequestHandler;
import com.breadcrumbs.client.ImageChooser.ImageAdapter;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by aDirtyCanvas on 6/30/2015.
 */
public class BreadCrumbsImageSelector extends Activity {

    private String trailId;
    private String jsonResponseString;
    private LinearLayout imageHolder;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_chooser);
        trailId = this.getIntent().getExtras().getString("TrailId");
        SetUpImages();
    }

    private void SetUpImages() {
        // Get all the id's
        // load each id into the page
        AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(LoadBalancer.RequestServerAddress() +"/rest/TrailManager/GetAllSavedCrumbIdsForATrail/"+trailId, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                //Initialise our object, and attempt to construct it from the string.
                if (result != null) {
                    // Put all our images into a
                    ArrayList<String> imageIds = convertJsonToStringArray(result);
                    setUpGridView(imageIds);
                }
            }
        });
        asyncDataRetrieval.execute();
    }

    private void setUpGridView(final ArrayList<String> ids) {
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this, ids));
        // Once finished loading the images, construct the adapter and display the images
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // This is our trailLoading situation -
                String selectedId = ids.get(position);
                String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/SetCoverPhotoForTrail/"+trailId+"/"+selectedId;
                HTTPRequestHandler requestHandler = new HTTPRequestHandler();
                requestHandler.SendSimpleHttpRequest(url);
                finish();
            }
        });
    }
    // load the first three images into our options
    private ArrayList<String> convertJsonToStringArray(String jsonResult) {
        ArrayList<String> idsToReturn = new ArrayList<String>(); //What is going to happen when we have more than 1000
        JSONObject json = null;
        try {
            json = new JSONObject(jsonResult);
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String id = json.getString(key);
                idsToReturn.add(id);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // We are returning this shitty string array because thats what the adapter I copied and pasted
        // Uses. Should probably change this at a later date.
        return idsToReturn;
    }
}
