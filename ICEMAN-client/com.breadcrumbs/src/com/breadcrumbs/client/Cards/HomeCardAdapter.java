package com.breadcrumbs.client.Cards;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.breadcrumbs.ServiceProxy.AsyncFetchThumbnail;
import com.breadcrumbs.ServiceProxy.HTTPRequestHandler;
import com.breadcrumbs.ServiceProxy.UpdateViewElementWithProperty;
import com.breadcrumbs.caching.TextCachingInterface;
import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.ServiceProxy.AsyncDataRetrieval;
import com.breadcrumbs.R;
import com.breadcrumbs.caching.TextCaching;
import com.bumptech.glide.Glide;
import com.pkmmte.view.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by aDirtyCanvas on 7/7/2015.
 */
public class HomeCardAdapter extends RecyclerView.Adapter<HomeCardAdapter.ViewHolder> {
    private ArrayList<String> mDataset;
    private Context mContext;
    private String userId;
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
                .inflate(R.layout.story_board_item_light, parent, false);
        // Set the name, user, views etc for a trail and go about loading its image
       // setTextAndHandlers(v);
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
        FetchAndBindObject(holder.CardInner, id);
    }

    private void FetchAndBindObject(final LinearLayout card, final String trailId) {

        // This is our url.
        final String keyUrl = "TrailManagerGetBaseDetailsForATrail" + trailId;
        final String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetBaseDetailsForATrail/" + trailId;

        // Check cache for data that has this url
        final TextCachingInterface textRetriever = new TextCachingInterface(mContext);
        final String cacheData = textRetriever.FetchDataInStringFormat(keyUrl);

        // If we dont have data we will need to fetch it. otherwise we can use the cache
        if (cacheData == null) {
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
                    BindObject(result, card, trailId);
                    TextCaching textCaching = new TextCaching(mContext);
                    if (!result.isEmpty() && keyUrl != null) {
                        textRetriever.CacheText(keyUrl, result);
                    }
                }
            });
            fetchCardDetails.execute();
        }   else {
            BindObject(cacheData, card, trailId);
        }

        // Need to fetch description with the other data but icbf
        final String key = trailId + "Description";
        String descriptionUrl = LoadBalancer.RequestServerAddress() +"/rest/login/GetPropertyFromNode/"+trailId+"/Description";

        final String cacheDescription = textRetriever.FetchDataInStringFormat(key);

        if (cacheDescription == null) {
            AsyncDataRetrieval fetchDescription = new AsyncDataRetrieval(descriptionUrl, new AsyncDataRetrieval.RequestListener() {
                @Override
                public void onFinished(String result) {
                    TextView description = (TextView) card.findViewById(R.id.trail_description);
                    description.setText(result);
                    if (result != null) {
                        textRetriever.CacheText(key, result);
                    }
                }
            });
            fetchDescription.execute();
        } else {
            TextView description = (TextView) card.findViewById(R.id.trail_description);
            description.setText(cacheDescription);
        }

        updateViews(trailId, card);
    }

    // Update the cached version of t
    private void updateViews(String trailId, LinearLayout card) {
        TextView viewsTextView = (TextView) card.findViewById(R.id.trail_views);
        UpdateViewElementWithProperty updateViewElementWithProperty = new UpdateViewElementWithProperty();
        updateViewElementWithProperty.UpdateTextViewWithElementAndExtraString(viewsTextView, trailId, "Views", " Views");
    }

    private void deleteTrail(String trailId) {
        String url = LoadBalancer.RequestServerAddress() + "/rest/login/DeleteNode/" + trailId;
        HTTPRequestHandler requestHandler = new HTTPRequestHandler();
        requestHandler.SendSimpleHttpRequest(url);
        // Hopefully that is all I need to do. May need to return something here if it fails.
    }
    private void showDeleteDialog() {

        final Dialog dialog = new Dialog(mContext);
       // dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.trail_manager_first_time_notifier);
        TextView dialogButton = (TextView) dialog.findViewById(R.id.dismiss_dialog);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean("SHOW_EDITOR_DIALOG", false).commit();
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void BindObject(String result, final LinearLayout card, final String trailId) {
        try {
            // Fetch our data, then
            JSONObject resultJSON = new JSONObject(result);
            // These four should all exist. The fifth we have to check for.
            String desc = resultJSON.get("description").toString();
            String title = resultJSON.get("trailName").toString();
            String userId = resultJSON.getString("userId");
            final String trailsUserId = resultJSON.get("userId").toString();
            String views = resultJSON.get("views").toString() + " views";
            final String userName = resultJSON.get("userName").toString();


            String localUserId = PreferenceManager.getDefaultSharedPreferences(mContext).getString("USERID", "-1");

            if (localUserId.equals(userId)) {
                final TextView deleteButton = (TextView) card.findViewById(R.id.delete_button);
                deleteButton.setVisibility(View.VISIBLE);
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        deleteTrail(trailId);
                    }
                });
            }

            // Grab views, and set the text/photo for them
            TextView titleTextView = (TextView) card.findViewById(R.id.Title);
            TextView belongsTo = (TextView) card.findViewById(R.id.belongs_to);
            TextView viewsTextView = (TextView) card.findViewById(R.id.trail_views);
            final Activity act = (Activity) mContext;
            final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) act.findViewById(R.id.main_content);
            final TextView followButton = (TextView) card.findViewById(R.id.follow_button);
            final TextView detailsButton = (TextView) card.findViewById(R.id.details_button);

            followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (followButton.getTag() != null && followButton.getTag().toString().equals("0")) {
                        followButton.setTextColor(Color.parseColor("#8a000000"));
                        followButton.setTag("1");

                        Snackbar snackbar = Snackbar
                                .make(coordinatorLayout, "Unfollowed trail", Snackbar.LENGTH_LONG)
                                .setAction("Undo", null);
                        snackbar.setActionTextColor(Color.RED);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.DKGRAY);
                        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(act.getResources().getColor(R.color.accent));
                        snackbar.show();
                    }   else {
                        followButton.setTextColor(Color.parseColor("#FF4081"));
                        followButton.setTag("0");
                        Snackbar snackbar = Snackbar
                                .make(coordinatorLayout, "Following trail", Snackbar.LENGTH_LONG)
                                .setAction("Undo", null);
                        snackbar.setActionTextColor(Color.RED);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.DKGRAY);
                        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(act.getResources().getColor(R.color.accent));
                        snackbar.show();
                    }

                }
            });
            final ImageView trailCoverPhoto = (ImageView) card.findViewById(R.id.main_photo);
            // Load cover photo here and set it
            if (resultJSON.has("coverPhotoId")) {
                String coverId = resultJSON.getString("coverPhotoId");

                Glide.with(mContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+coverId+".jpg").centerCrop().crossFade().into(trailCoverPhoto);
            } else {
                trailCoverPhoto.setBackgroundColor(Color.BLUE);
            }
            trailCoverPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent TrailViewer = new Intent();
                    TrailViewer.setClassName("com.breadcrumbs", "com.breadcrumbs.client.Maps.MapViewer");
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
                    TrailViewer.setClassName("com.breadcrumbs", "com.breadcrumbs.client.Maps.MapViewer");
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
                    profileIntent.setClassName("com.breadcrumbs", "com.breadcrumbs.client.ProfilePageViewer");
                    profileIntent.putExtra("userId", trailsUserId);
                    profileIntent.putExtra("name", userName);
                    mContext.startActivity(profileIntent);
                }
            });

            titleTextView.setText(title);
            belongsTo.setText(userName);
            viewsTextView.setText(views);
            CircularImageView profilePic = (CircularImageView) card.findViewById(R.id.profilePicture);
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
                profileIntent.setClassName("com.breadcrumbs", "com.breadcrumbs.client.ProfilePageViewer");
                profileIntent.putExtra("userId", trailsUserId);
                profileIntent.putExtra("name", userName);
                mContext.startActivity(profileIntent);
            }
        };
    }

    private void setProfilePic(final CircularImageView profilePic, String trailUserId) {
        //Try load
        final String keyId = trailUserId+"CoverPhotoId";
        final TextCachingInterface textCachingInterface = new TextCachingInterface(mContext);

        String imageIdUrl = LoadBalancer.RequestServerAddress() + "/rest/login/GetPropertyFromNode/"+trailUserId+"/CoverPhotoId";

        String photoId = textCachingInterface.FetchDataInStringFormat(keyId);
        if (photoId == null|| photoId.equals("error")) {
            AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(imageIdUrl, new AsyncDataRetrieval.RequestListener() {
                @Override
                public void onFinished(String result) {
                   // Glide.with(mContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+result + ".jpg").centerCrop().crossFade().into(profilePic);
                    AsyncFetchThumbnail fetchThumbnail = new AsyncFetchThumbnail(result, new AsyncFetchThumbnail.RequestListener() {
                        @Override
                        public void onFinished(Bitmap result) {
                            profilePic.setImageBitmap(result);
                        }
                    });
                    fetchThumbnail.execute();
                    textCachingInterface.CacheText(keyId, result);
                }
            });
            asyncDataRetrieval.execute();
        } else {
            AsyncFetchThumbnail fetchThumbnail = new AsyncFetchThumbnail(photoId, new AsyncFetchThumbnail.RequestListener() {
                @Override
                public void onFinished(Bitmap result) {
                    profilePic.setImageBitmap(result);
                }
            });
            fetchThumbnail.execute();
        }
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
        TextView description = (TextView) card.findViewById(R.id.trail_description);
        description.setText(null);
        TextView title = (TextView) card.findViewById(R.id.Title);
        title.setText(null);
    }
}
