package com.teamunemployment.breadcrumbs.client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.teamunemployment.breadcrumbs.R;

/**
 * Created by Josiah on 2/03/2016.
 */
public class Settings extends Activity {

    private Context context;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        context = this;
        setUpClickHandlers();
    }

    private void setUpClickHandlers() {
        RelativeLayout logoutIcon = (RelativeLayout) findViewById(R.id.logout_wrapper);
        logoutIcon.setOnClickListener(logoutClickListener);
        ImageView backButton = (ImageView) findViewById(R.id.backButtonSettings);
        backButton.setOnClickListener(backClickListener);
        RelativeLayout about = (RelativeLayout) findViewById(R.id.about_wrapper);
        about.setOnClickListener(aboutOnClickListener);
    }

    private View.OnClickListener logoutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Remove UserId from preferences so that Main knows we are logged out.
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove("USERID").commit();
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove("TRAILID").commit();
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove("FACEBOOK_REGISTRATION_ID").commit();

            FacebookSdk.sdkInitialize(context);
            LoginManager.getInstance().logOut();
            // Start our main intent.
            Intent myIntent = new Intent(context, Main.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(myIntent);
        }
    };

    private View.OnClickListener backClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
           finish();
        }
    };

    private View.OnClickListener aboutOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent myIntent = new Intent(context, About.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(myIntent);
        }
    };
}
