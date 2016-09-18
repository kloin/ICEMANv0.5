package com.teamunemployment.breadcrumbs.Network.ServiceProxy;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import com.teamunemployment.breadcrumbs.Crumb;
import com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jek40 on 4/05/2016.
 */
public class UploadFile extends AsyncTask<Void, Integer, String> {
    private ProgressBar progressBar;
    private String filePath;
    private String url;
    private String fileType;
    private Crumb crumb;
    private Bitmap bitmap;

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

    public UploadFile(String url, RequestListener requestListener, Bitmap bitmap) {
        this.url = url;
        this.requestListener = requestListener;
        this.bitmap = bitmap;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {

    }

    @Override
    protected String doInBackground(Void... params) {
        if (bitmap != null) {
            return uploadFile(bitmap);
        }
        return uploadFile();
    }

    private String uploadFile() {
        try {
            File sourceFile = new File(filePath);
            Log.d("IMAGESAVE", "File...::::" + sourceFile + " : " + sourceFile.exists());

            MediaType MEDIA_TYPE = MediaType.parse("image/jpg");

            // Build up and send response
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
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

    private String uploadFile(Bitmap bitmap) {
        try {
            byte[] bites = Utils.ConvertBitmapToByteArray(bitmap);


            MediaType MEDIA_TYPE = MediaType.parse("image/jpg");

            // Build up and send response
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "data", RequestBody.create(MEDIA_TYPE, bites))
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

        if (requestListener != null) {
            requestListener.onFinished(jsonResult);
        }
    }

}
