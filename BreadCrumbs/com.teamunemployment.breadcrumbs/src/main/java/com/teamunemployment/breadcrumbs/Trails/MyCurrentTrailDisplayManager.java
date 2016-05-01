package com.teamunemployment.breadcrumbs.Trails;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncPost;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.client.Maps.MapDisplayManager;
import com.teamunemployment.breadcrumbs.client.StoryBoard.StoryBoardItemData;
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

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by aDirtyCanvas on 5/20/2015.
 * Created to manage and display a users current class.
 */
public class MyCurrentTrailDisplayManager {
    private GoogleMap map;
    private AsyncDataRetrieval clientRequestProxy;
    private MyCurrentTrailDisplayManager mapContext;
    private Activity context;
    private LinkedList<String> linkedList = new LinkedList<>();
    // Location shit.
    private Location lastCheckedLocation;
    private GoogleApiClient locationclient;
    private LocationRequest locationrequest;
    private Intent mIntentService;
    private PendingIntent mPendingIntent;
    private static MyCurrentTrailDisplayManager currentTrailManager;
    private MapDisplayManager mapDisplayManager;
    private float currentZoom = -1;
    private boolean alreadyFocused = false;
    private int mDayOfYear = 0;
    private ArrayList<StoryBoardItemData> mStoryBoardItems;
    /*
    Display a single trail and its crumbs
    */
    public MyCurrentTrailDisplayManager(GoogleMap map, Activity context) {
        this.map = map;
        this.context = context;
        mapContext=this;
        currentTrailManager = this;
        mapDisplayManager = new MapDisplayManager(map, context,Integer.toString(PreferencesAPI.GetInstance(context).GetLocalTrailId()));

        linkedList.add(0, "#2196F3");
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setCompassEnabled(false);
        uiSettings.setMapToolbarEnabled(false);
    }

    // Use with caution
    public MyCurrentTrailDisplayManager(Activity context) {
        this.context = context;
        mapContext = this;
        currentTrailManager = this;
    }

    private void setOnMapCameraChangedListener() {
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                if (position.zoom != currentZoom) {
                    currentZoom = position.zoom;  // here you get zoom level
                    //redraw();
                }
            }
        });
    }

    /*
    This should probably be a singleton

    A static intsance for classes that want to get a hold of this instance but have no map etc.. May
    want to change this at a later date tho. - seperate the drawing and the recording of shit.

     */
    public static MyCurrentTrailDisplayManager GetCurrentTrailManagerInstance() {
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

        // fetch metadata first
        final String fetchMetadataUrl = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/FetchMetadata/" + trailId;
        clientRequestProxy = new AsyncDataRetrieval(fetchMetadataUrl, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) throws JSONException {
                JSONObject jsonObject = new JSONObject(result);
                displayMetadata(jsonObject);
            }
        }, context);
        clientRequestProxy.execute();
    }

    public void displayMetadata(JSONObject metadata) throws JSONException {
        // Get an iterator of all the event nodes.
        Iterator<String> keys = metadata.keys();
        LatLng endOfLastPolyline = null;
        int index = 0;
        while (keys.hasNext()) {
            String nodeString = metadata.getString(Integer.toString(index));
            JSONObject node = new JSONObject(nodeString);
           // testIfNeedToDrawDay(nodeString);
            // This is to stop the bug where the different lines dont connect up.
            endOfLastPolyline = drawOnMap(node, endOfLastPolyline);
            drawNodeOnMap(node);
            index += 1;
            keys.next();
        }
        // Get fisrt object and display it when we are ready to animate over the photos.
    }

    // Draw a day label on the map.
    private void testIfNeedToDrawDay(String node) {
        try {
            JSONObject jsonObject = new JSONObject(node);
            String timestamp = jsonObject.getString("TimeStamp");
            DateTime dateTime = new DateTime(timestamp);
            if (dateTime.getDayOfYear() > mDayOfYear) {
                double latitude = Double.parseDouble(jsonObject.getString("Latitude"));
                double longitude = Double.parseDouble(jsonObject.getString("Longitude"));
                IconGenerator iconFactory = new IconGenerator(context);
                iconFactory.setColor(Color.CYAN);
                addIcon(iconFactory, "Custom color", new LatLng(latitude, longitude));
                mDayOfYear = dateTime.getDayOfYear();
            }
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }

    private void addIcon(IconGenerator iconFactory, CharSequence text, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("Day 1"))).
                position(position).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        map.addMarker(markerOptions);
    }

    private void setMapFocus(LatLng lastPoint) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPoint, 10.0f));
    }

    private void drawNodeOnMap(JSONObject node) throws JSONException {
        String type = node.getString("EventType");
        int eventType = Integer.parseInt(type);
        IconGenerator iconFactory = new IconGenerator(context);
        iconFactory.setColor(Color.CYAN);
        // If we have a rest zone we want to draw a little circle on the map.
        if (eventType == TrailManagerWorker.REST_ZONE) {
            String latitude = node.getString("Latitude");
            String longitude = node.getString("Longitude");

            LatLng location = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            map.addMarker(new MarkerOptions()
                    .position(location)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.player_sprite)));

        }
        else if (eventType == TrailManagerWorker.CRUMB) {
            // Draw crumb type on the map.

        }
    }


    private LatLng drawOnMap(JSONObject node, LatLng endOfLastLine) throws JSONException {
        List<LatLng> listOfPoints;
        if (node.has("PolylineString")) {
            String polyLineString = node.getString("PolylineString");
            if (node.getString("PolylineIsEncoded").equals("0")) {
                listOfPoints = PolyUtil.decode(polyLineString);
            } else {
                listOfPoints = parseNonEncodedPolyline(polyLineString);
                drawDashedPoly(listOfPoints);
                return listOfPoints.get(0);
            }
            if (endOfLastLine != null) {
               // listOfPoints.add(endOfLastLine);
            }
            DrawPolyline(listOfPoints);
            return listOfPoints.get(0);
        }

        return null;
    }

    private void drawDashedPoly(List<LatLng> list) {
        DrawPolyline(list);
    }

    private List<LatLng> parseNonEncodedPolyline(String polylineString) {
        ArrayList<LatLng> listOfPoints = new ArrayList<>();
        String[] pointsString = polylineString.split("\\|");
        for (int index = 0; index < pointsString.length; index += 1 ) {
            try {
                // Points are store in string array like lat,long so we need to spit it again for each point.
                String[] latAndLong = pointsString[index].split(",");
                LatLng latLng = new LatLng(Double.parseDouble(latAndLong[0]), Double.parseDouble(latAndLong[1]));
                listOfPoints.add(latLng);
            } catch(NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
        return listOfPoints;
    }

    public void DrawPolyline(List<LatLng> listOfPoints) {
        PolylineOptions options = new PolylineOptions().width(12).color(Color.parseColor("#E57373")).geodesic(true);
        for (int z = 0; z < listOfPoints.size(); z++) {
            LatLng point = listOfPoints.get(z);
            options.add(point);
        }

        map.addPolyline(options);
    }

    // DOnt think this is used anymroe.
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
                } //#E57373 , #00796B, #B71C1C #004D40
            }
            PolylineOptions options = new PolylineOptions().width(15).color(Color.parseColor("#E57373")).geodesic(true);

            map.addPolyline(options);

            // navigate to base of polyline.

            //map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } catch (JSONException e) {
            Log.e("TRAIL", "Failed to draw trail on the map - Likey an issue finding the correct json Point");
            e.printStackTrace();
        }
    }

    /*
    *    Method that triggers the the start of displaying crumbs on the map.

    *    @Param trailId : The id of the trail that we want to view. Used to get all the crumb Ids for
    *    displaying on the map.
    */
    public void StartCrumbDisplay(String trailId) {
        mStoryBoardItems = new ArrayList<>();
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

                    // All the crumbs are under the object that has the id "Title".
                    JSONArray crumbListJSON = new JSONArray(returnedCrumbs.getString("Title"));
                    JSONObject next = null;
                    for (int index=0; index<crumbListJSON.length(); index += 1 ) {
                        // The next node to get data from and Draw.
                         next =  crumbListJSON.getJSONObject(index);
                        //mStoryBoardItems.add(buildStoryBoardItem(next));
                        mapDisplayManager.DrawCrumbFromJson(next, false);
                    }

                    // Now that we are done, we want to set the focus to the last crumb added
                    Double Latitude = next.getDouble("Latitude");
                    Double Longitude = next.getDouble("Longitude");

                    CameraPosition position = new CameraPosition(new LatLng(Latitude, Longitude), 10, 72, 0);
                    CameraUpdate first = CameraUpdateFactory.newCameraPosition(position);

                    CameraUpdate center=
                            CameraUpdateFactory.newLatLng(new LatLng(Latitude,
                                    Longitude));
                    CameraUpdate zoom=CameraUpdateFactory.zoomTo(12);
                    // Move/animate camera to location
                    map.moveCamera(first);
                    map.animateCamera(zoom);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                   Log.e("BC.MAP", "A JsonException was thrown during the display process for a crumb." +
                           "This probably means that you are missing a field on your json. Stacktrace follows");
                    e.printStackTrace();
                }
            }
        }, context);

        clientRequestProxy.execute();
        Log.i("MAP", "Finished Loading crumbs and displaying them on the map");
    }

    // Simple method to build story board item out of jsonObject.
    private StoryBoardItemData buildStoryBoardItem(JSONObject json) throws JSONException {
        String id = json.getString("Id");
        Double latitude = json.getDouble("Latitude");
        Double Longitude = json.getDouble("Longitude");
        String mime = json.getString("Extension");
        String placeId = json.getString("PlaceId");

        return new StoryBoardItemData(id, placeId, mime, latitude, Longitude);
    }

    public void DisplayCrumbsFromLocalDatabase(JSONObject crumbs) {

        // This creates the async request with a callback method of what I want completed when the
        try {
            // Get the crumb title ??? why do i do this? last crumb?
            Iterator<String> keys = crumbs.keys();
            JSONObject next = null;
            while (keys.hasNext()) {
                // The next node to get data from and Draw.
                //next = crumbs.get
                String id = keys.next();
                next = crumbs.getJSONObject(id);
                mapDisplayManager.DrawLocalCrumbFromJson(next, "-1");
                mapDisplayManager.clusterManager.cluster();

            }
            // Now that we are done, we want to set the focus to the last crumb added

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.e("BC.MAP", "A JsonException was thrown during the display process for a crumb." +
                    "This probably means that you are missing a field on your json. Stacktrace follows");
            e.printStackTrace();
        }
        Log.i("MAP", "Finished Loading crumbs and displaying them on the map");
    }

    /*
    Method that begins the background tracking of a users trail.
     */
    public boolean CreateTrailAndBeginTracking(String trailTitle) {
        // I am doing this after rather than before because if a trail fails, and I want to retry in
        // the background.
        SendCreateTrailRequest(trailTitle);
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
        final String localTrailId = Integer.toString(PreferencesAPI.GetInstance(context).GetLocalTrailId());

        // Dont want to be saving a non existent trail - I will do other shit with it first
        if (!localTrailId.equals("-1")) {
            JSONObject jsonObject = dbc.getAllSavedTrailPoints(localTrailId);
            HTTPRequestHandler saver = new HTTPRequestHandler();

            // Save our trails.
            String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/SaveTrailPoints/";
            AsyncPost post = new AsyncPost(url, new AsyncPost.RequestListener() {
                @Override
                public void onFinished(String result) {
                    dbc.DeleteAllSavedTrailPoints(localTrailId);
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

    public void DrawPersonalTrailOnMap() {
        // Need to fetch all our metadata and save it to the server. Then we need to get all the
        // photos on the database and display it on the map

    }


}
