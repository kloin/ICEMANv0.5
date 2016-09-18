/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.test.java.com.breadcrumbs.models;

import com.breadcrumbs.models.Trail;
import com.breadcrumbs.models.UserService;
import com.breadcrumbs.resource.RESTTrailManager;
import com.breadcrumbs.resource.RetrieveData;
import org.json.JSONArray;
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
    public void TestThatWeCanSaveComments() {
        RESTTrailManager tTrailManager = new RESTTrailManager();
         Trail trail = new Trail();
        RetrieveData retrieveData = new RetrieveData();
        String userId = retrieveData.CreateNewUser("john", "joe", "123", "M", "fdsfds", "fdsfsd", "fdsfsd");
    	System.out.println("Saving trail");
        
        String trailId1= trail.SaveTrail("TEST", userId, "TEST");
        tTrailManager.AddCommentToAlbum(trailId1, userId, "This is a test");
        tTrailManager.AddCommentToAlbum(trailId1, userId, "This is a second test");
        
        String jsonArrayOfCommentsString = tTrailManager.GetAllCommentsForAnAlbum(trailId1);
        JSONArray j = new JSONArray(jsonArrayOfCommentsString);
        Assert.assertTrue(j.length() == 2);
        
        JSONObject object2 = new JSONObject(j.get(1));
        String commentText = object2.getString("CommentText");
        Assert.assertTrue(commentText.equals("This is a second test"));
    }
    
    @Test
    public void TestThatWeCanDeleteAComment() {
         RESTTrailManager tTrailManager = new RESTTrailManager();
         Trail trail = new Trail();
        RetrieveData retrieveData = new RetrieveData();
        String userId = retrieveData.CreateNewUser("john", "joe", "123", "M", "fdsfds", "fdsfsd", "fdsfsd");
    	System.out.println("Saving trail");
        
        String trailId1= trail.SaveTrail("TEST", userId, "TEST");
        String comment0Id = tTrailManager.AddCommentToAlbum(trailId1, userId, "This is a test");
        String comment1Id = tTrailManager.AddCommentToAlbum(trailId1, userId, "This is a second test");
//        tTrailManager.DeleteComment(comment0Id);
       // String jsonArrayOfCommentsString = tTrailManager.GetAllCommentsForAnAlbum(trailId1);
       // JSONArray j = new JSONArray(jsonArrayOfCommentsString);
       // Assert.assertTrue(j.length() == 1);
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
    
    @Test
    public void TestThatWeCanAddAViewToATripThatHasZeroViews() {
        Trail trail = new Trail();
        RetrieveData retrieveData = new RetrieveData();
        String userId = retrieveData.CreateNewUser("john", "joe", "123", "M", "fdsfds", "fdsfsd", "fdsfsd");
        String trailId1= trail.SaveTrail("TEST", userId, "TEST");
        trail.AddViewForATrail(trailId1);
        String jsonResultString = trail.GetTrip(trailId1);
        JSONObject result = new JSONObject(jsonResultString);
        int views = Integer.parseInt(result.getString("Views"));
        Assert.assertTrue(views == 1);
    }
    
    @Test
    public void TestThatWeCanAddAViewToATripThatHasMoreThanZeroViews() {
        Trail trail = new Trail();
        RetrieveData retrieveData = new RetrieveData();
        String userId = retrieveData.CreateNewUser("john", "joe", "123", "M", "fdsfds", "fdsfsd", "fdsfsd");
        String trailId1= trail.SaveTrail("TEST", userId, "TEST");
        trail.AddViewForATrail(trailId1);
        String jsonResultString = trail.GetTrip(trailId1);
        JSONObject result = new JSONObject(jsonResultString);
        int views = Integer.parseInt(result.getString("Views"));
        Assert.assertTrue(views == 1);
        trail.AddViewForATrail(trailId1);
        jsonResultString = trail.GetTrip(trailId1);
        result = new JSONObject(jsonResultString);
        views = Integer.parseInt(result.getString("Views"));
        Assert.assertTrue(views == 2);
    }
    
    @Test
    public void TestThatWeCanGetAllFollowedUserTrips() {
        Trail trail = new Trail();
        RetrieveData retrieveData = new RetrieveData();
        String userId = retrieveData.CreateNewUser("john", "joe", "123", "M", "fdsfds", "fdsfsd", "fdsfsd");
        String trailId1= trail.SaveTrail("TEST", userId, "TEST");
        String trailId2 = trail.SaveTrail("TEST2", userId, "TEST2");
        String user2Id = retrieveData.CreateNewUser("john", "joe", "123", "M", "fdsfds", "fdsfsd", "fdsfsd");
        UserService userservice = new UserService();
        userservice.PinUserForUser(user2Id, userId);
        RESTTrailManager resttm = new RESTTrailManager();
        String resultJSON = resttm.GetAllAlbumsFromFollowedUser(user2Id);
        JSONArray jsonArray = new JSONArray(resultJSON);
        Assert.assertTrue(jsonArray.length() == 2);
    }
    
    @Test
    public void TestThatWeCanGetAlbumsFromMultipleAlbums() {
        Trail trail = new Trail();
        RetrieveData retrieveData = new RetrieveData();
        String userId = retrieveData.CreateNewUser("john", "joe", "123", "M", "fdsfds", "fdsfsd", "fdsfsd");
        String trailId1= trail.SaveTrail("TEST", userId, "TEST");
        String trailId2 = trail.SaveTrail("TEST2", userId, "TEST2");
        RESTTrailManager resttm = new RESTTrailManager();
        String user2Id = retrieveData.CreateNewUser("john", "joe", "123", "M", "fdsfds", "fdsfsd", "fdsfsd");
        String user3Id = retrieveData.CreateNewUser("john", "joe", "123", "M", "fdsfds", "fdsfsd", "fdsfsd");

        String trailId3= trail.SaveTrail("TEST", user3Id, "TEST");
        UserService userservice = new UserService();
        userservice.PinUserForUser(user2Id, userId);
        userservice.PinUserForUser(user2Id, user3Id);

        String resultJSON = resttm.GetAllAlbumsFromFollowedUser(user2Id);
        JSONArray jsonArray = new JSONArray(resultJSON);
        Assert.assertTrue(jsonArray.length() == 3);
    }
    
}
