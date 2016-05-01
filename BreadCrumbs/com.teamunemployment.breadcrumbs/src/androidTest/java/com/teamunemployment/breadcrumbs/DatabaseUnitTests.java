//package com.teamunemployment.breadcrumbs;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.support.test.InstrumentationRegistry;
//import android.support.test.runner.AndroidJUnit4;
//import android.test.RenamingDelegatingContext;
//import android.test.suitebuilder.annotation.SmallTest;
//import android.util.Log;
//
//import com.teamunemployment.breadcrumbs.Trails.TrailManagerWorker;
//import com.teamunemployment.breadcrumbs.database.DatabaseController;
//
//import org.joda.time.DateTime;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.net.URL;
//
//import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.fail;
//
///**
// * Created by jek40 on 30/03/2016.
// */
//@RunWith(AndroidJUnit4.class)
//@SmallTest
//public class DatabaseUnitTests {
//    private DatabaseController db;
//
//    @Before
//    public void Before() throws Exception {
//        RenamingDelegatingContext context = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
//        db = new DatabaseController(context);
//    }
//
//    //According to Zainodis annotation only for legacy and not valid with gradle>1.1:
//    @Test
//    public void TestUserCanBeCreatedAndRetrieved(){
//        db.SaveUser("7898", "Josiah", 24, "0123");
//        assertTrue(db.CheckUserExists("7898"));
//        // Here i have my new database wich is not connected to the standard database of the App
//    }
//
//    @Test
//    public void TestUserCannotBeFoundIfItDoesNotExist() {
//        db.SaveUser("7899", "Josiah", 24, "0123");
//        assertTrue(!db.CheckUserExists("-1"));
//    }
//
//    @Test
//    public void TestCrumbCanBeSavedAndRetrieved() {
//        try {
//            URL url = new URL("http://weknowyourdreamz.com/images/wave/wave-04.jpg");
//            Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//            assertTrue(image!=null);
//            //db.SaveCrumb("1", "0", 123.000, -123.000, ".jpg", DateTime.now().toString(), getBitmapAsByteArray(image));
//            JSONObject jsonObject = db.GetCrumbsWithMedia("1", 0);
//            assertTrue(jsonObject.length() == 1);
//
//            JSONObject jsonObject1 = jsonObject.getJSONObject("0");
//            // Image test
//            byte[] media = (byte[])jsonObject1.get("media");
//            Log.d("MEDIA", media.toString());
//            Bitmap bitmap = BitmapFactory.decodeByteArray(media, 0, media.length);
//            assertTrue(media.length > 1);
//
//            String eventId = jsonObject1.getString("eventId");
//            assertTrue(eventId.equals("0"));
//        } catch (IOException ex) {
//            fail();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void TestThatMetadataCanBeStoredAndRetrieved() {
//        db.AddMetadata(1, DateTime.now().toString(),123.000, -123.000, "0", TrailManagerWorker.GPS, 0);
//        JSONObject jsonObject = db.fetchMetadataFromDB("0");
//        assertTrue(jsonObject.length() > 0);
//    }
//
//    @Test
//    public void TestThatMetadataGetsIdProperty() throws JSONException {
//        db.AddMetadata(1, DateTime.now().toString(),123.000, -123.000, "0", TrailManagerWorker.GPS, 0);
//        db.AddMetadata(1, DateTime.now().toString(),123.000, -123.000, "0", TrailManagerWorker.GPS, 0);
//        db.AddMetadata(1, DateTime.now().toString(),123.000, -123.000, "0", TrailManagerWorker.GPS, 0);
//        db.AddMetadata(1, DateTime.now().toString(),123.000, -123.000, "0", TrailManagerWorker.GPS, 0);
//
//        JSONObject jsonObject = db.fetchMetadataFromDB("0");
//        JSONObject jsonObject1 = jsonObject.getJSONObject("1");
//        String id = jsonObject1.getString("id");
//        Log.d("TEST", "Id : " + id);
//        assertTrue(id.equals("2"));
//    }
//
//    @Test
//    public void TestThatRestZoneCanBeSaved() {
//        db.SaveRestZone("0", 1,123.000, -123.000, "123456789",DateTime.now().toString());
//        db.SaveRestZone("0", 2,123.000, -122.000, "123456",DateTime.now().toString());
//        db.SaveRestZone("0",3,123.000, -121.000, "123454456",DateTime.now().toString());
//
//        JSONObject jsonObject = db.GetAllRestZonesForATrail("0");
//        Log.d("TEST", "REST Zones Count: " + jsonObject.length());
//        assertTrue(jsonObject.length() > 2);
//    }
//
//    @Test
//    public void IngegrationTestSimulateTrailBehaviour() {
//
//        // Create restZones
//        // Create gps points
//        // create crumbs
//
//
//    }
//
//
//    private static byte[] getBitmapAsByteArray(Bitmap bitmap) {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
//        return outputStream.toByteArray();
//    }
//}
