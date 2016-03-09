package com.teamunemployment.breadcrumbs.client;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.teamunemployment.breadcrumbs.Framework.JsonHandler;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncRetrieveImage;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.client.Cards.CrumbCardAdapter;
import com.teamunemployment.breadcrumbs.client.Cards.CrumbCardDataObject;
import com.teamunemployment.breadcrumbs.client.Maps.DisplayCrumb;
import com.google.maps.android.clustering.Cluster;

import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * Created by aDirtyCanvas on 6/11/2015.
 */
public class SelectedEventViewerBase extends AppCompatActivity {

    private LruCache<String, Bitmap> bitmapMemoryCache;
    private ImageView photo;
    private AsyncDataRetrieval clientRequestProxy;
    private JSONObject JSONcomments;
    private JsonHandler jsonHandler;
    private String EntityId;
    private String UserId;
    private Context context;
    private GlobalContainer globalContainer;
    private String CommentId;
    private String extension; // 0 = image, 1 = vid.
    ProgressBar loadingBruh;
    private PopupWindow popupWindow;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<CrumbCardDataObject> crumbsArray;
    private CrumbCardAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set our view.
        setContentView(R.layout.crumbs_holder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        crumbsArray = getIntent().getParcelableArrayListExtra("CrumbArray");
        String trailId = getIntent().getStringExtra("TrailId");
        jsonHandler = new JsonHandler();
        mRecyclerView = (RecyclerView) findViewById(R.id.crumb_recycler);

        toolbar.setTitleTextAppearance(this, R.style.HeaderFont);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setTitleTextColor(Color.WHITE);
        //setUpToolbarBackButton(toolbar);

        setSupportActionBar(toolbar);
        setUpToolbarBackButton(toolbar);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CrumbCardAdapter(crumbsArray, this);
        mRecyclerView.setAdapter(mAdapter);
        setToolbarTitle(toolbar, trailId);
        //BeginLoad();
    }

    private void setUpToolbarBackButton(Toolbar toolbar) {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setToolbarTitle(final Toolbar toolbar, String trailId) {

        // fetch the trailId
        String url = LoadBalancer.RequestServerAddress() + "/rest/login/GetPropertyFromNode/"+trailId+"/TrailName";
        AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                toolbar.setTitle(result);
            }
        });
        asyncDataRetrieval.execute();

    }

        // I do this check here to see if there is only one crumb. If there is, I will load just the one,
        // Else I will load a whole bunch.

    private void BeginLoad() {
        try {
            String crumbId = (String) this.getIntent().getExtras().get("crumbId");
            String ext = (String) this.getIntent().getExtras().get("extension");
            if ( crumbId != null && ext != null ) {
                LoadSingleCrumb(crumbId, ext);
            }
        } catch (NullPointerException ex) {
            Cluster<DisplayCrumb> cluster = globalContainer.GetCluster();
            UserId = globalContainer.GetUserId();
            LoadAndDisplayMedia(cluster);
        }
    }

    // Load a single crumb as opposed to a bunch
    private void LoadSingleCrumb(String crumbId, String ext) {

        if (ext.equals(".jpg")) {
            LoadImage(crumbId);
        } else {
            LoadVideo(crumbId);
        }
    }

        // Our method for sending the save comment http request.
        private void SaveCommentToServer(String UserId, String EntityId, String CommentText) {
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
        }

        // Send the async request for the image. Needs an image id for url
        private void LoadAndDisplayMedia(Cluster<DisplayCrumb> cluster) {
            Object[] crumbCollection = globalContainer.GetCluster().getItems().toArray();

            // Get all the ids for the crumbs that we need to display
            for (int index = 0; index < cluster.getSize(); index+=1) {
                DisplayCrumb crumb = (DisplayCrumb) crumbCollection[index];
                final String trailId = crumb.getId();
                String extension = crumb.getExtension();
                if (extension.equals(".jpg")) {
                    LoadImage(trailId);
                } else {
                    LoadVideo(trailId);
                }
            }
    }

    private void LoadVideo(final String trailId) {
        LayoutInflater inflater = getLayoutInflater();
        final View crumb = inflater.inflate(R.layout.single_video_crumb, null);
        final VideoView video = (VideoView) crumb.findViewById(R.id.crumb_video);
        String urlString = LoadBalancer.RequestCurrentDataAddress() + "/images/"+trailId +".mp4";
        Uri uri= Uri.parse(urlString);

        video.setVideoURI(uri);
//        photo.setVisibility(View.GONE);
        video.setVisibility(View.VISIBLE);
   //     loadingBruh.setVisibility(View.GONE);
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                video.start();
            }
        });

    }

    private void LoadImage(final String trailId) {
        LayoutInflater inflater = getLayoutInflater();
        final View crumb = inflater.inflate(R.layout.crumb_viewing_item, null);
        final ImageView crumb_image = (ImageView) crumb.findViewById(R.id.crumb_image);

        //Listener for opening up the comments view.
        ImageButton commentsButton = (ImageButton) crumb.findViewById(R.id.open_comments_button);
        commentsButton.setTag(trailId);
        commentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load comments intent
                Intent intent = new Intent();
                intent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.CrumbViewer.CommentViewer");
                intent.putExtra("CrumbId", trailId);
                startActivity(intent);

            }
        });

        AsyncRetrieveImage asyncFetch = new AsyncRetrieveImage(trailId, new AsyncRetrieveImage.RequestListener() {
            @Override
            public void onFinished(Bitmap result) {
                crumb_image.setImageBitmap(result);
            }
        });
        asyncFetch.execute();
    }

    private void SetUpClickListeners(View layout) {



//        final Button commentButton = (Button) findViewById(R.id.comment_button);
//        commentButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Get our variables to save.
//                EditText commentTextField = (EditText) findViewById(R.id.user_comment);
//                String CommentText = commentTextField.getText().toString();
//                commentTextField.setText("");
//
//                // This removes the focus. There are other was but this hack is just so simple.
//                commentTextField.setEnabled(false);
//                commentTextField.setEnabled(true);
//
//                // Save this beast
//                SaveCommentToServer(UserId, EntityId, CommentText);
//                CreateCommentObjectAndDisplayIt("0", UserId, CommentText, EntityId);
//            }
//        });
    }


}
