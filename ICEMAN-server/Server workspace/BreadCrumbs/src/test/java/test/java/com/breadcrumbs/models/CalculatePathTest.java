/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.test.java.com.breadcrumbs.models;

import com.breadcrumbs.models.Path;
import com.breadcrumbs.models.Polyline2;
import java.util.ArrayList;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Path calculation tests.
 * @author Josiah kendall
 */
public class CalculatePathTest {
    private static final String LATITUDE_KEY = "Latitude";
    private static final String LONGITUDE_KEY = "Longitude";
    private static final String CURRENT_ACTIVITY_KEY = "CurrentActivity";
    private static final String PAST_ACTIVITY_KEY = "LastActivity";

    @Test
    public void TestThatWeCanCalculateADrivingPolylineGivenTwoPoints() {
        
    }
    
    /**=========================================================================
     * ++++++++++++++++++++++ Driving patterns +++++++++++++++++++++++++++++++++
     * =========================================================================
     * Here we are just trying to break the polyline calculating algorithm with different tests
     */
    /**
     *  (W)(D)(W)(W) (Live);
     */
    @Test
    public void DrivingNodePattern1() {
         
        JSONObject walking1 = new JSONObject();
        walking1.put(LATITUDE_KEY, -36.888227);
        walking1.put(LONGITUDE_KEY, 174.711354);
        walking1.put(CURRENT_ACTIVITY_KEY, 7);
        walking1.put(PAST_ACTIVITY_KEY, -1);

        JSONObject driving1 = new JSONObject();
        driving1.put(LATITUDE_KEY, -36.828460);
        driving1.put(LONGITUDE_KEY,  174.747094);
        driving1.put(CURRENT_ACTIVITY_KEY, 0);
        driving1.put(PAST_ACTIVITY_KEY, 7);
        
        JSONObject walking2 = new JSONObject();
        walking2.put(LATITUDE_KEY, -36.791423);
        walking2.put(LONGITUDE_KEY, 174.779304);
        walking2.put(CURRENT_ACTIVITY_KEY, 7);
        walking2.put(PAST_ACTIVITY_KEY, 0);
        
        JSONObject walking3 = new JSONObject();
        walking3.put(LATITUDE_KEY, -36.791366);
        walking3.put(LONGITUDE_KEY, 174.779301);
        walking3.put(CURRENT_ACTIVITY_KEY, 7);
        walking3.put(PAST_ACTIVITY_KEY, 7);
        
        JSONObject wrapper = new JSONObject();
        wrapper.put("1", walking1);
        wrapper.put("2", driving1);
        wrapper.put("3", walking2);
        wrapper.put("4", walking3);
        
        Path path = new Path(wrapper);
        ArrayList<Polyline2> lines = path.CalculatePolylines();
        Assert.assertTrue(lines.size() == 2);
        Assert.assertTrue(lines.get(0).isEncoded);
        Assert.assertTrue(!lines.get(1).isEncoded);


        //Path path = new Path()
    }
     private ArrayList<Polyline2> getFakeLines(String csvOfTestIndicators) {
        String[] csvs = csvOfTestIndicators.split(",");
        ArrayList<Polyline2> lines = new ArrayList<>();
        JSONObject wrapper = new JSONObject();
        for (int index = 0; index < csvs.length; index += 1) {
            String item = csvs[index];
            if (item.equals("W")) {
                lines.add(new Polyline2("MOCK", false));
            } else {
                lines.add(new Polyline2("MOCK", true));
            }
        }
        return lines;
    }
    
    
     private boolean isDriving(Polyline2 line) {
        return line.isEncoded;
    }
    
    private boolean isWalking(Polyline2 line) {
        return !line.isEncoded;
    }
    
   
    private JSONObject Walking() {
        JSONObject walking1 = new JSONObject();
        walking1.put(LATITUDE_KEY, -36.888227);
        walking1.put(LONGITUDE_KEY, 174.711354);
        walking1.put(CURRENT_ACTIVITY_KEY, 7);
        walking1.put(PAST_ACTIVITY_KEY, -7);
        
        return walking1;
    }
    
    private JSONObject Driving() {
        JSONObject driving = new JSONObject();
        driving.put(LATITUDE_KEY, -36.888227);
        driving.put(LONGITUDE_KEY, 174.711354);
        driving.put(CURRENT_ACTIVITY_KEY, 0);
        return driving;
    }
    
    /**
     * (W)(W)(D)(W) == {W,D};
     */
    @Test
    public void DrivingNodePattern2() {
        JSONObject wrapper = new JSONObject();
        wrapper.put("1", Walking());
        wrapper.put("2", Walking());
        wrapper.put("3", Driving());
        wrapper.put("4", Walking());
        
        Path path = new Path(wrapper);
        ArrayList<Polyline2> lines = path.CalculatePolylines(true);
        Assert.assertTrue(lines.size() == 2);
        Assert.assertTrue(isWalking(lines.get(0)));
        Assert.assertTrue(isDriving(lines.get(1)));
    }
    
    /**
     * (W)(D)(D)(W)(W) = {D,W}
     */
    @Test 
    public  void DrivingNodePattern3() {
        JSONObject wrapper = new JSONObject();
        wrapper.put("1", Walking());
        wrapper.put("2", Driving());
        wrapper.put("3", Driving());
        wrapper.put("4", Walking());
        wrapper.put("5", Walking());
        
        Path path = new Path(wrapper);
        ArrayList<Polyline2> lines = path.CalculatePolylines(true);
        Assert.assertTrue(lines.size() == 2);
        Assert.assertTrue(isDriving(lines.get(0)));
        Assert.assertTrue(isWalking(lines.get((1))));
    }
    
   
    
    /**
     * (W)(W)(D)(W)(D)(W) = {W,D}
     */
    @Test 
    public void DrivingNodePattern4() {
        JSONObject wrapper = new JSONObject();
        wrapper.put("1", Walking());
        wrapper.put("2", Walking());
        wrapper.put("3", Driving());
        wrapper.put("4", Walking());
        wrapper.put("5", Driving());
        wrapper.put("6", Walking());

        Path path = new Path(wrapper);
        // Tell it we are testing, so that we dont send requests to the metered api.
        ArrayList<Polyline2> lines = path.CalculatePolylines(true);
        // Check correct size
        Assert.assertTrue(lines.size() == 2);
        
        // Check items
        Assert.assertTrue(isWalking(lines.get(0)));
        Assert.assertTrue(isDriving(lines.get(1)));
    }
    
   
    
    /**
     * W,D,W,D,W,W,D,W,W = 
     */
    @Test 
    public  void DrivingNodePattern5() {
        
    }
    
    
//    @Test 
//    public  void DrivingNodePattern6() {
//        
//    }
//    @Test 
//    public  void DrivingNodePattern7() {
//        
//    }
//    @Test
//    public void DrivingNodePattern8() {
//        
//    }
    
}
