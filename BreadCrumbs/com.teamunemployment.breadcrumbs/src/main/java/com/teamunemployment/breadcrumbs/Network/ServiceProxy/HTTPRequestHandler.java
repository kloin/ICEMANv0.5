package com.teamunemployment.breadcrumbs.Network.ServiceProxy;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.NetworkConnectivityManager;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.caching.TextCaching;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by aDirtyCanvas on 5/8/2015.
 *
 * This is a class to ease the use of the async classes, by doing the async behavour shit in here,
 * so that I dont have to keep adding in the same callbacks etc, I can just do one function call.
 *
 * If you are doing  simple save request or fetching  string etc or just want to send a url, the method
 * you are looking for is probably in here.
 */
public class HTTPRequestHandler {
    private AsyncDataRetrieval clientRequestProxy;
    private JSONObject jsonObject;
    private String result;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    //This is a request which doesnt need a custom callback, we just want to send something to the server. this does not return anything
    public void SendSimpleHttpRequest(String url, Context context) {
                url = url.replaceAll(" ", "%20");
                url = url.replaceAll("\n", "%0A");

                clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
                    @Override
                    public void onFinished(String result) {
                        Log.i("SAVE", result);
                    }
        },context);

        clientRequestProxy.execute();
    }

    public String SendSimpleHttpRequestAndReturnString(String url, Context context) {
        url = url.replaceAll(" ", "%20");
        clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            /*
             * Override for the
             */
            @Override
            public void onFinished(String res) {
                result = res;
            }
        }, context);
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
                new PreferencesAPI(context).SaveCurrentServerTrailId(Integer.parseInt(res));

            }
        }, context);
        clientRequestProxy.execute();

        return result;
    }

    /*
        Saves a property to a given node. AS per method name.
     */
    public void SaveNodeProperty(String nodeId, String property, String value, Context context) {
        String saveUrl = LoadBalancer.RequestServerAddress() + "/rest/login/SaveStringPropertyToNode/"+nodeId + "/"+property + "/"+ value;
        SendSimpleHttpRequest(saveUrl, context);
    }

    /**
     * <p> Send a network request. Do not call this on the UI thread, it will break. </p>
     * @param url The url you are sending a request to.
     * @param context The context. Used for caching and {@link NetworkConnectivityManager}
     * @return The string output of the result. Careful calling this on anything that does not
     * return its result in string format. This method was written to target ReST methods that return
     * a String.
     */
    public String SendDataRequest(String url, Context context) {
        // check the cache first
        TextCaching caching = new TextCaching(context);
        String key = url.replace("/","");
        key = key.replace(":", "");
        String result = caching.FetchCachedText(key);
        if (result == null) {
            if (!NetworkConnectivityManager.IsNetworkAvailable(context)) {
                Log.d("NETWORK", "No network connection available.");
                return null;
            }
            OkHttpClient client = new OkHttpClient();
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = client.newCall(request).execute();
                result = response.body().string();
                caching.CacheText(key, result);
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Nullable
    public String SendJSONRequest(String url, JSONObject jsonParam) {
        // NOTE - THIS HAS NO CACHE.
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, jsonParam.toString());
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();

            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
