package com.teamunemployment.breadcrumbs.client.tabs;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.teamunemployment.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.teamunemployment.breadcrumbs.Location.PlaceManager;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncUploadVideo;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.Trails.MyCurrentTrailDisplayManager;
import com.teamunemployment.breadcrumbs.Trails.TrailManagerWorker;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by aDirtyCanvas on 5/30/2015.
 */
public class SaveVideoActivity  extends Activity implements TextureView.SurfaceTextureListener {

    private static final float ROTATE_FROM = 0.0f;
    private static final float ROTATE_TO = 360.0f;// 3.141592654f * 32.0f;
    private Activity context;
    private GlobalContainer globalContainer;
    private MyCurrentTrailDisplayManager currentTrailManager;
    private HashMap<String, String> trailAndIdMap;
    private AsyncDataRetrieval asyncDataRetrieval;
    private String SavedCrumbId;
    private JSONObject editableTrails;
    private String filePath;
    private BreadCrumbsFusedLocationProvider locationProvider;

    // Media player shit
    private Surface mSurface;
    private TextureView mTextureView;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        filePath = extras.getString("videoUrl");
        setContentView(R.layout.save_video);

        context = this;
        globalContainer = GlobalContainer.GetContainerInstance();
        addTapListeners();

        currentTrailManager = MyCurrentTrailDisplayManager.GetCurrentTrailManagerInstance();
        locationProvider = new BreadCrumbsFusedLocationProvider(this);
        mTextureView = (TextureView) findViewById(R.id.video);
        mTextureView.setSurfaceTextureListener(this);

    }

    // Add listeners for saving, cancelling etc..
    private void addTapListeners() {
        //adding listeners
        ImageButton backButton = (ImageButton) findViewById(R.id.backAddScreen);
        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();

            }
        });
        final TextView newTrailButton = (TextView) findViewById(R.id.save_video);
        newTrailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // need to have  saving indicator like a loading indicator around this button.
                // doShit()
                // No i need to send the save network request.
                createNewEvent();
                finish();
            }
        });
    }

    // Create new crumb to save the video to.
    private void createNewEvent() {
        // Need to change this
        final String TrailId = Integer.toString(PreferencesAPI.GetInstance(context).GetLocalTrailId());
        if (TrailId.equals("-1")) {
            Toast.makeText(this, "No current active trail. Create a trail from the main menu.", Toast.LENGTH_LONG).show();
            return;
        }

        final Location location = locationProvider.GetLastKnownLocation();
        if (location == null) {
            // Need to display and error to the user
            Toast.makeText(this, "Failed to find location. Please ensure you have location services enabled", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch Time
        Calendar calendar = Calendar.getInstance();
        final String timeStamp = calendar.getTime().toString();

        // Get the address
        Address address = PlaceManager.GetPlace(context, location.getLatitude(), location.getLongitude());

        // Parameters that we need to save to for a crumb.
        final String suburb =  PlaceManager.GetSuburb(address);
        final String finalCity =  PlaceManager.GetCity(address);
        final String finalCountry = PlaceManager.GetCountry(address);
        locationProvider.GetCurrentPlace(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                String placeId = "";
                PlaceLikelihood placeLikelihood = likelyPlaces.get(0);
                if (placeLikelihood != null) {
                    Place place = placeLikelihood.getPlace();
                    if (place != null) {
                        placeId = place.getId();
                    }
                }
                likelyPlaces.release();
                String userId = PreferencesAPI.GetInstance(context).GetUserId();

                // We need to wait
                String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                int eventId = PreferencesAPI.GetInstance(context).GetEventId();
                fileName += "/" + eventId + ".mp4";
                createNewCrumb(" ", userId, TrailId, location, "icon", ".mp4", placeId, suburb, finalCity, finalCountry, timeStamp, fileName);
            }
        });
    }
    private void createNewCrumb(String Chat, String UserId,	String TrailId,	Location location, String Icon, String Extension, String placeId, String suburb, String city, String country, String timeStamp, String fileName) {
        DatabaseController dbc = new DatabaseController(context);
        int eventId = PreferenceManager.getDefaultSharedPreferences(context).getInt("EVENTID", -1);
        if (eventId == -1) {
            eventId = 0;
        }

//        eventId += 1;
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        preferences.edit().putInt("EVENTID", eventId).commit();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userId = preferences.getString("USERID", null);

        if (userId == null) {
            // Crash the app here. I am doing this because this should never happen in production - if it does happen I want to catch it as soon as possiible
            throw new NullPointerException("User id was null.");
        }
        // Need to set our trailId as the local one.
        String localTrailId = Integer.toString(PreferencesAPI.GetInstance(context).GetLocalTrailId());

        // save our crumb to the db. It will be saved to the server when we publish
        dbc.SaveCrumb(localTrailId," ", userId, eventId, location.getLatitude(), location.getLongitude(), ".mp4", timeStamp, "", placeId, suburb, city, country);
        TrailManagerWorker trailManagerWorker = new TrailManagerWorker(context);
        trailManagerWorker.CreateEventMetadata(TrailManagerWorker.CRUMB, location);
    }

    private void saveVideo() {
        String url = MessageFormat.format("{0}/rest/login/saveCrumbWithVideo/{1}",
                LoadBalancer.RequestServerAddress(),
                SavedCrumbId);

        url = url.replaceAll(" ", "%20");
        AsyncUploadVideo uploadVideo = new AsyncUploadVideo(url, filePath, new AsyncUploadVideo.RequestListener() {

            /*
             * Override for the
             */
            @Override
            public void onFinished(String result) {
                finish();
            }
        });
        uploadVideo.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = new Surface(surface);
        buildMediaPlayer(filePath);
    }

    private void buildMediaPlayer(String videoPath) {
        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(videoPath);
            mediaPlayer.setSurface(mSurface);
            mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            mediaPlayer.prepare();
            MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer = mp;
                    mMediaPlayer.start();
                    mMediaPlayer.setLooping(true);
                }
            };
            mediaPlayer.setOnPreparedListener(onPreparedListener);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mSurface.release();
        Log.d("SURFACE", "Suface destroyed");
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
