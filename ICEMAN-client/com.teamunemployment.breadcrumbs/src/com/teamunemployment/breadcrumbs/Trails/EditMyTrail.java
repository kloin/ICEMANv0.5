package com.teamunemployment.breadcrumbs.Trails;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UpdateViewElementWithProperty;
import com.teamunemployment.breadcrumbs.client.Adapters.CrumbCardEditAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by jek40 on 25/02/2016.
 */
public class EditMyTrail extends AppCompatActivity {

    private Activity context;
    private TextView startDateTextView;
    private boolean setFront = false;
    private String trailId = "0";
    private String coverId = "0";
    static final int PICK_PROFILE_REQUEST = 1;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<String> crumbsArray;
    private CrumbCardEditAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trail_editor_2);
        context = this;
        // setUpButtonListeners();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            trailId = extras.getString("TrailId");
            coverId = extras.getString("CoverId");
        } else {
            trailId = PreferenceManager.getDefaultSharedPreferences(context).getString("TRAILID", "-1");
        }

      //  setUpFields();
        setUpHeaderPhoto(trailId);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        crumbsArray = getIntent().getStringArrayListExtra("IdArray");
        // Fetch the crumbs, then build the
        setUpCollapsableToolbar("Trail Editor");
        mRecyclerView = (RecyclerView) findViewById(R.id.edit_crumb_recycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        loadCrumbs();
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsable_toolbar_holder);
        setBackButtonListener();
        setUpButtonListeners();
        //setToolbarTitle(collapsingToolbarLayout, trailId);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CodeFontWhite);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.HeaderFont);
    }

    private void setBackButtonListener() {
        ImageButton backButton = (ImageButton) findViewById(R.id.trail_edit_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void loadCrumbs() {
        String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetAllSavedCrumbIdsForATrail/"+trailId;
        url = url.replaceAll(" ", "%20");
        AsyncDataRetrieval clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                try {
                    // Hide loading spinner
                   // ProgressBar loadingSpinner = (ProgressBar) context.findViewById(R.id.explore_progress_bar);
                  //  loadingSpinner.setVisibility(View.GONE);
                    // Get our arrayList for the card adapter
                    JSONObject jsonResult = new JSONObject(result);
                    ArrayList<String> ids = convertJSONToArrayList(jsonResult);
                    //globalContainer.SetTrailIdsCurrentlyDisplayed(ids);
                    // Create the adapter, and set it to the recyclerView so that it displays
                    ids.add(0, trailId); // Add the trail Id to the base, so that we can load the trailDetails for the first card.
                    mAdapter = new CrumbCardEditAdapter(ids, context);
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

    private void deleteTrail(String trailId) {
        String url = LoadBalancer.RequestServerAddress() + "/rest/login/DeleteNode/" + trailId;
        HTTPRequestHandler requestHandler = new HTTPRequestHandler();
        requestHandler.SendSimpleHttpRequest(url);
        // Hopefully that is all I need to do. May need to return something here if it fails.
    }

    private void showDeleteDialog(final String trailId) {
        final Dialog dialog = new Dialog(context);
         dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.delete);
        TextView dialogButton = (TextView) dialog.findViewById(R.id.cancel_dialog_button);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        TextView deleteButton = (TextView) dialog.findViewById(R.id.delete_dialog_button);
        // if button is clicked, close the custom dialog
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // what if this fails?
                deleteTrail(trailId);
                // Remove trailIdFromPreferences so that we know that we no longer have a trail.
                PreferenceManager.getDefaultSharedPreferences(context).edit().remove("TRAILID").commit();
                dialog.dismiss();
                finish();
            }
        });
        dialog.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // This is the check for when we return with no data. Usually when the user hits the back button
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                // Load our new image
                final ImageView header = (ImageView) findViewById(R.id.headerPicture);
                String id = PreferenceManager.getDefaultSharedPreferences(context).getString("TRAILCOVERPHOTO", "-1");
                Glide.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+id + ".jpg").centerCrop().crossFade().into(header);
                String saveUrl = LoadBalancer.RequestServerAddress() + "/rest/login/SaveStringPropertyToNode/"+ trailId + "/CoverPhotoId/" + id;
                HTTPRequestHandler simpleHttpRequest = new HTTPRequestHandler();
                simpleHttpRequest.SendSimpleHttpRequest(saveUrl);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private void setUpCollapsableToolbar(String name) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // mRecyclerView = (RecyclerView) findViewById(R.id.crumb_recycler);
        //  mRecyclerView.setHasFixedSize(true);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsable_toolbar_holder);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.HeaderFont);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setTitle(name);

        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout_profile);
        ViewGroup.LayoutParams layoutParams = appBarLayout.getLayoutParams();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int widthDouble = displaymetrics.widthPixels;
        layoutParams.height = widthDouble;
        appBarLayout.setLayoutParams(layoutParams);
        //setUpToggleEditModeListener();
    }

    private void setUpFields() {
        UpdateViewElementWithProperty updater = new UpdateViewElementWithProperty();

        EditText trailTitleEdit = (EditText) findViewById(R.id.title_edit);
        updater.UpdateEditTextElement(trailTitleEdit, trailId, "TrailName");

        EditText about = (EditText) findViewById(R.id.countries_edit);
        updater.UpdateEditTextElement(about, trailId, "Description");
    }

    private void setUpHeaderPhoto(String crumbId) {
        ImageView header = (ImageView) findViewById(R.id.headerPicture);
        // header.setLayoutParams(layoutParams);
        //header.setBackgroundResource(R.color.ColorPrimary);
        UpdateViewElementWithProperty viewElementWithProperty = new UpdateViewElementWithProperty();
        TextView textView = (TextView) findViewById(R.id.profile_select_prompt);
        viewElementWithProperty.UpdateImageViewElementAndHidePlaceholder(header, crumbId, "CoverPhotoId", context, textView);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //click handler here for loading up a profile selection page
                Intent intent = new Intent();
                intent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.ImageChooser.TrailCoverImageSelector");
                startActivityForResult(intent, PICK_PROFILE_REQUEST);
            }
        });
    }
    // Handler to create the trail based on the data we have been given
    private void setUpButtonListeners() {

        ImageButton delete = (ImageButton) findViewById(R.id.delete_trail);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog(trailId);
            }
        });
    }

    private void updateProperties() {
        EditText title = (EditText) findViewById(R.id.title_edit);
        EditText description = (EditText) findViewById(R.id.countries_edit);

        String newTitle = title.getText().toString();
        String newDescription = description.getText().toString();

        String titleUpdateUrl = LoadBalancer.RequestServerAddress() + "/rest/login/SaveStringPropertyToNode/"+trailId+"/TrailName/"+newTitle;
        String descriptionUpdateUrl = LoadBalancer.RequestServerAddress() + "/rest/login/SaveStringPropertyToNode/"+trailId+"/Description/"+newDescription;

        AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(titleUpdateUrl, null);
        asyncDataRetrieval.execute();
        AsyncDataRetrieval asyncDataRetrieval1 = new AsyncDataRetrieval(descriptionUpdateUrl, null);
        asyncDataRetrieval1.execute();
        finish();
    }


}
