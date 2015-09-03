package com.breadcrumbs.ServiceProxy;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

/**
 * Created by aDirtyCanvas on 5/8/2015.
 *
 * This is a class to ease the use of the async classes, by doing the async behavour shit in here,
 * so that I dont have to keep adding in the same callbacks etc, I can just do one function call.
 */
public class HTTPRequestHandler {
    private AsyncDataRetrieval clientRequestProxy;
    private JSONObject jsonObject;
    private String result;
    //This is a request which doesnt need a custom callback, we just want to send something to the server. this does not return anything
    public void SendSimpleHttpRequest(String url) {

                url = url.replaceAll(" ", "%20");
                clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
                    @Override
                    public void onFinished(String result) {
                        Log.i("QUERY", result);
                    }
        });

        clientRequestProxy.execute();

    }
    public String SendSimpleHttpRequestAndReturnString(String url) {
        url = url.replaceAll(" ", "%20");
        clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            /*
             * Override for the
             */
            @Override
            public void onFinished(String res) {
                result = res;
            }
        });
        clientRequestProxy.execute();

        return result;
    }

    public String SendSimpleHttpRequestAndSavePreference(String url, final Context context) {
        url = url.replaceAll(" ", "%20");
        clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {

            /*
             * Override for the
             */
            @Override
            public void onFinished(String res) {
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("TRAILID", res).commit();

            }
        });
        clientRequestProxy.execute();

        return result;
    }
}
