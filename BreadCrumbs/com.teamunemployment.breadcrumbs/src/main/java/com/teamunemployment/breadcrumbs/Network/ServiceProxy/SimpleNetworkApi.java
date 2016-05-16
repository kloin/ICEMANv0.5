package com.teamunemployment.breadcrumbs.Network.ServiceProxy;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;

import java.util.ArrayList;

/**
 * Created by jek40 on 10/05/2016.
 */
public class SimpleNetworkApi {

    public static void UpdateTextViewWithStringResponseFromAGivenUrl(final TextView view, final String url, Context context){
        AsyncDataRetrieval fetchDescription = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                Log.d("SimpleHttp", "Finished sending request to : " + url + " and recieved result: " + result);
                if (result != null && !result.isEmpty())
                        view.setText(result);
                    }
        }, context);
        fetchDescription.execute();
    }
}
