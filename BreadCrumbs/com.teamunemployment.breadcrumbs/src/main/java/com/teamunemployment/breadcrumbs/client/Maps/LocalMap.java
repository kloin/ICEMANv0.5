package com.teamunemployment.breadcrumbs.client.Maps;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.teamunemployment.breadcrumbs.BackgroundServices.MessageObjects.TrailObject;
import com.teamunemployment.breadcrumbs.Dialogs.IDialogCallback;
import com.teamunemployment.breadcrumbs.Dialogs.SimpleMaterialDesignDialog;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UpdateViewElementWithProperty;
import com.teamunemployment.breadcrumbs.Preferences.Preferences;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.ImageChooser.TrailCoverImageSelector;
import com.teamunemployment.breadcrumbs.client.TrailManager;
import com.teamunemployment.breadcrumbs.database.DatabaseController;
import com.teamunemployment.breadcrumbs.database.Models.LocalTrailModel;
import com.teamunemployment.breadcrumbs.database.Models.TrailSummaryModel;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by jek40 on 22/05/2016.
 */
public class LocalMap extends MapViewer {
    private PreferencesAPI preferencesAPI;
    private EventBus bus = EventBus.getDefault();
    private String localTrailId;

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
        bus.post(trailObject);
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
    }

    @Override
    public void StartCrumbDisplay(String trailId) {
        // Set up our crumbs
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
        ImageView coverPhoto = (ImageView) findViewById(R.id.trail_cover_photo);
        if (id.endsWith("L")) {
            id =  id.substring(0, id.length()-1);
            Glide.with(context).load(Utils.FetchLocalPathToImageFile(id)).centerCrop().placeholder(Color.GRAY).crossFade().into(coverPhoto);
        } else {
            Glide.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+id+".jpg").centerCrop().placeholder(Color.GRAY).crossFade().into(coverPhoto);
        }
    }
}
