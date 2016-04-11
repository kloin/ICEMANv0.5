package com.teamunemployment.breadcrumbs.Trails;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.teamunemployment.breadcrumbs.BreadcrumbsLocationAPI;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncImageFetch;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.sql.Blob;
import java.text.MessageFormat;
import java.util.Iterator;

/**
 * Created by jek40 on 30/03/2016.
 */
public class TrailManager {
    public static final int TRAIL_START = 0;
    public static final int TRAIL_END = 1;
    public static final int CRUMB = 2;
    public static final int REST_ZONE = 3;
    public static final int GPS = 4;


    private Context mContext;
    private PlaceManager mPlaceManager;
    private DatabaseController mDbc;
    private SharedPreferences mPreferences;
    public TrailManager(Context context) {
        mContext = context;
        mPlaceManager = new PlaceManager();
        mDbc = new DatabaseController(context);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // This is the method that extracts all our saved data about the server and saves it to the server.
    public void SaveEntireTrail(String trailId) {
        DatabaseController dbc = new DatabaseController(mContext);
        try {
            // Fetch metadata
            JSONObject metadataJson = dbc.fetchMetadataFromDB(trailId);
            JSONObject metadataPackage = new JSONObject();

            metadataPackage.put("Events", metadataJson);
            metadataPackage.put("TrailId", trailId);
            // fetch crumb data
            JSONObject crumbsWithMedia = dbc.GetCrumbsWithMedia(trailId);

            // fetch weather
            // Fetch RestZones
            JSONObject restZones = dbc.GetAllRestZonesForATrail(trailId);

            saveMetadata(metadataPackage, trailId);
            // save the crumbs one by one. Their id matters for loading the crumb, so save them with that AND
            // the metadata id, so i can link the tables together if need be
            Iterator iterator = crumbsWithMedia.keys();
            Log.d("TRAIL_SAVE_TEST", "saving " + iterator.toString());
            while (iterator.hasNext()) {
                try {
                    String key = iterator.next().toString();
                    JSONObject crumb = crumbsWithMedia.getJSONObject(key);
                    String eventId = crumb.getString("eventId");
                    double latitude = crumb.getDouble("latitude");
                    double longitude = crumb.getDouble("longitude");
                    String timeStamp = crumb.getString("timeStamp");
                    String description = crumb.getString("description");
                    String userId = crumb.getString("userId");
                    String icon = " ";//crumb.getString("icon");
                    String media = crumb.getString("media");
                    String placeId = crumb.getString("placeId");
                    String suburb = crumb.getString("suburb");
                    String city = crumb.getString("city");
                    String country = " ";//crumb.getString("country");
                    String mime = crumb.getString("mime");

                    byte[] mediaBytes = Base64.decode(media, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(mediaBytes, 0, mediaBytes.length);
                    Log.d("TRAIL_TEST", "bitmap height " + bitmap.getHeight());

                    createNewCrumb(description, userId, trailId, Double.toString(latitude), Double.toString(longitude), icon, mime, placeId, suburb, city, country, mime, bitmap);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }catch(JSONException e) {
            e.printStackTrace();
        }
    }

   // private void saveRestZones
    public void saveMetadata(JSONObject metadata, String trailId) {
        String url = MessageFormat.format("{0}/rest/TrailManager/SaveMetaData/{1}/{2}",
                LoadBalancer.RequestServerAddress(),
                URLEncoder.encode(metadata.toString()),
                trailId);
        url = url.replaceAll(" ", "%20");
        AsyncDataRetrieval asyncDataRetrieval  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {

            /*
             * Override for the
             */
            @Override
            public void onFinished(String result) {
                Log.d("TRAIL_SAVE", "saved metadata");
            }
        });
        asyncDataRetrieval.execute();
    }

    private void createNewCrumb(String chat, String userId, String trailId, String latitude, String longitude, String icon, String ext, String placeId,
                                String suburb, String city, String country, String timeStamp, final Bitmap media) {
        String url = MessageFormat.format("{0}/rest/login/savecrumb/{1}/{2}/{3}/{4}/{5}/{6}/{7}/{8}/{9}/{10}/{11}/{12}",
                LoadBalancer.RequestServerAddress(),
                chat,
                userId,
                trailId,
                latitude,
                longitude,
                icon,
                ext,
                placeId,
                suburb,
                city,
                country,
                timeStamp);

        url = url.replaceAll(" ", "%20");

        AsyncDataRetrieval asyncDataRetrieval  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {

            /*
             * Override for the
             */
            @Override
            public void onFinished(String result) {
                if (result == null) {
                    throw new NullPointerException("FAILED TO SAVE CRUMB");
                }
                Log.d("TRAIL_SAVE", "saving ID : " + result);

                saveImage(media, result);
            }
        });
        asyncDataRetrieval.execute();
    }

    private void saveImage(Bitmap media, String crumbId) {
        AsyncImageFetch imagesave = new AsyncImageFetch(media, crumbId , new AsyncImageFetch.RequestListener() {

            /*
             * Override for the
             */
            @Override
            public void onFinished(String result) {
                //finish();
            }
        });
        imagesave.execute();
    }

    public void CreateTrail() {

    }

    /*
        Events can currently be:
        * Rest zone
        * crumb
        * trailStart
     */
    public void CreateEventMetadata(int eventType, Location location) {
        // Fetch placeId

        String trailId = mPreferences.getString("TRAILID", null);
        int eventId = mPreferences.getInt("EVENTID", -1);

        if (trailId == null) {
            throw new NullPointerException("Cannot create event because we failed to find the trailId");
        }

        if (eventId == -1) {
            eventId = 0;
            //throw new NullPointerException("Cannot create event because we failed to find the eventId");
        }

        // Save event to appropriate database
        switch (eventType) {
            case TRAIL_START:
                mDbc.AddMetadata(eventId, DateTime.now().toString(),location.getLatitude(), location.getLongitude(), trailId, TRAIL_START);
                break;
            case TRAIL_END:
                mDbc.AddMetadata(eventId, DateTime.now().toString(),location.getLatitude(), location.getLongitude(), trailId, TRAIL_END);
                break;
            case CRUMB:
                mDbc.AddMetadata(eventId, DateTime.now().toString(),location.getLatitude(), location.getLongitude(), trailId, CRUMB);
                break;
            case REST_ZONE:
                mDbc.AddMetadata(eventId, DateTime.now().toString(),location.getLatitude(), location.getLongitude(), trailId, REST_ZONE);
                break;
        }

        mPreferences.edit().putInt("EVENTID", eventId+1).commit();

    }

    public void SavePhotoCrumb(Location location, Bitmap media, String description) {
        String trailId = mPreferences.getString("TRAILID", null);

        //mDbc.SaveCrumb(trailId, description);
    }

    // We want to save the start of the trail. Want an accurate recording for this.
    public void SaveTrailStart() {
        // Need to fetch our location before we save the trail start.
        BreadcrumbsLocationAPI locationAPI = new BreadcrumbsLocationAPI();
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                CreateEventMetadata(TrailManager.TRAIL_START, location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };


        locationAPI.singleAccurateGpsRequest(locationListener);
    }
}
