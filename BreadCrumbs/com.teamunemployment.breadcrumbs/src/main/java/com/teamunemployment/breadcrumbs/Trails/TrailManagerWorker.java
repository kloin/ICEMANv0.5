package com.teamunemployment.breadcrumbs.Trails;

import android.app.Activity;
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

import com.teamunemployment.breadcrumbs.BreadcrumbsLocationAPI;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncImageFetch;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncSendLargeJsonParam;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Iterator;

/**
 * Created by jek40 on 30/03/2016.
 */
public class TrailManagerWorker {
    public static final int TRAIL_START = 0;
    public static final int TRAIL_END = 1;
    public static final int CRUMB = 2;
    public static final int REST_ZONE = 3;
    public static final int GPS = 4;
    public static final int ACTIVITY_CHANGE = 5;


    public static final int DRIVING = 0;
    public static final int ON_FOOT = 1;


    private Context mContext;
    private PlaceManager mPlaceManager;
    private DatabaseController mDbc;
    private SharedPreferences mPreferences;
    private PreferencesAPI mPreferencesAPI;
    public TrailManagerWorker(Context context) {
        mContext = context;
        mPlaceManager = new PlaceManager();
        mDbc = new DatabaseController(context);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mPreferencesAPI = PreferencesAPI.GetInstance(mContext);
    }

    // This is the method that extracts all our saved data about the server and saves it to the server.
    public void SaveEntireTrail(String trailId) {
        DatabaseController dbc = new DatabaseController(mContext);
        try {
            int localTrailId = PreferencesAPI.GetInstance(mContext).GetLocalTrailId();
            String localTrailString = "";
            if (localTrailId != -1) {
                localTrailString = Integer.toString(localTrailId);
            }

            int index = PreferencesAPI.GetInstance(mContext).GetCurrentIndex();
            // Fetch metadata
            JSONObject metadataJson = dbc.fetchMetadataFromDB(localTrailString);
            JSONObject metadataPackage = new JSONObject();
            JSONObject trailSummary = dbc.GetTrailSummary(trailId);

            metadataPackage.put("Events", metadataJson);
            metadataPackage.put("TrailId", trailId);
            metadataPackage.put("StartDate", dbc.GetStartDateForCurrentTrail());
            metadataPackage.put("EndDate", DateTime.now().toString());
            metadataPackage.put("StartingIndex", index);

            // fetch crumb data
            JSONObject crumbsWithMedia = dbc.GetCrumbsWithMedia(trailId);

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
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

   // private void saveRestZones
    public void saveMetadata(JSONObject metadata, String trailId) {
        String url = MessageFormat.format("{0}/rest/TrailManager/SaveMetaData/{1}",
                LoadBalancer.RequestServerAddress(),
                trailId);
        url = url.replaceAll(" ", "%20");

        AsyncSendLargeJsonParam asyncJSON = new AsyncSendLargeJsonParam(url, new AsyncSendLargeJsonParam.RequestListener() {
            @Override
            public void onFinished(String result) throws JSONException {
                // Our metadata gets returned, so now we have to load the map using the data that gets returned.
                if (result != null) {
                    Log.d("Result", result);
                }

            }
        }, metadata);
        asyncJSON.execute();
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
        }, mContext);
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

    /*
        Events can currently be:
        * Rest zone
        * crumb
        * trailStart
        * activity change
     */
    public void CreateEventMetadata(int eventType, Location location) {
        // Fetch placeId

        String trailId = Integer.toString(PreferencesAPI.GetInstance(mContext).GetLocalTrailId());
        int eventId = mPreferences.getInt("EVENTID", -1);

        if (trailId.equals("-1")) {
            throw new NullPointerException("Cannot create event because we failed to find the trailId");
        }

        if (eventId == -1) {
            eventId = 0;
            //throw new NullPointerException("Cannot create event because we failed to find the eventId");
        }

        // Save event to appropriate database
        switch (eventType) {
            case TRAIL_START:
                mDbc.AddMetadata(eventId, DateTime.now().toString(),location.getLatitude(), location.getLongitude(), trailId, TRAIL_START, mPreferencesAPI.GetTransportMethod());
                break;
            case TRAIL_END:
                mDbc.AddMetadata(eventId, DateTime.now().toString(),location.getLatitude(), location.getLongitude(), trailId, TRAIL_END, mPreferencesAPI.GetTransportMethod());
                break;
            case CRUMB:
                mDbc.AddMetadata(eventId, DateTime.now().toString(),location.getLatitude(), location.getLongitude(), trailId, CRUMB, mPreferencesAPI.GetTransportMethod());
                break;
            case REST_ZONE:
                mDbc.AddMetadata(eventId, DateTime.now().toString(),location.getLatitude(), location.getLongitude(), trailId, REST_ZONE, mPreferencesAPI.GetTransportMethod());
                break;
        }

        mPreferences.edit().putInt("EVENTID", eventId+1).commit();

    }

    public void StartLocalTrail() {
        DatabaseController dbc = new DatabaseController(mContext);
        dbc.SaveTrailStart(null, DateTime.now().toString());
        BreadcrumbsLocationAPI locationAPI = new BreadcrumbsLocationAPI();
        locationAPI.StartLocationService();
        locationAPI.singleAccurateGpsRequest(fetchFirstPointListener);


    }

    private LocationListener fetchFirstPointListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            CreateEventMetadata(0, location);
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
}
