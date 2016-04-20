package com.teamunemployment.breadcrumbs.client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.facebook.CallbackManager;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;

/**
 * Created by jek40 on 8/03/2016.
 */
public class SplashScreen extends Activity {
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.splash_screen);
        doLoadAndPassOn();
    }

    private void doLoadAndPassOn() {
        String userId = PreferenceManager.getDefaultSharedPreferences(this).getString("USERID","-1");
        String url = LoadBalancer.RequestServerAddress() + "/rest/login/GetPropertyFromNode/"+userId + "/ActiveTrail";
        AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                if (result == null) {
                    return; // not sure how this is happening, but fuck you anyway it will stop here.
                }

                if (!result.equals("0") && !result.equals("")) {
                    PreferencesAPI.GetInstance(context).SaveCurrentLocalTrailId(Integer.parseInt(result));

                }

                // Start up home page.
                Intent myIntent = new Intent();
                myIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.BaseViewModel");
                startActivity(myIntent);
            }
        }, context);
        asyncDataRetrieval.execute();
    }
}
