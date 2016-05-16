package com.teamunemployment.breadcrumbs.client.Cards;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.Crumb;
import com.teamunemployment.breadcrumbs.CustomElements.FancyFollow;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.SimpleNetworkApi;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UpdateViewElementWithProperty;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.ElementLoadingManager.TextViewLoadingManager;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by jek40 on 12/05/2016.
 */
public class MeCardAdapter extends RecyclerView.Adapter<MeCardAdapter.ViewHolder> {

    private ArrayList<String> dataSet;
    private Context context;
    private PreferencesAPI preferencesAPI;

    private final String TAG = "ME_CARD_ADAPTER";
    private DatabaseController databaseController;
    private String userId;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Each data item is just a string in this case
        public LinearLayout CardInner;
        public ViewHolder(View v) {
            super(v);
            CardInner = (LinearLayout)v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MeCardAdapter(ArrayList<String> myDataset, Context context) {
        // This is our JSONObject of trailData (id, name etc).
        dataSet = myDataset;
        this.context = context;
        userId = PreferenceManager.getDefaultSharedPreferences(context).getString("USERID", "-1");
        databaseController = new DatabaseController(context);
        preferencesAPI = new PreferencesAPI(context);
        userId = preferencesAPI.GetUserId();

    }

    @Override
    public MeCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        CardView card = null;
        if (viewType == 0) {
            // First object, therefore we want to create a profile card.
            v  = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.profile_card_viewholder, parent, false);
             card = (CardView) v.findViewById(R.id.me_card);

        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_base_card_base_view, parent, false);
            card = (CardView) v.findViewById(R.id.card_view);
        }

        Activity activity = (Activity) context;
        ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.explore_progress_bar);
        progressBar.setVisibility(View.GONE);
        card.setPreventCornerOverlap(false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String id = dataSet.get(position);
        if (id.endsWith("L")) {
            Log.d(TAG, "Found local ID: " + id);
            FetchAndBindLocalObject(holder.CardInner, id, position);
        }
    }

    private void FetchAndBindLocalObject(final LinearLayout card, String trailId, final int position) {
        Log.d(TAG, "Begin loading card at position : " + position);


        trailId = trailId.replace("L", "");
        // If we dont have data we will need to fetch it. otherwise we can use the cache
        // Probably not going to have any details unless it has been published
        JSONObject result = databaseController.GetTrailSummary(trailId);
        bindLocalCard(result, card, trailId, position);

                    //Result is our card details. We need to go though and fetch these

       // setNumberOfCrumbsView(card, trailId);

      //  setTempDeleteButton(card, trailId);
        // Need to fetch description with the other data but icbf

    }

    /*
        Set up our 'Delete trail' button.
     */
    private void setTempDeleteButton(LinearLayout card, final String trailId) {
        TextView deleteTextView = (TextView) card.findViewById(R.id.delete);
        deleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = LoadBalancer.RequestServerAddress() + "/rest/login/DeleteNode/" + trailId;
                HTTPRequestHandler requestHandler = new HTTPRequestHandler();
                requestHandler.SendSimpleHttpRequest(url, context);
            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        // Just returning the position for now.
        return position;
    }

    /*
            Method to bind the local data that we have to the trail card. This method is for a local card,
            where we have not uploaded data to the server.
         */
    private void bindLocalCard(JSONObject resultJSON, final LinearLayout card, final String trailId, final int position) {
        try {
            String titleObject = resultJSON.getString("TrailName");
            // Set up our trailTitle
            if (titleObject!= null && !titleObject.isEmpty()) {
                Log.d(TAG, "Found trailName. Setting now");
                TextView titleTextView = (TextView) card.findViewById(R.id.Title);
                Log.d(TAG, "Set trail Name as: " + titleObject);
                titleTextView.setText(titleObject);
            } else {
                Log.d(TAG, "Failed to find trailName, setting as default.");
                TextView titleTextView = (TextView) card.findViewById(R.id.Title);
                titleTextView.setText("Snazzy Trail Name Goes Here!");
                titleTextView.setTypeface(null, Typeface.ITALIC);
            }

            setUserNameForCard(card);

            String coverPhotoId = resultJSON.getString("CoverPhotoId");
            if (coverPhotoId.equals("0")) {
                // Grab a cover photo Id so that we can display a photo. Not diplaying a photo will be ugly.
                coverPhotoId = fetchFirstImageAvailableFromDatabase(trailId);
            }

            setCoverPhoto(card, coverPhotoId);
            setUserProfileImage(card);
            // Use the views texview to show we are not published
            TextView viewsTextView = (TextView) card.findViewById(R.id.trail_views);
            viewsTextView.setText("Trail not published");

            final Activity act = (Activity) context;
            final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) act.findViewById(R.id.main_content);
            final TextView detailsButton = (TextView) card.findViewById(R.id.details_button);
            detailsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // FIXME At the moment this is just loading the currently active trail by default. It would be better if we load
                    // FIXME ? On second thoughts this actually seems pretty reasonable. We should delete old trails if we create a new one while the old one has not been saved.
                    Intent TrailViewer = new Intent();
                    TrailViewer.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.MapHostBase");
                    context.startActivity(TrailViewer);
                }
            });
            // Load cover photo here and set it

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "Error parsing json when loading trails");
        } catch (NullPointerException nullPointerException) {
            // Maybe I should be notifying the user here, but i would rather just not have this shit happen.
            nullPointerException.printStackTrace();
            Log.e("TRAIL", "null pointer exception loading trail");
        }
    }

    private void setUserProfileImage(LinearLayout card) {
        CircleImageView profilePic = (CircleImageView) card.findViewById(R.id.profilePicture);

        // This uses a cache, so (hopefully) it will work offline.
        String imageIdUrl = LoadBalancer.RequestServerAddress() + "/rest/login/GetPropertyFromNode/"+userId+"/CoverPhotoId";
        TextViewLoadingManager.LoadCircularImageView(imageIdUrl, profilePic, context);
    }

    // Simple method to return the id of the first image that we have in the database.
    private String fetchFirstImageAvailableFromDatabase(String trailId) {
        JSONObject crumbsJson = databaseController.GetCrumbsWithMedia(trailId, 0); // Maybe we should grab without the images. Grabbing with the images may be slow.
        Iterator<String> keys = crumbsJson.keys();
        String result = null;

        boolean foundImage = false;
        while (!foundImage && keys.hasNext()) {
            try {
                JSONObject crumbJSON = crumbsJson.getJSONObject(keys.next());
                Crumb crumb = new Crumb(crumbJSON);
                if (crumb.GetMediaType().equals(".jpg")) {
                    // This means we have found our crumb
                    result = crumb.GetEventId();
                    foundImage = true;
                }
            } catch (JSONException e) {
                    Log.d(TAG, "Failed to find the crumb with an id. Stacktrace follows");
                    e.printStackTrace();
            }
        }
        // Return the event Id. I can use that to find the bitmap on our local disk.
        return result;
    }

    // Simple method to setup the name for our card.
    private void setUserNameForCard(LinearLayout card) {
        String userName = preferencesAPI.GetUserName();
        if (userName == null) {
            Log.d(TAG, "UserName was null. Attempting to fetch and save from the server");
            //HTTPRequestHandler requestHandler = new HTTPRequestHandler();
            //UpdateViewElementWithProperty updateViewElementWithProperty = new UpdateViewElementWithProperty();
            //updateViewElementWithProperty.Up(arrayList, userId, "Username", mContext);
        }
        Log.d(TAG, "Setting userName as: " + userName);

        TextView belongsTo = (TextView) card.findViewById(R.id.belongs_to);
        belongsTo.setText(userName);
        belongsTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch our profile
                Intent profileIntent = new Intent();
                profileIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.NavMenu.Profile.ProfilePageViewer");
                profileIntent.putExtra("userId", userId);
                profileIntent.putExtra("name", "You");
                context.startActivity(profileIntent);
            }
        });


    }

    private void setCoverPhoto(LinearLayout card, String coverPhotoId) {
        final ImageView trailCoverPhoto = (ImageView) card.findViewById(R.id.main_photo);
        trailCoverPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent TrailViewer = new Intent();
                TrailViewer.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.MapHostBase");
                context.startActivity(TrailViewer);
            }
        });

       // Load local cover photo.
        //Glide.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+coverId+".jpg").centerCrop().crossFade().into(trailCoverPhoto);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
