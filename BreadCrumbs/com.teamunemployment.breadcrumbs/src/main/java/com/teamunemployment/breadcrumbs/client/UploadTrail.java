package com.teamunemployment.breadcrumbs.client;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Trails.TrailManagerWorker;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.json.JSONException;

import java.text.MessageFormat;

/**
 * Created by jek40 on 19/04/2016.
 */
public class UploadTrail extends Activity {

    private PreferencesAPI mPreferencesAPI;
    private boolean isEditingTrailName = false;
    private Context mContext;
    private DatabaseController mDbc;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_trail);
        mContext = this;
        mPreferencesAPI = PreferencesAPI.GetInstance(this);
        setClickHandlers();
        setTrailNameView();
    }

    private void setTrailNameView() {
        if (mDbc == null) {
            mDbc = new DatabaseController(mContext);
        }

        String trailName = mPreferencesAPI.GetTrailName();
        setEditText(trailName);
        setTextView(trailName);
    }

    private void setEditText(String textToSet) {
        EditText editText = (EditText) findViewById(R.id.title_edit);
        editText.setText(textToSet);
    }

    private void setTextView(String textToSet) {
        TextView textView = (TextView) findViewById(R.id.trail_title_text_view);
        textView.setText(textToSet);
    }

    private void setClickHandlers() {
        ImageView backButton = (ImageView) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final TextView edit = (TextView) findViewById(R.id.edit_trail_name_publish_page);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save and change back to default
                if (isEditingTrailName) {
                    edit.setText("EDIT");
                    showTextView();
                    hideEditText();
                    saveNewTrailName();

                    // Set our textview to have the same as the text that we just saved.
                    String text = fetchTrailNameFromCard();
                    setTextView(text);
                    isEditingTrailName = false;
                }
                // Show edit text, change to SAVE
                else {
                    edit.setText("SAVE");
                    isEditingTrailName = true;
                    hideTextView();
                    showEditText();
                }
            }
        });

        TextView publishUpdate = (TextView) findViewById(R.id.publish_trail);
        publishUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send save Request to server
                int serverTrailId = mPreferencesAPI.GetServerTrailId();
                if (serverTrailId == -1) {
                    handleFirstTimePublishing();
                } else {
                    publishTrail(Integer.toString(serverTrailId));
                }
                finish();
            }
        });
    }

    private void hideTextView() {
        TextView trailTitle = (TextView) findViewById(R.id.trail_title_text_view);
        trailTitle.setVisibility(View.GONE);
    }

    private void showTextView() {
        TextView trailTitle = (TextView) findViewById(R.id.trail_title_text_view);
        trailTitle.setVisibility(View.VISIBLE);
    }

    private void showEditText() {
        EditText trailNameEditText = (EditText) findViewById(R.id.title_edit);
        trailNameEditText.setVisibility(View.VISIBLE);
    }

    private void hideEditText() {
        EditText trailNameEditText = (EditText) findViewById(R.id.title_edit);
        trailNameEditText.setVisibility(View.GONE);
    }

    public void saveNewTrailName() {

        // save to database
        int trailId = PreferencesAPI.GetInstance(mContext).GetServerTrailId();
        String trailName = fetchTrailNameFromCard();
        if (trailId == -1) {
            // Am just returning because when I publish for the first time we will create a trail using the name in the edit text/textview.
            mPreferencesAPI.SaveTrailNameString(trailName);
            return;
        }


        mPreferencesAPI.SaveTrailNameString(trailName);
        if (trailName == null) {
            Toast.makeText(mContext, "Trail name must have at least 1 character", Toast.LENGTH_LONG);
            return;
        }

        // Save our trail.
        sendSaveTrailRequest(Integer.toString(trailId), trailName);

    }
    private void sendSaveTrailRequest(String trailId, String trailName) {
        HTTPRequestHandler requestHandler = new HTTPRequestHandler();
        requestHandler.SaveNodeProperty(trailId, "TrailName", trailName, mContext);

    }

    private String fetchTrailNameFromCard() {
        EditText trailNameEditText = (EditText) findViewById(R.id.title_edit);
        Editable trailNameEditable = trailNameEditText.getText();
        if (trailNameEditable != null) {
            return trailNameEditable.toString();
        }
        return null;
    }

    private void handleFirstTimePublishing() {
        String trailName = fetchTrailNameFromCard();
        String userId = mPreferencesAPI.GetUserId();

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
                mPreferencesAPI.SaveCurrentServerTrailId(Integer.parseInt(result));
                publishTrail(result);
            }
        }, mContext);
        asyncDataRetrieval.execute();
    }

    private void publishTrail(String trailId) {
        // publishing needs :
        // get trail from current index to now.
        TrailManagerWorker trailManager = new TrailManagerWorker(mContext);
        trailManager.SaveEntireTrail(trailId);
    }
}
