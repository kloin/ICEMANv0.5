package com.teamunemployment.breadcrumbs.client.CrumbViewer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.Framework.JsonHandler;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.Iterator;

/**
 * Created by aDirtyCanvas on 6/14/2015.
 */
public class CommentViewer extends Activity {
    private String crumbId;
    private AsyncDataRetrieval clientRequestProxy;
    private JsonHandler jsonHandler;
    private JSONObject JSONcomments;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_popup);
        crumbId = this.getIntent().getStringExtra("CrumbId");
        jsonHandler = new JsonHandler();
        LoadComments();
        userId = GlobalContainer.GetContainerInstance().GetUserId();
        SetUpClickListeners();
    }

    private void SetUpClickListeners() {
        final ImageButton commentButton = (ImageButton) findViewById(R.id.comment_button);
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get our variables to save.
                EditText commentTextField = (EditText) findViewById(R.id.user_comment);
                String CommentText = commentTextField.getText().toString();
                //commentTextField.setText("");

                // This removes the focus. There are other was but this hack is just so simple.
                //commentTextField.setEnabled(true);
                commentTextField.setEnabled(false);

                // Save this beast
                SaveCommentToServer(userId, crumbId, CommentText);
                CreateCommentObjectAndDisplayIt("0", userId, CommentText, crumbId);
            }
        });
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
               // CommentId = result;
            }
        }, getApplicationContext());
        clientRequestProxy.execute();
    }

    private void LoadComments() {
        // NOTE - It might just be better to start saving the url with the crumb, and setting that as the title.......
        // ===================================================================================================
        String url = MessageFormat.format("{0}/rest/login/LoadCommentsForEvent/{1}",
                LoadBalancer.RequestServerAddress(),
                crumbId);

        url = url.replaceAll(" ", "%20");
        clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {

            @Override
            public void onFinished(String result) {
                //Initialise our object, and attempt to construct it from the string.
                JSONcomments = jsonHandler.convertJsonStringToJsonObject(result);
                // Begin comment construction.
                ConstructCommentsFromJSON();
            }
        }, getApplicationContext());
        clientRequestProxy.execute();
    }

    /*
    This method is used to parse the json object. It then calls the create object for creating the
    comments.      */
    private void ConstructCommentsFromJSON() {
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
        LayoutInflater inflater = getLayoutInflater();
        View comment = inflater.inflate(R.layout.comment_base_layout, null);

        TextView commentText = (TextView) comment.findViewById(R.id.comment_text);
        commentText.setText(CommentText);

        // Get the comment scroll view
        LinearLayout scrollLayout = (LinearLayout)findViewById(R.id.comment_holder);
        scrollLayout.addView(comment);
        SetProfilePicture();
    }

    private void SetProfilePicture() {

       /* final ImageView CommentProfilePic = (ImageView) findViewById(R.id.user_comment_profile_photo);
        Random random = new Random();
        int id =  random.nextInt();
        CommentProfilePic.setId(id);
        AsyncRetrieveImage asyncFetch = new AsyncRetrieveImage(userId+"P", new AsyncRetrieveImage.RequestListener() {

            @Override
            public void onFinished(Bitmap result) {
                CommentProfilePic.setImageBitmap(result);

            }
        });

        asyncFetch.execute();*/
    }

}
