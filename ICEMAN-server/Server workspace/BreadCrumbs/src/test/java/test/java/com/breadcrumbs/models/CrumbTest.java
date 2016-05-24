package test.java.test.java.com.breadcrumbs.models;

import com.breadcrumbs.database.DBMaster;
import com.breadcrumbs.models.Crumb;
import com.breadcrumbs.models.Trail;
import com.breadcrumbs.resource.RetrieveData;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

/**
 * Testing methods relating to crumbs.
 * @author jek40
 */
public class CrumbTest {
    
    @Test
    public void TestThatWeCanAddNewPropertyToExistingNode() {
        String Username = "name";
        
        // Create a node (user for ease of coding - doesnt really matter what node type, it should work universally.
        RetrieveData retrieveData = new RetrieveData();
        String id = retrieveData.CreateNewUser(Username, "7873", "21", "M", Username, Username, Username);
        
        // Add a new property to our node.
        DBMaster dbm = DBMaster.GetAnInstanceOfDBMaster();
        dbm.UpdateNodeWithCypherQuery(id, "PropertyTest", "rest");
        GraphDatabaseService _db = dbm.GetDatabaseInstance();
        // Chekck that our node has the property set propeerly. 
        Node node = dbm.RetrieveNode(Long.parseLong(id));
        Transaction tx = _db.beginTx();
				
        try {
            Assert.assertTrue(node.hasProperty("PropertyTest"));
            System.out.println("Test result: " + node.getProperty("PropertyTest").toString());

        } catch (Exception ex) {
            System.out.println("DBMaster - Line 343: Failed to update"
                    + "property due to general exception. Stack trace follows");
            ex.printStackTrace();
            tx.failure();
        } finally {
            tx.finish();
        }
    }
    
    @Test
    public void TestThatWeCanUpdateCurrentPropertyOnNodeWithCypherQuery() {
        String Username = "name";
        
        // Create a node (user for ease of coding - doesnt really matter what node type, it should work universally.
        RetrieveData retrieveData = new RetrieveData();
        String id = retrieveData.CreateNewUser(Username, "7873", "21", "M", Username, Username, Username);
        
        // Add a new property to our node.
        DBMaster dbm = DBMaster.GetAnInstanceOfDBMaster();
        dbm.UpdateNodeWithCypherQuery(id, "Username", "rest");
        GraphDatabaseService _db = dbm.GetDatabaseInstance();
        // Chekck that our node has the property set propeerly. 
        Node node = dbm.RetrieveNode(Long.parseLong(id));
        Transaction tx = _db.beginTx();
				
        try {
            Assert.assertTrue(node.getProperty("PropertyTest").equals("rest"));

        } catch (Exception ex) {
            System.out.println("DBMaster - Line 343: Failed to update"
                    + "property due to general exception. Stack trace follows");
            ex.printStackTrace();
            tx.failure();
        } finally {
            tx.finish();
        }
    }
    
    @Test
    public void TestThatWeCanUpdateCoverPhoto() {
        // Create a node (user for ease of coding - doesnt really matter what node type, it should work universally.
        RetrieveData retrieveData = new RetrieveData();
        String id = retrieveData.CreateNewUser("Me", "7873", "21", "M", " ", " ", " ");

        String trailId = retrieveData.SaveTrail("OOOHRAA", "just testing yo", id);
        String crumbId = retrieveData.SaveCrumb("testing123", id, trailId, "-36.8", "174.5", "icon", ".jpg", "1", "Greenlane", "Auckland", "New Zealand", "time");
        
        
        // Add a new property to our node.
        Crumb crumb = new Crumb();
        Assert.assertTrue(crumb.trailHasCoverPhoto(trailId));
        crumb.updateCoverPhoto(trailId, crumbId);
        crumb.updateCoverPhoto(trailId, "21312321");
        Assert.assertTrue(crumb.trailHasCoverPhoto(trailId));
    }
    
    @Test
    public void TestThatWeCanGetAllPhotoIdsForATrail() {
        RetrieveData retrieveData = new RetrieveData();
        String id = retrieveData.CreateNewUser("Me", "7873", "21", "M", " ", " ", " ");
        String trailId = retrieveData.SaveTrail("OOOHRAA", "just testing yo", id);
        String crumbId = retrieveData.SaveCrumb("testing123", id, trailId, "-36.8", "174.5", "icon", ".jpg", "1", "Greenlane", "Auckland", "New Zealand", "time");
        Trail trail = new Trail();
        JSONObject jsonObject = new JSONObject(trail.GetAllPhotoIdsForATrail(trailId));
        Assert.assertTrue(jsonObject.length() == 1);
        
        // Add a mp4 crumb. WE should not get this back as it is not an image.
        retrieveData.SaveCrumb("testing123", id, trailId, "-36.8", "174.5", "icon", ".mp4", "1", "Greenlane", "Auckland", "New Zealand", "time");
        jsonObject = new JSONObject(trail.GetAllPhotoIdsForATrail(trailId));
        Assert.assertTrue(jsonObject.length() == 1);
          
        retrieveData.SaveCrumb("testing123", id, trailId, "-36.8", "174.5", "icon", ".jpg", "1", "Greenlane", "Auckland", "New Zealand", "time");
        jsonObject = new JSONObject(trail.GetAllPhotoIdsForATrail(trailId));
        Assert.assertTrue(jsonObject.length() == 2);
    }
    
    
    
}
