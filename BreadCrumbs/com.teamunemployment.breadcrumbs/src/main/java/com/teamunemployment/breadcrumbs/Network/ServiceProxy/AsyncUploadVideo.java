package com.teamunemployment.breadcrumbs.Network.ServiceProxy;

/**
 * Created by aDirtyCanvas on 5/30/2015.
 */

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

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
        try {
            File sourceFile = new File(filePath);

            Log.d("VIDEOSAVE", "File...::::" + sourceFile + " : " + sourceFile.exists());

            final MediaType MEDIA_TYPE_MP4 = MediaType.parse("video/mp4");

            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("file", "test.mp4", RequestBody.create(MEDIA_TYPE_MP4, sourceFile))
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (UnknownHostException | UnsupportedEncodingException e) {
            Log.e("VIDEOSAVE", "Error: " + e.getLocalizedMessage());
        } catch (Exception e) {
            Log.e("VIDEOSAVE", "Other Error: " + e.getLocalizedMessage());
        }
        return null;
    }

    // We want to call our interface method that we defined where we called this class once done.
    // THis will allow us to manipulate the data in the way we want once finished.
    @Override
    protected void onPostExecute(String jsonResult) {
        requestListener.onFinished(jsonResult);
    }
}