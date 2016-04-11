package com.teamunemployment.breadcrumbs.client;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLogoutListener;
import com.teamunemployment.breadcrumbs.BreadcrumbsActivityAPI;
import com.teamunemployment.breadcrumbs.BreadcrumbsLocationAPI;
import com.teamunemployment.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.client.NavMenu.About;

/**
 * Created by Josiah on 2/03/2016.
 */
public class Settings extends Activity {
    private final String TAG = "SETTINGS";
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

        RelativeLayout deleteAccount = (RelativeLayout) findViewById(R.id.delete_account_wrapper);
        deleteAccount.setOnClickListener(deleteAccountClickListener);
    }

    private OnLogoutListener onLogoutListener = new OnLogoutListener() {

        @Override
        public void onLogout() {
            Log.i(TAG, "You are logged out");
        }
    };
    private View.OnClickListener logoutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Remove UserId from preferences so that Main knows we are logged out.
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove("USERID").commit();
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove("TRAILID").commit();
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove("FACEBOOK_REGISTRATION_ID").commit();
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove("COVERPHOTOID").commit();
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove("TRACKING").commit();

            //BreadCrumbsFusedLocationProvider provider = GlobalContainer.GetContainerInstance().GetBreadCrumbsFusedLocationProvider();
           // if (provider != null) {
         //       provider.StopBackgroundGPSSerivce();
           // }
            //Stop Tracking
            BreadcrumbsLocationAPI locationAPI = new BreadcrumbsLocationAPI();
            locationAPI.StopLocationService();
            // Need to destroy service here

            SimpleFacebook simpleFacebook = SimpleFacebook.getInstance();
            if (simpleFacebook != null) {
                // Should never be null anyway
                simpleFacebook.logout(onLogoutListener);
            }

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

    private View.OnClickListener deleteAccountClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Need to do account logic here.
            showDialog("You are about to delete your account. This action is not reversible.");
        }
    };

    private void showDialog(String message) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.delete_account_popup);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.denied_message);
        messageTextView.setText(message);
        TextView dialogButton = (TextView) dialog.findViewById(R.id.cancel_dialog);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        TextView deleteButton = (TextView) dialog.findViewById(R.id.delete_dialog);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser();
            }
        });
        dialog.show();
    }

    private void deleteUser() {
        String userId = PreferenceManager.getDefaultSharedPreferences(context).getString("TRAILID", "-1");
        String url = LoadBalancer.RequestServerAddress() + "/rest/login/DeleteNode/" + userId;
        HTTPRequestHandler requestHandler = new HTTPRequestHandler();
        requestHandler.SendSimpleHttpRequest(url);

        // Hopefully that is all I need to do. May need to return something here if it fails.
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove("USERID").commit();
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove("TRAILID").commit();
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove("FACEBOOK_REGISTRATION_ID").commit();
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove("COVERPHOTOID").commit();
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove("TRACKING").commit();

        //Stop Tracking
        BreadCrumbsFusedLocationProvider provider = GlobalContainer.GetContainerInstance().GetBreadCrumbsFusedLocationProvider();
        provider.StopBackgroundGPSSerivce();

        // Close the app

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);

        Intent myIntent = new Intent(context, Main.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myIntent);
    }
}
