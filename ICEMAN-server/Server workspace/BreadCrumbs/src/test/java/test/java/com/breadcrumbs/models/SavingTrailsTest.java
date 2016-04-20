/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.test.java.com.breadcrumbs.models;

import com.breadcrumbs.heavylifting.TrailManager20;
import com.breadcrumbs.models.TrailMetadata;
import com.breadcrumbs.resource.RetrieveData;
import java.util.Iterator;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jek40
 */

public class SavingTrailsTest {
    
    private TrailManager20 trailManager20; 
    private TrailMetadata returnedMetadata;
	
    @Before
    public void setUp() throws Exception {
            trailManager20 = new TrailManager20();
            if (returnedMetadata == null) {
                processTestMetadata();
            }
    }       

	@After
	public void tearDown() throws Exception {
	
        }
        
        private void processTestMetadata() {
       /*     JSONObject mainJSON = new JSONObject();
            
            JSONObject wrapper = new JSONObject();
            wrapper.put("TrailId", "1234567");
            wrapper.put("Events",   mainJSON);
            
            JSONObject metadata = new JSONObject();
            metadata.put("driving_method", "0");
            metadata.put("latitude", "-44.9437402");
            metadata.put("longitude", "168.8378104");
            metadata.put("eventId", "1");
            metadata.put("type", "0");
            
            JSONObject metadata2 = new JSONObject();
            metadata2.put("driving_method", "0");
            metadata2.put("latitude", "-45.0180531");
            metadata2.put("longitude", "168.8378104");
            metadata2.put("eventId", "2");
            metadata2.put("type", "1");
            
            JSONObject metadata3 = new JSONObject();
            metadata3.put("driving_method", "0");
            metadata3.put("latitude", "-45.0375501");
            metadata3.put("longitude", "169.1944608");
            metadata3.put("eventId", "3");
            metadata3.put("type", "0");
            
            JSONObject metadata4 = new JSONObject();
            metadata4.put("driving_method", "0");
            metadata4.put("latitude", "-43.8648759");
            metadata4.put("longitude", "169.0460804");
            metadata4.put("eventId", "4");
            metadata4.put("type", "0");
            
            JSONObject metadata5 = new JSONObject();
            metadata5.put("driving_method", "0");
            metadata5.put("latitude", "-43.4667567");
            metadata5.put("longitude", "170.0178356");
            metadata5.put("eventId", "5");
            metadata5.put("type", "0");
            
            mainJSON.put("1", metadata);
            mainJSON.put("2", metadata2);
            mainJSON.put("3", metadata3);
            mainJSON.put("4", metadata4);
            mainJSON.put("5", metadata5);*/
            JSONObject wrapper = new JSONObject();
            JSONObject events = new JSONObject();
            wrapper.put("TrailId", "1234567");
            wrapper.put("Events", events);

 /*            Location location1 = new Location("gps");
        location1.setLatitude(-44.9437402);
        location1.setLongitude(168.8378104);
        Location location2 = new Location("gps");
        location1.setLatitude(-45.0180531);
        location1.setLongitude(168.9337654);
        Location location3 = new Location("gps");
        location1.setLatitude(-45.0375501);
        location1.setLongitude(169.1944608);*/
        
            /*
             First object - we are generally going to be standing at most places we take photos. I need
             to be able to figure out the transport method - this will come later.
              */
            /*JSONObject event = new JSONObject();
            event.put("driving_method", "0"); // Just for now
            event.put("latitude", "-44.9437402");
            event.put("longitude", "168.8378104");
            event.put("eventId", "0");
            event.put("type", "0");

            JSONObject event2 = new JSONObject();
            event2.put("driving_method", "0"); // Just for now
            event2.put("latitude", "-45.0180531");
            event2.put("longitude", "168.9337654");
            event2.put("eventId", "0");
            event2.put("type", "1");

            JSONObject event3 = new JSONObject();
            event3.put("driving_method", "0"); // Just for now
            event3.put("latitude", "-45.0375501");
            event3.put("longitude", "169.1944608");
            event3.put("eventId", "0");
            event3.put("type", "1");

            JSONObject event4 = new JSONObject();
            event4.put("driving_method", "0");
            event4.put("latitude", "-43.8648759");
            event4.put("longitude", "169.0460804");
            event4.put("eventId", "1");
            event4.put("type", "0");

            JSONObject event5 = new JSONObject();
            event5.put("driving_method", "0");
            event5.put("latitude", "-43.4667567");
            event5.put("longitude", "170.0178356");
            event5.put("eventId", "2");
            event5.put("type", "0");

            JSONObject restZone1 = new JSONObject();//-43.287581, 170.222576

            restZone1.put("driving_method", "0");
            restZone1.put("latitude", "-43.287581");
            restZone1.put("longitude", "170.222576");
            restZone1.put("eventId", "3");
            restZone1.put("type", "2");
            JSONObject restZone2 = new JSONObject();//-42.444079, 171.217216

            restZone2.put("driving_method", "0");
            restZone2.put("latitude", "-42.444079");
            restZone2.put("longitude", "171.217216");
            restZone2.put("eventId", "4");
            restZone2.put("type", "2");
            events.put("0", event);
            events.put("1", event2);
            events.put("2", event3);
            events.put("3", event4);
            events.put("4", event5);  
            events.put("5", restZone1);
            events.put("6", restZone2);*/
            String jsonString = "{\"0\":{\"latitude\":\"-36.79617049428431\",\"longitude\":\"174.7136550740327\",\"timeStamp\":\"2016-04-20T03:13:36.725Z\",\"trailId\":\"1\",\"eventId\":\"0\",\"type\":\"4\",\"id\":1,\"driving_method\":\"1\"},\"1\":{\"latitude\":\"-36.79627037292398\",\"longitude\":\"174.7132901095896\",\"timeStamp\":\"2016-04-20T03:13:43.332Z\",\"trailId\":\"1\",\"eventId\":\"0\",\"type\":\"4\",\"id\":2,\"driving_method\":\"1\"},\"2\":{\"latitude\":\"-36.79635223235293\",\"longitude\":\"174.71321911905173\",\"timeStamp\":\"2016-04-20T03:13:49.329Z\",\"trailId\":\"1\",\"eventId\":\"0\",\"type\":\"4\",\"id\":3,\"driving_method\":\"1\"},\"3\":{\"latitude\":\"-36.79642602934952\",\"longitude\":\"174.7131964639095\",\"timeStamp\":\"2016-04-20T03:13:55.329Z\",\"trailId\":\"1\",\"eventId\":\"0\",\"type\":\"4\",\"id\":4,\"driving_method\":\"1\"},\"4\":{\"latitude\":\"-36.7964897141922\",\"longitude\":\"174.71326261826863\",\"timeStamp\":\"2016-04-20T03:14:01.292Z\",\"trailId\":\"1\",\"eventId\":\"0\",\"type\":\"4\",\"id\":5,\"driving_method\":\"1\"},\"5\":{\"latitude\":\"-36.79605239437224\",\"longitude\":\"174.71299673487627\",\"timeStamp\":\"2016-04-20T03:14:16.301Z\",\"trailId\":\"1\",\"eventId\":\"0\",\"type\":\"4\",\"id\":6,\"driving_method\":\"1\"},\"6\":{\"latitude\":\"-36.7962752\",\"longitude\":\"174.7133922\",\"timeStamp\":\"2016-04-20T15:21:25.847+12:00\",\"trailId\":\"1\",\"eventId\":\"0\",\"type\":\"3\",\"id\":7,\"driving_method\":\"1\"}}";
            events = new JSONObject(jsonString);
            returnedMetadata = trailManager20.ProcessMetadata(events, 0);
            System.out.println("working");
        }
        
//        @Test
//        public void TestTrailManagerCanSaveMetadataPolylinesTest() {
//            String polylineResponse = returnedMetadata.GetEvents().get(1).GetPolyline().EncodedPolyline;
//            Assert.assertTrue(polylineResponse != null);
//        }
//        
//        @Test
//        public void CheckThatWeCanSaveTypes() {
//            int type = returnedMetadata.GetEvents().get(1).GetType();
//            //Assert.assertTrue(type == 0);
//        }
//        
//        @Test
//        public void CheckThatWeCanSaveMetaDataToTheDB() {
//            RetrieveData retrieve = new RetrieveData();
//            String id = retrieve.CreateNewUser("joe", "7873", "24", "m", "GCM", "fdsfd", "facebook");
//            String trailId = retrieve.SaveTrail("test", "hey more testing", id);
//            trailManager20.SaveMetadata(returnedMetadata, Integer.parseInt(trailId));
//            String result = trailManager20.FetchMetadataFromTrail(trailId);
//            JSONObject json = new JSONObject(result);
//            Iterator<String> keys = json.keys();
//            while (keys.hasNext()) {
//                String key = keys.next();
//                System.out.println("key :" + key + "result: " + json.get(key).toString());
//            }
//        }
//        
//        @Test
//        public void SilyPointsPolylinesTest() {
//            
//            JSONObject mainJSON = new JSONObject();
//            JSONObject wrapper = new JSONObject();
//            wrapper.put("TrailId", "1234");
//            wrapper.put("Events",   mainJSON);
//            
//            JSONObject metadata = new JSONObject();
//            metadata.put("driving_method", "0");
//            metadata.put("latitude", "0.0");
//            metadata.put("longitude", "0.0");
//            metadata.put("eventId", "1");
//            metadata.put("type", "0");
//            
//            JSONObject metadata2 = new JSONObject();
//            metadata2.put("driving_method", "0");
//            metadata2.put("latitude", "0.0");
//            metadata2.put("longitude", "0.0");
//            metadata2.put("eventId", "2");
//            metadata2.put("type", "0");
//            
//            mainJSON.put("0", metadata);
//            mainJSON.put("1", metadata2);
//            
//            returnedMetadata = trailManager20.ProcessMetadata(mainJSON, 0);
//            System.out.println(returnedMetadata.GetEvents().get(1).GetPolyline().EncodedPolyline); 
//        }
//        
//        @Test
//        public void TestThatSavingWalkingAndDrivingDirectionsWorks() {
//            
//          JSONObject wrapper = new JSONObject();
//            JSONObject events = new JSONObject();
//            wrapper.put("TrailId", "1234567");
//            wrapper.put("Events", events);
//
// /*            Location location1 = new Location("gps");
//        location1.setLatitude(-44.9437402);
//        location1.setLongitude(168.8378104);
//        Location location2 = new Location("gps");
//        location1.setLatitude(-45.0180531);
//        location1.setLongitude(168.9337654);
//        Location location3 = new Location("gps");
//        location1.setLatitude(-45.0375501);
//        location1.setLongitude(169.1944608);*/
//        
//            /*
//             First object - we are generally going to be standing at most places we take photos. I need
//             to be able to figure out the transport method - this will come later.
//              */
//            JSONObject event = new JSONObject();
//            event.put("driving_method", "0"); // Just for now
//            event.put("latitude", "-44.9437402");
//            event.put("longitude", "168.8378104");
//            event.put("timeStamp", "noew");
//            event.put("eventId", "0");
//            event.put("type", "0");
//
//            JSONObject event2 = new JSONObject();
//            event2.put("driving_method", "0"); // Just for now
//            event2.put("latitude", "-45.0180531");
//            event2.put("longitude", "168.9337654");
//            event2.put("timeStamp", "noew");
//            event2.put("eventId", "0");
//            event2.put("type", "1");
//
//            JSONObject event3 = new JSONObject();
//            event3.put("driving_method", "0"); // Just for now
//            event3.put("latitude", "-45.0375501");
//            event3.put("longitude", "169.1944608");
//            event3.put("eventId", "0");
//            event3.put("type", "1");
//            event3.put("timeStamp", "noew");
//
//            JSONObject event4 = new JSONObject();
//            event4.put("driving_method", "0");
//            event4.put("latitude", "-47.8648759");
//            event4.put("longitude", "167.0460804");
//            event4.put("eventId", "1");
//            event4.put("type", "0");
//            event4.put("timeStamp", "noew");
//
//            JSONObject event5 = new JSONObject();
//            event5.put("driving_method", "0");
//            event5.put("latitude", "-49.4667567");
//            event5.put("longitude", "170.0178356");
//            event5.put("eventId", "2");
//             event5.put("timeStamp", "noew");
//           
//            event5.put("type", "0");
//
//            JSONObject restZone1 = new JSONObject();//-43.287581, 170.222576
//            restZone1.put("timeStamp", "noew");
//            restZone1.put("driving_method", "0");
//            restZone1.put("latitude", "-43.287581");
//            restZone1.put("longitude", "170.222576");
//            restZone1.put("eventId", "3");
//            restZone1.put("type", "2");
//            JSONObject restZone2 = new JSONObject();//-42.444079, 171.217216
//            restZone2.put("timeStamp", "noew");
//
//            restZone2.put("driving_method", "0");
//            restZone2.put("latitude", "-42.444079");
//            restZone2.put("longitude", "171.217216");
//            restZone2.put("eventId", "4");
//            restZone2.put("type", "2");
//            events.put("0", event);
//            events.put("1", event2);
//            events.put("2", event3);
//            events.put("3", event4);
//            events.put("4", event5);  
//            events.put("5", restZone1);
//            events.put("6", restZone2);
//            returnedMetadata = trailManager20.ProcessMetadata(events, 0);
//            System.out.println("working");
//        }
//        
//        @Test
//        public void TestWeCanMapWalkingPolylines() {
//            JSONObject event = new JSONObject();
//            event.put("driving_method", "0"); // Just for now
//            event.put("latitude", "-39.302974");
//            event.put("longitude", "176.708501");
//            event.put("eventId", "0");
//            event.put("type", "3");
//            
//            // Our walking GPS event
//            JSONObject walkingEvent = new JSONObject();
//            walkingEvent.put("driving_method", "0");
//            walkingEvent.put("latitude", "-39.263688");
//            walkingEvent.put("longitude", "176.692364");
//            walkingEvent.put("eventId", "1");
//            walkingEvent.put("type", "4");
//             
//            JSONObject gps2 = new JSONObject();
//            gps2.put("driving_method", "1");
//            gps2.put("latitude", "-39.250156");
//            gps2.put("longitude", "176.707978");
//            gps2.put("eventId", "1");
//            gps2.put("type", "4");
//            // event change
//            JSONObject eventChange = new JSONObject();
//            eventChange.put("driving_method", "1");
//            eventChange.put("latitude", "-39.254256");
//            eventChange.put("longitude", "176.736919");
//            eventChange.put("eventId", "1");
//            eventChange.put("type", "5");
//            
//            JSONObject walkingEvent1 = new JSONObject();
//            walkingEvent1.put("driving_method", "1");
//            walkingEvent1.put("latitude", "-39.253900");
//            walkingEvent1.put("longitude", "176.741899");
//            walkingEvent1.put("eventId", "1");
//            walkingEvent1.put("type", "4");
//            
//             JSONObject walkingEvent2 = new JSONObject();
//            walkingEvent2.put("driving_method", "1");
//            walkingEvent2.put("latitude", "-39.255749");
//            walkingEvent2.put("longitude", "176.745133");
//            walkingEvent2.put("eventId", "1");
//            walkingEvent2.put("type", "4");
//            
//            JSONObject walkingEvent3 = new JSONObject();
//            walkingEvent3.put("driving_method", "1");
//            walkingEvent3.put("latitude", "-39.261117");
//            walkingEvent3.put("longitude", "176.751300");
//            walkingEvent3.put("eventId", "1");
//            walkingEvent3.put("type", "4");
//            
//            JSONObject walkingEvent4 = new JSONObject();
//            walkingEvent4.put("driving_method", "1");
//            walkingEvent4.put("latitude", "-39.261389");
//            walkingEvent4.put("longitude", "176.758212");
//            walkingEvent4.put("eventId", "1");
//            walkingEvent4.put("type", "4");
//            
//            JSONObject walkingEvent5 = new JSONObject();
//            walkingEvent5.put("driving_method", "1");
//            walkingEvent5.put("latitude", "-39.259891");
//            walkingEvent5.put("longitude", "176.762655");
//            walkingEvent5.put("eventId", "1");
//            walkingEvent5.put("type", "4");
//            
//            JSONObject walkingEvent6 = new JSONObject();
//            walkingEvent6.put("driving_method", "1");
//            walkingEvent6.put("latitude", "-39.261888");
//            walkingEvent6.put("longitude", "176.770343");
//            walkingEvent6.put("eventId", "1");
//            walkingEvent6.put("type", "4");
//            
//            JSONObject finishEvent = new JSONObject();
//            finishEvent.put("driving_method", "1");
//            finishEvent.put("latitude", "-39.261888");
//            finishEvent.put("longitude", "176.770343");
//            finishEvent.put("eventId", "1");
//            finishEvent.put("type", "3");
//            
//            JSONObject events = new JSONObject();
//            events.put("0", event);
//            events.put("1", walkingEvent);
//            events.put("2", walkingEvent1);
//            events.put("3", walkingEvent2);
//            events.put("4", walkingEvent3);
//            events.put("5", walkingEvent4);
//            events.put("6", walkingEvent5);
//            events.put("7", walkingEvent6);
//            events.put("8", finishEvent);
//
//            returnedMetadata = trailManager20.ProcessMetadata(events, 0);
//            System.out.println("working");
//            trailManager20.SaveMetadata(returnedMetadata, 123456);
//        }
        
        @Test
        public void TestThatWeCanDoMultisave() {
            JSONObject event = new JSONObject();
            event.put("driving_method", "0"); // Just for now
            event.put("latitude", "-39.302974");
            event.put("longitude", "176.708501");
            event.put("eventId", "0");
            event.put("type", "3");
            
            // Our walking GPS event
            JSONObject walkingEvent = new JSONObject();
            walkingEvent.put("driving_method", "0");
            walkingEvent.put("latitude", "-39.263688");
            walkingEvent.put("longitude", "176.692364");
            walkingEvent.put("eventId", "1");
            walkingEvent.put("type", "4");
             
            JSONObject gps2 = new JSONObject();
            gps2.put("driving_method", "1");
            gps2.put("latitude", "-39.250156");
            gps2.put("longitude", "176.707978");
            gps2.put("eventId", "1");
            gps2.put("type", "4");
            // event change
            JSONObject eventChange = new JSONObject();
            eventChange.put("driving_method", "1");
            eventChange.put("latitude", "-39.254256");
            eventChange.put("longitude", "176.736919");
            eventChange.put("eventId", "1");
            eventChange.put("type", "5");
            
            JSONObject walkingEvent1 = new JSONObject();
            walkingEvent1.put("driving_method", "1");
            walkingEvent1.put("latitude", "-39.253900");
            walkingEvent1.put("longitude", "176.741899");
            walkingEvent1.put("eventId", "1");
            walkingEvent1.put("type", "4");
            
             JSONObject walkingEvent2 = new JSONObject();
            walkingEvent2.put("driving_method", "1");
            walkingEvent2.put("latitude", "-39.255749");
            walkingEvent2.put("longitude", "176.745133");
            walkingEvent2.put("eventId", "1");
            walkingEvent2.put("type", "4");
            
            JSONObject walkingEvent3 = new JSONObject();
            walkingEvent3.put("driving_method", "1");
            walkingEvent3.put("latitude", "-39.261117");
            walkingEvent3.put("longitude", "176.751300");
            walkingEvent3.put("eventId", "1");
            walkingEvent3.put("type", "4");
            
            JSONObject walkingEvent4 = new JSONObject();
            walkingEvent4.put("driving_method", "1");
            walkingEvent4.put("latitude", "-39.261389");
            walkingEvent4.put("longitude", "176.758212");
            walkingEvent4.put("eventId", "1");
            walkingEvent4.put("type", "4");
            
            JSONObject walkingEvent5 = new JSONObject();
            walkingEvent5.put("driving_method", "1");
            walkingEvent5.put("latitude", "-39.259891");
            walkingEvent5.put("longitude", "176.762655");
            walkingEvent5.put("eventId", "1");
            walkingEvent5.put("type", "4");
            
            JSONObject walkingEvent6 = new JSONObject();
            walkingEvent6.put("driving_method", "1");
            walkingEvent6.put("latitude", "-39.261888");
            walkingEvent6.put("longitude", "176.770343");
            walkingEvent6.put("eventId", "1");
            walkingEvent6.put("type", "4");
            
            JSONObject finishEvent = new JSONObject();
            finishEvent.put("driving_method", "1");
            finishEvent.put("latitude", "-39.261888");
            finishEvent.put("longitude", "176.770343");
            finishEvent.put("eventId", "1");
            finishEvent.put("type", "3");
            
            JSONObject events = new JSONObject();
            events.put("0", event);
            events.put("1", walkingEvent);
            events.put("2", walkingEvent1);

            returnedMetadata = trailManager20.ProcessMetadata(events, 0);
            
            events = new JSONObject();
            events.put("3", walkingEvent2);
            events.put("4", walkingEvent3);
            events.put("5", walkingEvent4);
            events.put("6", walkingEvent5);
            events.put("7", walkingEvent6);
            System.out.println("working");
            returnedMetadata = trailManager20.ProcessMetadata(events, 3);

            trailManager20.SaveMetadata(returnedMetadata, 123456);
        }
       
        
        
        
}
