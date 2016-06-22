package com.teamunemployment.breadcrumbs.Trails;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ProgressBar;

import com.pathsense.locationengine.apklib.data.t;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.teamunemployment.breadcrumbs.ActivityRecognition.ActivityController;
import com.teamunemployment.breadcrumbs.BackgroundServices.StopTrackingIntent;
import com.teamunemployment.breadcrumbs.BreadcrumbsActivityAPI;
import com.teamunemployment.breadcrumbs.BreadcrumbsLocationAPI;
import com.teamunemployment.breadcrumbs.Crumb;
import com.teamunemployment.breadcrumbs.Location.SimpleGps;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncSendLargeJsonParam;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncUploadVideo;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UploadFile;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.caching.TextCaching;
import com.teamunemployment.breadcrumbs.caching.Utils;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Iterator;

/**
 * Created by jek40 on 30/03/2016.
 */
public class TrailManagerWorker {
    private static final String TAG = "TrailManagerWorker";

    // Possible activities
    public static final int TRAIL_START = 0;
    public static final int TRAIL_END = 1;
    public static final int CRUMB = 2;
    public static final int REST_ZONE = 3;
    public static final int GPS = 4;
    public static final int ACTIVITY_CHANGE = 5;

    public static final int DRIVING = 0;
    public static final int ON_FOOT = 1;

    private Context mContext;
    private DatabaseController dbc;
    private SharedPreferences mPreferences;
    private PreferencesAPI mPreferencesAPI;
    private Iterator<String> crumbsIterator;
    private JSONObject crumbsWithMedia;
    public TrailManagerWorker(Context context) {
        mContext = context;
        dbc = new DatabaseController(context);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mPreferencesAPI = new PreferencesAPI(mContext);
    }

    /**
     * Method to publish a new or updated trail to the server.
     *
     * @param trailId The server trail Id of the trail that we want to save.
     */
    public void SaveEntireTrail(String trailId) {
        try {
            mPreferencesAPI.SetIsUploading(true);
            // The id for the data we want to retrieve from our local databases.
            String localTrailString = getLocalTrailId();

            // Shit all we can do in this case. Can not think of any reason as to why this would ever happen though.
            if (localTrailString.isEmpty()) {
                // Need to notify the user.
                mPreferencesAPI.SetIsUploading(false);
                return;
            }
            // Index - incase this is not a new trail but an update.
            int index = mPreferencesAPI.GetCurrentIndex();
            // Grab data to save
            JSONObject metadataJson = fetchMetadataJSONForOurCurrentTrail();
            // The package of metadata that we will send to the server.
            JSONObject metadataPackage = new JSONObject();

            // Add the metadata info to the package.
            metadataPackage.put("Events", metadataJson);
            metadataPackage.put("TrailId", trailId);
            metadataPackage.put("StartDate", dbc.GetStartDateForCurrentTrail());
            metadataPackage.put("EndDate", DateTime.now().toString());
            metadataPackage.put("StartingIndex", index);

            int localTrailId = mPreferencesAPI.GetLocalTrailId();
            JSONObject metadata = dbc.GetAllActivityData(localTrailId);

            savePath(metadata, trailId);
            // Fetch crumb data. Remember this may be an update
            int crumbsIndex = mPreferencesAPI.GetLastSavedMediaCrumbIndex();
            crumbsWithMedia= dbc.GetCrumbsWithMedia(localTrailString, crumbsIndex);

            // Save the metadata for the trail
            saveMetadata(metadataPackage, trailId);

            // Save the crumbs in this trail
            saveCrumbs(crumbsWithMedia);
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private void savePath(JSONObject metadata, String serverTrailId) {
        String url = MessageFormat.format("{0}/rest/TrailManager/SavePath/{1}",
                LoadBalancer.RequestServerAddress(),
                serverTrailId);
        url = url.replaceAll(" ", "%20");

        // We should not be sending JSON as url encoded parameters. I know in some places we do and that is wrong. This ius the correct way to do it.
        AsyncSendLargeJsonParam asyncJSON = new AsyncSendLargeJsonParam(url, new AsyncSendLargeJsonParam.RequestListener() {
            @Override
            public void onFinished(String result) throws JSONException {
                // Our metadata gets returned, so now we have to load the map using the data that gets returned.
                if (result != null) {
                    Log.d("Result", result);
                }

                // If we fail, we should handle this here.
            }
        }, metadata);
        asyncJSON.execute();
    }

    /**
     * Saves the medatadata. This is done using a single request to the server, by sending the
     * data to the server as JSON.
     *
     * @param metadata The json object of metadata that we are POSTing to the server
     */
    public void saveMetadata(JSONObject metadata, String trailId) {
        String url = MessageFormat.format("{0}/rest/TrailManager/SaveMetaData/{1}",
                LoadBalancer.RequestServerAddress(),
                trailId);
        url = url.replaceAll(" ", "%20");

        // We should not be sending JSON as url encoded parameters. I know in some places we do and that is wrong. This ius the correct way to do it.
        AsyncSendLargeJsonParam asyncJSON = new AsyncSendLargeJsonParam(url, new AsyncSendLargeJsonParam.RequestListener() {
            @Override
            public void onFinished(String result) throws JSONException {
                // Our metadata gets returned, so now we have to load the map using the data that gets returned.
                if (result != null) {
                    Log.d("Result", result);
                }

                // If we fail, we should handle this here.
            }
        }, metadata);
        asyncJSON.execute();
    }

    /**
     * Create an event in the metadata database.
     * @param eventType The type of event that we are creating metadata for. See {@link #TRAIL_START}
     *                  to see the list of possible events. THis is not an exhaustive list - it will
     *                  need to be added too.
     * @param location The GPS location of the event that we are saving.
     */
    public void CreateEventMetadata(int eventType, Location location) {
        // Fetch placeId
        String trailId = Integer.toString(mPreferencesAPI.GetLocalTrailId());

        // Grab the event id for this event.
        int eventId = mPreferences.getInt("EVENTID", -1);

        // Standard check. Probably shouldnt throw an error but fetch the trailId.
        if (trailId.equals("-1")) {
            throw new NullPointerException("Cannot create event because we failed to find the trailId");
        }

        // Standard check.
        if (eventId == -1) {
            eventId = 0;
            //throw new NullPointerException("Cannot create event because we failed to find the eventId");
        }

        // Save event to appropriate database
        switch (eventType) {
            case TRAIL_START:
                dbc.AddMetadata(eventId, " ",location.getLatitude(), location.getLongitude(), trailId, TRAIL_START, mPreferencesAPI.GetTransportMethod());
                break;
            case TRAIL_END:
                dbc.AddMetadata(eventId, " ",location.getLatitude(), location.getLongitude(), trailId, TRAIL_END, mPreferencesAPI.GetTransportMethod());
                break;
            case CRUMB:
                dbc.AddMetadata(eventId, " ",location.getLatitude(), location.getLongitude(), trailId, CRUMB, mPreferencesAPI.GetTransportMethod());
                break;
            case REST_ZONE:
                dbc.AddMetadata(eventId, " ",location.getLatitude(), location.getLongitude(), trailId, REST_ZONE, mPreferencesAPI.GetTransportMethod());
                break;
        }
    }

    /**
     * Handle the create trail call from the user. This method handles setting up and starting a trail.
     */
    public void StartLocalTrail() {
        final DatabaseController dbc = new DatabaseController(mContext);
        BreadcrumbsLocationAPI locationAPI = new BreadcrumbsLocationAPI();

        // clean up any old trail data that may exist.
        locationAPI.RemoveGeofences();
        mPreferencesAPI.RemoveTrailBasedValues();
        mPreferencesAPI.SetUserTracking(true);

        // Start our trail. note that the local trial id is saved to preferences inside this method
        dbc.SaveTrailStart(null, DateTime.now().toString());

        // Shouldnt really be putting this shit here.
        getFirstPoint();
        ActivityController activityController = new ActivityController(mContext);
        activityController.StartListenting();
    }

    private void getFirstPoint() {
        SimpleGps simpleGps = new SimpleGps(mContext);
        Location location = simpleGps.GetInstantLocation();
        if (location != null) {
            dbc.SaveActivityPoint(7, 7, location.getLatitude(), location.getLongitude(), 0);
        } else {
            simpleGps.FetchFineLocation(new SimpleGps.Callback() {
                @Override
                public void doCallback(Location location) {
                    dbc.SaveActivityPoint(7,7,location.getLatitude(),location.getLongitude(),0);
                }
            });
        }
    }


    // ==================================================================================
    //                      local methods
    // ====================================================================================

    /**
     * The save all of our crumbs. A crumb is a a point the user has saved. This is the media
     * and the information that a user has added.
     *
     * @param crumbsWithMedia This is the json object that contains all the crumb data for all the
     *                        crums that we are currently trying to save.
     */
    private void saveCrumbs(JSONObject crumbsWithMedia) {
        crumbsIterator = crumbsWithMedia.keys();

        Log.d(TAG, "Saving Crumbs with these keys: " + crumbsIterator.toString());

        // Grab the first in the iterator, and let the inner class move through the rest. This is crucial because
        // if we iterator through here things will fuck up because we are on a different thread.

            try {
                // Check to ensure that we have data.
                if (!crumbsIterator.hasNext()) {
                    Log.d(TAG, "Attempted to save but we have not crumbs to save :(");
                    showFinishedNotification();
                    return;
                }

                // Grab the key
                String key = crumbsIterator.next().toString();

                // Grab the JSON using the key and create a crumb.
                JSONObject crumbJSON = crumbsWithMedia.getJSONObject(key);
                Crumb crumb = new Crumb(crumbJSON);

                // Start the process.
                saveCrumb(crumb);
            } catch (JSONException e) {
                Log.e(TAG, "Failed to find jsonObejct. Stack trace follows.");
                e.printStackTrace();
            }
    }

    private int saveCrumb(Crumb crumb /*, callback*/) {
        // Create url request
        String url = MessageFormat.format("{0}/rest/login/savecrumb/{1}/{2}/{3}/{4}/{5}/{6}/{7}/{8}/{9}/{10}/{11}/{12}",
                LoadBalancer.RequestServerAddress(),
                " ", // DOnt use description yet
                crumb.GetUserId(),  //
                Integer.toString(mPreferencesAPI.GetServerTrailId()), // Trail Id
                Double.toString(crumb.GetLatitude()), // latitude
                Double.toString(crumb.GetLongitude()), // Longitude
                " ", // icon
                crumb.GetMediaType(), // mime
                crumb.GetPlaceId(), //place id
                crumb.GetSuburb(), // suburb
                crumb.GetCity(), // cioty
                crumb.GetCountry(), // country
                crumb.GetTimestamp()); // timestamp
        url = url.replaceAll(" ", "%20");

        // Index is where we are in the list of metadata
        final int index = crumb.GetIndex();
        final String eventId = crumb.GetEventId();

        // Do save with url
        final String fileName =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/"+ eventId + ".mp4";

        // send request, then do callback
        // Send request to save our crumb. If successfull, it will trigger this method but with
        // data for the next item so that we iterator through the objects, saving each in a linear fashion.
        LocalMediaUpload upload = new LocalMediaUpload(url, crumb);
        upload.execute();

        // By deafault this means success. Not sure this is correct.
        return 0;
    }

    /**
     * @return The metadata for this trail.
     */
    private JSONObject fetchMetadataJSONForOurCurrentTrail() {
        String localTrailString = getLocalTrailId();
        JSONObject metadataJson = dbc.fetchMetadataFromDB(localTrailString, false);
        return metadataJson;
    }

    /**
     * @return the local trail Id as a string.
     */
    private String getLocalTrailId() {
        int localTrailId = mPreferencesAPI.GetLocalTrailId();
        String localTrailString = "";
        if (localTrailId == -1) {
            // SHit
            Log.d(TAG, "Error saving trail - local trail Id was not found. No changes to remote or local databases were made");
        }
        localTrailString = Integer.toString(localTrailId);

        return localTrailString;
    }

    /**
     * Simple inner class to handle the async uploading of a file to the server. We have this in here
     * because we want the network request to run off the main UI thread, but we also want to be able
     * to process the saving of the crumbs individually and in linear order, so that if it fails at
     * any point (e.g overloaded server, network connectivity issues) we know what point in the trail
     * we were up to / had saved.
     *
     * This class was designed to be used by the background service process, so there is no need to worry about
     * finishing activites etc, but we do need to be aware that we are still running on the main thread,
     * so heavy computation should be avoided/moved off the UI thread.
     */
    private class LocalMediaUpload extends AsyncTask<Void, Integer, String> {
        private String url;
        private Crumb crumb;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Pass in the callback and the url to our constructor.
        public LocalMediaUpload(String url, Crumb crumb){
            this.url = url;
            this.crumb = crumb;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // For them big files. Not sure we really need this
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        private String uploadFile() {
            try {

                File sourceFile;
                MediaType MEDIA_TYPE;

                // Build up the file path and mime type based on the media type. We need to know this for sending to the server.
                if (crumb.GetMediaType().equals(".mp4")) {
                    String filePath = com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils.FetchLocalPathToVideoFile(crumb.GetEventId());
                    sourceFile = new File(filePath);
                    MEDIA_TYPE = MediaType.parse("image/mp4");
                } else {
                    String filePath = com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils.FetchLocalPathToImageFile(crumb.GetEventId());
                    sourceFile = new File(filePath);
                    MEDIA_TYPE = MediaType.parse("image/jpg");
                }

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

                    // Update the saved crumb index
                    if(crumb.GetMediaType().endsWith("4")) {
                        saveThumbnail(crumb, response.body().string());
                    }
                    mPreferencesAPI.SetLastSavedMediaCrumbIndex(crumb.GetIndex());
                    if (crumbsIterator.hasNext()) {
                        // go to next and do save
                        String key = crumbsIterator.next();
                        JSONObject JSONcrumb = crumbsWithMedia.getJSONObject(key);
                        Crumb lcoalCrumb = new Crumb(JSONcrumb);
                        saveCrumb(lcoalCrumb);
                    } else {
                        showFinishedNotification();
                        mPreferencesAPI.SetIsUploading(false);
                    }
                    // other wise just exit out.
                return response.body().string();
            } catch (UnknownHostException | UnsupportedEncodingException e) {
                Log.e("IMAGESAVE", "Error: " + e.getLocalizedMessage());
            } catch (Exception e) {
                Log.e("IMAGESAVE", "Other Error: " + e.getLocalizedMessage());
            }
            return null;
        }

//        // We want to call our interface method that we defined where we called this class once done.
//        // THis will allow us to manipulate the data in the way we want once finished.
//        @Override
//        protected void onPostExecute(String jsonResult) {
//            requestListener.onFinished(jsonResult);
//        }
    }

    /**
     * Interface for creating the on finished call back for {@link LocalMediaUpload }.
     */
    private interface RequestListener {
        public void onFinished(String result);
    }

    private void saveThumbnail(Crumb crumb, String crumbDatabaseId) {
        String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/"+crumb.GetEventId() + ".mp4";
        Bitmap bitmap;
        long id = com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils.FetchContentIdFromFilePath(fileName, mContext.getContentResolver());

        ContentResolver crThumb = mContext.getContentResolver();
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inSampleSize = 1;
        bitmap = MediaStore.Video.Thumbnails.getThumbnail(crThumb, id, MediaStore.Video.Thumbnails.MICRO_KIND, options);

        // Save our bitmap here.
        String url = LoadBalancer.RequestServerAddress() + "/rest/Crumb/SaveImageToDatabase/"+crumbDatabaseId+"T";
        UploadFile uploadFile = new UploadFile(url, null, bitmap);
        uploadFile.execute();
    }

    private void showFinishedNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
// Sets an ID for the notification, so it can be updated
        int notifyID = 8;
        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(mContext)
                .setContentTitle("Upload Complete")
                .setContentText("Sucessfully updated your trip.")
                .setSmallIcon(R.drawable.ic_cloud_done_white_24dp);
        mNotificationManager.notify(
                notifyID,
                mNotifyBuilder.build());
    }
}
