package com.teamunemployment.breadcrumbs.client.ImageChooser;

import android.app.Activity;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.Facebook.AccountManager;
import com.teamunemployment.breadcrumbs.Framework.JsonHandler;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.Adapters.FacebookImageGridViewAdapter;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.Cards.ImageChooserGridViewAdapter;

import java.util.ArrayList;

/**
 * Created by jek40 on 10/03/2016.
 */
public class FacebookTab extends GridImageSelector {
    private TextView emptyGridInfo;
    private AccountManager.IAccountManagerCallback accountManagerCallback = new AccountManager.IAccountManagerCallback() {
        @Override
        public void onFacebookRequestFinished(ArrayList<String> arrayList) {
            Log.d("FACEBOOK", "Recieved an array of images: " + arrayList.toString());
            loadUpFacebookImages(arrayList);
        }
    };

    private void loadUpFacebookImages(ArrayList<String> ids) {
        final GridView gridview = (GridView)  rootView.findViewById(R.id.gridView1);
        if (ids.size() < 1) {
            // We want to show the placeholder.
            emptyGridInfo.setVisibility(View.VISIBLE);
            SimpleAnimations simpleAnimations = new SimpleAnimations();
            simpleAnimations.FadeInView(emptyGridInfo);
        }

        FacebookImageGridViewAdapter adapter = new FacebookImageGridViewAdapter(ids, activityContext);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // Save our profile header pic.
                String newPicId = idsArray.get(position);

                // Save our new selection locally too.
                //PreferenceManager.getDefaultSharedPreferences(activityContext).edit().putString("TRAILCOVERPHOTO", newPicId).commit();
                //Intent returnIntent = new Intent();
                //activityContext.setResult(Activity.RESULT_OK,returnIntent);

                // Quiting on select for now.
                activityContext.finish();
            }
        });
    }
    @Override
    public void setUpGridAndListeners() {
        String facebookId = PreferenceManager.getDefaultSharedPreferences(activityContext).getString("FACEBOOK_REGISTRATION_ID", "-1");
        if (facebookId.equals("-1")) {
            return;
        }
        emptyGridInfo = (TextView) rootView.findViewById(R.id.empty_grid_placeholder);
        AccountManager accountManager = new AccountManager(activityContext);
        accountManager.GetAllAlbumsForAUser(facebookId, accountManagerCallback);
    }
}
