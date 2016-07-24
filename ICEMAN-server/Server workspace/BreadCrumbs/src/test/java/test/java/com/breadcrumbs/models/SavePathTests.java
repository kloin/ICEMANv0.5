///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package test.java.test.java.com.breadcrumbs.models;
//
//import Statics.StaticValues;
//import com.breadcrumbs.heavylifting.Path;
//import com.breadcrumbs.heavylifting.TripManager;
//import com.breadcrumbs.resource.RetrieveData;
//import junit.framework.Assert;
//import org.json.JSONObject;
//import org.junit.Test;
//
///**
// *
// * @author Josiah Kendall
// */
//public class SavePathTests {
//    
//    @Test
//    public void SavePathToDatabaseTest() {
//      // create objects
//      // Create test trail.
//      // save objects
//      // Fetch data
//      // ensure data is correct.
//       JSONObject walking1 = new JSONObject();
//        walking1.put(StaticValues.LATITUDE_KEY, -36.888227);
//        walking1.put(StaticValues.LONGITUDE_KEY, 174.711354);
//        walking1.put(StaticValues.CURRENT_ACTIVITY_KEY, 7);
//        walking1.put(StaticValues.PAST_ACTIVITY_KEY, -1);
//
//        JSONObject driving1 = new JSONObject();
//        driving1.put(StaticValues.LATITUDE_KEY, -36.828460);
//        driving1.put(StaticValues.LONGITUDE_KEY,  174.747094);
//        driving1.put(StaticValues.CURRENT_ACTIVITY_KEY, 0);
//        driving1.put(StaticValues.PAST_ACTIVITY_KEY, 7);
//        
//        JSONObject walking2 = new JSONObject();
//        walking2.put(StaticValues.LATITUDE_KEY, -36.791423);
//        walking2.put(StaticValues.LONGITUDE_KEY, 174.779304);
//        walking2.put(StaticValues.CURRENT_ACTIVITY_KEY, 7);
//        walking2.put(StaticValues.PAST_ACTIVITY_KEY, 0);
//        
//        JSONObject walking3 = new JSONObject();
//        walking3.put(StaticValues.LATITUDE_KEY, -36.791366);
//        walking3.put(StaticValues.LONGITUDE_KEY, 174.779301);
//        walking3.put(StaticValues.CURRENT_ACTIVITY_KEY, 7);
//        walking3.put(StaticValues.PAST_ACTIVITY_KEY, 7);
//        
//        JSONObject wrapper = new JSONObject();
//        wrapper.put("1", walking1);
//        wrapper.put("2", driving1);
//        wrapper.put("3", walking2);
//        wrapper.put("4", walking3);
//        
//        TripManager tripManager = new TripManager();
//        RetrieveData retrieve = new RetrieveData();
//        String id = retrieve.CreateNewUser("Josiah", "7873", "23", "M", "1", "testing@gmail.com", "1234567");
//        String tripId = retrieve.SaveTrail("OOOHRAA", "just testing yo", id);
//        tripManager.SavePath(tripId, wrapper.toString());
//        JSONObject tripObjects = tripManager.FetchPathForTrip(tripId);
//        Assert.assertTrue(tripObjects.length() == 2);
//    }
//}
