package com.breadcrumbs.Trails;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.breadcrumbs.Location.LocationService;
import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.ServiceProxy.AsyncDataRetrieval;
import com.breadcrumbs.ServiceProxy.AsyncPost;
import com.breadcrumbs.ServiceProxy.HTTPRequestHandler;
import com.breadcrumbs.caching.GlobalContainer;
import com.breadcrumbs.client.Maps.DisplayCrumb;
import com.breadcrumbs.client.Maps.MapDisplayManager;
import com.breadcrumbs.client.Maps.TrailDrawer;
import com.breadcrumbs.client.R;
import com.breadcrumbs.client.tabs.SubscriptionManagerTab;
import com.breadcrumbs.database.DatabaseController;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.ClusterManager;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Iterator;

/**
 * Created by aDirtyCanvas on 5/20/2015.
 * Created to manage and display a users current class.
 */
public class MyCurrentTrailManager extends Activity {
    private GoogleMap map;
    private AsyncDataRetrieval clientRequestProxy;
    private MyCurrentTrailManager mapContext;
    private Activity context;
    // Location shit.
    private Location lastCheckedLocation;
    private LocationClient locationclient;
    private LocationRequest locationrequest;
    private Intent mIntentService;
    private PendingIntent mPendingIntent;
    private static MyCurrentTrailManager currentTrailManager;
    private MapDisplayManager mapDisplayManager;
    private float currentZoom = -1;
    /*
    Display a single trail and its crumbs
    */
    public MyCurrentTrailManager(GoogleMap map , Activity context) {
        this.map = map;
        this.context = context;
        mapContext=this;
        currentTrailManager = this;


    }
    // Use with caution
    public MyCurrentTrailManager(Activity context) {
        this.context = context;
        mapContext = this;
        currentTrailManager = this;
    }

    private void setOnMapCameraChangedListener() {
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {



            @Override
            public void onCameraChange(CameraPosition position) {
                if (position.zoom != currentZoom){
                    currentZoom = position.zoom;  // here you get zoom level
                    redraw();
                }
            }
        });
    }

    /*
    This should probably be a singleton

    A static intsance for classes that want to get a hold of this instance but have no map etc.. May
    want to change this at a later date tho. - seperate the drawing and the recording of shit.

     */
    public static MyCurrentTrailManager GetCurrentTrailManagerInstance() {
        if (currentTrailManager == null) {
            return null;
        }
        return currentTrailManager;
    }

    // Redraw the map
    public void redraw() {
        DisplayTrailOnMap(GlobalContainer.GetContainerInstance().GetTrailsJSON().toString());
    }

    public void GetAndDisplayTrailOnMap(String trailId) {
        setOnMapCameraChangedListener();
        // First construct our url that we want:
        mapDisplayManager = new MapDisplayManager(map, context, trailId);
        String fetchTrailsUrl = MessageFormat.format("{0}/rest/TrailManager/GetAllTrailPoints/{1}",
                LoadBalancer.RequestServerAddress(),
                trailId);

        // Get trailPoints
        clientRequestProxy  = new AsyncDataRetrieval(fetchTrailsUrl, new AsyncDataRetrieval.RequestListener() {
            /*
             * Override for the
             */
            @Override
            public void onFinished(String result) {
                GlobalContainer.GetContainerInstance().SetTrailsJSON(result);
                DisplayTrailOnMap(result);
            }
        });
        clientRequestProxy.execute();
       /* HTTPRequestHandler requestHandler = new HTTPRequestHandler();
        String trailPointsJSONString = requestHandler.SendSimpleHttpRequestAndReturnString(fetchTrailsUrl);*/

        // Draw trailPoints onto the map
    }

    public void DisplayTrailOnMap(String trailsJSONString) {
        JSONObject trailJSON = null;
        try {
            // Construct our jsonObject for saving
            trailJSON = new JSONObject(trailsJSONString);
            Iterator<String> nodeKeys = trailJSON.keys();
            // The first two values
            String backNode = "0";
            String frontNode = "1";
            while (nodeKeys.hasNext()) {
                // Get node
                JSONObject pointNodeBase = trailJSON.getJSONObject("Node"+backNode);
                // Draw base point on the map
                // Get the node we are drawing to
                JSONObject pointNodeHead = new JSONObject(trailJSON.getString("Node"+frontNode));
                // Get the variable for base
                Double baseLatitude = pointNodeBase.getDouble("latitude");
                Double baseLongitude =  pointNodeBase.getDouble("longitude");
                // Get the variables for the head.
                Double headLatitude = pointNodeHead.getDouble("latitude");
                Double headLongitude = pointNodeHead.getDouble("longitude");
                LatLng latLng = new LatLng(headLatitude, headLongitude);
                TrailDrawer trailDrawer = new TrailDrawer(map, context);
                trailDrawer.DrawPointOnMap(latLng);
                // Draw line from base to head
                map.addCircle(new CircleOptions()
                        .center(new LatLng(headLatitude, headLongitude))
                        .radius(20)
                        .strokeColor(Color.parseColor("#9C27B0"))
                        .fillColor(Color.parseColor("#9C27B0"))).setZIndex(2);

                map.addPolyline(new PolylineOptions().add(new LatLng(baseLatitude, baseLongitude),
                        new LatLng(headLatitude, headLongitude)).width(10).color(Color.parseColor("#33B5E5"))).setZIndex(1);

                // move to next pointers. This will throw an exception on the last one but it will be
                // caught. Not too much we can do.
                backNode = pointNodeHead.getString("pointId");
                frontNode = pointNodeHead.getString("next");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /*
     Take a trail Id and use this to find the crumbs and trailPoints.
     */
    public void DisplayTrailAndCrumbs(String trailId) {
        // Draw the actual trail.
        GetAndDisplayTrailOnMap(trailId);
        String url = MessageFormat.format("{0}/rest/login/getAllCrumbsForATrail/{1}",
                LoadBalancer.RequestServerAddress(),
                trailId);
        //setMapListener();
        url = url.replaceAll(" ", "%20");

        // This creates the async request with a callback method of what I want completed when the
        // request is finished.
        clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                //Initialise our object, and attempt to construct it from the string.
                JSONObject returnedCrumbs = null;
                try {
                    returnedCrumbs = new JSONObject(result);
                    // Get the crumb title ??? why do i do this? last crumb?
                    JSONArray crumbListJSON = new JSONArray(returnedCrumbs.getString("Title"));
                    JSONObject next = null;
                    for (int index=0; index<crumbListJSON.length(); index += 1 ) {
                        // The next node to get data from and Draw.
                         next =  crumbListJSON.getJSONObject(index);
                        mapDisplayManager.DrawCrumbFromJson(next);
                    }
                    // Now that we are done, we want to set the focus to the last crumb added
                    Double Latitude = next.getDouble("Latitude");
                    Double Longitude = next.getDouble("Longitude");
                    CameraUpdate center=
                            CameraUpdateFactory.newLatLng(new LatLng(Latitude,
                                    Longitude));
                    CameraUpdate zoom=CameraUpdateFactory.zoomTo(10);
                    // Move/animate camera to location

                    map.moveCamera(center);
                    map.animateCamera(zoom);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                   Log.e("BC.MAP", "A JsonException was thrown during the display process for a crumb." +
                           "This probably means that you are missing a field on your json. Stacktrace follows");
                    e.printStackTrace();
                }
            }
        });

        clientRequestProxy.execute();
        Log.i("MAP", "Finished Loading crumbs and displaying them on the map");
    }


    /*
    Method that begins the background tracking of a users trail.

    @Param Context - I need the context to begin tracking, so I get the context
                from wherever is asking for it
     */
    public boolean CreateTrailAndBeginTracking(String trailTitle) {
        // I am doing this after rather than before because if a trail fails, and I want to retry in
        // the background.
        SendCreateTrailRequest(trailTitle);
        // Start listening
        //locationrequest = LocationRequest.create();
        //locationrequest.setInterval(10000);
       // locationclient.requestLocationUpdates(locationrequest, mPendingIntent);
        return true;
    }

    public boolean SendCreateTrailRequest(String trailTitle) {
        // Get userId from shared preferences.
        String userId = PreferenceManager.getDefaultSharedPreferences(context).getString("USERID", "-1");
        //store variable to sharedPreferences (and db for backup? It is pretty vital that I dont lose this trail)
        // I love coding high. This source code is like a diary.
        String url = MessageFormat.format(LoadBalancer.RequestServerAddress()+ "/rest/login/saveTrail/{0}/{1}/{2}",trailTitle, "test", userId);
        HTTPRequestHandler saver = new HTTPRequestHandler();
        saver.SendSimpleHttpRequestAndSavePreference(url, context);
        // Save to the database.

        return true;
    }
    /*
    Needs to be moved out at sometime
     */
    public boolean StopTracking() {
        if (locationclient == null) {
            return true;
        }
        if (locationclient.isConnected()) {
            locationclient.removeLocationUpdates(mPendingIntent);
            locationclient.disconnect();
        }
        //No fetch data and send it to server.
        fetchTrailPointDataAndSaveItToTheServer();
        return true;
    }

    private void fetchTrailPointDataAndSaveItToTheServer() {
        final String trailPointsId;
        // HACZ - cos im just testing.
        final DatabaseController dbc = new DatabaseController(context);
        // But how will I actually know this?
        final String trailId = PreferenceManager.getDefaultSharedPreferences(context).getString("TRAILID", "-1");
        // Dont want to be saving a non existent trail - I will do other shit with it first
        if (!trailId.equals("-1")) {
            JSONObject jsonObject = dbc.getAllSavedTrailPoints(trailId);
            HTTPRequestHandler saver = new HTTPRequestHandler();

            // Save our trails.
            String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/SaveTrailPoints/";
            AsyncPost post = new AsyncPost(url, new AsyncPost.RequestListener() {
                @Override
                public void onFinished(String result) {
                    dbc.DeleteAllSavedTrailPoints(trailId);
                }
            }, jsonObject);

            post.execute();
        }
    }
}
