package com.teamunemployment.breadcrumbs.Network.ServiceProxy;

import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;

/**
 * Created by jek40 on 9/01/2016.
 */
public class HomelessNetworkTools {

    public void FetchStringAndSaveToPreferences(final String preferenceReference, String nodeId, String property, final Context context) {
        String url = LoadBalancer.RequestServerAddress() +"/rest/login/GetPropertyFromNode/"+nodeId+"/"+property;
        AsyncDataRetrieval fetchDescription = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                if (result != null && !result.isEmpty()) {
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putString(preferenceReference, result).commit();
                } else {
                    // Should I thrown and exception here?
                    throw new Resources.NotFoundException("Failed to find the requested resource.");
                }
            }
        });
        fetchDescription.execute();
    }
}
