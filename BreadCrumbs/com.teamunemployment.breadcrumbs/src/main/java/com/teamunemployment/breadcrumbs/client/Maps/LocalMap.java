package com.teamunemployment.breadcrumbs.client.Maps;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.teamunemployment.breadcrumbs.BackgroundServices.MessageObjects.TrailObject;
import com.teamunemployment.breadcrumbs.BreadcrumbsActivityAPI;
import com.teamunemployment.breadcrumbs.BreadcrumbsLocationAPI;
import com.teamunemployment.breadcrumbs.Dialogs.IDialogCallback;
import com.teamunemployment.breadcrumbs.Dialogs.SimpleMaterialDesignDialog;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.NetworkConnectivityManager;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncSendLargeJsonParam;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UpdateViewElementWithProperty;
import com.teamunemployment.breadcrumbs.Preferences.Preferences;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils;
import com.teamunemployment.breadcrumbs.Trails.MyCurrentTrailDisplayManager;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.ImageChooser.TrailCoverImageSelector;
import com.teamunemployment.breadcrumbs.client.TrailManager;
import com.teamunemployment.breadcrumbs.database.DatabaseController;
import com.teamunemployment.breadcrumbs.database.Models.LocalTrailModel;
import com.teamunemployment.breadcrumbs.database.Models.TrailSummaryModel;


import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by jek40 on 22/05/2016.
 */
public class LocalMap extends MapViewer {
    private PreferencesAPI preferencesAPI;
    private String localTrailId;

    private BreadcrumbsLocationAPI locationAPI;
    private BreadcrumbsActivityAPI activityAPI;

    @Override
    public void DoBottomSheetClick() {
        switch (BOTTOM_SHEET_STATE) {
            case EDIT_MODE:
                setUpReadOnlyMode();
                break;
            case READ_ONLY_MODE:
                setUpEditMode();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (preferencesAPI == null) {
            preferencesAPI = new PreferencesAPI(context);
        }

        String coverId = preferencesAPI.GetCurrentTrailCoverPhoto();
        if (coverId == null) {
            return;
        }
        setCoverPhoto(coverId);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void SetUpDetailsItems() {
        findViewById(R.id.settings_my_trail).setVisibility(View.VISIBLE);
        findViewById(R.id.toggle_tracking).setVisibility(View.VISIBLE);
        findViewById(R.id.publish_trail).setVisibility(View.VISIBLE);

        setPublishClickListener();
    }

    private void setPublishClickListener() {
        FancyButton fancyButton = (FancyButton) findViewById(R.id.publish_trail);
        fancyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doPublish();
            }
        });
    }

    private void doPublish() {
        // Create the shell to hold our simple dialog.
        final SimpleMaterialDesignDialog materialDesignDialog = SimpleMaterialDesignDialog.Build(context);
        if (preferencesAPI == null) {
           preferencesAPI = new PreferencesAPI(context);
        }
        // Callback for the action button on the dialog.
        IDialogCallback onAccept = new IDialogCallback() {
            @Override
            public void DoCallback() {
                // Send save Request to server
                int serverTrailId = preferencesAPI.GetServerTrailId();
                if (serverTrailId == -1) {
                    boolean result = handleFirstTimePublishing();
                    if (result) {
                        //toggleButton(publishFab);
                    }
                } else {

                    boolean result = publishTrail(Integer.toString(serverTrailId));
                    if (result) {
                        //toggleButton(publishFab);
                    }
                }

            databaseController.SetLastUpdate(Integer.toString(preferencesAPI.GetLocalTrailId()), DateTime.now().toString());
            setLastUpdate(DateTime.now());
            }
        };

        // Make the dialog look and work the way we want
        materialDesignDialog.SetTitle("Publish Trip")
                .SetTextBody("This will publish your trip and make it visible to the world.")
                .SetActionWording("Publish")
                .SetCallBack(onAccept);

        // Show the dialog.
        materialDesignDialog.Show();
    }



    /**
     * Push the changes for a trail to the server
     * @param trailId
     * @return The trail publish service was successfully launched.
     */
    private boolean publishTrail(String trailId) {
        String trailName = fetchCurrentTrailName();

        // This is the use case where the trail title has no data. This is an issue an we cannot save
        if (trailName.isEmpty()) {
            return false;
        }

        int serverTrailId = preferencesAPI.GetServerTrailId();
        if (serverTrailId == -1) {
            // Issue, it should have this at this point
            return false;
        }

        // Save our name locally for future reference.
        preferencesAPI.SaveTrailNameString(trailName);

        // Save the trail name to the server too.
        HTTPRequestHandler httpRequestHandler = new HTTPRequestHandler();
        httpRequestHandler.SaveNodeProperty(Integer.toString(serverTrailId), "TrailName", trailName, context);

        // get trail from current index to now.
        TrailObject trailObject = new TrailObject(trailId);
        return true;
    }

    /**
     * Handle the use case where we have not saved our trail yet. This involves creating the trail
     * and saving the Id that gets returned.
     *
     */
    private boolean handleFirstTimePublishing() {
        String trailName = fetchCurrentTrailName();

        // This is the use case where the trail title has no data. This is an issue an we cannot save
        if (trailName.isEmpty()) {
            return false;
        }
        String userId = preferencesAPI.GetUserId();

        String url = MessageFormat.format("{0}/rest/login/saveTrail/{1}/{2}/{3}",
                LoadBalancer.RequestServerAddress(),
                trailName,
                " ",
                userId);
        Log.d("UPLOAD", "Attempting to create a new Trail with url: " + url);
        url = url.replaceAll(" ", "%20");

        AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) throws JSONException {
                //result is the id of the trail we just saved.
                preferencesAPI.SaveCurrentServerTrailId(Integer.parseInt(result));
                publishTrail(result);

            }
        }, this);
        asyncDataRetrieval.execute();
        return true;
    }

    private void saveLocalFileMetadataAndLoadMapWithResponse(String localTrailId) {
        initDatabaseController();
        int trailId = preferencesAPI.GetServerTrailId();
        if (trailId == -1 || !NetworkConnectivityManager.IsNetworkAvailable(context)) {
            doLocalLoad();
            return;
        }
        doProcessingOnServer(Integer.toString(trailId), localTrailId);
    }

    private void doProcessingOnServer(final String trailId, final String localTrailId) {
        int index = preferencesAPI.GetCurrentIndex();
        JSONObject json = databaseController.fetchMetadataFromDB(localTrailId, false);
        JSONObject wrapper = new JSONObject();
        try {
            wrapper.put("Events", json);
            wrapper.put("TrailId", trailId);
            wrapper.put("StartingIndex", index);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = MessageFormat.format("{0}/rest/TrailManager/SaveMetadataAndReturnIt/{1}",
                LoadBalancer.RequestServerAddress(),
                trailId);
        url = url.replaceAll(" ", "%20");
        // save our metadata and then do the loading with the metadata that gets returned.
        AsyncSendLargeJsonParam asyncJSON = new AsyncSendLargeJsonParam(url, new AsyncSendLargeJsonParam.RequestListener() {
            @Override
            public void onFinished(String metadataJSON) throws JSONException {
                // Our metadata gets returned, so now we have to load the map using the data that gets returned.
                Log.d("Result", metadataJSON);
                displayMetadata(new JSONObject(metadataJSON));
                mMap.setMyLocationEnabled(true);
                JSONObject crumbs = databaseController.GetAllCrumbs(localTrailId);
                DisplayCrumbsFromLocalDatabase(crumbs);
                Log.d(TAG, "Successfully retrieved crumbs: " + crumbs.toString());
                //  myCurrentTrailDisplayManager.DisplayCrumbsFromLocalDatabase(crumbs);
            }
        }, wrapper);
        asyncJSON.execute();
    }

    private void initDatabaseController() {
        if (databaseController == null) {
            databaseController = new DatabaseController(context);
        }
    }

    private void doLocalLoad() {
        // Check if we have a database controller - if not this method creates it.
        initDatabaseController();

        // We will use this multiple times - so get it now as a member variable.
        trailId = Integer.toString(preferencesAPI.GetLocalTrailId());

        // Build trail path from our database
        buildTrailPathFromLocalDB();

        // Grab the images and show them.
        displayCrumbsFromLocal();

        // The user is looking at their own trail, which should draw to where they are, so we zoom onto them.
        //zoomOnMyLocation();
    }

    private void buildTrailPathFromLocalDB() {
        List<LatLng> list = new ArrayList<>();
        JSONObject metadata = databaseController.fetchMetadataFromDB(trailId, false);
        Iterator<String> metadataKeys = metadata.keys();
        while (metadataKeys.hasNext()) {
            String next = metadataKeys.next();
            try {
                JSONObject node = metadata.getJSONObject(next);
                String latitude = node.getString("latitude");
                String longitude = node.getString("longitude");
                LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                list.add(point);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        // Need to load local trails here too.
        DrawPolyline(list);
    }

    private void displayCrumbsFromLocal() {
        JSONObject crumbsJson = databaseController.GetAllCrumbs(trailId);
        DisplayCrumbsFromLocalDatabase(crumbsJson);
    }

    public void DisplayCrumbsFromLocalDatabase(JSONObject crumbs) {
        // This creates the async request with a callback method of what I want completed when the
        try {
            // Get the crumb title ??? why do i do this? last crumb?
            Iterator<String> keys = crumbs.keys();
            JSONObject next = null;
            while (keys.hasNext()) {
                // The next node to get data from and Draw.
                String id = keys.next();
                next = crumbs.getJSONObject(id);
                mapDisplayManager.DrawLocalCrumbFromJson(next);
                mapDisplayManager.clusterManager.cluster();
            }
            // Now that we are done, we want to set the focus to the last crumb added

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.e("BC.MAP", "A JsonException was thrown during the display process for a crumb." +
                    "This probably means that you are missing a field on your json. Stacktrace follows");
            e.printStackTrace();
        }
        Log.i("MAP", "Finished Loading crumbs and displaying them on the map");
    }

    private String fetchCurrentTrailName() {
        EditText trailTitle = (EditText) findViewById(R.id.trail_title_input);
        Editable currentTitleEditable = trailTitle.getText();
        if (currentTitleEditable == null) {
            trailTitle.setError("Trip title must not be blank.");
            return "";
        }

        if (currentTitleEditable.toString().isEmpty()) {
            trailTitle.setError("Trip title must not be blank.");
            return "";
        }

        return currentTitleEditable.toString();
    }
    /**
     *	Define how the bottom sheet fab works in edit mode.
     */
    private void setUpEditMode() {
        BOTTOM_SHEET_STATE = EDIT_MODE;

        Activity act = (Activity) context;
        EditText trailName = (EditText) act.findViewById(R.id.trail_title_input);
        trailName.setEnabled(true);

        TextView tellThemToSelectACoverPhoto = (TextView) act.findViewById(R.id.cover_photo_prompt);
        SimpleAnimations.FadeInView(tellThemToSelectACoverPhoto);
        tellThemToSelectACoverPhoto.setOnClickListener(selectACoverPhoto());
        SimpleAnimations.ShrinkToggleAFab(bottomSheetFab, "#00E676", context.getResources().getDrawable(R.drawable.ic_action_accept));
    }

    private View.OnClickListener selectACoverPhoto() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               launchCoverPhotoSelector();
            }
        };
    }

    /**
     * Set read only mode.
     */
    private void setUpReadOnlyMode() {
        BOTTOM_SHEET_STATE = READ_ONLY_MODE;

        Activity act = (Activity) context;
        EditText trailName = (EditText) act.findViewById(R.id.trail_title_input);
        trailName.setEnabled(false);

        TextView tellThemToSelectACoverPhoto = (TextView) act.findViewById(R.id.cover_photo_prompt);
        SimpleAnimations.FadeOutView(tellThemToSelectACoverPhoto);

        // Save trailName and trailId.
        Editable trailNameEditable = trailName.getText();
        if (trailNameEditable != null) {
            saveTrailName(trailNameEditable.toString());
        }
        // Set the trail
        SimpleAnimations.ShrinkToggleAFab(bottomSheetFab, "#ffffff", context.getResources().getDrawable(R.drawable.ic_action_edit));
    }

    // save our trail name.
    private void saveTrailName(String trailName) {
        databaseController.SaveTrailName(trailName, preferencesAPI.GetLocalTrailId());
    }

    @Override
    public void SetBaseDetailsForATrail(String trailId) {
        // Load details
        if (databaseController == null) {
            databaseController = new DatabaseController(context);
        }

        if (preferencesAPI == null) {
            preferencesAPI = new PreferencesAPI(context);
        }
        if (trailId == null) {
            trailId = Integer.toString(preferencesAPI.GetLocalTrailId());
        }
        setCoverPhoto(preferencesAPI.GetCurrentTrailCoverPhoto());

        TrailSummaryModel trailSummaryModel = new TrailSummaryModel(databaseController.GetTrailSummary(trailId));
        LocalTrailModel localTrailModel = new LocalTrailModel(databaseController.GetAllCrumbs(trailId));

        TextView days = (TextView) bottomSheet.findViewById(R.id.duration_details);
        days.setText(trailSummaryModel.GetDaysDuration() + " Days");
        TextView pois = (TextView) bottomSheet.findViewById(R.id.number_of_crumbs_details);
        pois.setText(localTrailModel.getNumberOfPOI() + " Points of Interest");

        TextView videos = (TextView) bottomSheet.findViewById(R.id.number_of_videos_details);
        videos.setText(localTrailModel.getNumberOfVideos() + " Videos");

        TextView photosTextView = (TextView) bottomSheet.findViewById(R.id.number_of_photos_details);
        photosTextView.setText(localTrailModel.getNumberOfPhotos() + " Photos");

        EditText trailName = (EditText) bottomSheet.findViewById(R.id.trail_title_input);
        trailName.setText(trailSummaryModel.GetTripName());

        TextView trailTitle = (TextView) findViewById(R.id.bottom_sheet_trail_title);
        trailTitle.setText(trailSummaryModel.GetTripName());
        if (trailSummaryModel.GetLastUpdate() != null) {
            setLastUpdate(new DateTime(trailSummaryModel.GetLastUpdate()));
        }

        final SwitchCompat activeSwitch = (SwitchCompat) findViewById(R.id.toggle_tracking);
        activeSwitch.setChecked(preferencesAPI.isTrackingEnabledByUser());
        setUpToggleTrackingListener(activeSwitch);
    }

    private void setUpToggleTrackingListener(SwitchCompat activeSwitch) {
        activeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (locationAPI == null) {
                    locationAPI = new BreadcrumbsLocationAPI();
                }
                if (activityAPI == null) {
                    activityAPI = new BreadcrumbsActivityAPI();
                }

                if (isChecked) {
                    locationAPI.StartLocationService();
                    activityAPI.ListenToUserActivityChanges();
                    preferencesAPI.SetUserTracking(true);
                } else {
                    locationAPI.StopLocationService();
                    // FIXME Stop listening to activity changes too, otherwise we will just start tracking again when we recognize walking.
                    preferencesAPI.SetUserTracking(false);
                    preferencesAPI.SetTracking(false);
                }
            }
        });
    }

    /**
     * Set the last update on the bottom bar in hours or days (depending on how long its been)
     * @param lastUpdate The date time of the last update.
     */
    private void setLastUpdate(DateTime lastUpdate) {
        TextView lastUpdateTextView = (TextView) findViewById(R.id.last_update);

        DateTime currentDate = DateTime.now();
        int days = Days.daysBetween(lastUpdate.toLocalDate(), currentDate.toLocalDate()).getDays();
        if (days < 1) {
            int hours = Hours.hoursBetween(lastUpdate.toLocalDate(), currentDate.toLocalDate()).getHours();
            if (hours < 1) {
                lastUpdateTextView.setText("Today");
                lastUpdateTextView.setTextColor(Color.parseColor("#00E676"));
                return;
            }
            if (hours == 1) {
                lastUpdateTextView.setText("1 hour ago");
                return;
            }
            lastUpdateTextView.setText(hours + " hours ago");
            return;
        }

        if (days == 1) {
            lastUpdateTextView.setText("1 day ago");
            return;
        }

        lastUpdateTextView.setText(days + " days ago");
    }

    @Override
    public void StartCrumbDisplay(String trailId) {
        // Set up our crumbs
        trailId = Integer.toString(preferencesAPI.GetLocalTrailId());
        saveLocalFileMetadataAndLoadMapWithResponse(trailId);
    }

    @Override
    public void GetAndDisplayTrailOnMap(String trailId) {
        // Get and display our trail
    }

    @Override
    public void SetUpBottomSheetFab() {

        bottomSheetFab.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        bottomSheetFab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_edit));

        bottomSheetFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoBottomSheetClick();
            }
        });
    }

    /**
     * Launch a grid view from which we can select the
     */
    private void launchCoverPhotoSelector() {
        Intent selectCoverPhotoIntent = new Intent(context, TrailCoverImageSelector.class);
        startActivityForResult(selectCoverPhotoIntent, 155);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 155) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Recieved result for saving cover photo.");

                // The user picked an image. We need to set this image.
                String id = data.getStringExtra("Id");
                setCoverPhoto(id);
                setUpReadOnlyMode();
            }
        }
    }

    private void setCoverPhoto(String id) {
        if (id == null) {
            return;
        }
        ImageView coverPhoto = (ImageView) findViewById(R.id.trail_cover_photo);
        if (id.endsWith("L")) {
            id =  id.substring(0, id.length()-1);
            Glide.with(context).load(Utils.FetchLocalPathToImageFile(id)).centerCrop().placeholder(Color.GRAY).crossFade().into(coverPhoto);
        } else {
            Glide.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+id+".jpg").centerCrop().placeholder(Color.GRAY).crossFade().into(coverPhoto);
        }
    }

    private void ZoomOnMyLocation() {
        FetchMyLocation();
    }

    @Override
    public void setCollapsedToolbarState() {
        super.setCollapsedToolbarState();
        ZoomOnMyLocation();
    }
}
