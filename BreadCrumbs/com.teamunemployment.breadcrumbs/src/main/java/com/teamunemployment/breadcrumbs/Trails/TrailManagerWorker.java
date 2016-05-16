package com.teamunemployment.breadcrumbs.Trails;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.teamunemployment.breadcrumbs.BackgroundServices.StopTrackingIntent;
import com.teamunemployment.breadcrumbs.BreadcrumbsActivityAPI;
import com.teamunemployment.breadcrumbs.BreadcrumbsLocationAPI;
import com.teamunemployment.breadcrumbs.Crumb;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncSendLargeJsonParam;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncUploadVideo;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UploadFile;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.caching.TextCaching;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

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
    private DatabaseController mDbc;
    private SharedPreferences mPreferences;
    private PreferencesAPI mPreferencesAPI;
    public TrailManagerWorker(Context context) {
        mContext = context;
        mDbc = new DatabaseController(context);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mPreferencesAPI = new PreferencesAPI(mContext);
    }

    // This is the method that extracts all our saved data about the server and saves it to the server.
    public void SaveEntireTrail(String trailId) {
        DatabaseController dbc = new DatabaseController(mContext);
        try {
            int localTrailId = mPreferencesAPI.GetLocalTrailId();
            String localTrailString = "";
            if (localTrailId != -1) {
                localTrailString = Integer.toString(localTrailId);
            }

            int index = mPreferencesAPI.GetCurrentIndex();

            // Fetch metadata
            JSONObject metadataJson = dbc.fetchMetadataFromDB(localTrailString, true);
            JSONObject metadataPackage = new JSONObject();
            JSONObject trailSummary = dbc.GetTrailSummary(trailId);
            metadataPackage.put("Events", metadataJson);
            metadataPackage.put("TrailId", trailId);
            metadataPackage.put("StartDate", dbc.GetStartDateForCurrentTrail());
            metadataPackage.put("EndDate", DateTime.now().toString());
            metadataPackage.put("StartingIndex", index);

            // Fetch crumb data
            int crumbsIndex = mPreferencesAPI.GetLastSavedMediaCrumbIndex();
            JSONObject crumbsWithMedia = dbc.GetCrumbsWithMedia(localTrailString, crumbsIndex);

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
                    JSONObject crumbJSON = crumbsWithMedia.getJSONObject(key);
                    Crumb crumb = new Crumb(crumbJSON);
                    if (crumb.GetMediaType().equals(".mp4")) {
                        saveMp4Crumb(crumb);
                    } else {
                        saveJpegCrumb(crumb);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveMp4Crumb(Crumb crumb) {
        // Save crumb for the crumb
        String url = MessageFormat.format("{0}/rest/login/savecrumb/{1}/{2}/{3}/{4}/{5}/{6}/{7}/{8}/{9}/{10}/{11}/{12}",
                LoadBalancer.RequestServerAddress(),
                " ",
                crumb.GetUserId(),
                Integer.toString(mPreferencesAPI.GetServerTrailId()),
                Double.toString(crumb.GetLatitude()),
                Double.toString(crumb.GetLongitude()),
                " ",
                ".mp4",
                crumb.GetPlaceId(),
                crumb.GetSuburb(),
                crumb.GetCity(),
                crumb.GetCountry(),
                crumb.GetTimestamp());
        url = url.replaceAll(" ", "%20");
        final int index = crumb.GetIndex();
        final String eventId = crumb.GetEventId();
        final String fileName =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/"+ eventId + ".mp4";
        // Send request to save vide
        AsyncDataRetrieval asyncDataRetrieval  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                if (result == null) {
                    //throw new NullPointerException("FAILED TO SAVE CRUMB");
                }
                Log.d("TRAIL_SAVE", "saving ID : " + result);
                saveVideo(result, fileName, index);
            }
        }, mContext);
        asyncDataRetrieval.execute();
    }

    private void saveVideo (String savedCrumbId, String filePath, final int index) {
        String url = MessageFormat.format("{0}/rest/login/saveCrumbWithVideo/{1}",
                LoadBalancer.RequestServerAddress(),
                savedCrumbId);

        url = url.replaceAll(" ", "%20");
        AsyncUploadVideo uploadVideo = new AsyncUploadVideo(url, filePath, new AsyncUploadVideo.RequestListener() {

            /*
             * Override for the
             */
            @Override
            public void onFinished(String result) {
                if (result != null && !result.startsWith("<")) {
                    // update where we are up to.
                    mPreferencesAPI.SetLastSavedMediaCrumbIndex(index);
                    TextCaching textCaching = new TextCaching(mContext);
                }
            }
        });
        uploadVideo.execute();
    }
    public void saveJpegCrumb(Crumb crumb) {
        String url = MessageFormat.format("{0}/rest/login/savecrumb/{1}/{2}/{3}/{4}/{5}/{6}/{7}/{8}/{9}/{10}/{11}/{12}",
                LoadBalancer.RequestServerAddress(),
                " ",
                crumb.GetUserId(),
                Integer.toString(mPreferencesAPI.GetServerTrailId()),
                Double.toString(crumb.GetLatitude()),
                Double.toString(crumb.GetLongitude()),
                " ",
                ".jpg",
                crumb.GetPlaceId(),
                crumb.GetSuburb(),
                crumb.GetCity(),
                crumb.GetCountry(),
                crumb.GetTimestamp());
        url = url.replaceAll(" ",
         "%20");
        final int index = crumb.GetIndex();
        final String filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/"+ crumb.GetEventId() + ".jpg";
        final String eventId = crumb.GetEventId();
        AsyncDataRetrieval asyncDataRetrieval  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {

            /*
             * Override for the
             */
            @Override
            public void onFinished(String result) {
                if (result == null) {
                    //throw new NullPointerException("FAILED TO SAVE CRUMB");
                }
                Log.d("TRAIL_SAVE", "saving ID : " + result);

                saveImage(filename, result, index, eventId);
            }
        }, mContext);
        asyncDataRetrieval.execute();
    }

    private Bitmap getCrumbBitmap(String eventId) {
        String filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/"+ eventId + ".jpg";
        // Grab the bitmap that we have saved to the pictures/breadcrumbs/eventId.png directory

        return null;
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
                                String suburb, String city, String country, String timeStamp, final Bitmap media, final int index, final String eventId) {

    }

    private void saveImage(String fileName, String crumbId, final int index, final String eventId) {
        String url = LoadBalancer.RequestServerAddress() + "/rest/login/savecrumb/"+ crumbId;
        UploadFile imagesave = new UploadFile(url, fileName, new UploadFile.RequestListener() {

            /*
             * Override for the
             */
            @Override
            public void onFinished(String result) {
                if (result != null && !result.startsWith("<")) {
                    // update where we are up to.
                    mPreferencesAPI.SetLastSavedMediaCrumbIndex(index);
                    TextCaching textCaching = new TextCaching(mContext);
                    textCaching.DeleteCacheFile(eventId);
                }
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

        String trailId = Integer.toString(mPreferencesAPI.GetLocalTrailId());
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

        // This is where we increment
        mPreferences.edit().putInt("EVENTID", eventId+1).commit();

    }

    public void StartLocalTrail() {
        mPreferencesAPI.RemoveTrailBasedValues();
        DatabaseController dbc = new DatabaseController(mContext);

        // Start our trail. note that the local trial id is saved to preferences inside this method
        dbc.SaveTrailStart(null, DateTime.now().toString());
        BreadcrumbsLocationAPI locationAPI = new BreadcrumbsLocationAPI();

        Intent stopIntent = new Intent(mContext.getApplicationContext(), StopTrackingIntent.class);
       // stopIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
       // stopIntent.setAction("STOPGPS");
      //  int requestID = (int) System.currentTimeMillis();
        PendingIntent pIntent = PendingIntent.getActivity(mContext.getApplicationContext(), 1000, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Start up our background service.
        locationAPI.StartLocationService();
        final NotificationManager systemService = (NotificationManager) mContext.getApplicationContext().getSystemService(mContext.getApplicationContext().NOTIFICATION_SERVICE);
        Notification n  = new Notification.Builder(mContext.getApplicationContext())
                .setContentTitle("Breadcrumbs")
                .setContentText("Tracking is currently enabled.")
                .setSmallIcon(R.drawable.ic_launcher)
                .addAction(R.drawable.ic_action_cancel, "Stop", pIntent)
                .build();
        systemService.notify(2222, n);
        locationAPI.singleAccurateGpsRequest(fetchFirstPointListener);

        // Start up our activity service
        BreadcrumbsActivityAPI activityAPI = new BreadcrumbsActivityAPI();
        activityAPI.ListenToUserActivityChanges();
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
