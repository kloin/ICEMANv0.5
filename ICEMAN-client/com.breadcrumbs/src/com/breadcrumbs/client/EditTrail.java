package com.breadcrumbs.client;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.ServiceProxy.AsyncDataRetrieval;
import com.breadcrumbs.caching.GlobalContainer;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.text.MessageFormat;
import java.util.prefs.Preferences;

/**
 * Created by aDirtyCanvas on 6/28/2015.
 */
public class EditTrail extends Activity {

    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trail_editor_window);
        context = this;
        setUpButtonListeners();
        ActionBar actionBar = getActionBar();
      //  actionBar.hide();

    }

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
    }

    // Handler to create the trail based on the data we have been given
    private void setUpButtonListeners() {
       FloatingActionButton saveTrail = (FloatingActionButton) findViewById(R.id.save_trail);
        saveTrail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewTrail();
                finish();
            }
        });

        ImageView trailCoverPhoto = (ImageView) findViewById(R.id.trail_image);
        trailCoverPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageSelector = new Intent();
                imageSelector.putExtra("TrailId", "1");
                imageSelector.setClassName("com.breadcrumbs.client", "com.breadcrumbs.client.BreadCrumbsImageSelector");
                startActivity(imageSelector);
            }
        });

        ImageButton backButton = (ImageButton) findViewById(R.id.exit_trail_editor);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  if (isDirty) {
                    //save();
                   // Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                //}
                finish();
            }
        });


    }

    private void createNewTrail() {
        //Get the text for description etc..
        EditText trailTitleEdit = (EditText) findViewById(R.id.trail_title);
        EditText trailDescriptionEdit = (EditText) findViewById(R.id.trail_description);

        String trailTitle = trailTitleEdit.getText().toString();
        if (trailTitle == null) {
            Toast.makeText(context, "Enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        String trailDescription = trailDescriptionEdit.getText().toString();
        String userId = GlobalContainer.GetContainerInstance().GetUserId();//PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("USERID", "-1");

        String url = MessageFormat.format("{0}/rest/login/saveTrail/{1}/{2}/{3}",
                LoadBalancer.RequestServerAddress(),
                trailTitle,
                trailDescription,
                userId);

        url = url.replaceAll(" ", "%20");
        AsyncDataRetrieval asyncDataRetrieval  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {

            /*
             * Override for the
             */
            @Override
            public void onFinished(String result) {
                // Not sure I want to do anything here.
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("TRAILID", result).commit();
            }
        });

        asyncDataRetrieval.execute();
        finish();
    }
}
