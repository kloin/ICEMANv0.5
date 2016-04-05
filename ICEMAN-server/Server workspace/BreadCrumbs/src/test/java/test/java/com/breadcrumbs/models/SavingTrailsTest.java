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
            JSONObject event = new JSONObject();
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
            events.put("6", restZone2);
            returnedMetadata = trailManager20.ProcessMetadata(events);
            System.out.println("working");
        }
        
        @Test
        public void TestTrailManagerCanSaveMetadataPolylinesTest() {
            String polylineResponse = returnedMetadata.GetEvents().get(1).GetPolyline().EncodedPolyline;
            Assert.assertTrue(polylineResponse != null);
        }
        
        @Test
        public void CheckThatWeCanSaveTypes() {
            int type = returnedMetadata.GetEvents().get(1).GetType();
            Assert.assertTrue(type == 0);
        }
        
        @Test
        public void CheckThatWeCanSaveMetaDataToTheDB() {
            RetrieveData retrieve = new RetrieveData();
            String id = retrieve.CreateNewUser("joe", "7873", "24", "m", "GCM", "fdsfd", "facebook");
            String trailId = retrieve.SaveTrail("test", "hey more testing", id);
            trailManager20.SaveMetadata(returnedMetadata, Integer.parseInt(trailId));
            String result = trailManager20.FetchMetadataFromTrail(trailId);
            JSONObject json = new JSONObject(result);
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                System.out.println("key :" + key + "result: " + json.get(key).toString());
            }
        }
        
        @Test
        public void SilyPointsPolylinesTest() {
            
            JSONObject mainJSON = new JSONObject();
            JSONObject wrapper = new JSONObject();
            wrapper.put("TrailId", "1234");
            wrapper.put("Events",   mainJSON);
            
            JSONObject metadata = new JSONObject();
            metadata.put("driving_method", "0");
            metadata.put("latitude", "0.0");
            metadata.put("longitude", "0.0");
            metadata.put("eventId", "1");
            metadata.put("type", "0");
            
            JSONObject metadata2 = new JSONObject();
            metadata2.put("driving_method", "0");
            metadata2.put("latitude", "0.0");
            metadata2.put("longitude", "0.0");
            metadata2.put("eventId", "2");
            metadata2.put("type", "0");
            
            mainJSON.put("0", metadata);
            mainJSON.put("1", metadata2);
            
            returnedMetadata = trailManager20.ProcessMetadata(mainJSON);
            System.out.println(returnedMetadata.GetEvents().get(1).GetPolyline().EncodedPolyline); 
        }
        
        @Test
        public void TestThatSavingWalkingAndDrivingDirectionsWorks() {
            
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
            JSONObject event = new JSONObject();
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
            event4.put("latitude", "-47.8648759");
            event4.put("longitude", "167.0460804");
            event4.put("eventId", "1");
            event4.put("type", "0");

            JSONObject event5 = new JSONObject();
            event5.put("driving_method", "0");
            event5.put("latitude", "-49.4667567");
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
            events.put("6", restZone2);
            returnedMetadata = trailManager20.ProcessMetadata(events);
            System.out.println("working");
        }
        
        
        
}
