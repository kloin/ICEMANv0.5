package com.teamunemployment.breadcrumbs.Network.ServiceProxy;

import android.os.AsyncTask;
import android.util.Log;

import com.teamunemployment.breadcrumbs.client.FragmentMaster;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by aDirtyCanvas on 5/23/2015.
 */
public class AsyncPost extends AsyncTask<String, Integer, String> {

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
            // Create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // Make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            // Convert JSONObject to JSON to String
            String jsonString = json.toString();

            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person); // ? what is this?

            // Set json to StringEntity
            StringEntity se = new StringEntity(jsonString);

            // Set httpPost Entity
            httpPost.setEntity(se);

            // Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);
            // Receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
            // Convert inputstream to string
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
            } else {
                result = "-1";
            }

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