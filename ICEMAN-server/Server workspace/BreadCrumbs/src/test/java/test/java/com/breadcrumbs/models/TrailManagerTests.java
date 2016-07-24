/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.test.java.com.breadcrumbs.models;

import com.breadcrumbs.models.Trail;
import com.breadcrumbs.resource.RetrieveData;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * TESTS
 * @author jek40
 */
public class TrailManagerTests {
    
    @Test
    public void TestThatWeCanLoadTheMostPopularTwentyTripIds() {
        Trail trail = new Trail();
        RetrieveData retrieveData = new RetrieveData();
        String userId = retrieveData.CreateNewUser("john", "joe", "123", "M", "fdsfds", "fdsfsd", "fdsfsd");
    	System.out.println("Saving trail");
    	String trailId1= trail.SaveTrail("TEST", userId, "TEST");
        String trailId2 = trail.SaveTrail("TEST", userId, "TEST");
    	String trailId3 = trail.SaveTrail("TEST", userId, "TEST");
    	String trailId4 = trail.SaveTrail("TEST", userId, "TEST");
        String test  = trail.GetIdsOfTwentyMostPopularTrips();
        String[] ids = test.split(",");
        Assert.assertTrue(ids.length >=4);
    }
    
    @Test
    public void TestThatWeCanLoadExistingTripById() {
        Trail trail = new Trail();
        RetrieveData retrieveData = new RetrieveData();
        String userId = retrieveData.CreateNewUser("john", "joe", "123", "M", "fdsfds", "fdsfsd", "fdsfsd");
    	System.out.println("Saving trail");
        
        String trailId1= trail.SaveTrail("TEST", userId, "TEST");
        
        String result = trail.GetTrip(trailId1);
        JSONObject jsonresult = new JSONObject(result);
        String description = jsonresult.getString(trail.DESCRIPTION);
        Assert.assertTrue(description.equals("TEST"));
    }
    
    @Test
    public void TestThatMethodThrows500IfWeRequestSomethingThatDoesntExist() {
        Trail trail = new Trail();
        String result = trail.GetTrip("-1");
        Assert.assertTrue(result.equals("500"));
    }
    
}
