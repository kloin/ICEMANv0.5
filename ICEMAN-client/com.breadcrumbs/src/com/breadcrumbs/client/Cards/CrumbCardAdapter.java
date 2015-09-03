package com.breadcrumbs.client.Cards;

/**
 * Created by aDirtyCanvas on 7/9/2015.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.breadcrumbs.Framework.JsonHandler;
import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.ServiceProxy.AsyncDataRetrieval;
import com.breadcrumbs.ServiceProxy.AsyncRetrieveImage;
import com.breadcrumbs.ServiceProxy.UpdateViewElementWithProperty;
import com.breadcrumbs.caching.GlobalContainer;
import com.breadcrumbs.client.Maps.DisplayCrumb;
import com.breadcrumbs.client.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by aDirtyCanvas on 7/7/2015.
 */
public class CrumbCardAdapter extends RecyclerView.Adapter<CrumbCardAdapter.ViewHolder> {
    private ArrayList<String> mDataset;
    private Context mContext;
    private String userId;
    private boolean commentsOpen = false;
    private boolean commentsLoaded = false;
    private GlobalContainer globalContainer;
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
    public CrumbCardAdapter(ArrayList<String> myDataset, Context context) {
        // This is our JSONObject of trailData (id, name etc).
        mDataset = myDataset;
        mContext = context;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public CrumbCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.crumb_card, parent, false);
        // Set the name, user, views etc for a trail and go about loading its image

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        // Here we need to get the trailId from the array.
        String crumbId = mDataset.get(position);

        FetchAndBindObject(holder.CardInner, crumbId);
        SetCommentClickHandlers(holder.CardInner, crumbId);
    }

    private void FetchAndBindObject(final LinearLayout card, final String crumbId) {
        AsyncRetrieveImage asyncFetch = new AsyncRetrieveImage(crumbId, new AsyncRetrieveImage.RequestListener() {
            @Override
            public void onFinished(Bitmap result) {
                ImageView imageView = (ImageView) card.findViewById(R.id.crumb_image);
                imageView.setImageBitmap(result);
                card.findViewById(R.id.crumb_progress_bar).setVisibility(View.GONE);
                card.findViewById(R.id.crumb_image).setVisibility(View.VISIBLE);
            }
        });
        String url = LoadBalancer.RequestServerAddress() +"/rest/Crumb/GetLatitudeAndLogitudeForCrumb/"+crumbId;
        AsyncDataRetrieval fetchCardDetails = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                // Result is our card details. We need to go though and fetch these
                try {
                    // Fetch our data, then
                    JSONObject resultJSON = new JSONObject(result);
                    Double Latitude = resultJSON.getDouble("Latitude");
                    Double Longitude = resultJSON.getDouble("Longitude");
                    TextView locality = (TextView) card.findViewById(R.id.location);
                    Geocoder gcd = new Geocoder(mContext, Locale.getDefault());
                    List<Address> addresses = gcd.getFromLocation(Latitude, Longitude, 1);
                    if (addresses.size() > 0)
                        locality.setText(addresses.get(0).getLocality());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        String crumbDescriptionUrl = LoadBalancer.RequestServerAddress() +"/rest/login/GetPropertyFromNode/"+crumbId+"/Chat/";
        AsyncDataRetrieval fetchCrumbDescription = new AsyncDataRetrieval(crumbDescriptionUrl, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                TextView crumbDescription = (TextView) card.findViewById(R.id.crumb_description);
                crumbDescription.setText(result);
            }
        });

        fetchCrumbDescription.execute();
        fetchCardDetails.execute();
        asyncFetch.execute();

    }

    private void SetCommentClickHandlers(final LinearLayout card, final String crumbId) {
        final ImageButton openComments = (ImageButton) card.findViewById(R.id.open_comments_button);
        openComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the views
                LinearLayout commentsHolder = (LinearLayout) card.findViewById(R.id.comments_holder);
                ToggleCommentsVisibility(commentsHolder, openComments);
            }
        });
        openComments.setTag(crumbId);

        final TextView commentTextView = (TextView) card.findViewById(R.id.comment_button);
        commentTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the views
                LinearLayout commentsHolder = (LinearLayout) card.findViewById(R.id.comments_holder);
                ToggleCommentsVisibility(commentsHolder, openComments);
            }
        });

        final ImageButton commentButton = (ImageButton) card.findViewById(R.id.save_comment);
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get our variables to save.
                EditText commentTextField = (EditText) card.findViewById(R.id.user_comment);
                String CommentText = commentTextField.getText().toString();
                //commentTextField.setText("");

                // This removes the focus. There are other was but this hack is just so simple.
                commentTextField.setEnabled(false);
                commentTextField.setEnabled(true);


                // Save this beast
                globalContainer = GlobalContainer.GetContainerInstance();
                if (userId == null) {
                    userId = PreferenceManager.getDefaultSharedPreferences(mContext).getString("USERID", globalContainer.GetUserId());
                }
                SaveCommentToServer(userId, crumbId, CommentText);
                CreateCommentObjectAndDisplayIt(card, "0", userId, CommentText, crumbId);
            }
        });
    }

    private void LoadComments(String crumbId, final LinearLayout card) {
        // NOTE - It might just be better to start saving the url with the crumb, and setting that as the title.......
        // ===================================================================================================
        String url = MessageFormat.format("{0}/rest/login/LoadCommentsForEvent/{1}",
                LoadBalancer.RequestServerAddress(),
                crumbId);

        url = url.replaceAll(" ", "%20");
        AsyncDataRetrieval clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {

            @Override
            public void onFinished(String result) {
                //Initialise our object, and attempt to construct it from the string.
                JsonHandler jsonHandler = new JsonHandler();
                JSONObject JSONcomments = jsonHandler.convertJsonStringToJsonObject(result);
                // Begin comment construction.
                ConstructCommentsFromJSON(JSONcomments, card);
            }
        });
        clientRequestProxy.execute();
    }

    private void ConstructCommentsFromJSON(JSONObject JSONcomments, LinearLayout card) {
        String baseNodeFinder = "Node"; // String we append the index to to get all the keys.
        int index = 0; // Index that we append to the baseNodeFinder to find the comment.
        if (JSONcomments == null) {
            return;
        }
        Iterator<String> commentIterator = JSONcomments.keys();
        try {
            while (commentIterator.hasNext()) {
                // Get the next comment in the iterator.
                String next = commentIterator.next();
                JSONObject commentObject = JSONcomments.getJSONObject(next);

                // Get our fields on the comment.
                String CommentId = commentObject.getString("Id");
                String UserId = commentObject.getString("UserId");
                String CommentText = commentObject.getString("CommentText");
                String EntityId = commentObject.getString("EntityId");

                // Create the  comment object/display it.
                CreateCommentObjectAndDisplayIt(card, CommentId, UserId, CommentText, EntityId);

                // Move to the next object.
                index += 1;
            }
        } catch (JSONException e) {
            Log.println(0, "BAD JSON DATA READ BRO", "Errors thrown trying to parse json object. " +
                    "Error printed below");

            // Just printing this at the moment. Later, we may want to throw this error but we need to
            // make sure we are handling it correctly at the front. I dont feel that the user needs
            // to know about this error, its just a "bad data read"
            e.printStackTrace();
        }
    }


    private void SaveCommentToServer(String UserId, String EntityId, String CommentText) {
        String url = MessageFormat.format("{0}/rest/login/SaveComment/{1}/{2}/{3}",
                LoadBalancer.RequestServerAddress(),
                UserId,
                EntityId,
                CommentText);

        url = url.replaceAll(" ", "%20");
        AsyncDataRetrieval clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                // not Really sure if we can do anything here bar caching. The only problem would be
                // expense for caching this much text???. Doing nothing for the moment.
                // CommentId = result;
            }
        });
        clientRequestProxy.execute();
    }

    // Create an object with and xml template, and set each of the fields on the comment.
    private void CreateCommentObjectAndDisplayIt(LinearLayout card, String CommentId, String UserId, String CommentText, String EntityId ) {
        //Inflate the comment xml.
        UpdateViewElementWithProperty viewUpdater = new UpdateViewElementWithProperty();
        View comment = LayoutInflater.from(card.getContext())
                .inflate(R.layout.comment_base_layout, card, false);

        TextView commentText = (TextView) comment.findViewById(R.id.comment_text);
        commentText.setText(CommentText);
        TextView commenter = (TextView) comment.findViewById(R.id.user_name);
        viewUpdater.UpdateTextViewElement(commenter, UserId, "Username");
        LinearLayout commentHolder = (LinearLayout) card.findViewById(R.id.comments_holder);
        commentHolder.addView(comment);
    }

    private void ToggleCommentsVisibility(LinearLayout commentsSection, ImageButton openCommentsButton) {

        if (!commentsOpen) {
            commentsSection.setVisibility(View.VISIBLE);
            openCommentsButton.setRotation(180);
            commentsOpen = true;
            String crumbId = openCommentsButton.getTag().toString();

            // Only load comments first time
            if (commentsSection.getChildCount() <= 1) {
                LoadComments(crumbId, commentsSection);
            }
        } else {
            commentsSection.setVisibility(View.GONE);
            openCommentsButton.setRotation(0);
            commentsOpen = false;
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}