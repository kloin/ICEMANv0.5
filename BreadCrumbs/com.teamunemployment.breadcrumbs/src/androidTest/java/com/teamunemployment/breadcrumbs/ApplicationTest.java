package com.teamunemployment.breadcrumbs;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Trails.TrailManagerWorker;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ApplicationTest {
    private Context mContext;
    private DatabaseController db;
    private TrailManagerWorker trailManagerWorker;
    private SharedPreferences mPreferences;
    private static final String mockTrailId = "12345678";
    @Before
    public void Before() throws Exception {
        RenamingDelegatingContext context = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        db = new DatabaseController(context);
        trailManagerWorker = new TrailManagerWorker(context);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context;
    }

    //According to Zainodis annotation only for legacy and not valid with gradle>1.1:
    @Test
    public void TestUserCanBeCreatedAndRetrieved(){

        db.SaveUser("7898", "Josiah", 24, "0123");
        assertTrue(db.CheckUserExists("7898"));
        // Here i have my new database wich is not connected to the standard database of the App
    }


    @Test
    public void TestWeCanCreateTrailSummary() {
        db.SaveTrailStart("1", Long.toString(System.currentTimeMillis()));
        assertTrue(db.GetTrailSummary("1") != null);
    }



    @Test
    public void TestUserCannotBeFoundIfItDoesNotExist() {
        db.SaveUser("7899", "Josiah", 24, "0123");
        assertTrue(!db.CheckUserExists("-1"));
    }

    @Test
    public void TestCrumbCanBeSavedAndRetrieved() {
        try {
            URL url = new URL("http://104.199.132.109:8080/images/6504.jpg");
            Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            assertTrue(image!=null);
            String trailId = "99";
            String description = "test";
            String userId = "0";
            int eventId = 0;
            double latitude = 123;
            double longitude = -123;
            byte[] media = getBitmapAsByteArray(image);
            String icon = "test";
            String placeId = "123456789";
            String suburb = "Birkdale";
            String city = "North Shore";
            String country = "New Zealand";
           // db.SaveCrumb(trailId, description, userId, eventId, latitude, longitude, mime, timeStamp, media, icon, placeId, suburb, city, country);
            JSONObject jsonObject = db.GetCrumbsWithMedia("99", 0);
            assertTrue(jsonObject.length() == 1);

            JSONObject jsonObject1 = jsonObject.getJSONObject("0");
            // Image test
            String mediaBase64 = jsonObject1.getString("media");
            Log.d("MEDIA", media.toString());
            Bitmap bitmap = BitmapFactory.decodeByteArray(media, 0, media.length);
            assertTrue(media.length > 1);

            String eventId2 = jsonObject1.getString("eventId");
            assertTrue(eventId == 0);

        } catch (IOException ex) {
            fail();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestThatMetadataCanBeStoredAndRetrieved() {
        trailManagerWorker.CreateEventMetadata(TrailManagerWorker.TRAIL_START, mockMeALocation());
        //db.AddMetadata("1", DateTime.now().toString(),123.000, -123.000, "0", );
        JSONObject jsonObject = db.fetchMetadataFromDB(mockTrailId, false);
        assertTrue(jsonObject.length() > 0);
    }

    @Test
    public void TestHowTrailSavePointWorks() {
        trailManagerWorker.CreateEventMetadata(TrailManagerWorker.TRAIL_START, mockMeALocation());

        int test = db.GetSavedIndexForTrail(mockTrailId);
        assertTrue(test> 0);
    }

    // Just a standard fake location with fake values. Might randomise these if neccesary
    private Location mockMeALocation() {
        Location location = new Location("NETWORK");
        location.setLatitude(123.123);
        location.setLongitude(-123.123);
        return location;
    }

    @Test
    public void TestThatMetadataGetsIdProperty() throws JSONException {
        Location location = mockMeALocation();
        mPreferences.edit().putString("TRAILID", "12345678").commit();
        trailManagerWorker.CreateEventMetadata(TrailManagerWorker.TRAIL_START, location);
        trailManagerWorker.CreateEventMetadata(TrailManagerWorker.REST_ZONE, location);
        trailManagerWorker.CreateEventMetadata(TrailManagerWorker.CRUMB, location);
        trailManagerWorker.CreateEventMetadata(TrailManagerWorker.TRAIL_END, location);

        JSONObject jsonObject = db.fetchMetadataFromDB("12345678", false);
        JSONObject jsonObject1 = jsonObject.getJSONObject("1");
        String id = jsonObject1.getString("id");
        Log.d("TEST", "Id : " + id);
        assertTrue(id.equals("2"));
    }

    @Test
    public void TestThatRestZoneCanBeSavedToDB() {
        db.SaveRestZone("0", 1, 123.000, -123.000, "123456789", Long.toString(System.currentTimeMillis()));
        db.SaveRestZone("0", 2, 123.000, -122.000, "123456", Long.toString(System.currentTimeMillis()));
        db.SaveRestZone("0", 3, 123.000, -121.000, "123454456", Long.toString(System.currentTimeMillis()));
        db.SaveRestZone("0", 4,123.000, -121.000, "123454456",Long.toString(System.currentTimeMillis()));

        JSONObject jsonObject = db.GetAllRestZonesForATrail("0");
        Log.d("TEST", "REST Zones Count: " + jsonObject.length());
        assertTrue(jsonObject.length() == 4);
    }


    @Test
    public void IngegrationTestSimulateTrailBehaviour() throws IOException {

        // Create restZones
        db.SaveRestZone("0", 1, 123.000, -123.000, "123456789", Long.toString(System.currentTimeMillis()));
        db.SaveRestZone("0", 2, 123.000, -122.000, "123456", Long.toString(System.currentTimeMillis()));
        db.SaveRestZone("0", 3,123.000, -121.000, "123454456",Long.toString(System.currentTimeMillis()));

        // Create gps points
        URL url = new URL("http://104.199.132.109:8080/images/6504.jpg");
        Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        assertTrue(image!=null);
        String trailId = "99";
        String description = "test";
        String userId = "0";
        int eventId = 0;
        double latitude = 123;
        double longitude = -123;
        String mime = ".jpg";
        byte[] media = getBitmapAsByteArray(image);
        String icon = "test";
        String placeId = "123456789";
        String suburb = "Birkdale";
        String city = "North Shore";
        String country = "New Zealand";

//        db.SaveCrumb(trailId, description, userId, eventId, latitude, longitude, mime, timeStamp, media, icon, placeId, suburb, city, country);
        URL url2 = new URL("http://104.199.132.109:8080/images/6504.jpg");
        Bitmap image2 = BitmapFactory.decodeStream(url2.openConnection().getInputStream());
        assertTrue(image2!=null);
        String trailId2 = "99";
        String description2 = "test";
        String userId2 = "0";
        int eventId2 = 0;
        double latitude2 = 123;
        double longitude2 = -123;
        String mime2 = ".jpg";
        byte[] media2 = getBitmapAsByteArray(image);
        String icon2 = "test";
        String placeId2 = "123456789";
        String suburb2 = "Birkdale";
        String city2 = "North Shore";
        String country2 = "New Zealand";
//        db.SaveCrumb(trailId2, description2, userId2, eventId2, latitude2, longitude2, mime2, timeStamp2, media2, icon2, placeId2, suburb2, city2, country2);

        // Hit SaveMethod
        trailManagerWorker.SaveEntireTrail("99");
    }

    private static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    @Test
    public void CreateTrailIntegration() throws JSONException {
        // CreateNewTrail
        String userId = mPreferences.getString("USERID", null);
        //store variable to sharedPreferences (and db for backup? It is pretty vital that I dont lose this trail)
        String url = MessageFormat.format(LoadBalancer.RequestServerAddress() + "/rest/login/saveTrail/{0}/{1}/{2}", "Test data 1", "test", userId);
        String trailId = "4243";
        mPreferences.edit().putString("TRAILID", "4243").commit();
        trailManagerWorker.StartLocalTrail();
        Location location1 = new Location("gps");
        location1.setLatitude(-44.9437402);
        location1.setLongitude(168.8378104);
        Location location2 = new Location("gps");
        location1.setLatitude(-45.0180531);
        location1.setLongitude(168.9337654);
        Location location3 = new Location("gps");
        location1.setLatitude(-45.0375501);
        location1.setLongitude(169.1944608);

        trailManagerWorker.CreateEventMetadata(TrailManagerWorker.REST_ZONE, location1);
        trailManagerWorker.CreateEventMetadata(TrailManagerWorker.CRUMB, location2);
        // trailManagerWorker.CreateEventMetadata(TrailManagerWorker.GPS, mockMeALocation());
        trailManagerWorker.CreateEventMetadata(TrailManagerWorker.REST_ZONE, location3);
        JSONObject jsonObject = db.fetchMetadataFromDB(trailId, false);
        JSONObject wrapper = new JSONObject();
        wrapper.put("TrailId", trailId);
        wrapper.put("Events", jsonObject);
        trailManagerWorker.saveMetadata(wrapper, trailId);
    }

    @Test
    public void TestThatSavingCrumbSavesMetadata() throws JSONException {
//        db.SaveCrumb("1234","test", "1", 2, 0.0, 0.0, ".jpg", DateTime.now().toString(), null, "", "", "", "", "");
        JSONObject jsonObject = db.fetchMetadataFromDB("1234", false);
        jsonObject = jsonObject.getJSONObject("0");
        assertTrue(jsonObject.getDouble("latitude") == 0.0);
    }


    private String fetchJsonString(String trailId) throws JSONException {
        JSONObject wrapper = new JSONObject();
        JSONObject events = new JSONObject();
        wrapper.put("TrailId", trailId);
        wrapper.put("Events", events);

        /*
         First object - we are generally going to be standing at most places we take photos. I need
         to be able to figure out the transport method - this will come later.
          */
        JSONObject event = new JSONObject();
        event.put("driving_method", "0"); // Just for now
        event.put("latitude", "-44.9437402");
        event.put("longitude", "168.8378104");
        event.put("eventId", "0");
        event.put("type", "0");

        JSONObject event2 = new JSONObject();
        event.put("driving_method", "0"); // Just for now
        event.put("latitude", "-45.0180531");
        event.put("longitude", "168.9337654");
        event.put("eventId", "1");
        event.put("type", "0");

        JSONObject event3 = new JSONObject();
        event.put("driving_method", "0"); // Just for now
        event.put("latitude", "-45.0375501");
        event.put("longitude", "169.1944608");
        event.put("eventId", "0");
        event.put("type", "0");

        JSONObject event4 = new JSONObject();
        event.put("driving_method", "0");
        event.put("latitude", "-43.8648759");
        event.put("longitude", "169.0460804");
        event.put("eventId", "0");
        event.put("type", "0");

        JSONObject event5 = new JSONObject();
        event.put("driving_method", "0");
        event.put("latitude", "-43.4667567");
        event.put("longitude", "170.0178356");
        event.put("eventId", "0");
        event.put("type", "0");

        JSONObject restZone1 = new JSONObject();//-43.287581, 170.222576
        restZone1.put("driving_method", "0");
        restZone1.put("latitude", "-43.287581");
        restZone1.put("longitude", "170.222576");
        restZone1.put("eventId", "0");
        restZone1.put("type", "2");

        JSONObject restZone2 = new JSONObject();//-42.444079, 171.217216
        restZone1.put("driving_method", "0");
        restZone1.put("latitude", "-42.444079");
        restZone1.put("longitude", "171.217216");
        restZone1.put("eventId", "0");
        restZone1.put("type", "2");


        return restZone1.toString();



    }



    @Test
    public void TestGrabbingImagesAndFolders() {
        // which image properties are we querying


    }






}