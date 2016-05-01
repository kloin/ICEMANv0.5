package com.teamunemployment.breadcrumbs.client.Cards;

/**
 * Created by aDirtyCanvas on 7/9/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaCodec;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.SampleSource;
import com.google.android.exoplayer.TrackRenderer;
import com.teamunemployment.breadcrumbs.Framework.JsonHandler;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UpdateViewElementWithProperty;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.caching.TextCachingInterface;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

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
    private ArrayList<CrumbCardDataObject> mDataset;
    private Activity mContext;
    private String userId;
    private boolean commentsOpen = false;
    private boolean commentsLoaded = false;
    private GlobalContainer globalContainer;
    private String jsonResult;
    TextCachingInterface textCachingInterface;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout CardInner;
        public ViewHolder(View view) {
            super(view);
            CardInner = (LinearLayout)view;
        }
    }

    public CrumbCardAdapter(ArrayList<CrumbCardDataObject> myDataset, Activity context) {
        // This is our JSONObject of trailData (id, name etc).
        mDataset = myDataset;
        mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        // The purpose of this method is to let the onCreateViewHolder of the adapter know if it is the first in the list or not.
        return position;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CrumbCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int position) {
        View v = null;
        // Need to figure out if it is a video or a still crumb.
        CrumbCardDataObject cardDataObject = mDataset.get(position);
        if (cardDataObject.GetDataType().equals(".mp4")) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.crumb_card_video, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.crumb_card, parent, false);
        }

        // Set the name, user, views etc for a trail and go about loading its image
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        // Here we need to get the trailId from the array.
        textCachingInterface  = new TextCachingInterface(mContext);
        globalContainer = GlobalContainer.GetContainerInstance();
        userId = PreferenceManager.getDefaultSharedPreferences(mContext).getString("USERID", globalContainer.GetUserId());

        // We need to create the view based on whether it is video or image
        CrumbCardDataObject crumb = mDataset.get(position);
        String crumbId = crumb.GetCrumbId();
        FetchAndBindObject(holder.CardInner, crumbId, position);
        SetCommentClickHandlers(holder.CardInner, crumbId);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        ImageView imageView = (ImageView) holder.CardInner.findViewById(R.id.crumb_image);
        imageView.setImageDrawable(null);
    }

    // Load eith
    private void loadAndAdjustImage(LinearLayout card, String crumbId) {
        final ImageView imageView = (ImageView) card.findViewById(R.id.crumb_image);
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        mContext.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        layoutParams.height = width;
        imageView.setLayoutParams(layoutParams);
        // Need to make a base class for loadBitmap, non cards wont work with this.
        Glide.with(mContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+crumbId+".jpg").centerCrop().placeholder(R.drawable.background3).crossFade().into(imageView);
    }

    // Test for the ExoPlayer
    private void exoplayerPlayTest(String strUri, SurfaceView surface) {
//        Uri uri = Uri.parse("http://download.wavetlan.com/SVV/Media/HTTP/H264/Talkinghead_Media/H264_test1_Talkinghead_mp4_480x360.mp4");
//        final int numRenderers = 2;
//
//        // Build the sample source
//        SampleSource sampleSource =
//                new FrameworkSampleSource((Context) mContext, uri, /* headers */ null);
//
//        // Build the track renderers
//        TrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(sampleSource, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
//        TrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource);
//
//        // Build the ExoPlayer and start playback
//        ExoPlayer exoPlayer = ExoPlayer.Factory.newInstance(numRenderers);
//        exoPlayer.prepare(videoRenderer, audioRenderer);
//
//        // Pass the surface to the video renderer.
//        exoPlayer.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface.getHolder().getSurface());
//
//        exoPlayer.setPlayWhenReady(true);
    }
    private void loadAndAdjustVideo(LinearLayout card, String crumbId) {
        final SurfaceView video = (SurfaceView) card.findViewById(R.id.crumb_video);
        ViewGroup.LayoutParams layoutParams = video.getLayoutParams();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        mContext.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        layoutParams.height = width;
        video.setLayoutParams(layoutParams);
        exoplayerPlayTest(LoadBalancer.RequestCurrentDataAddress() + "/images/"+crumbId +".mp4", video);
       /* MediaController mediaController = new MediaController(mContext);
        mediaController.setAnchorView(video);
        video.setMediaController(null);
        video.stopPlayback();

        String urlString = LoadBalancer.RequestCurrentDataAddress() + "/images/"+crumbId +".mp4";
        video.setVideoPath(urlString);
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });*/
//        photo.setVisibility(View.GONE);
        //video.setVisibility(View.VISIBLE);

        //     loadingBruh.setVisibility(View.GONE);
    }

    private void FetchAndBindObject(final LinearLayout card, final String crumbId, int position) {
        if (mDataset.get(position).GetDataType().equals(".mp4")) {
            loadAndAdjustVideo(card, crumbId);
        } else {
            loadAndAdjustImage(card, crumbId);
        }


        final String latLongKey = "GetLatitudeAndLogitudeForCrumb"+crumbId;
        String cachedLatLon =  textCachingInterface.FetchDataInStringFormat(latLongKey);
        if (cachedLatLon == null) {
            String url = LoadBalancer.RequestServerAddress() +"/rest/Crumb/GetLatitudeAndLogitudeForCrumb/"+crumbId;
            AsyncDataRetrieval fetchCardDetails = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
                @Override
                public void onFinished(String result) {
                    BindCard(result, card);
                    textCachingInterface.CacheText(latLongKey, result);
                }
            }, mContext);
            fetchCardDetails.execute();
        } else {
            BindCard(cachedLatLon, card);
        }

               TextView likesCount = (TextView) card.findViewById(R.id.likes_count);
        setUpLikes(crumbId, likesCount);
        final String descriptionKey = "chat"+crumbId;
        String description =  textCachingInterface.FetchDataInStringFormat(descriptionKey);
        if (description == null) {
            String crumbDescriptionUrl = LoadBalancer.RequestServerAddress() +"/rest/login/GetPropertyFromNode/"+crumbId+"/Chat/";
            AsyncDataRetrieval fetchCrumbDescription = new AsyncDataRetrieval(crumbDescriptionUrl, new AsyncDataRetrieval.RequestListener() {
                @Override
                public void onFinished(String result) {
                    TextView crumbDescription = (TextView) card.findViewById(R.id.crumb_description);
                    crumbDescription.setText(result);
                    textCachingInterface.CacheText(descriptionKey, result);
                }
            }, mContext);
            fetchCrumbDescription.execute();
        } else {
            TextView crumbDescription = (TextView) card.findViewById(R.id.crumb_description);
            crumbDescription.setText(description);
        }
    }

    private  void setUpLikes(String crumbId, final TextView likesCountTextView) {
        // Check if the user has liked this crumb

        String url = LoadBalancer.RequestServerAddress() + "/rest/Crumb/GetNumberOfLikesForCrumb/"+ crumbId;
        AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                if (result != null) {
                    likesCountTextView.setText(" ("+result + ")");
                }
            }
        }, mContext);
        asyncDataRetrieval.execute();
    }

    private void BindCard(String data, final LinearLayout card) {
        jsonResult = data;
        // Result is our card details. We need to go though and fetch these
        new Thread(new Runnable() {
            public void run() {
                try {
                    // Fetch our data
                    JSONObject resultJSON = new JSONObject(jsonResult);
                    Double Latitude = resultJSON.getDouble("Latitude");
                    Double Longitude = resultJSON.getDouble("Longitude");
                    Geocoder gcd = new Geocoder(mContext, Locale.getDefault());
                    final List<Address> addresses = gcd.getFromLocation(Latitude, Longitude, 1);
                    if (addresses.size() > 0)
                        // Need to get back on the UI thread to update.
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView locality = (TextView) card.findViewById(R.id.location);
                                locality.setText(addresses.get(0).getLocality());
                            }
                        });
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();
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
                commentTextField.setText("");

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
        final TextView likeButton = (TextView) card.findViewById(R.id.like_button);
        final String likedByUser = textCachingInterface.FetchDataInStringFormat(crumbId + userId);
        if (likedByUser != null && likedByUser.equals("y")) {
            //changed color to blue. Make it unclickable
            likeButton.setTextColor(mContext.getResources().getColor(R.color.accent));
        }

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (likedByUser!= null && likedByUser.equals("y")) {
                    // Remove like from server
                    likeButton.setTextColor(mContext.getResources().getColor(R.color.my_secondary_text));
                    textCachingInterface.CacheText(crumbId+userId, "n");
                } else {
                    // Send the like request url, and set this as a pink so we know its liked.
                    likeButton.setTextColor(mContext.getResources().getColor(R.color.accent));
                    textCachingInterface.CacheText(crumbId+userId, "y");
                    String url = LoadBalancer.RequestServerAddress() + "/rest/Crumb/UserLikesCrumb/"+crumbId +"/"+userId;
                    HTTPRequestHandler httpRequestHandler = new HTTPRequestHandler();
                    httpRequestHandler.SendSimpleHttpRequest(url, mContext);
                }
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
        }, mContext);
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
        }, mContext);
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
        viewUpdater.UpdateTextViewElement(commenter, UserId, "Username", mContext);
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