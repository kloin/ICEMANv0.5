package com.breadcrumbs.client.tabs;

import android.app.ActionBar;
import android.app.Activity;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.breadcrumbs.Location.CanvasLocationManager;
import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.ServiceProxy.AsyncDataRetrieval;
import com.breadcrumbs.ServiceProxy.AsyncUploadVideo;
import com.breadcrumbs.Trails.MyCurrentTrailManager;
import com.breadcrumbs.caching.GlobalContainer;
import com.breadcrumbs.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by aDirtyCanvas on 5/30/2015.
 */
public class SaveVideoActivity  extends Activity {

    private static final float ROTATE_FROM = 0.0f;
    private static final float ROTATE_TO = 360.0f;// 3.141592654f * 32.0f;

    private GlobalContainer globalContainer;
    private MyCurrentTrailManager currentTrailManager;
    private HashMap<String, String> trailAndIdMap;
    private AsyncDataRetrieval asyncDataRetrieval;
    private String SavedCrumbId;
    private Spinner s;
    private JSONObject editableTrails;
    private String filePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_video);
        Bundle extras = getIntent().getExtras();

        globalContainer = GlobalContainer.GetContainerInstance();
        addTapListeners();
        // Hide the action bar as per.
        ActionBar actionBar = getActionBar();
        actionBar.hide();
        filePath = extras.getString("videoUrl");
        setMedia(filePath); // Set the video.
        currentTrailManager = MyCurrentTrailManager.GetCurrentTrailManagerInstance(); // use with care ....
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
        final ImageButton cancelSaveButton = (ImageButton) findViewById(R.id.cancelButton);
        cancelSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Exit this page
                finish();
            }
        });
        final ImageButton newTrailButton = (ImageButton) findViewById(R.id.doneButton);
        newTrailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewEvent();
                RotateAnimation r; // = new RotateAnimation(ROTATE_FROM, ROTATE_TO);
                r = new RotateAnimation(ROTATE_FROM, ROTATE_TO, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                r.setDuration((long) 2*1500);
                r.setRepeatCount(100);
                newTrailButton.startAnimation(r);
            }
        });
    }

    // Set the video to the video player view.
    private void setMedia(String videoPath) {
        getAllOwnedOrPartOfTrailsForAUser();
        VideoView videoView = (VideoView)findViewById(R.id.video);
        videoView.setVideoPath(videoPath);
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
    }

    // Create new crumb to save the video to.
    private void createNewEvent() {
        //Currently we will only be creating a crumb. In the future we will be able to create both.
        //TextView eventName = (TextView) findViewById(R.id.CrumbName);
        String TrailId = trailAndIdMap.get(s.getSelectedItem()); // Need to pass in the trailId
        Location location = CanvasLocationManager.getLastCheckedLocation();
        double lat = location.getLatitude();
        double longit = location.getLongitude();
        String latitude = String.valueOf(lat);
        String longitude = String.valueOf(longit);
        String Icon = "Shitty styff";
        String UserId = globalContainer.GetUserId();
        createNewCrumb("tete", UserId, TrailId, latitude, longitude,  "icon", ".mp4");
    }

    private void getAllOwnedOrPartOfTrailsForAUser() {
        // Get all trails with a url request, convert the result to json
        String userId = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("USERID", "-1");
        String url = MessageFormat.format("{0}/rest/User/GetAllEditibleTrailsForAUser/{1}",
                LoadBalancer.RequestServerAddress(),
                userId);
        asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                //Trail Created
                try {
                    editableTrails = new JSONObject(result);
                    loadTrailsIntoHashMap(editableTrails);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    //No trail Data - this is an issue - should be catching this earlier.
                }
            }
        });
        asyncDataRetrieval.execute();
    }

    private void loadTrailsIntoHashMap(JSONObject trailsToLoad) {
        // Get all trails Owned/part_of for our user.
        s = (Spinner) findViewById(R.id.spinner);
        //Get all trails
        //Add them to hashMap of string/trail
        trailAndIdMap = new HashMap<String, String>();
        Iterator iterator = trailsToLoad.keys();
        while (iterator.hasNext()) {
            JSONObject trail;
            try {
                trail = trailsToLoad.getJSONObject(iterator.next().toString());
                String title = trail.getString("TrailName");
                String id = trail.getString("Id");
                trailAndIdMap.put(title, id);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // Load the trails from the hashSet into the spinner adapter.
        ArrayList<String> array = new ArrayList<String>();
        Collection baseList = trailAndIdMap.keySet();
        Iterator<String> listIterator = baseList.iterator();
        while (listIterator.hasNext()) {
            array.add(listIterator.next());
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array); // selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(spinnerArrayAdapter);
    }

    private void createNewCrumb(String Chat, String UserId,	String TrailId,	String Latitude, String Longitude, String Icon, String Extension) {

        String url = MessageFormat.format("{0}/rest/login/savecrumb/{1}/{2}/{3}/{4}/{5}/{6}/{7}",
                LoadBalancer.RequestServerAddress(),
                Chat,
                UserId,
                TrailId,
                Latitude,
                Longitude,
                Icon,
                Extension);

        url = url.replaceAll(" ", "%20");
        asyncDataRetrieval  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {

            /*
             * Override for the
             */
            @Override
            public void onFinished(String result) {

                SavedCrumbId = result;
                saveVideo();
            }
        });

        asyncDataRetrieval.execute();
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
}
