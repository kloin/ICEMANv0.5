package com.teamunemployment.breadcrumbs.Trails;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncPost;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.client.Maps.MapDisplayManager;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.database.DatabaseController;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
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
    private GoogleApiClient locationclient;
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
            int length = trailJSON.length();
            Iterator<String> nodeKeys = trailJSON.keys();
            // The first two values
            int backindex = 0;
            int frontindex = 1;
            String backNode = "0";
            String frontNode = "1";
            /*while (backindex < length) {
                String key = nodeKeys.next();
                // Get node 0
                // Get node 1
                // increase counters as we go
                String base = "Node" + Integer.toString(backindex);
                JSONObject pointNodeBase = trailJSON.getJSONObject(base);
                // Get the next point in the trail.
                String next = "Node"+pointNodeBase.getString("next");
                // Draw base point on the map
                // Get the node we are drawing to
                JSONObject pointNodeHead = new JSONObject(trailJSON.getString(next));

                // Get the variable for base
                Double baseLatitude = pointNodeBase.getDouble("latitude");
                Double baseLongitude =  pointNodeBase.getDouble("longitude");

                // Get the variables for the head.
                Double headLatitude = pointNodeHead.getDouble("latitude");
                Double headLongitude = pointNodeHead.getDouble("longitude");

                // Draw line from base to head
                Bitmap bm = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.pinksquare);

                bm = getCircleBitmap(bm);
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(headLatitude, headLongitude))
                        .icon(BitmapDescriptorFactory.fromBitmap(bm)));

                map.addPolyline(new PolylineOptions().add(new LatLng(baseLatitude, baseLongitude),
                        new LatLng(headLatitude, headLongitude)).width(10).color(Color.parseColor("#808080"))).setZIndex(1);

                // move to next pointers. This will throw an exception on the last one but it will be
                // caught. Not too much we can do.
                backindex += 1;
                //frontNode = pointNodeHead.getString("next");
            }*/

            ArrayList<LatLng> list = new ArrayList<>();
            int counter = 0;
            while (counter < length) {

                String key = "Node" + Integer.toString(counter);
                if (trailJSON.has(key)) {
                    JSONObject pointNodeBase = trailJSON.getJSONObject(key);
                    Double baseLatitude = pointNodeBase.getDouble("latitude");
                    Double baseLongitude = pointNodeBase.getDouble("longitude");
                    list.add(new LatLng(baseLatitude, baseLongitude));
                    counter += 1;
                } else {
                    counter += 2;
                }
            }
            PolylineOptions options = new PolylineOptions().width(15).color(Color.BLUE).geodesic(true);
            for (int z = 0; z < list.size(); z++) {
                LatLng point = list.get(z);
                options.add(point);
            }
            map.addPolyline(options);

            //CameraPosition cameraPosition;
            //cameraPosition = new CameraPosition.Builder().target(new LatLng(41.020811, 29.046113)).zoom(15).build();

            //map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } catch (JSONException e) {
            Log.e("TRAIL", "Failed to draw trail on the map - Likey an issue finding the correct json Point");
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
                    CameraUpdate zoom=CameraUpdateFactory.zoomTo(12);
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
            //locationclient.removeLocationUpdates(mPendingIntent);
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

    /*
    // create bitmap from resource
 Bitmap bm = BitmapFactory.decodeResource(getResources(),
  R.drawable.simple_image);

 // set circle bitmap
 ImageView mImage = (ImageView) findViewById(R.id.image);
 mImage.setImageBitmap(getCircleBitmap(bm));
     */
    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    // This is the method that extracts all our saved data about the server and saves it to the server.
    public void SaveEntireTrail(String trailId) {
        DatabaseController dbc = new DatabaseController(mapContext);

        // Fetch metadata
        JSONObject metadataJson = dbc.fetchMetadataFromDB(trailId);
        // fetch crumb data
        JSONObject crumbsWithoutMedia = dbc.getCrumbsWithoutMedia(trailId);
        // fetch weather


        // save the crumbs one by one. Their id matters for loading the crumb, so save them with that AND
        // the metadata id, so i can link the tables together if need be
    }

}
