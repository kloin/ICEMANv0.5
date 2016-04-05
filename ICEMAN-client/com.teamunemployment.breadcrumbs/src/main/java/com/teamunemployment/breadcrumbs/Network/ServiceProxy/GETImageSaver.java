package com.teamunemployment.breadcrumbs.Network.ServiceProxy;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.client.FragmentMaster;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * The purpose of this class is to save images to the server using the server GET method.
 * This should replace the current AsyncImageFetch.
 */
public class GETImageSaver extends AsyncTask<String, Integer, String> {
    private JSONObject json = new JSONObject();
    private FragmentMaster fragment;
    private String stringUrl;
    private Bitmap bm;
    private File image;
    // This is the interface for the callback method to use.
    public interface RequestListener {
        public void onFinished(String result);
    }
    // Our callback instance
    private RequestListener requestListener;

    // Pass in the callback and the url to our constructor.
    public GETImageSaver(Bitmap bm, String url, RequestListener requestListener){
        this.bm = bm;
        this.stringUrl = url;
        this.requestListener = requestListener;
    }

    @Override
    protected String doInBackground(String... urls) {
        return PostImage();
    }

    @Override
    protected void onPostExecute(String jsonResult) {
        requestListener.onFinished(jsonResult);
    }

    /*
     * Send an image to the server
     */
    public String PostImage() {

        HttpURLConnection connection = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
            byte[] b = baos.toByteArray();
           // b = Arrays.copyOfRange(b, 0, b.length-2);
            String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            String urlParameters = encodedImage;
            //Create connection
            URL url = new URL(stringUrl);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/json");

            connection.setRequestProperty("Content-Length", "" +
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches (false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream ());
            wr.writeBytes (urlParameters);
            wr.flush ();
            wr.close ();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            if(connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
}
