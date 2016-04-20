package com.teamunemployment.breadcrumbs.Network.ServiceProxy;

import android.os.AsyncTask;


import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;



/**
 * Created by jek40 on 15/04/2016.
 */
public class AsyncSendLargeJsonParam extends AsyncTask<String, Integer, String> {
    private String url;
    private JSONObject json;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    // jsonResult was losing a variable some how, so im testing with this to see what happens.

    // This is the interface for the callback method to use.
    public interface RequestListener {
        public void onFinished(String result) throws JSONException;


    }

    // Our callback instance
    private RequestListener requestListener;

    // Pass in the callback and the url to our constructor.
    public AsyncSendLargeJsonParam(String url, RequestListener requestListener, JSONObject json) {
        this.url = url;
        this.json = json;
        this.requestListener = requestListener;
    }

    @Override
    protected String doInBackground(String... urls) {
        return SendDataRequest(url, json);
    }

    @Override
    protected void onPostExecute(String jsonResult) {
        if (requestListener != null) {
            try {
                requestListener.onFinished(jsonResult);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    /*
     * This is our method we use to retrieve data using get (or store data.)
     * Remember - GET is pretty shit, I need to do more research
     */
    public String SendDataRequest(String url, JSONObject json) {

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, json.toString());
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
