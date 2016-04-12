package com.teamunemployment.breadcrumbs.client.Maps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.teamunemployment.breadcrumbs.Framework.JsonHandler;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Trails.MyCurrentTrailManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.MessageFormat;

/**
 * Created by jek40 on 11/04/2016.
 */
public class MapHostBase extends Activity implements
        GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    private JSONObject json;
    private String TAG = "MapViewer";
    private AsyncDataRetrieval clientRequestProxy;
    private boolean requestingImage = false;
    private MyCurrentTrailManager myCurrentTrailManager;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_map);
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        mContext = this;
        setListenersAndLoaders();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Begining onStart");
        // TrailManager is used for drawing trails and shit on the map.
    }

    // This is a method that wraps up all the startup shit done in onStart() and
    private void setListenersAndLoaders() {
        createCurrentTrailManager(mMap);

        // Button listeners, do as they say.
        setToggleSatellite();
        setBackButtonListener();

		/* Grab the trail Id, which is used to load the details of the map and */
        String trailId = PreferenceManager.getDefaultSharedPreferences(mContext).getString("TRAILID", null);
        saveLocalFileMetadataAndLoadMapWithResponse(trailId);

    }

    private void saveLocalFileMetadataAndLoadMapWithResponse(String trailId) {
        JSONObject json = buildJSONObject();
        String url = MessageFormat.format("{0}/rest/TrailManager/SaveMetadataAndReturnIt/{1}/{2}",
                LoadBalancer.RequestServerAddress(),
                URLEncoder.encode(json.toString()),
                trailId);
        url = url.replaceAll(" ", "%20");
        // save our metadata and then do the loading with the metadata that gets returned.
        AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String metadataJSON) throws JSONException {
                // Our metadata gets returned, so now we have to load the map using the data that gets returned.
                Log.d("Result", metadataJSON);
            }
        });
        asyncDataRetrieval.execute();
    }

    private JSONObject buildJSONObject() {
        JSONObject wrapper = new JSONObject();
        try {
            String trailId = PreferenceManager.getDefaultSharedPreferences(mContext).getString("TRAILID", null);
            wrapper.put("TrailId", trailId);
            JSONObject events = new JSONObject();
            JSONObject event1 = new JSONObject();
            event1.put("latitude", "-44.9437402");
            event1.put("longitude", "168.8378104");
            event1.put("timeStamp", "Fri Mar 18 14:20:36 GMT+13:00 2016");
            event1.put("placeId", "ChIJW5vp6H4i1akRfdGerI5H1ls");
            event1.put("type", "2");
            event1.put("eventId", "0");
            event1.put("driving_method", "0");
            event1.put("trailId", trailId);
            event1.put("id", "0");

            JSONObject event2 = new JSONObject();
            event2.put("latitude", "-45.0180531");
            event2.put("longitude", "168.9337654");
            event2.put("timeStamp", "Sat Mar 19 10:32:10 GMT+13:00 2016");
            event2.put("placeId", "ChIJrU9-weUm1akROcYLfMhhlcA");
            event2.put("type", "2");
            event2.put("eventId", "1");
            event2.put("driving_method", "0");
            event2.put("trailId", trailId);
            event2.put("id", "1");

            JSONObject event3 = new JSONObject();
            event3.put("latitude", "-45.0375501");
            event3.put("longitude", "169.1944608");
            event3.put("timeStamp", "Sat Mar 19 17:35:49 GMT+13:00 2016");
            event3.put("placeId", "ChIJk3AwrMbUKqgRMgirxIlaXqY");
            event3.put("type", "2");
            event3.put("eventId", "2");
            event3.put("driving_method", "0");
            event3.put("trailId", trailId);
            event3.put("id", "2");

            JSONObject event4 = new JSONObject();
            event4.put("latitude", "-43.8648759");
            event4.put("longitude", "169.0460804");
            event4.put("timeStamp", "Thu Mar 24 21:02:43 GMT+13:00 2016");
            event4.put("placeId", "ChIJQTOFmNyf1WwRzIQxezCwWFc");
            event4.put("type", "2");
            event4.put("eventId", "3");
            event4.put("driving_method", "0");
            event4.put("trailId", trailId);
            event4.put("id", "3");

            JSONObject event5 = new JSONObject();
            event5.put("latitude", "-43.593703");
            event5.put("longitude", "169.606258");
            event5.put("timeStamp", "Fri Mar 25 22:02:42 GMT+13:00 2016");
            event5.put("placeId", "ChIJQTOFmNyf1WwRzIQxezCwWFc");
            event5.put("type", "2");
            event5.put("eventId", "4");
            event5.put("driving_method", "0");
            event5.put("trailId", trailId);
            event5.put("id", "4");

            events.put("0", event1);
            events.put("1", event2);
            events.put("2", event3);
            events.put("3", event4);
            events.put("4", event5);

            wrapper.put("Events", events);

            return wrapper;


        } catch(JSONException ex) {
            ex.printStackTrace(); // this is just temp shit
        }

        return null;
    }

    private void addViewToTrail(String trailId) {
        clientRequestProxy  = new AsyncDataRetrieval(LoadBalancer.RequestServerAddress() +
                "/rest/TrailManager/AddTrailView/"+trailId,
                new AsyncDataRetrieval.RequestListener() {
                    @Override
                    public void onFinished(String result) {
                        // Dont actually need to do anything with this result, so I just log it.
                        Log.i("MapViewer.ViewUpdate", "Successfully added view to map. Status : " + result);
                    }
                });
        clientRequestProxy.execute();
    }

    private void createCurrentTrailManager(GoogleMap map) {
        myCurrentTrailManager = new MyCurrentTrailManager(map, this);
    }

    private void setTrailClickHandlers(final String userId) {
        CardView authorCard = (CardView) findViewById(R.id.author_view);
        authorCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load the users profile page.
                Intent intent = new Intent();
                intent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.NavMenu.Profile.ProfilePageViewer");
                intent.putExtra("userId", userId);
                //intent.putExtra("name", name);
                startActivity(intent);

            }
        });
    }
    private void getBaseDetailsForATrail(final String trailId) {
        String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetBaseDetailsForATrail/"+trailId;

    }

    private void setToggleSatellite() {
//		ImageButton imageButton = (ImageButton) findViewById(R.id.sattellite_toggle);
//		imageButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (mMap.getMapType() == 1) {
//					mMap.setMapType(2);
//				} else {
//					mMap.setMapType(1);
//				}
//			}
//		});
    }


    private void setBackButtonListener() {
        ImageView backButton = (ImageView) findViewById(R.id.backButtonCapture);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close this activity, which will exit the screen
                finish();
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Popup window with image
        setContentView(R.layout.crumb_activity);

        //requestingImage = true;
        return true;
    }


    @Override
    public void onMapClick(LatLng point) {
        // TODO Auto-generated method stub
        SlidingUpPanelLayout slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    @Override
    public void onMapLongClick(LatLng point) {
        // TODO Auto-generated method stub

    }

   

}
