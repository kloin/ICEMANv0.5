package com.breadcrumbs.ServiceProxy;

import android.view.View;
import android.widget.TextView;

import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.client.R;

/**
 * Created by aDirtyCanvas on 7/26/2015.
 */
public class UpdateViewElementWithProperty {

    public void UpdateTextViewElement(final TextView viewElementToUpdate, String nodeId, String nodeProperty) {
        String url = LoadBalancer.RequestServerAddress() +"/rest/login/GetPropertyFromNode/"+nodeId+"/"+nodeProperty;
        AsyncDataRetrieval fetchDescription = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
              viewElementToUpdate.setText(result);
            }
        });
        fetchDescription.execute();
    }
}
