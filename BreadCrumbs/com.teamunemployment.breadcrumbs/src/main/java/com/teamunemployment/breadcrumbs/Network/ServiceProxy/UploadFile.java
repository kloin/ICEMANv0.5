package com.teamunemployment.breadcrumbs.Network.ServiceProxy;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.teamunemployment.breadcrumbs.Crumb;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

/**
 * Created by jek40 on 4/05/2016.
 */
public class UploadFile extends AsyncTask<Void, Integer, String> {
    private ProgressBar progressBar;
    private String filePath;
    private String url;
    private String fileType;
    private Crumb crumb;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    // This is the interface for the callback method to use.
    public interface RequestListener {
        public void onFinished(String result);
    }

    // Our callback instance
    private RequestListener requestListener;

    // Pass in the callback and the url to our constructor.
    public UploadFile(String url, String filePath, RequestListener requestListener){
        this.filePath = filePath;
        this.url = url;
        this.requestListener = requestListener;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {

    }

    @Override
    protected String doInBackground(Void... params) {
        return uploadFile();
    }

    private String uploadFile() {
        try {
            File sourceFile = new File(filePath);
            Log.d("IMAGESAVE", "File...::::" + sourceFile + " : " + sourceFile.exists());

            MediaType MEDIA_TYPE = MediaType.parse("image/jpg");

            // Build up and send response
            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("file", "data", RequestBody.create(MEDIA_TYPE, sourceFile))
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();


            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (UnknownHostException | UnsupportedEncodingException e) {
            Log.e("IMAGESAVE", "Error: " + e.getLocalizedMessage());
        } catch (Exception e) {
            Log.e("IMAGESAVE", "Other Error: " + e.getLocalizedMessage());
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
