package test.java.test.java.com.breadcrumbs.models;

import com.breadcrumbs.database.DBMaster;
import com.breadcrumbs.models.Crumb;
import com.breadcrumbs.resource.RetrieveData;
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
        dbm.UpdateNodeWithCypherQuery(id, "Username", "'rest'");
        GraphDatabaseService _db = dbm.GetDatabaseInstance();
        // Chekck that our node has the property set propeerly. 
        Node node = dbm.RetrieveNode(Long.parseLong(id));
        Transaction tx = _db.beginTx();
				
        try {
            Assert.assertTrue(node.getProperty("PropertyTest").equals("rest"));
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
    
}
