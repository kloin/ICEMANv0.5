package com.teamunemployment.breadcrumbs.client;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.BackgroundServices.MessageObjects.TrailObject;
import com.teamunemployment.breadcrumbs.Dialogs.IDialogCallback;
import com.teamunemployment.breadcrumbs.Dialogs.SimpleMaterialDesignDialog;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.database.DatabaseController;
import com.teamunemployment.breadcrumbs.database.Models.LocalTrailStatisticsModel;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;

/**
 * @author Josiah Kendall
 */
public class TrailDetailsViewer extends AppCompatActivity {

    private PreferencesAPI preferencesAPI;
    private EventBus bus = EventBus.getDefault();
    private Context context;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trail_details_layout);
        context = this;
        // We use this for storing some info
        preferencesAPI = new PreferencesAPI(this);

        // Set up the name and details and sharable link.
        setTrailName();
        loadTrailDetails();
        setUpShareableLink();

        SetUpPublishButton();
        setUpBackButton();

        setUpCoordinatorLayout();
    }

    private void setUpCoordinatorLayout() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.trail_details_cl);
    }

    /**
     * Set up the trail name for this trail (if applicable
     */
    private void setTrailName() {
        String trailName = preferencesAPI.GetTrailName();
        if (trailName != null) {
            EditText trailNameEditText = (EditText) findViewById(R.id.trail_title_input);
            trailNameEditText.setText(trailName);
        }
    }

    /**
     * Load the details for a trail.
     */
    private void loadTrailDetails() {
        DatabaseController dbc = new DatabaseController(this);
        String localTrailId = Integer.toString(preferencesAPI.GetLocalTrailId());

        // Grab the metadata to get the info we need.
        JSONObject localTrailMetadata = dbc.GetAllCrumbs(localTrailId);

        // If we have data, we create a model for easy use.
        if (localTrailMetadata.length() > 0) {
            LocalTrailStatisticsModel statisticsModel = new LocalTrailStatisticsModel(localTrailMetadata);
            setLocalDetails(statisticsModel);
        }


        // Server variables
        String distanceTravelled;
        String duration;
    }

    /**
     * A simple method to bind our trail stats to the view
     * @param statisticsModel The model which contains our data we are going to bind.
     */
    private void setLocalDetails(LocalTrailStatisticsModel statisticsModel) {

        // Textviews we bind to.
        TextView numberOfCrumbs = (TextView) findViewById(R.id.number_of_crumbs_details);
        TextView numberOfPhotos = (TextView) findViewById(R.id.number_of_photos_details);
        TextView numberOfVideos = (TextView) findViewById(R.id.number_of_videos_details);

        // Bind views and their text
        numberOfCrumbs.setText(statisticsModel.getNumberOfPOI()+ " Points of interest");
        numberOfPhotos.setText(statisticsModel.getNumberOfPhotos()+ " photos");
        numberOfVideos.setText(statisticsModel.getNumberOfVideos()+ " videos");
    }

    /**
     * Set up the sharing capabilities so that a user can share to the places they want
     */
    private void setUpShareableLink() {

    }

    /**
     * Sets up the click handler for the publish button.
     */
    private void SetUpPublishButton() {
        // If we have no crumbs/trails/new metadata points, we can show the green up to date button
        final FloatingActionButton publishFab = (FloatingActionButton) findViewById(R.id.publish_trail_fab);
        assert publishFab != null;
        publishFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create the shell to hold our simple dialog.
                final SimpleMaterialDesignDialog materialDesignDialog = SimpleMaterialDesignDialog.Build(context);

                // Callback for the action button on the dialog.
                IDialogCallback onAccept = new IDialogCallback() {
                    @Override
                    public void DoCallback() {
                        // Send save Request to server
                        int serverTrailId = preferencesAPI.GetServerTrailId();
                        if (serverTrailId == -1) {
                            boolean result = handleFirstTimePublishing();
                            if (result) {
                                toggleButton(publishFab);
                            }
                        } else {
                            boolean result = publishTrail(Integer.toString(serverTrailId));
                            if (result) {
                                toggleButton(publishFab);
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
        });
    }

    /**
     * Toggle our fab to indicat the save was successful.
     * @param publishFab The fab that we are toggling.
     */
    private void toggleButton(final FloatingActionButton publishFab) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(coordinatorLayout, "Your trail has been saved", Snackbar.LENGTH_LONG).show();
                SimpleAnimations.ShrinkUnshrinkStandardFab(publishFab);
                publishFab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00E676")));
                publishFab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_cloud_done_white_24dp));
                publishFab.setOnClickListener(null);
            }
        }, 150);
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

    private void setUpBackButton() {
        ImageView backButton = (ImageView) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do save of trail name
                // save
                finish();
            }
        });
    }
}
