package com.breadcrumbs.client.CrumbViewer;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.ServiceProxy.AsyncDataRetrieval;
import com.breadcrumbs.ServiceProxy.AsyncRetrieveImage;
import com.breadcrumbs.ServiceProxy.MasterProxy;
import com.breadcrumbs.caching.GlobalContainer;
import com.breadcrumbs.client.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by aDirtyCanvas on 6/2/2015.
 */
public class CrumbViewerFragment extends Fragment {
    private View rootView;
    private ProgressBar loadingBruh;
    private ImageView photo;
    private Double latitude;
    private Double longitude;
    private String extension;
    private String id;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.unamo_test_view, container, false);
        id = getArguments().getString("Id");
        extension = getArguments().getString("Extension");
        latitude = getArguments().getDouble("Latitude");
        longitude = getArguments().getDouble("Longitude");
        LoadMedia(id);
        return rootView;

    }

    private void SetUpClickHandlers() {
        final Button commentButton = (Button) rootView.findViewById(R.id.comment_button);
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get our variables to save.
                EditText commentTextField = (EditText) rootView.findViewById(R.id.user_comment);
                String CommentText = commentTextField.getText().toString();
                commentTextField.setText("");

                // This removes the focus. There are other ways but this hack is just so simple.
                commentTextField.setEnabled(false);
                commentTextField.setEnabled(true);

                // Save this beast
                //SaveCommentToServer(UserId, EntityId, CommentText);
                //CreateCommentObjectAndDisplayIt("0", UserId, CommentText, EntityId);
            }
        });
    }

    // Our method for sending the save comment http request.
 /*   private void SaveCommentToServer(String UserId, String EntityId, String CommentText) {
        String url = MessageFormat.format("{0}/rest/login/SaveComment/{1}/{2}/{3}",
                LoadBalancer.RequestServerAddress(),
                UserId,
                EntityId,
                CommentText);

        url = url.replaceAll(" ", "%20");
        clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {

            @Override
            public void onFinished(String result) {
                // not Really sure if we can do anything here bar caching. The only problem would be
                // expense for caching this much text???. Doing nothing for the moment.
                CommentId = result;
            }
        });

        clientRequestProxy.execute();
    }*/

    // Send the async request for the image. Needs an image id for url
    private void LoadMedia(String id) {
        loadingBruh = (ProgressBar) rootView.findViewById(R.id.loading_constant_crumb);
        photo = (ImageView) rootView.findViewById(R.id.photo);
        if (extension.equals(".jpg")) {

            AsyncRetrieveImage asyncFetch = new AsyncRetrieveImage(id, new AsyncRetrieveImage.RequestListener() {

                @Override
                public void onFinished(Bitmap result) {
                    loadingBruh.setVisibility(View.GONE);
                    photo.setImageBitmap(result);

                }
            });

            asyncFetch.execute();
        } else {
            //Load video
            String urlString = LoadBalancer.RequestCurrentDataAddress() + "/images/"+id +extension;
            Uri uri= Uri.parse(urlString);
            final VideoView video=(VideoView)rootView.findViewById(R.id.crumbVideo);

            video.setVideoURI(uri);
            photo.setVisibility(View.GONE);
            video.setVisibility(View.VISIBLE);
            loadingBruh.setVisibility(View.GONE);
            video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    video.start();
                }
            });
            //video.start();
           /* video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                }
            });*/


            video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    //getActivity().finish();
                    return false;
                }
            });
        }
    }
/*
    private void LoadComments(String id) {
        // NOTE - It might just be better to start saving the url with the crumb, and setting that as the title.......
        // ===================================================================================================
        String url = MessageFormat.format("{0}/rest/login/LoadCommentsForEvent/{1}",
                LoadBalancer.RequestServerAddress(),
                EntityId);

        url = url.replaceAll(" ", "%20");
        clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {

            @Override
            public void onFinished(String result) {
                //Initialise our object, and attempt to construct it from the string.
                JSONcomments = jsonHandler.convertJsonStringToJsonObject(result);
                // Begin comment construction.
                ConstructCommentsFromJSON();
            }
        });

        clientRequestProxy.execute();
    }
    */
    /*
    This method is used to parse the json object. It then calls the create object for creating the
    comments.      */
   /* private void ConstructCommentsFromJSON() {
        String baseNodeFinder = "Node"; // String we append the index to to get all the keys.
        int index = 0; // Index that we append to the baseNodeFinder to find the comment.
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
                CreateCommentObjectAndDisplayIt(CommentId, UserId, CommentText, EntityId);

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

    // Create an object with and xml template, and set each of the fields on the comment.
    private void CreateCommentObjectAndDisplayIt(String CommentId, String UserId, String CommentText, String EntityId ) {

        //Inflate the comment xml.
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View comment = inflater.inflate(R.layout.comment_base_layout, null);

        TextView commentText = (TextView) comment.findViewById(R.id.comment_text);
        commentText.setText(CommentText);

        // Get the comment scroll view
        LinearLayout scrollLayout = (LinearLayout) rootView.findViewById(R.id.comment_container);
        scrollLayout.addView(comment);

        SetProfilePicture();
    }

    private void SetProfilePicture() {

        final ImageView CommentProfilePic = (ImageView) rootView.findViewById(R.id.user_comment_profile_photo);
        Random random = new Random();
        int id =  random.nextInt();
        CommentProfilePic.setId(id);
        AsyncRetrieveImage asyncFetch = new AsyncRetrieveImage(UserId+"P", new AsyncRetrieveImage.RequestListener() {

            @Override
            public void onFinished(Bitmap result) {
                CommentProfilePic.setImageBitmap(result);

            }
        });

        asyncFetch.execute();
    }*/
}
