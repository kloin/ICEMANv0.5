/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.test.java.com.breadcrumbs.models;

import com.breadcrumbs.models.UserService;
import com.breadcrumbs.resource.RESTUser;
import com.breadcrumbs.resource.RetrieveData;
import org.junit.Assert;
import org.junit.Test;
import scala.sys.BooleanProp;

/**
 * Test suite for user Model
 * @author jek40
 */
public class UserModelTest {
    
    private RetrieveData retrieveData;
    
    @Test
    public void TestThatWeCanGetNumberOfFollowers() {
        retrieveData = new RetrieveData();
        String userId = retrieveData.CreateNewUser("john", "joe", "123", "M", "fdsfds", "fdsfsd", "fdsfsd");
        String userId2 = retrieveData.CreateNewUser("john", "joe", "123", "M", "fdsfds", "fdsfsd", "fdsfsd");
        
        UserService userService = new UserService();
        userService.PinUserForUser(userId, userId2);
        String numberOfFollowers = userService.GetNumberOfUsersThatFollowUs(userId2);
        System.out.println(numberOfFollowers);
        Assert.assertTrue(numberOfFollowers.equals("1"));
    }
    
    @Test 
    public void TestThatWeCanFollowUser() {
        retrieveData = new RetrieveData();
        String userId = retrieveData.CreateNewUser("john", "joe", "123", "M", "fdsfds", "fdsfsd", "fdsfsd");
        String userId2 = retrieveData.CreateNewUser("john", "joe", "123", "M", "fdsfds", "fdsfsd", "fdsfsd");
        
        RESTUser restUser = new RESTUser();
        restUser.PinUserForUser(userId, userId2);
        boolean result = Boolean.parseBoolean(restUser.IsUserFollowingOtherUser(userId, userId2));
        Assert.assertTrue(result);
    }
}
