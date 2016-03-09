///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package test.java.com.breadcrumbs.models;
//
//import com.breadcrumbs.database.DBMaster;
//import com.breadcrumbs.models.Trail;
//import org.json.JSONObject;
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import static org.junit.Assert.*;
//import org.neo4j.graphdb.Node;
//
///**
// *
// * @author jek40
// */
//public class TrailTest {
//    
//    public TrailTest() {
//    }
//    
//    @BeforeClass
//    public static void setUpClass() {
//    }
//    
//    @AfterClass
//    public static void tearDownClass() {
//    }
//    
//    @Before
//    public void setUp() {
//    }
//    
//    @After
//    public void tearDown() {
//    }
//
//    @Test
//    public void TestDateSavesRight() {
//        
//    }
//    /**
//     * Test of SaveTrail method, of class Trail.
//     */
//    @Test
//    public void testSaveTrail() {
//        System.out.println("SaveTrail");
//        String trailName = "";
//        String userId = "";
//        String description = "";
//        Trail instance = new Trail();
//        String expResult = "";
//        String result = instance.SaveTrail(trailName, userId, description);
//        DBMaster db = DBMaster.GetAnInstanceOfDBMaster();
//        Node node = db.RetrieveNode(Long.getLong(result));
//      
//      //  assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//
//    }
//
//    /**
//     * Test of SavePointFromJSON method, of class Trail.
//     */
//    @Test
//    public void testSavePointFromJSON() {
//        System.out.println("SavePointFromJSON");
//        JSONObject json = null;
//        String trailId = "";
//        Trail instance = new Trail();
//        instance.SavePointFromJSON(json, trailId);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of SavePoint method, of class Trail.
//     */
//    @Test
//    public void testSavePoint() {
//        System.out.println("SavePoint");
//        String latitude = "";
//        String longitude = "";
//        String lastPointId = "";
//        String trailId = "";
//        Trail instance = new Trail();
//        String expResult = "";
//        String result = instance.SavePoint(latitude, longitude, lastPointId, trailId);
//        //assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//      //  fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of SaveCommentForAnEntity method, of class Trail.
//     */
//    @Test
//    public void testSaveCommentForAnEntity() {
//        System.out.println("SaveCommentForAnEntity");
//        String UserId = "";
//        String EntityId = "";
//        String CommentText = "";
//        Trail instance = new Trail();
//        String expResult = "";
//        String result = instance.SaveCommentForAnEntity(UserId, EntityId, CommentText);
//        //assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetTrailAndCrumbs method, of class Trail.
//     */
//    @Test
//    public void testGetTrailAndCrumbs() {
//        System.out.println("GetTrailAndCrumbs");
//        String trailId = "";
//        Trail instance = new Trail();
//        String expResult = "";
//        String result = instance.GetTrailAndCrumbs(trailId);
//        //assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of FetchTrailsForGivenIds method, of class Trail.
//     */
//    @Test
//    public void testFetchTrailsForGivenIds() {
//        System.out.println("FetchTrailsForGivenIds");
//        String trailArrayList = "";
//        Trail instance = new Trail();
//        String expResult = "";
//        String result = instance.FetchTrailsForGivenIds(trailArrayList);
//        //assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetAllRelatedNodes method, of class Trail.
//     */
//    @Test
//    public void testGetAllRelatedNodes() {
//        System.out.println("GetAllRelatedNodes");
//        int baseNodeId = 0;
//        DBMaster.myLabels label = null;
//        String property = "";
//        Trail instance = new Trail();
//        String expResult = "";
//        String result = instance.GetAllRelatedNodes(baseNodeId, label, property);
//        //assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetAllTrailsForAUser method, of class Trail.
//     */
//    @Test
//    public void testGetAllTrailsForAUser() {
//        System.out.println("GetAllTrailsForAUser");
//        int id = 0;
//        Trail instance = new Trail();
//        String expResult = "";
//        String result = instance.GetAllTrailsForAUser(id);
//        //assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//       // fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of FindAllPinnedTrailsForAUser method, of class Trail.
//     */
//    @Test
//    public void testFindAllPinnedTrailsForAUser() {
//        System.out.println("FindAllPinnedTrailsForAUser");
//        String userId = "";
//        Trail instance = new Trail();
//        String expResult = "";
//        String result = instance.FindAllPinnedTrailsForAUser(userId);
//        //assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of PinTrailForUser method, of class Trail.
//     */
//    @Test
//    public void testPinTrailForUser() {
//        System.out.println("PinTrailForUser");
//        String UserId = "";
//        String TrailId = "";
//        Trail instance = new Trail();
//        instance.PinTrailForUser(UserId, TrailId);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of UnpintrailForUser method, of class Trail.
//     */
//    @Test
//    public void testUnpintrailForUser() {
//        System.out.println("UnpintrailForUser");
//        String UserId = "";
//        String TrailId = "";
//        Trail instance = new Trail();
//        instance.UnpintrailForUser(UserId, TrailId);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of SaveJSONOfTrailPoints method, of class Trail.
//     */
//    @Test
//    public void testSaveJSONOfTrailPoints() {
//        System.out.println("SaveJSONOfTrailPoints");
//        JSONObject trailPointsJSON = null;
//        Trail instance = new Trail();
//        String expResult = "";
//        String result = instance.SaveJSONOfTrailPoints(trailPointsJSON);
//        //assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of updateDistance method, of class Trail.
//     */
//    @Test
//    public void testUpdateDistance() {
//        System.out.println("updateDistance");
//        String trailId = "";
//        String latHeadString = "";
//        String lonHeadString = "";
//        String latBaseString = "";
//        String lonBaseString = "";
//        Trail instance = new Trail();
//        instance.updateDistance(trailId, latHeadString, lonHeadString, latBaseString, lonBaseString);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetAllTrailPointsForATrail method, of class Trail.
//     */
//    @Test
//    public void testGetAllTrailPointsForATrail() {
//        System.out.println("GetAllTrailPointsForATrail");
//        String trailId = "";
//        Trail instance = new Trail();
//        String expResult = "";
//        String result = instance.GetAllTrailPointsForATrail(trailId);
//        //assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of DeleteNodeAndRelationship method, of class Trail.
//     */
//    @Test
//    public void testDeleteNodeAndRelationship() {
//        System.out.println("DeleteNodeAndRelationship");
//        String trailId = "";
//        Trail instance = new Trail();
//        String expResult = "";
//        String result = instance.DeleteNodeAndRelationship(trailId);
//        //assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetAllTrails method, of class Trail.
//     */
//    @Test
//    public void testGetAllTrails() {
//        System.out.println("GetAllTrails");
//        Trail instance = new Trail();
//        String expResult = "";
//        String result = instance.GetAllTrails();
//       // assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//       // fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of Obliterate method, of class Trail.
//     */
//    @Test
//    public void testObliterate() {
//        System.out.println("Obliterate");
//        Trail instance = new Trail();
//        instance.Obliterate();
//        /// TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of SetCoverPhoto method, of class Trail.
//     */
//    @Test
//    public void testSetCoverPhoto() {
//        System.out.println("SetCoverPhoto");
//        String TrailId = "";
//        String ImageId = "";
//        Trail instance = new Trail();
//        instance.SetCoverPhoto(TrailId, ImageId);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of AddViewForATrail method, of class Trail.
//     */
//    @Test
//    public void testAddViewForATrail() {
//        System.out.println("AddViewForATrail");
//        String TrailId = "";
//        Trail instance = new Trail();
//        instance.AddViewForATrail(TrailId);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetNumberOfViewsForATrail method, of class Trail.
//     */
//    @Test
//    public void testGetNumberOfViewsForATrail() {
//        System.out.println("GetNumberOfViewsForATrail");
//        String TrailId = "";
//        Trail instance = new Trail();
//        String expResult = "";
//        String result = instance.GetNumberOfViewsForATrail(TrailId);
//        //assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of AddLike method, of class Trail.
//     */
//    @Test
//    public void testAddLike() {
//        System.out.println("AddLike");
//        String userId = "";
//        String entityId = "";
//        Trail instance = new Trail();
//        instance.AddLike(userId, entityId);
//        // TODO review the generated test code and remove the default call to fail.
//       // fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetNumberOfLikesForAnEntity method, of class Trail.
//     */
//    @Test
//    public void testGetNumberOfLikesForAnEntity() {
//        System.out.println("GetNumberOfLikesForAnEntity");
//        String entityId = "";
//        Trail instance = new Trail();
//        String expResult = "";
//        String result = instance.GetNumberOfLikesForAnEntity(entityId);
//       // assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetNumberOfCrumbsForATrail method, of class Trail.
//     */
//    @Test
//    public void testGetNumberOfCrumbsForATrail() {
//        System.out.println("GetNumberOfCrumbsForATrail");
//        String trailId = "";
//        Trail instance = new Trail();
//        String expResult = "";
//        String result = instance.GetNumberOfCrumbsForATrail(trailId);
//      //  assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setCoverPhotoId method, of class Trail.
//     */
//    @Test
//    public void testSetCoverPhotoId() {
//        System.out.println("setCoverPhotoId");
//        Node trail = null;
//        int crumbId = 0;
//        Trail instance = new Trail();
//        instance.setCoverPhotoId(trail, crumbId);
//        // TODO review the generated test code and remove the default call to fail.
//       // fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetAllCrumbIdsForATrail method, of class Trail.
//     */
//    @Test
//    public void testGetAllCrumbIdsForATrail() {
//        System.out.println("GetAllCrumbIdsForATrail");
//        String trailId = "";
//        Trail instance = new Trail();
//        String expResult = "";
//        String result = instance.GetAllCrumbIdsForATrail(trailId);
//        //assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetSimpleDetailsForATrail method, of class Trail.
//     */
//    @Test
//    public void testGetSimpleDetailsForATrail() {
//        System.out.println("GetSimpleDetailsForATrail");
//        String trailId = "";
//        Trail instance = new Trail();
//        String expResult = "";
//        String result = instance.GetSimpleDetailsForATrail(trailId);
//       // assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetAllTrailIds method, of class Trail.
//     */
//    @Test
//    public void testGetAllTrailIds() {
//        System.out.println("GetAllTrailIds");
//        Trail instance = new Trail();
//        String expResult = "";
//        String result = instance.GetAllTrailIds();
//        //assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//       // fail("The test case is a prototype.");
//    }
//    
//}
