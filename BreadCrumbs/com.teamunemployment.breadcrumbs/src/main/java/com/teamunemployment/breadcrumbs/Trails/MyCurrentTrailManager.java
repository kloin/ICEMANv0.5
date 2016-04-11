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

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.maps.android.PolyUtil;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncImageFetch;
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
import java.util.LinkedList;
import java.util.List;

/**
 * Created by aDirtyCanvas on 5/20/2015.
 * Created to manage and display a users current class.
 */
public class MyCurrentTrailManager extends Activity {
    private GoogleMap map;
    private AsyncDataRetrieval clientRequestProxy;
    private MyCurrentTrailManager mapContext;
    private Activity context;
    private LinkedList<String> linkedList = new LinkedList<>();
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
        linkedList.add(0, "#2196F3");
    }

    // Use with caution
    public MyCurrentTrailManager(Activity context) {
        this.context = context;
        mapContext = this;
        currentTrailManager = this;
    }

    public MyCurrentTrailManager() {

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

        // fetch metadata first
        String fetchMetadataUrl = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/FetchMetadata/" + trailId;
        clientRequestProxy = new AsyncDataRetrieval(fetchMetadataUrl, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) throws JSONException {
                Log.d("RESULT", result);
                JSONObject jsonResult = new JSONObject(result);

                // Get an iterator of all the event nodes.
                Iterator<String> keys = jsonResult.keys();
                while (keys.hasNext()) {

                    String nodeString = jsonResult.getString(keys.next());
                    JSONObject node = new JSONObject(nodeString);
                    drawOnMap(node);
                    drawNodeOnMap(node);
                }
            }
        });
        clientRequestProxy.execute();

//        String fetchTrailsUrl = MessageFormat.format("{0}/rest/TrailManager/GetAllTrailPoints/{1}",
//                LoadBalancer.RequestServerAddress(),
//                trailId);
//
//        // Get trailPoints
//        clientRequestProxy  = new AsyncDataRetrieval(fetchTrailsUrl, new AsyncDataRetrieval.RequestListener() {
//            /*
//             * Override for the
//             */
//            @Override
//            public void onFinished(String result) {
//                GlobalContainer.GetContainerInstance().SetTrailsJSON(result);
//                DisplayTrailOnMap(result);
//            }
//        });
//        clientRequestProxy.execute();
       /* HTTPRequestHandler requestHandler = new HTTPRequestHandler();
        String trailPointsJSONString = requestHandler.SendSimpleHttpRequestAndReturnString(fetchTrailsUrl);*/

        // Draw trailPoints onto the map
    }

    private void drawNodeOnMap(JSONObject node) throws JSONException {
        String type = node.getString("EventType");
        int eventType = Integer.parseInt(type);

        // If we have a rest zone we want to draw a little circle on the map.
        if (eventType == TrailManager.REST_ZONE) {
            String latitude = node.getString("Latitude");
            String longitude = node.getString("Longitude");
            LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            Circle circle = map.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(500)
                    .strokeColor(Color.parseColor("#E57373"))
                    .fillColor(Color.parseColor("#E57373")));
        }
    }

    private void drawOnMap(JSONObject node) throws JSONException {
        List<LatLng> listOfPoints;
        if (node.has("PolylineString")) {
            String polyLineString = node.getString("PolylineString");
            if (node.getString("PolylineIsEncoded").equals("0")) {
                listOfPoints = PolyUtil.decode(polyLineString);
            } else {
                listOfPoints = parseNonEncodedPolyline(polyLineString);
            }
            drawPolyline(listOfPoints);
        }
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

    private void drawPolyline(List<LatLng> listOfPoints) {
        PolylineOptions options = new PolylineOptions().width(25).color(Color.parseColor("#E57373")).geodesic(true);
        for (int z = 0; z < listOfPoints.size(); z++) {
            LatLng point = listOfPoints.get(z);
            options.add(point);
        }
        map.addPolyline(options);
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
            List<LatLng> listOfPoints = PolyUtil.decode("deiqGmeoe_@bJxGjAeUlnBsdAdpA~MhCy[bo@i_@lkBa|Bz\\qc@lKapAvRoiDvfBkmHtl@owDrd@ufBf^oiAnP{@|WhLfUySiGkiAehAlMw~@l\\}Wya@ge@_Fod@spAagBibAx@oy@fm@ep@leAsq@jw@skB`a@_w@`y@eWrp@q^|_@ebBoQefCoxAwbA}d@yiBdDcAwIvN{LaVcg@w[}bCweByeDqtBuaFstCucHquEaqAyg@avAsHkt@qfAkaB}hAuoAyq@uf@al@o_AeTqcAvo@ejE|_@g}@pu@ko@bsA{QngBzI`sAqM~y@ycBzpAmt@pyAcv@bpDc{@~zAs]zdAub@`~AydAgjBgsAch@ql@kc@ig@qwAujByzAmqAmy@apBum@}YxTkaAqh@_`C~w@qtAlS}aBw`@mfCwi@qyB~Ju~@vH{iAoLm}AlOqmAp{B_XxsBmcAp{AkVnp@od@nHmuAkKieDcFi_Dfp@wl@bw@yr@mNujAcy@{cAye@opA{i@}zCwo@{qAlKacAog@{p@udAgcBegAwt@oUaxB}vAoxAee@_|@shAaq@q_@asAss@yZalB}kBiiE}dA}|@yeAaVkqCekCqpAat@sf@_tAwtBrd@a`@Rcg@mWyo@tpAyPjn@gl@}B{m@qmA{h@wl@oMvR{a@yh@ePkk@mv@g}@io@c`Auq@gi@yQiEiJ~QmKl[Ubf@nHlqBaGrr@u~@~nAoi@b`Aw@nd@tMp|CoKbdBag@jw@_f@|s@{LdbBtz@jcDzfA~bAnc@pt@~f@tmAiD~y@{I~_CkLfnBmZlqAe|@d|@sd@xTwRyOmHnXggAxzEc^ht@cHlqAgiEhmFiwBpr@}j@{`@yf@hJkj@ynAyfBwHgqAq`@w}D}sHoP{`Agj@iJ_cCabC{i@an@kWikA}oAk_B}_@ugABmz@wP|ByL{ToTyHgJsk@pKsPiWFmg@mu@cBqO|i@yKzp@mlAiAc`@i`AyeBzdAkxCxZipAtqAsoArkAax@fm@}c@|C{g@zTm[pIegAei@maB{iBowCkd@wXk_CodBoFee@ms@a[sUqr@aB_cGlYugC}iB_}CqmCm~EsaBeiDe}A~J}v@we@__DkCcp@q`@_n@sxBegAcgB}AkvBvKiyAkm@wsAmr@mIgSofAjFgnDoJemEhZebDny@ayC}`@abAceCszB}t@iKcl@eAcv@cwBiy@slAy~AqLam@iC{`@sw@`GocCfMqjBjw@adH{d@_e@wz@uoBqe@uxB_e@ij@");
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
            for (int z = 0; z < listOfPoints.size(); z++) {
                LatLng point = listOfPoints.get(z);
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


}
