package com.teamunemployment.breadcrumbs.Network.ServiceProxy;

import android.os.AsyncTask;
import android.util.Log;

import com.teamunemployment.breadcrumbs.client.FragmentMaster;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by aDirtyCanvas on 5/23/2015.
 */
public class AsyncPost extends AsyncTask<String, Integer, String> {

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private FragmentMaster fragment;
    private String url;
    private JSONObject json;
    // jsonResult was losing a variable some how, so im testing with this to see what happens.
    private String localResponse = null;

    // This is the interface for the callback method to use.
    public interface RequestListener {
        public void onFinished(String result);
    }
    // Our callback instance
    private RequestListener requestListener;

    // Pass in the callback and the url to our constructor.
    public AsyncPost(String url, RequestListener requestListener, JSONObject json){
        this.url = url;
        this.json = json;
        this.requestListener = requestListener;
    }

    @Override
    protected String doInBackground(String... urls) {
        return SendDataRequest(url);
    }

    @Override
    protected void onPostExecute(String jsonResult) {
        requestListener.onFinished(this.localResponse);
    }

    /*
     * This is our method we use to retrieve data using get (or store data.)
     * Remember - GET is pretty shit, I need to do more research
     */
    public String SendDataRequest(String url) {

        InputStream inputStream = null;
        // What we will return.
        String result = "";
        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            result = response.body().string();
        } catch (Exception e) {
            Log.d("AsyncPost", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

}