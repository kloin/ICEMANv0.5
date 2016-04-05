package com.teamunemployment.breadcrumbs.Network.ServiceProxy;

/**
 * Created by aDirtyCanvas on 5/30/2015.
 */

import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.teamunemployment.breadcrumbs.Network.ServiceProxy.MultiPartEntity.AndroidMultiPartEntity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;

/**
 * Uploading the file to server
 * */
public class AsyncUploadVideo extends AsyncTask<Void, Integer, String> {
    private ProgressBar progressBar;
    private String filePath;
    private String url;


    @Override
    protected void onPreExecute() {
        // setting progress bar to zero
//        progressBar.setProgress(0);
        super.onPreExecute();
    }

    // This is the interface for the callback method to use.
    public interface RequestListener {
        public void onFinished(String result);
    }
    // Our callback instance
    private RequestListener requestListener;

    // Pass in the callback and the url to our constructor.
    public AsyncUploadVideo(String url, String filePath, RequestListener requestListener){
        this.filePath = filePath;
        this.url = url;
        this.requestListener = requestListener;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        // Making progress bar visible
       // progressBar.setVisibility(View.VISIBLE);

        // updating progress bar value
       // progressBar.setProgress(progress[0]);

        // updating percentage value
        //txtPercentage.setText(String.valueOf(progress[0]) + "%");
    }

    @Override
    protected String doInBackground(Void... params) {
        return uploadFile();
    }

    @SuppressWarnings("deprecation")
    private String uploadFile() {
        String responseString = null;

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);


        try {
            AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                    new AndroidMultiPartEntity.ProgressListener() {

                        @Override
                        public void transferred(long num) {
                            publishProgress((int) ((num / (float) 100) * 100));
                        }
                    });

            File sourceFile = new File(filePath);

            // Adding file data to http body
            entity.addPart("image", new FileBody(sourceFile));

            // Extra parameters if you want to pass to server. Not sure I need this.
   /*         entity.addPart("website",
                    new StringBody("www.androidhive.info"));
            entity.addPart("email", new StringBody("abc@gmail.com"));*/

            long totalSize = entity.getContentLength();
            httppost.setEntity(entity);

            // Making server call
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity r_entity = response.getEntity();

        } catch (ClientProtocolException e) {
            responseString = e.toString();
        } catch (IOException e) {
            responseString = e.toString();
        }
        return responseString;
    }

    // We want to call our interface method that we defined where we called this class once done.
    // THis will allow us to manipulate the data in the way we want once finished.
    @Override
    protected void onPostExecute(String jsonResult) {
        requestListener.onFinished(jsonResult);
    }

}