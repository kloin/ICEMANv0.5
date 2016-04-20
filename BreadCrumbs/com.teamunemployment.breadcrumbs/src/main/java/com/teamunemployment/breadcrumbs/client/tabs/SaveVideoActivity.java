package com.teamunemployment.breadcrumbs.client.tabs;

import android.app.ActionBar;
import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.teamunemployment.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncUploadVideo;
import com.teamunemployment.breadcrumbs.Trails.MyCurrentTrailDisplayManager;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.R;

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
public class SaveVideoActivity  extends Activity {

    private static final float ROTATE_FROM = 0.0f;
    private static final float ROTATE_TO = 360.0f;// 3.141592654f * 32.0f;
    private Activity context;
    private GlobalContainer globalContainer;
    private MyCurrentTrailDisplayManager currentTrailManager;
    private HashMap<String, String> trailAndIdMap;
    private AsyncDataRetrieval asyncDataRetrieval;
    private String SavedCrumbId;
    private Spinner s;
    private JSONObject editableTrails;
    private String filePath;
    private BreadCrumbsFusedLocationProvider locationProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_video);
        Bundle extras = getIntent().getExtras();
        context = this;
        globalContainer = GlobalContainer.GetContainerInstance();
        addTapListeners();
        // Hide the action bar as per.
        ActionBar actionBar = getActionBar();
        //actionBar.hide();
        filePath = extras.getString("videoUrl");
        setMedia(filePath); // Set the video.
        currentTrailManager = MyCurrentTrailDisplayManager.GetCurrentTrailManagerInstance();
        VideoView content = (VideoView) findViewById(R.id.video);
        ViewGroup.LayoutParams layoutParams = content.getLayoutParams();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        layoutParams.height = displaymetrics.widthPixels;
        content.setLayoutParams(layoutParams);
        locationProvider = new BreadCrumbsFusedLocationProvider(this);

        //setBackButtonListener();
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
//        final ImageButton cancelSaveButton = (ImageButton) findViewById(R.id.cancelButton);
//        cancelSaveButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Exit this page
//                finish();
//            }
//        });
        final FloatingActionButton newTrailButton = (FloatingActionButton) findViewById(R.id.done_button);
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
        String descriptionString = " ";
        EditText descriptionEntry = (EditText) findViewById(R.id.crumb_description);
        Editable editable = descriptionEntry.getText();
        if ( editable != null) {
            descriptionString = editable.toString();
        }

        // Need to change this
        final String TrailId = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("TRAILID", "-1");
        if (TrailId.equals("-1")) {
            Toast.makeText(this, "No current active trail. Create a trail from the main menu.", Toast.LENGTH_LONG).show();
            return;
        }

        Location location = locationProvider.GetLastKnownLocation();
        if (location == null) {
            // Need to display and error to the user
            Toast.makeText(this, "Failed to find location. Please ensure you have location services enabled", Toast.LENGTH_SHORT).show();
            return;
        }

        double lat = location.getLatitude();
        double longit = location.getLongitude();
        String suburb = "";
        String city = "";
        String country = "";
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        try {
            final List<Address> addresses = gcd.getFromLocation(lat, longit, 1);
            addresses.get(0);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                suburb = address.getSubLocality();
                city = address.getLocality();
                country = address.getCountryName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        final String latitude = String.valueOf(lat);
        final String longitude = String.valueOf(longit);

        String Icon = "Shitty styff";
        final String UserId = globalContainer.GetUserId();

        Calendar calendar = Calendar.getInstance();
        final String timeStamp = calendar.getTime().toString();
        final String finalSuburb = suburb;
        final String finalCity = city;
        final String finalCountry = country;
        // Just think of this shitty code as a javascript promise
        final String finalDescriptionString = descriptionString;
        locationProvider.GetCurrentPlace(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                String placeId = "";
                PlaceLikelihood placeLikelihood = likelyPlaces.get(0);
                if (placeLikelihood != null ) {
                    Place place = placeLikelihood.getPlace();
                    if (place!=null) {
                        placeId = place.getId();
                    }
                }
		/*		for (PlaceLikelihood placeLikelihood : likelyPlaces) {
					Log.i("TEST", String.format("Place '%s' has likelihood: %g",
							placeLikelihood.getPlace().getName(),
							placeLikelihood.getLikelihood()));
							id = placeLikelihood.getPlace().getId();

				}*/
                likelyPlaces.release();
                // We need to wait
                createNewCrumb(finalDescriptionString, UserId, TrailId, latitude, longitude,  "icon", ".mp4", placeId, finalSuburb, finalCity, finalCountry, timeStamp);
            }
        });
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
        }, context);
        asyncDataRetrieval.execute();
    }

    private void loadTrailsIntoHashMap(JSONObject trailsToLoad) {
        // Get all trails Owned/part_of for our user.

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
//        ArrayList<String> array = new ArrayList<String>();
//        Collection baseList = trailAndIdMap.keySet();
//        Iterator<String> listIterator = baseList.iterator();
//        while (listIterator.hasNext()) {
//            array.add(listIterator.next());
//        }

//        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array); // selected item will look like a spinner set from XML
//        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        s.setAdapter(spinnerArrayAdapter);
    }

    private void createNewCrumb(String Chat, String UserId,	String TrailId,	String Latitude, String Longitude, String Icon, String Extension, String placeId, String suburb, String city, String country, String timeStamp) {
        String url = MessageFormat.format("{0}/rest/login/savecrumb/{1}/{2}/{3}/{4}/{5}/{6}/{7}/{8}/{9}/{10}/{11}/{12}",
                LoadBalancer.RequestServerAddress(),
                Chat,
                UserId,
                TrailId,
                Latitude,
                Longitude,
                Icon,
                Extension,
                placeId,
                suburb,
                city,
                country,
                timeStamp);

        url = url.replaceAll(" ", "%20");

        asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {

            /*
             * Override for the
             */
            @Override
            public void onFinished(String result) {

                SavedCrumbId = result;
                saveVideo();
            }
        }, context);

        asyncDataRetrieval.execute();
        System.out.println("Sending save request to : " + url);
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
