package com.teamunemployment.breadcrumbs.client.Cards;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.CustomElements.FancyFollow;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncFetchThumbnail;
import com.teamunemployment.breadcrumbs.caching.TextCachingInterface;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.caching.TextCaching;
import com.bumptech.glide.Glide;
import com.teamunemployment.breadcrumbs.client.ElementLoadingManager.TextViewLoadingManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by aDirtyCanvas on 7/7/2015.
 */
public class HomeCardAdapter extends RecyclerView.Adapter<HomeCardAdapter.ViewHolder> {
    private ArrayList<String> mDataset;
    private Context mContext;
    private String userId;
    private String LAST_CACHED_TIME = "LAST_CACHED_TIME";
    private final String TAG = "HOME_CARD";
    private TextCaching mTextCaching;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout CardInner;
        public ViewHolder(View v) {
            super(v);
            CardInner = (LinearLayout)v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public HomeCardAdapter(ArrayList<String> myDataset, Context context) {
        // This is our JSONObject of trailData (id, name etc).
        mDataset = myDataset;
        mContext = context;
        userId = PreferenceManager.getDefaultSharedPreferences(context).getString("USERID", "-1");
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HomeCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_base_card_base_view, parent, false);
        CardView card = (CardView) v.findViewById(R.id.card_view);
        card.setPreventCornerOverlap(false);
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // Here we need to get the trailId from the array.
        String id = mDataset.get(position);
        FetchAndBindObject(holder.CardInner, id, position);
    }

    private void FetchAndBindObject(final LinearLayout card, final String trailId, final int position) {
        Log.d(TAG, "Begin loading card at position : " +position);
        // This is our url.
        final String keyUrl = "TrailManagerGetBaseDetailsForATrail" + trailId;
        final String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetBaseDetailsForATrail/" + trailId;

        // Check cache for data that has this url
        final TextCachingInterface textRetriever = new TextCachingInterface(mContext);
        final String cacheData = textRetriever.FetchDataInStringFormat(keyUrl);

        // If we dont have data we will need to fetch it. otherwise we can use the cache
        if (cacheData == null) {
            Log.d(TAG, "No cache found, Sending network request: " +url );
            AsyncDataRetrieval fetchCardDetails = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
                /*
                ****** This is what an example data return should look like
                {
                    views: "34",
                    description: "ayy",
                    trailName: "test",
                    userName: "kloin",
                    coverId: "6"
                }
                 */
                @Override
                public void onFinished(String result) {
                    //Result is our card details. We need to go though and fetch these
                    BindObject(result, card, trailId, position);
                    if (!result.isEmpty() && keyUrl != null) {
                        textRetriever.CacheText(keyUrl, result);
                    }
                }
            });
            fetchCardDetails.execute();
        }   else {
            Log.d(TAG, "Found cache, will begin Binding card");
            BindObject(cacheData, card, trailId, position);
            // Check if it has been 1000 seconds. If it has, we need to do an update of the cache.
            final Calendar c = Calendar.getInstance();
            int seconds = c.get(Calendar.SECOND);
            final int lastCachedTime = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(LAST_CACHED_TIME + trailId, -1000);
            if ( lastCachedTime + 1000 < seconds || seconds < lastCachedTime) {
                AsyncDataRetrieval fetchCardDetails = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
                    /*
                    ****** This is what an example data return should look like
                    {
                        views: "34",
                        description: "ayy",
                        trailName: "test",
                        userName: "kloin",
                        coverId: "6"
                    }
                     */
                    @Override
                    public void onFinished(String result) {
                        //Result is our card details. We need to go though and fetch these
                        TextCaching textCaching = new TextCaching(mContext);
                        if (result != null && !result.isEmpty() && keyUrl != null) {
                            textRetriever.CacheText(keyUrl, result);
                            int newCacheTime = c.get(Calendar.SECOND);
                            PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt(LAST_CACHED_TIME + trailId, newCacheTime).commit();
                        }
                    }
                });
                fetchCardDetails.execute();
            }
        }

        // Need to fetch description with the other data but icbf
        final String key = trailId + "Description";
        String descriptionUrl = LoadBalancer.RequestServerAddress() +"/rest/login/GetPropertyFromNode/"+trailId+"/Description";

        final String cacheDescription = textRetriever.FetchDataInStringFormat(key);

        if (cacheDescription == null) {

        } else {
           // TextView description = (TextView) card.findViewById(R.id.trail_description);
           // description.setText(cacheDescription);
        }

        updateViews(trailId, card);
    }

    // Update the cached version of t
    private void updateViews(String trailId, LinearLayout card) {
        TextView viewsTextView = (TextView) card.findViewById(R.id.trail_views);
        // UpdateViewElementWithProperty updateViewElementWithProperty = new UpdateViewElementWithProperty();
        //updateViewElementWithProperty.UpdateTextViewWithElementAndExtraString(viewsTextView, trailId, "Views", " Views");
    }

    private void BindObject(String result, final LinearLayout card, final String trailId, final int position) {
        try {
            // Fetch our data, then
            JSONObject resultJSON = new JSONObject(result);
            // These four should all exist. The fifth we have to check for.
            String desc = resultJSON.get("description").toString();
            String title = resultJSON.get("trailName").toString();
            final String trailsUserId = resultJSON.get("userId").toString();
            String views = resultJSON.get("views").toString() + " views";
            final String userName = resultJSON.get("userName").toString();

            // Grab views, and set the text/photo for them
            TextView titleTextView = (TextView) card.findViewById(R.id.Title);
            TextView belongsTo = (TextView) card.findViewById(R.id.belongs_to);
            TextView viewsTextView = (TextView) card.findViewById(R.id.trail_views);
            final Activity act = (Activity) mContext;
            final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) act.findViewById(R.id.main_content);
            final FancyButton followButton = (FancyButton) card.findViewById(R.id.follow_button);
            final TextView detailsButton = (TextView) card.findViewById(R.id.details_button);
           // TextView description = (TextView) card.findViewById(R.id.trail_description);
//            description.setText(desc);
            final ImageView trailCoverPhoto = (ImageView) card.findViewById(R.id.main_photo);
            trailCoverPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent TrailViewer = new Intent();
                    TrailViewer.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.MapViewer");
                    Bundle extras = new Bundle();
                    extras.putString("TrailId", trailId);
                    TrailViewer.putExtras(extras);
                    mContext.startActivity(TrailViewer);
                }
            });

            detailsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent TrailViewer = new Intent();
                    TrailViewer.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.MapViewer");
                    Bundle extras = new Bundle();
                    extras.putString("TrailId", trailId);
                    TrailViewer.putExtras(extras);
                    mContext.startActivity(TrailViewer);
                }
            });
            belongsTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Launch our profile
                    Intent profileIntent = new Intent();
                    profileIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.NavMenu.Profile.ProfilePageViewer");
                    profileIntent.putExtra("userId", trailsUserId);
                    profileIntent.putExtra("name", userName);
                    mContext.startActivity(profileIntent);
                }
            });

            String currentUserId = PreferenceManager.getDefaultSharedPreferences(mContext).getString("USERID", null);
            FancyFollow customFollowButton = new FancyFollow(currentUserId, trailsUserId, followButton, mContext);
            customFollowButton.init();
            // Load cover photo here and set it
            if (resultJSON.has("coverPhotoId")) {
                String coverId = resultJSON.getString("coverPhotoId");
                Log.d(TAG, "Found coverPhotoId: " +coverId );
                if (coverId.equals("0")) {
                    trailCoverPhoto.setBackgroundResource(R.drawable.db3);
                }
                Glide.with(mContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+coverId+".jpg").centerCrop().crossFade().into(trailCoverPhoto);

            } else {
                Log.d(TAG, "Could not find coverPhotoId");
                trailCoverPhoto.setBackgroundResource(R.drawable.db3);
            }

            titleTextView.setText(title);
            belongsTo.setText(userName);
            viewsTextView.setText(views);
            CircleImageView profilePic = (CircleImageView) card.findViewById(R.id.profilePicture);
            setProfilePic(profilePic, trailsUserId);
            profilePic.setOnClickListener(getProfilePicClickListener(trailsUserId, userName));

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "Error parsing json when loading trails");
        } catch (NullPointerException nullPointerException) {
            // Maybe I should be notifying the user here, but i would rather just not have this shit happen.
            nullPointerException.printStackTrace();
            Log.e("TRAIL", "null pointer exception loading trail");
        }
    }

    private View.OnClickListener getProfilePicClickListener(final String trailsUserId, final String userName) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch our profile
                Intent profileIntent = new Intent();
                profileIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.NavMenu.Profile.ProfilePageViewer");
                profileIntent.putExtra("userId", trailsUserId);
                profileIntent.putExtra("name", userName);
                mContext.startActivity(profileIntent);
            }
        };
    }

    private void setProfilePic(final CircleImageView profilePic, String trailUserId) {
        //Try load
        String imageIdUrl = LoadBalancer.RequestServerAddress() + "/rest/login/GetPropertyFromNode/"+trailUserId+"/CoverPhotoId";
        TextViewLoadingManager.LoadCircularImageView(imageIdUrl, profilePic, mContext);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        LinearLayout card = holder.CardInner;
        ImageView imageView = (ImageView) card.findViewById(R.id.main_photo);
        imageView.setImageDrawable(null);
      //  TextView description = (TextView) card.findViewById(R.id.trail_description);
//        description.setText(null);
        TextView title = (TextView) card.findViewById(R.id.Title);
        title.setText(null);
        TextView userName = (TextView) card.findViewById(R.id.belongs_to);
        userName.setText("...");
        TextView views = (TextView) card.findViewById(R.id.trail_views);
        views.setText("...");
        CircleImageView profilePic = (CircleImageView) card.findViewById(R.id.profilePicture);
        profilePic.setImageResource(R.drawable.profileblank);

    }
}
