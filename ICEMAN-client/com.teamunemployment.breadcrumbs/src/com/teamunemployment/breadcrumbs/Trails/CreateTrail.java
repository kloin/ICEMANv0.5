package com.teamunemployment.breadcrumbs.Trails;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UpdateViewElementWithProperty;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.client.Cards.CrumbCardAdapter;
import com.teamunemployment.breadcrumbs.client.Cards.CrumbCardDataObject;
import com.teamunemployment.breadcrumbs.client.DialogWindows.DatePickerDialog;
import com.bumptech.glide.Glide;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * Created by aDirtyCanvas on 6/28/2015.
 */
public class CreateTrail extends AppCompatActivity implements DatePickerDialog.DatePickerDialogListener{

    private Context context;
    private TextView startDateTextView;
    private boolean setFront = false;
    private String trailId = "0";
    private String coverId = "0";
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<CrumbCardDataObject> crumbsArray;
    private CrumbCardAdapter mAdapter;
    static final int PICK_PROFILE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trail_editor_window);
        context = this;
        setUpButtonListeners();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            trailId = extras.getString("TrailId");
            coverId = extras.getString("CoverId");
            setUpFields();
        }
        setUpHeaderPhoto();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        crumbsArray = getIntent().getParcelableArrayListExtra("CrumbArray");
        String trailId = getIntent().getStringExtra("TrailId");
        setUpCollapsableToolbar("Create Trail");
        //mRecyclerView = (RecyclerView) findViewById(R.id.crumb_recycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
       // mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        //mLayoutManager = new LinearLayoutManager(this);
       // mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CrumbCardAdapter(crumbsArray, this);
        //mRecyclerView.setAdapter(mAdapter);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsable_toolbar_holder);
        //setBackButtonListener();
        //setToolbarTitle(collapsingToolbarLayout, trailId);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CodeFontWhite);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.HeaderFont);

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

    // Show a snackbar message to the user
    private void displayMessage(String string) {

        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        Snackbar snackbar = Snackbar.make(coordinatorLayout, string, Snackbar.LENGTH_LONG).setAction("UNDO", null);

        // Set text color
        snackbar.setActionTextColor(Color.WHITE);

        // Grab actual snackbar and set its color
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(getResources().getColor(R.color.ColorPrimary));

        // Grab our text view
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(R.color.white));

        // Show our work
        snackbar.show();
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

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
    }

    private void setUpFields() {
        UpdateViewElementWithProperty updater = new UpdateViewElementWithProperty();

        EditText trailTitleEdit = (EditText) findViewById(R.id.title_edit);
        updater.UpdateEditTextElement(trailTitleEdit, trailId, "TrailName");

        EditText about = (EditText) findViewById(R.id.countries_edit);
        updater.UpdateEditTextElement(about, trailId, "Description");
    }

    private void setUpHeaderPhoto() {
        ImageView header = (ImageView) findViewById(R.id.headerPicture);
       // header.setLayoutParams(layoutParams);
        //header.setBackgroundResource(R.color.ColorPrimary);
        if (coverId != null && !coverId.equals("0")) {
            Glide.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+coverId+".jpg").centerCrop().crossFade().into(header);
            TextView textView = (TextView) findViewById(R.id.profile_select_prompt);
            textView.setVisibility(View.GONE);
        }

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
       TextView saveTrail = (TextView) findViewById(R.id.trail_save);
        saveTrail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (trailId.equals("0")) {
                    createNewTrail();
                } else {
                    updateProperties();
                }


            }
        });


        ImageButton backButton = (ImageButton) findViewById(R.id.profile_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  if (isDirty) {
                    //save();
                   // Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                //}
                finish();
            }
        });
      /*  ImageView trailCoverPhoto = (ImageView) findViewById(R.id.trail_image);
        trailCoverPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageSelector = new Intent();
                imageSelector.putExtra("TrailId", "1");
                imageSelector.setClassName("com.teamunemployment.breadcrumbs.client", "com.teamunemployment.breadcrumbs.client.BreadCrumbsImageSelector");
                startActivity(imageSelector);
            }
        });*/

        //TextView startDateButton = (TextView) findViewById(R.id.start_date_picker);
        //startDateButton.setPaintFlags(startDateButton.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        //TextView endDateButton = (TextView) findViewById(R.id.end_date_picker);
        //endDateButton.setPaintFlags(endDateButton.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
//        startDateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openDatePicker();
//                setFront = true;
//            }
//        });
//
//        endDateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openDatePicker();
//                setFront = false;
//            }
//        });
//
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

    private void openDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog();
        datePickerDialog.show(getSupportFragmentManager(), "datepicker");
    }

    private void createNewTrail() {
        //Get the text for description etc..
        EditText trailTitleEdit = (EditText) findViewById(R.id.title_edit);
        EditText trailDescriptionEdit = (EditText) findViewById(R.id.countries_edit);

        String trailTitle = trailTitleEdit.getText().toString();
        if (trailTitle.isEmpty()) {
            displayMessage("Title is required");
            return;
        }

        String trailDescription = trailDescriptionEdit.getText().toString();
        if (trailDescription.isEmpty()) {
            trailDescription = " ";
        }
        // Not using preferences here seems risky but it also seems to be working. Maybe have a double check (i.e try both) if it is not working.
        final String userId = GlobalContainer.GetContainerInstance().GetUserId();//PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("USERID", "-1");

        String url = MessageFormat.format("{0}/rest/login/saveTrail/{1}/{2}/{3}",
                LoadBalancer.RequestServerAddress(),
                trailTitle,
                trailDescription,
                userId);

        url = url.replaceAll(" ", "%20");
        AsyncDataRetrieval asyncDataRetrieval  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {

            /*
             * Override for the
             */
            @Override
            public void onFinished(String result) {
                // Not sure I want to do anything here.
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("TRAILID", result).commit();
                String updateActiveTrailUrl = LoadBalancer.RequestServerAddress() + "/rest/login/SaveStringPropertyToNode/"+userId+"/ActiveTrail/"+result;
                AsyncDataRetrieval updateActiveTrail = new AsyncDataRetrieval(updateActiveTrailUrl, new AsyncDataRetrieval.RequestListener() {
                    @Override
                    public void onFinished(String result) {
                        // Dont actually need to do anything here.
                    }
                });
                updateActiveTrail.execute();
            }
        });


        asyncDataRetrieval.execute();
        finish();
    }

    /*
        Override our method in our DatePickerDialog, so that we can process and save data.
     */
    @Override
    public void onDateClick(int day, int month, int year) {
//        TextView endDateTextView = (TextView) findViewById(R.id.end_date_picker);
//        if (setFront) {
//            startDateTextView.setText(Integer.toString(day) + "/"+Integer.toString(month) + "/" + Integer.toString(year));
//        } else {
//            endDateTextView.setText(Integer.toString(day) + "/"+Integer.toString(month) + "/" + Integer.toString(year));
//        }
    }
}
