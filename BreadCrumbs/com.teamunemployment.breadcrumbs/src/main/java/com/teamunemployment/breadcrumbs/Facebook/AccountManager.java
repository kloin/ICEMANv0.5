package com.teamunemployment.breadcrumbs.Facebook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncImageFetch;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.caching.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * THIS CLASS NEEDS A REWORK IT IS GETTING BRUTAL WITH ALL THESE CALLBACKS IM JUST TOO DUMB TO KNOW HOW TO FIX IT.
 *
 * Class to handle all sorts of different facebook requests and shit.
 *
 * Written Josiah 9/03/2016.
 */
public class AccountManager {
    private String faceBookId;
    private String TAG = "FACEBOOK";
    /*
        I will need to do different sorts of shit once I have got the image. This callback interface
        pattern provides that option, although I dont know if I am going to do that yet.
     */
    public interface IAccountManagerCallback {

        void onFacebookRequestFinished(ArrayList<String> arrayList);
    }

    private Context context;

    public AccountManager(Context context) {
        this.context = context;
    }

    public void GetUserProfilePicture(final String facebookUserId) {

       new RetrieveFacebookImage().execute(facebookUserId);
    }

    private class RetrieveFacebookImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            URL imgUrl = null;
            try {
                imgUrl = new URL("https://graph.facebook.com/"
                        + params[0] + "/picture?type=large");
                faceBookId = params[0];
                InputStream in = (InputStream) imgUrl.getContent();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            // Do what we want with this bitmap. Probably save it to a folder on the users phone. com.teamunemployement.breadcrumbs.facebook or some shit.

           // final FileOutputStream cacheWriter = new FileOutputStream(path);
            //result.compress(Bitmap.CompressFormat.PNG, 100, cacheWriter);

            final String userId = PreferenceManager.getDefaultSharedPreferences(context).getString("USERID", "-1");
            Log.d(TAG, "Saving profile picture for userId : " + userId);
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("COVERPHOTOID", userId).commit();
            // Save our profile picture
            AsyncImageFetch imagesave = new AsyncImageFetch(result, userId, new AsyncImageFetch.RequestListener() {

                /*
                 * Override for the onFinished.
                 */
                @Override
                public void onFinished(String result) {
                    //finish();
                    // Save cover Id for user.
                    HTTPRequestHandler requestHandler = new HTTPRequestHandler();
                    requestHandler.SaveNodeProperty(userId, "CoverPhotoId", userId, context);
                }
            });

            imagesave.execute();
          //  cacheWriter.close();

        }
    }

    public ArrayList<String> GetAllAlbumsForAUser(String facebookId, final IAccountManagerCallback callback) {
        final ArrayList<String> ids = new ArrayList<>();

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/"+facebookId+"/albums",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        /* handle the result */
                        Log.d(TAG, "GetAllAlbums returned : " + response);
                        try {
                            JSONObject jsonResponse = response.getJSONObject();
                            JSONArray jsonArray = jsonResponse.getJSONArray("data");
                            int length = jsonArray.length();
                            int counter = 0;
                            while (counter < length) {
                                String id = jsonArray.getJSONObject(counter).get("id").toString();
                                counter+= 1;
                                ids.add(id);
                            }
                            callback.onFacebookRequestFinished(ids);
                            // Need to do some stuff here.
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();

        return ids;
    }

    public ArrayList<String> GetAllPhotoIdsForAnAlbum(String albumId, final IAccountManagerCallback callback) {
        final ArrayList<String> ids = new ArrayList<>();

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + albumId + "/photos",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        Log.d(TAG, "GetAllAlbums returned : " + response);
                        try {
                            JSONObject jsonResponse = response.getJSONObject();
                            JSONArray jsonArray = jsonResponse.getJSONArray("data");
                            int length = jsonArray.length();
                            int counter = 0;
                            while (counter < length) {
                                String id = jsonArray.getJSONObject(counter).get("id").toString();
                                counter+= 1;
                                ids.add(id);
                            }
                            callback.onFacebookRequestFinished(ids);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();

        return ids;
    }

    /*
        Get the id of every single photo a user has ever had on facebook (uploaded or tagged). Will this creep out a user?
     */
    public void GetAllPhotoIdsForAUser(String faceBookId, final IAccountManagerCallback callback) {

        // First we need to get a list of all the albums a user has.
        GetAllAlbumsForAUser(faceBookId, new IAccountManagerCallback() {
            /*
                This is what gets called after we have got all the album Ids. Now we need to go
                through and fetch all the photo ids for EACH album and add them to our list.
             */
            @Override
            public void onFacebookRequestFinished(ArrayList<String> albumIds) {
                final ArrayList<String> photoIds = new ArrayList<String>();
                // So first we need to iterate through the returned album ids
                Iterator<String> itemsIterator = albumIds.iterator();
                while (itemsIterator.hasNext()) {
                    // For each of the album ids we need to get the photo
                    GetAllPhotoIdsForAnAlbum(itemsIterator.next(), new IAccountManagerCallback() {
                        @Override
                        public void onFacebookRequestFinished(ArrayList<String> photoIdsThatWereInAnAlbum) {
                            // This here is where we get the ids for ONE album. We need to merge them into the photoIds arrayList.
                            photoIds.addAll(photoIdsThatWereInAnAlbum);
                        }
                    });
                }
                // This callback is the callback that we pass in from the class that is requesting the ids. They do what they want with them.
                // Probably need to make sure these are cached.
                callback.onFacebookRequestFinished(photoIds);
            }
        });
    }
}
