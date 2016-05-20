/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.test.java.com.breadcrumbs.models;

import com.breadcrumbs.database.DBMaster;
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
    private RetrieveData retrieve;
    private DBMaster dbMaster;
    private String mTrailId;
    
    @Before
    public void setUp() throws Exception {
            trailManager20 = new TrailManager20();

            
            retrieve = new RetrieveData();
            dbMaster = DBMaster.GetAnInstanceOfDBMaster();
            
            
    }       

	@After
	public void tearDown() throws Exception {
	
        }
        
        // This just sets up the trail.
        @Test
        public void doInitialProcessingForTestFile() {
        
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
            String jsonString = "{\"0\":{\"latitude\":\"-36.7963085\",\"longitude\":\"174.7134145\",\"timeStamp\":\"2016-04-21T02:11:48.397Z\",\"trailId\":\"1\",\"eventId\":\"0\",\"type\":\"3\",\"id\":1,\"driving_method\":\"1\"},\"1\":{\"latitude\":\"-36.796423091946316\",\"longitude\":\"174.71337173899687\",\"timeStamp\":\"2016-04-21T02:13:26.509Z\",\"trailId\":\"1\",\"eventId\":\"1\",\"type\":\"4\",\"id\":2,\"driving_method\":\"1\"},\"2\":{\"latitude\":\"-36.796423091946316\",\"longitude\":\"174.71337173899687\",\"timeStamp\":\"2016-04-21T02:13:32.535Z\",\"trailId\":\"1\",\"eventId\":\"1\",\"type\":\"4\",\"id\":3,\"driving_method\":\"1\"},\"3\":{\"latitude\":\"-36.796333873448305\",\"longitude\":\"174.71336244168305\",\"timeStamp\":\"2016-04-21T02:13:38.523Z\",\"trailId\":\"1\",\"eventId\":\"1\",\"type\":\"4\",\"id\":4,\"driving_method\":\"1\"},\"4\":{\"latitude\":\"-36.796311384497926\",\"longitude\":\"174.71338160164987\",\"timeStamp\":\"2016-04-21T02:13:44.530Z\",\"trailId\":\"1\",\"eventId\":\"1\",\"type\":\"4\",\"id\":5,\"driving_method\":\"1\"},\"5\":{\"latitude\":\"-36.796269366972844\",\"longitude\":\"174.7133974877993\",\"timeStamp\":\"2016-04-21T02:13:50.519Z\",\"trailId\":\"1\",\"eventId\":\"1\",\"type\":\"4\",\"id\":6,\"driving_method\":\"1\"},\"6\":{\"latitude\":\"-36.796206982598854\",\"longitude\":\"174.71373418790188\",\"timeStamp\":\"2016-04-21T02:14:34.503Z\",\"trailId\":\"1\",\"eventId\":\"1\",\"type\":\"4\",\"id\":7,\"driving_method\":\"1\"},\"7\":{\"latitude\":\"-36.796206982598854\",\"longitude\":\"174.71373418790188\",\"timeStamp\":\"2016-04-21T02:14:42.522Z\",\"trailId\":\"1\",\"eventId\":\"1\",\"type\":\"4\",\"id\":8,\"driving_method\":\"1\"},\"8\":{\"latitude\":\"-36.79662261228176\",\"longitude\":\"174.71324680549827\",\"timeStamp\":\"2016-04-21T02:14:48.515Z\",\"trailId\":\"1\",\"eventId\":\"1\",\"type\":\"4\",\"id\":9,\"driving_method\":\"1\"},\"9\":{\"latitude\":\"-36.79654505062987\",\"longitude\":\"174.7132502632501\",\"timeStamp\":\"2016-04-21T02:14:54.516Z\",\"trailId\":\"1\",\"eventId\":\"1\",\"type\":\"4\",\"id\":10,\"driving_method\":\"1\"},\"10\":{\"latitude\":\"-36.79645647951594\",\"longitude\":\"174.71329717940344\",\"timeStamp\":\"2016-04-21T02:15:00.517Z\",\"trailId\":\"1\",\"eventId\":\"1\",\"type\":\"4\",\"id\":11,\"driving_method\":\"1\"},\"11\":{\"latitude\":\"-36.79646083640322\",\"longitude\":\"174.71334144966147\",\"timeStamp\":\"2016-04-21T02:15:06.511Z\",\"trailId\":\"1\",\"eventId\":\"1\",\"type\":\"4\",\"id\":12,\"driving_method\":\"1\"},\"12\":{\"latitude\":\"-36.79629587759351\",\"longitude\":\"174.71376486402917\",\"timeStamp\":\"2016-04-21T02:15:14.503Z\",\"trailId\":\"1\",\"eventId\":\"1\",\"type\":\"4\",\"id\":13,\"driving_method\":\"1\"},\"13\":{\"latitude\":\"-36.796233154925126\",\"longitude\":\"174.71374736794735\",\"timeStamp\":\"2016-04-21T02:15:20.511Z\",\"trailId\":\"1\",\"eventId\":\"1\",\"type\":\"4\",\"id\":14,\"driving_method\":\"1\"},\"14\":{\"latitude\":\"-36.79632088243085\",\"longitude\":\"174.71355765704445\",\"timeStamp\":\"2016-04-21T02:15:34.486Z\",\"trailId\":\"1\",\"eventId\":\"1\",\"type\":\"4\",\"id\":15,\"driving_method\":\"1\"},\"15\":{\"latitude\":\"-36.79643658363605\",\"longitude\":\"174.71307221405573\",\"timeStamp\":\"2016-04-21T02:15:49.525Z\",\"trailId\":\"1\",\"eventId\":\"1\",\"type\":\"4\",\"id\":16,\"driving_method\":\"1\"},\"16\":{\"latitude\":\"-36.79643658363605\",\"longitude\":\"174.71307221405573\",\"timeStamp\":\"Thu Apr 21 14:15:50 NZST 2016\",\"trailId\":\"1\",\"eventId\":\"2\",\"type\":\"2\",\"id\":17,\"driving_method\":\"1\"},\"17\":{\"latitude\":\"-36.79643658363605\",\"longitude\":\"174.71307221405573\",\"timeStamp\":\"2016-04-21T02:15:51.927Z\",\"trailId\":\"1\",\"eventId\":\"2\",\"type\":\"2\",\"id\":18,\"driving_method\":\"1\"},\"18\":{\"latitude\":\"-36.79645302042755\",\"longitude\":\"174.71304462610956\",\"timeStamp\":\"2016-04-21T02:15:55.536Z\",\"trailId\":\"1\",\"eventId\":\"3\",\"type\":\"4\",\"id\":19,\"driving_method\":\"1\"},\"19\":{\"latitude\":\"-36.79647090333336\",\"longitude\":\"174.71304498367073\",\"timeStamp\":\"2016-04-21T02:16:01.515Z\",\"trailId\":\"1\",\"eventId\":\"3\",\"type\":\"4\",\"id\":20,\"driving_method\":\"1\"},\"20\":{\"latitude\":\"-36.796460993530445\",\"longitude\":\"174.71305075227704\",\"timeStamp\":\"2016-04-21T02:16:07.513Z\",\"trailId\":\"1\",\"eventId\":\"3\",\"type\":\"4\",\"id\":21,\"driving_method\":\"1\"},\"21\":{\"latitude\":\"-36.79645048388682\",\"longitude\":\"174.71304366877663\",\"timeStamp\":\"2016-04-21T02:16:13.517Z\",\"trailId\":\"1\",\"eventId\":\"3\",\"type\":\"4\",\"id\":22,\"driving_method\":\"1\"},\"22\":{\"latitude\":\"-36.79640945395638\",\"longitude\":\"174.71307053538678\",\"timeStamp\":\"2016-04-21T02:16:19.535Z\",\"trailId\":\"1\",\"eventId\":\"3\",\"type\":\"4\",\"id\":23,\"driving_method\":\"1\"},\"23\":{\"latitude\":\"-36.79640126535283\",\"longitude\":\"174.71307233669495\",\"timeStamp\":\"2016-04-21T02:16:25.516Z\",\"trailId\":\"1\",\"eventId\":\"3\",\"type\":\"4\",\"id\":24,\"driving_method\":\"1\"},\"24\":{\"latitude\":\"-36.79639442414697\",\"longitude\":\"174.71306823411402\",\"timeStamp\":\"2016-04-21T02:16:31.530Z\",\"trailId\":\"1\",\"eventId\":\"3\",\"type\":\"4\",\"id\":25,\"driving_method\":\"1\"},\"25\":{\"latitude\":\"-36.7984184737115\",\"longitude\":\"174.71655097931577\",\"timeStamp\":\"2016-04-21T02:17:11.516Z\",\"trailId\":\"1\",\"eventId\":\"3\",\"type\":\"4\",\"id\":26,\"driving_method\":\"1\"},\"26\":{\"latitude\":\"-36.799903137731555\",\"longitude\":\"174.71890738093748\",\"timeStamp\":\"2016-04-21T02:17:30.556Z\",\"trailId\":\"1\",\"eventId\":\"3\",\"type\":\"4\",\"id\":27,\"driving_method\":\"1\"},\"27\":{\"latitude\":\"-36.79974109136337\",\"longitude\":\"174.7195072354121\",\"timeStamp\":\"2016-04-21T02:17:34.796Z\",\"trailId\":\"1\",\"eventId\":\"3\",\"type\":\"5\",\"id\":28,\"driving_method\":\"0\"},\"28\":{\"latitude\":\"-36.7732152\",\"longitude\":\"174.7401434\",\"timeStamp\":\"2016-04-21T02:31:14.176Z\",\"trailId\":\"1\",\"eventId\":\"4\",\"type\":\"3\",\"id\":29,\"driving_method\":\"1\"},\"29\":{\"latitude\":\"-36.7783001\",\"longitude\":\"174.7422183\",\"timeStamp\":\"2016-04-21T02:37:30.168Z\",\"trailId\":\"1\",\"eventId\":\"4\",\"type\":\"5\",\"id\":30,\"driving_method\":\"0\"},\"30\":{\"latitude\":\"-36.77810364894767\",\"longitude\":\"174.7424303371746\",\"timeStamp\":\"2016-04-21T02:37:37.925Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":31,\"driving_method\":\"0\"},\"31\":{\"latitude\":\"-36.778100107211515\",\"longitude\":\"174.74243167950019\",\"timeStamp\":\"2016-04-21T02:37:39.228Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":32,\"driving_method\":\"0\"},\"32\":{\"latitude\":\"-36.778097180517214\",\"longitude\":\"174.74247010255172\",\"timeStamp\":\"2016-04-21T02:37:40.265Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":33,\"driving_method\":\"0\"},\"33\":{\"latitude\":\"-36.77809641065281\",\"longitude\":\"174.74248923642298\",\"timeStamp\":\"2016-04-21T02:37:41.264Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":34,\"driving_method\":\"0\"},\"34\":{\"latitude\":\"-36.778104900527666\",\"longitude\":\"174.74250949229474\",\"timeStamp\":\"2016-04-21T02:37:42.282Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":35,\"driving_method\":\"0\"},\"35\":{\"latitude\":\"-36.77810645300707\",\"longitude\":\"174.74252301319652\",\"timeStamp\":\"2016-04-21T02:37:43.284Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":36,\"driving_method\":\"0\"},\"36\":{\"latitude\":\"-36.77810762152284\",\"longitude\":\"174.74253649268687\",\"timeStamp\":\"2016-04-21T02:37:44.270Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":37,\"driving_method\":\"0\"},\"37\":{\"latitude\":\"-36.77810732575351\",\"longitude\":\"174.74253654398248\",\"timeStamp\":\"2016-04-21T02:37:45.271Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":38,\"driving_method\":\"0\"},\"38\":{\"latitude\":\"-36.77811056705125\",\"longitude\":\"174.74253323043894\",\"timeStamp\":\"2016-04-21T02:37:46.282Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":39,\"driving_method\":\"0\"},\"39\":{\"latitude\":\"-36.77811056705125\",\"longitude\":\"174.74253323043894\",\"timeStamp\":\"2016-04-21T02:37:47.267Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":40,\"driving_method\":\"0\"},\"40\":{\"latitude\":\"-36.77811056705125\",\"longitude\":\"174.74253323043894\",\"timeStamp\":\"2016-04-21T02:37:48.252Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":41,\"driving_method\":\"0\"},\"41\":{\"latitude\":\"-36.77811477835909\",\"longitude\":\"174.7425476642744\",\"timeStamp\":\"2016-04-21T02:37:49.285Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":42,\"driving_method\":\"0\"},\"42\":{\"latitude\":\"-36.77812101854314\",\"longitude\":\"174.7425090870108\",\"timeStamp\":\"2016-04-21T02:37:50.250Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":43,\"driving_method\":\"0\"},\"43\":{\"latitude\":\"-36.77812283762455\",\"longitude\":\"174.7425004140573\",\"timeStamp\":\"2016-04-21T02:37:51.250Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":44,\"driving_method\":\"0\"},\"44\":{\"latitude\":\"-36.77813061237138\",\"longitude\":\"174.74249392051652\",\"timeStamp\":\"2016-04-21T02:37:52.256Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":45,\"driving_method\":\"0\"},\"45\":{\"latitude\":\"-36.778126948336826\",\"longitude\":\"174.74251159551488\",\"timeStamp\":\"2016-04-21T02:37:53.354Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":46,\"driving_method\":\"0\"},\"46\":{\"latitude\":\"-36.77811410576082\",\"longitude\":\"174.74251934201914\",\"timeStamp\":\"2016-04-21T02:37:54.309Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":47,\"driving_method\":\"0\"},\"47\":{\"latitude\":\"-36.77810999080209\",\"longitude\":\"174.74252157667314\",\"timeStamp\":\"2016-04-21T02:37:55.257Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":48,\"driving_method\":\"0\"},\"48\":{\"latitude\":\"-36.77811832879721\",\"longitude\":\"174.74253427989296\",\"timeStamp\":\"2016-04-21T02:37:56.258Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":49,\"driving_method\":\"0\"},\"49\":{\"latitude\":\"-36.77811870695896\",\"longitude\":\"174.74254540035068\",\"timeStamp\":\"2016-04-21T02:37:58.304Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":50,\"driving_method\":\"0\"},\"50\":{\"latitude\":\"-36.77811870695896\",\"longitude\":\"174.74254540035068\",\"timeStamp\":\"2016-04-21T02:37:59.264Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":51,\"driving_method\":\"0\"},\"51\":{\"latitude\":\"-36.778305587678936\",\"longitude\":\"174.7424797819291\",\"timeStamp\":\"2016-04-21T02:38:00.289Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":52,\"driving_method\":\"0\"},\"52\":{\"latitude\":\"-36.77830776750956\",\"longitude\":\"174.7424167794032\",\"timeStamp\":\"2016-04-21T02:38:01.292Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":53,\"driving_method\":\"0\"},\"53\":{\"latitude\":\"-36.77832444210601\",\"longitude\":\"174.74241167495043\",\"timeStamp\":\"2016-04-21T02:38:02.292Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":54,\"driving_method\":\"0\"},\"54\":{\"latitude\":\"-36.77838089186438\",\"longitude\":\"174.74236447305066\",\"timeStamp\":\"2016-04-21T02:38:03.331Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":55,\"driving_method\":\"0\"},\"55\":{\"latitude\":\"-36.778471272436356\",\"longitude\":\"174.7422494662529\",\"timeStamp\":\"2016-04-21T02:38:04.291Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":56,\"driving_method\":\"0\"},\"56\":{\"latitude\":\"-36.7785372646667\",\"longitude\":\"174.74214462758565\",\"timeStamp\":\"2016-04-21T02:38:05.311Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":57,\"driving_method\":\"0\"},\"57\":{\"latitude\":\"-36.77860595891172\",\"longitude\":\"174.74208998819438\",\"timeStamp\":\"2016-04-21T02:38:06.290Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":58,\"driving_method\":\"0\"},\"58\":{\"latitude\":\"-36.77867135641087\",\"longitude\":\"174.7420575953114\",\"timeStamp\":\"2016-04-21T02:38:07.286Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":59,\"driving_method\":\"0\"},\"59\":{\"latitude\":\"-36.77881663216994\",\"longitude\":\"174.74191024102737\",\"timeStamp\":\"2016-04-21T02:38:08.285Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":60,\"driving_method\":\"0\"},\"60\":{\"latitude\":\"-36.778940042218274\",\"longitude\":\"174.74178259783628\",\"timeStamp\":\"2016-04-21T02:38:09.283Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":61,\"driving_method\":\"0\"},\"61\":{\"latitude\":\"-36.77905582986845\",\"longitude\":\"174.74165978690078\",\"timeStamp\":\"2016-04-21T02:38:10.284Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":62,\"driving_method\":\"0\"},\"62\":{\"latitude\":\"-36.779135677296324\",\"longitude\":\"174.7415443741687\",\"timeStamp\":\"2016-04-21T02:38:11.286Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":63,\"driving_method\":\"0\"},\"63\":{\"latitude\":\"-36.77923310435397\",\"longitude\":\"174.74141552851324\",\"timeStamp\":\"2016-04-21T02:38:12.298Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"4\",\"id\":64,\"driving_method\":\"1\"},\"64\":{\"latitude\":\"-36.7824135\",\"longitude\":\"174.7375411\",\"timeStamp\":\"2016-04-21T02:39:01.722Z\",\"trailId\":\"1\",\"eventId\":\"5\",\"type\":\"5\",\"id\":65,\"driving_method\":\"0\"},\"65\":{\"latitude\":\"-36.7832309\",\"longitude\":\"174.7397484\",\"timeStamp\":\"Thu Apr 21 15:12:47 NZST 2016\",\"trailId\":\"1\",\"eventId\":\"7\",\"type\":\"2\",\"id\":66,\"driving_method\":\"1\"},\"66\":{\"latitude\":\"-36.7832309\",\"longitude\":\"174.7397484\",\"timeStamp\":\"2016-04-21T03:12:49.192Z\",\"trailId\":\"1\",\"eventId\":\"7\",\"type\":\"2\",\"id\":67,\"driving_method\":\"1\"},\"67\":{\"latitude\":\"-36.7832271\",\"longitude\":\"174.739804\",\"timeStamp\":\"Thu Apr 21 15:13:05 NZST 2016\",\"trailId\":\"1\",\"eventId\":\"9\",\"type\":\"2\",\"id\":68,\"driving_method\":\"1\"},\"68\":{\"latitude\":\"-36.7832271\",\"longitude\":\"174.739804\",\"timeStamp\":\"2016-04-21T03:13:06.706Z\",\"trailId\":\"1\",\"eventId\":\"9\",\"type\":\"2\",\"id\":69,\"driving_method\":\"1\"},\"69\":{\"latitude\":\"-36.7832316\",\"longitude\":\"174.7397944\",\"timeStamp\":\"Thu Apr 21 15:13:34 NZST 2016\",\"trailId\":\"1\",\"eventId\":\"11\",\"type\":\"2\",\"id\":70,\"driving_method\":\"1\"},\"70\":{\"latitude\":\"-36.7832316\",\"longitude\":\"174.7397944\",\"timeStamp\":\"2016-04-21T15:13:36.013+12:00\",\"trailId\":\"1\",\"eventId\":\"11\",\"type\":\"2\",\"id\":71,\"driving_method\":\"1\"},\"71\":{\"latitude\":\"-36.7832232\",\"longitude\":\"174.7397722\",\"timeStamp\":\"2016-04-21T15:13:49.796+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"3\",\"id\":72,\"driving_method\":\"1\"},\"72\":{\"latitude\":\"-36.78360481405067\",\"longitude\":\"174.7404675328914\",\"timeStamp\":\"2016-04-21T15:17:40.209+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":73,\"driving_method\":\"1\"},\"73\":{\"latitude\":\"-36.783388018076636\",\"longitude\":\"174.74012363540425\",\"timeStamp\":\"2016-04-21T15:17:46.268+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":74,\"driving_method\":\"1\"},\"74\":{\"latitude\":\"-36.78339626403229\",\"longitude\":\"174.73992501844978\",\"timeStamp\":\"2016-04-21T15:17:52.273+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":75,\"driving_method\":\"1\"},\"75\":{\"latitude\":\"-36.78332942621943\",\"longitude\":\"174.73995512722314\",\"timeStamp\":\"2016-04-21T15:17:58.278+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":76,\"driving_method\":\"1\"},\"76\":{\"latitude\":\"-36.78332942621943\",\"longitude\":\"174.73995512722314\",\"timeStamp\":\"2016-04-21T15:18:04.291+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":77,\"driving_method\":\"1\"},\"77\":{\"latitude\":\"-36.78314807086321\",\"longitude\":\"174.74005734851207\",\"timeStamp\":\"2016-04-21T15:18:10.298+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":78,\"driving_method\":\"1\"},\"78\":{\"latitude\":\"-36.78332716575423\",\"longitude\":\"174.7399108211589\",\"timeStamp\":\"2016-04-21T15:18:16.278+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":79,\"driving_method\":\"1\"},\"79\":{\"latitude\":\"-36.78332676364765\",\"longitude\":\"174.73996714448316\",\"timeStamp\":\"2016-04-21T15:18:22.275+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":80,\"driving_method\":\"1\"},\"80\":{\"latitude\":\"-36.783483672769904\",\"longitude\":\"174.73996397765794\",\"timeStamp\":\"2016-04-21T15:18:32.243+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":81,\"driving_method\":\"1\"},\"81\":{\"latitude\":\"-36.7833716666768\",\"longitude\":\"174.73999957668025\",\"timeStamp\":\"2016-04-21T15:18:38.261+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":82,\"driving_method\":\"1\"},\"82\":{\"latitude\":\"-36.78340999847406\",\"longitude\":\"174.73986408005055\",\"timeStamp\":\"2016-04-21T15:18:52.239+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":83,\"driving_method\":\"1\"},\"83\":{\"latitude\":\"-36.78345695398968\",\"longitude\":\"174.7400471146057\",\"timeStamp\":\"2016-04-21T15:18:58.264+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":84,\"driving_method\":\"1\"},\"84\":{\"latitude\":\"-36.783411422690115\",\"longitude\":\"174.73988342664012\",\"timeStamp\":\"2016-04-21T15:19:13.242+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":85,\"driving_method\":\"1\"},\"85\":{\"latitude\":\"-36.78361337011368\",\"longitude\":\"174.73974059006144\",\"timeStamp\":\"2016-04-21T15:19:19.261+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":86,\"driving_method\":\"1\"},\"86\":{\"latitude\":\"-36.78360600769751\",\"longitude\":\"174.73977816800718\",\"timeStamp\":\"2016-04-21T15:19:25.259+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":87,\"driving_method\":\"1\"},\"87\":{\"latitude\":\"-36.783783032912304\",\"longitude\":\"174.73971312013668\",\"timeStamp\":\"2016-04-21T15:19:31.280+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":88,\"driving_method\":\"1\"},\"88\":{\"latitude\":\"-36.78373117121077\",\"longitude\":\"174.73983430287578\",\"timeStamp\":\"2016-04-21T15:19:38.272+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":89,\"driving_method\":\"1\"},\"89\":{\"latitude\":\"-36.78359676217719\",\"longitude\":\"174.73995583501386\",\"timeStamp\":\"2016-04-21T15:19:50.278+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":90,\"driving_method\":\"1\"},\"90\":{\"latitude\":\"-36.78364615959394\",\"longitude\":\"174.73982455829045\",\"timeStamp\":\"2016-04-21T15:19:57.268+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":91,\"driving_method\":\"1\"},\"91\":{\"latitude\":\"-36.783651591137385\",\"longitude\":\"174.7398274192972\",\"timeStamp\":\"2016-04-21T15:20:05.252+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":92,\"driving_method\":\"1\"},\"92\":{\"latitude\":\"-36.783674611098476\",\"longitude\":\"174.73982948419672\",\"timeStamp\":\"2016-04-21T15:20:11.271+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":93,\"driving_method\":\"1\"},\"93\":{\"latitude\":\"-36.783862616775295\",\"longitude\":\"174.73988508830908\",\"timeStamp\":\"2016-04-21T15:20:17.267+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":94,\"driving_method\":\"1\"},\"94\":{\"latitude\":\"-36.78395587759466\",\"longitude\":\"174.7398403547556\",\"timeStamp\":\"2016-04-21T15:20:23.260+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":95,\"driving_method\":\"1\"},\"95\":{\"latitude\":\"-36.7839031777909\",\"longitude\":\"174.73985132041977\",\"timeStamp\":\"2016-04-21T15:20:29.254+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":96,\"driving_method\":\"1\"},\"96\":{\"latitude\":\"-36.783934958125045\",\"longitude\":\"174.7397706009031\",\"timeStamp\":\"2016-04-21T15:20:39.246+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":97,\"driving_method\":\"1\"},\"97\":{\"latitude\":\"-36.784279758996924\",\"longitude\":\"174.73941826769624\",\"timeStamp\":\"2016-04-21T15:20:45.260+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":98,\"driving_method\":\"1\"},\"98\":{\"latitude\":\"-36.78470436131939\",\"longitude\":\"174.73938848506657\",\"timeStamp\":\"2016-04-21T15:20:52.288+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":99,\"driving_method\":\"1\"},\"99\":{\"latitude\":\"-36.784807585239506\",\"longitude\":\"174.73929158143503\",\"timeStamp\":\"2016-04-21T15:20:58.258+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":100,\"driving_method\":\"1\"},\"100\":{\"latitude\":\"-36.78476837477612\",\"longitude\":\"174.73928864044035\",\"timeStamp\":\"2016-04-21T15:21:04.256+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":101,\"driving_method\":\"1\"},\"101\":{\"latitude\":\"-36.78448789446659\",\"longitude\":\"174.73921826045242\",\"timeStamp\":\"2016-04-21T15:21:13.248+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":102,\"driving_method\":\"1\"},\"102\":{\"latitude\":\"-36.784416903582134\",\"longitude\":\"174.73911836673128\",\"timeStamp\":\"2016-04-21T15:21:19.270+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":103,\"driving_method\":\"1\"},\"103\":{\"latitude\":\"-36.78436548377011\",\"longitude\":\"174.73918019835838\",\"timeStamp\":\"2016-04-21T15:21:25.252+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":104,\"driving_method\":\"1\"},\"104\":{\"latitude\":\"-36.78436102899164\",\"longitude\":\"174.73917404486951\",\"timeStamp\":\"2016-04-21T15:21:31.268+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":105,\"driving_method\":\"1\"},\"105\":{\"latitude\":\"-36.784412136609674\",\"longitude\":\"174.73921888423902\",\"timeStamp\":\"2016-04-21T15:21:38.254+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":106,\"driving_method\":\"1\"},\"106\":{\"latitude\":\"-36.78438934787957\",\"longitude\":\"174.73924004057307\",\"timeStamp\":\"2016-04-21T15:21:44.272+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":107,\"driving_method\":\"1\"},\"107\":{\"latitude\":\"-36.78440334748726\",\"longitude\":\"174.73907257237303\",\"timeStamp\":\"2016-04-21T15:21:50.259+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":108,\"driving_method\":\"1\"},\"108\":{\"latitude\":\"-36.784381809554674\",\"longitude\":\"174.73907174960985\",\"timeStamp\":\"2016-04-21T15:21:56.265+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":109,\"driving_method\":\"1\"},\"109\":{\"latitude\":\"-36.784339725769954\",\"longitude\":\"174.73887141893422\",\"timeStamp\":\"2016-04-21T15:22:02.262+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":110,\"driving_method\":\"1\"},\"110\":{\"latitude\":\"-36.783024901240935\",\"longitude\":\"174.73742849724667\",\"timeStamp\":\"2016-04-21T15:22:21.243+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":111,\"driving_method\":\"1\"},\"111\":{\"latitude\":\"-36.78279896382965\",\"longitude\":\"174.73677632163322\",\"timeStamp\":\"2016-04-21T15:22:27.268+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":112,\"driving_method\":\"1\"},\"112\":{\"latitude\":\"-36.78279556954423\",\"longitude\":\"174.7365655483794\",\"timeStamp\":\"2016-04-21T15:22:29.257+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":113,\"driving_method\":\"1\"},\"113\":{\"latitude\":\"-36.78283471459289\",\"longitude\":\"174.73663257354482\",\"timeStamp\":\"2016-04-21T15:22:30.256+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":114,\"driving_method\":\"1\"},\"114\":{\"latitude\":\"-36.78292341601114\",\"longitude\":\"174.73658342577986\",\"timeStamp\":\"2016-04-21T15:22:31.270+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":115,\"driving_method\":\"1\"},\"115\":{\"latitude\":\"-36.78299861685216\",\"longitude\":\"174.73652976223335\",\"timeStamp\":\"2016-04-21T15:22:32.273+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":116,\"driving_method\":\"1\"},\"116\":{\"latitude\":\"-36.78304038360337\",\"longitude\":\"174.73650789017108\",\"timeStamp\":\"2016-04-21T15:22:33.258+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":117,\"driving_method\":\"1\"},\"117\":{\"latitude\":\"-36.7830912225467\",\"longitude\":\"174.7364654851236\",\"timeStamp\":\"2016-04-21T15:22:34.260+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":118,\"driving_method\":\"1\"},\"118\":{\"latitude\":\"-36.78312315887333\",\"longitude\":\"174.73639477574565\",\"timeStamp\":\"2016-04-21T15:22:35.256+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":119,\"driving_method\":\"1\"},\"119\":{\"latitude\":\"-36.78315675204859\",\"longitude\":\"174.7363055096565\",\"timeStamp\":\"2016-04-21T15:22:36.280+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":120,\"driving_method\":\"1\"},\"120\":{\"latitude\":\"-36.78326475569378\",\"longitude\":\"174.73616510491752\",\"timeStamp\":\"2016-04-21T15:22:37.261+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":121,\"driving_method\":\"1\"},\"121\":{\"latitude\":\"-36.78334183725254\",\"longitude\":\"174.73606922934863\",\"timeStamp\":\"2016-04-21T15:22:38.264+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":122,\"driving_method\":\"1\"},\"122\":{\"latitude\":\"-36.783423122362045\",\"longitude\":\"174.73594759135275\",\"timeStamp\":\"2016-04-21T15:22:39.261+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":123,\"driving_method\":\"1\"},\"123\":{\"latitude\":\"-36.78347035376233\",\"longitude\":\"174.7358478719778\",\"timeStamp\":\"2016-04-21T15:22:40.278+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":124,\"driving_method\":\"1\"},\"124\":{\"latitude\":\"-36.78353184023651\",\"longitude\":\"174.7357611989873\",\"timeStamp\":\"2016-04-21T15:22:41.256+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":125,\"driving_method\":\"1\"},\"125\":{\"latitude\":\"-36.783601032522256\",\"longitude\":\"174.7356817887375\",\"timeStamp\":\"2016-04-21T15:22:42.265+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":126,\"driving_method\":\"1\"},\"126\":{\"latitude\":\"-36.783679349816566\",\"longitude\":\"174.73558574475456\",\"timeStamp\":\"2016-04-21T15:22:43.415+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":127,\"driving_method\":\"1\"},\"127\":{\"latitude\":\"-36.78376175393519\",\"longitude\":\"174.73547549873743\",\"timeStamp\":\"2016-04-21T15:22:44.296+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":128,\"driving_method\":\"1\"},\"128\":{\"latitude\":\"-36.783845809810224\",\"longitude\":\"174.73537539767688\",\"timeStamp\":\"2016-04-21T15:22:45.251+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":129,\"driving_method\":\"1\"},\"129\":{\"latitude\":\"-36.783925849204394\",\"longitude\":\"174.73527472904914\",\"timeStamp\":\"2016-04-21T15:22:46.252+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":130,\"driving_method\":\"1\"},\"130\":{\"latitude\":\"-36.78400216357849\",\"longitude\":\"174.73517623566417\",\"timeStamp\":\"2016-04-21T15:22:47.318+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":131,\"driving_method\":\"1\"},\"131\":{\"latitude\":\"-36.78407110519262\",\"longitude\":\"174.7350915101174\",\"timeStamp\":\"2016-04-21T15:22:48.293+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":132,\"driving_method\":\"1\"},\"132\":{\"latitude\":\"-36.78412861374365\",\"longitude\":\"174.73502232094418\",\"timeStamp\":\"2016-04-21T15:22:49.260+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":133,\"driving_method\":\"1\"},\"133\":{\"latitude\":\"-36.78418446066143\",\"longitude\":\"174.7349507906597\",\"timeStamp\":\"2016-04-21T15:22:50.257+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":134,\"driving_method\":\"1\"},\"134\":{\"latitude\":\"-36.78427768448905\",\"longitude\":\"174.73484574103958\",\"timeStamp\":\"2016-04-21T15:22:51.275+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":135,\"driving_method\":\"1\"},\"135\":{\"latitude\":\"-36.784376096426925\",\"longitude\":\"174.73473102546973\",\"timeStamp\":\"2016-04-21T15:22:52.259+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":136,\"driving_method\":\"1\"},\"136\":{\"latitude\":\"-36.78444306419269\",\"longitude\":\"174.73463156102932\",\"timeStamp\":\"2016-04-21T15:22:53.256+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":137,\"driving_method\":\"1\"},\"137\":{\"latitude\":\"-36.784536559620825\",\"longitude\":\"174.7345193985344\",\"timeStamp\":\"2016-04-21T15:22:54.273+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":138,\"driving_method\":\"1\"},\"138\":{\"latitude\":\"-36.7846330569809\",\"longitude\":\"174.73441589600202\",\"timeStamp\":\"2016-04-21T15:22:55.267+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":139,\"driving_method\":\"1\"},\"139\":{\"latitude\":\"-36.784737206969275\",\"longitude\":\"174.7343141436489\",\"timeStamp\":\"2016-04-21T15:22:56.269+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":140,\"driving_method\":\"1\"},\"140\":{\"latitude\":\"-36.7848311888326\",\"longitude\":\"174.73421622659365\",\"timeStamp\":\"2016-04-21T15:22:57.266+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":141,\"driving_method\":\"1\"},\"141\":{\"latitude\":\"-36.78492785616996\",\"longitude\":\"174.7341088249332\",\"timeStamp\":\"2016-04-21T15:22:58.263+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":142,\"driving_method\":\"1\"},\"142\":{\"latitude\":\"-36.78501927030784\",\"longitude\":\"174.7340036585111\",\"timeStamp\":\"2016-04-21T15:22:59.289+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":143,\"driving_method\":\"1\"},\"143\":{\"latitude\":\"-36.78511872085446\",\"longitude\":\"174.7338999444089\",\"timeStamp\":\"2016-04-21T15:23:00.266+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":144,\"driving_method\":\"1\"},\"144\":{\"latitude\":\"-36.7852372196872\",\"longitude\":\"174.733767398559\",\"timeStamp\":\"2016-04-21T15:23:01.269+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":145,\"driving_method\":\"1\"},\"145\":{\"latitude\":\"-36.7853012109595\",\"longitude\":\"174.73366963970054\",\"timeStamp\":\"2016-04-21T15:23:02.267+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":146,\"driving_method\":\"1\"},\"146\":{\"latitude\":\"-36.78536802019108\",\"longitude\":\"174.73355187368477\",\"timeStamp\":\"2016-04-21T15:23:03.302+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":147,\"driving_method\":\"1\"},\"147\":{\"latitude\":\"-36.78546963645013\",\"longitude\":\"174.73342626062922\",\"timeStamp\":\"2016-04-21T15:23:04.267+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":148,\"driving_method\":\"1\"},\"148\":{\"latitude\":\"-36.78561125229439\",\"longitude\":\"174.73332561458417\",\"timeStamp\":\"2016-04-21T15:23:05.268+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":149,\"driving_method\":\"1\"},\"149\":{\"latitude\":\"-36.785708234657584\",\"longitude\":\"174.7331372902014\",\"timeStamp\":\"2016-04-21T15:23:06.303+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":150,\"driving_method\":\"1\"},\"150\":{\"latitude\":\"-36.78580093744015\",\"longitude\":\"174.73299187527618\",\"timeStamp\":\"2016-04-21T15:23:07.267+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":151,\"driving_method\":\"1\"},\"151\":{\"latitude\":\"-36.78622069120029\",\"longitude\":\"174.73212960479157\",\"timeStamp\":\"2016-04-21T15:23:12.266+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":152,\"driving_method\":\"1\"},\"152\":{\"latitude\":\"-36.786315559059375\",\"longitude\":\"174.73202255700426\",\"timeStamp\":\"2016-04-21T15:23:13.273+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":153,\"driving_method\":\"1\"},\"153\":{\"latitude\":\"-36.786377166526194\",\"longitude\":\"174.7319524683188\",\"timeStamp\":\"2016-04-21T15:23:14.269+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":154,\"driving_method\":\"1\"},\"154\":{\"latitude\":\"-36.786472092198416\",\"longitude\":\"174.73187568394496\",\"timeStamp\":\"2016-04-21T15:23:15.275+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":155,\"driving_method\":\"1\"},\"155\":{\"latitude\":\"-36.78659493571311\",\"longitude\":\"174.73177528554476\",\"timeStamp\":\"2016-04-21T15:23:16.270+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":156,\"driving_method\":\"1\"},\"156\":{\"latitude\":\"-36.786687850736065\",\"longitude\":\"174.7316898095089\",\"timeStamp\":\"2016-04-21T15:23:17.273+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":157,\"driving_method\":\"1\"},\"157\":{\"latitude\":\"-36.786778936356875\",\"longitude\":\"174.73160016424976\",\"timeStamp\":\"2016-04-21T15:23:18.272+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":158,\"driving_method\":\"1\"},\"158\":{\"latitude\":\"-36.786868293442474\",\"longitude\":\"174.7315135713901\",\"timeStamp\":\"2016-04-21T15:23:19.271+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":159,\"driving_method\":\"1\"},\"159\":{\"latitude\":\"-36.78695044495206\",\"longitude\":\"174.7314251824199\",\"timeStamp\":\"2016-04-21T15:23:20.275+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":160,\"driving_method\":\"1\"},\"160\":{\"latitude\":\"-36.78705378313907\",\"longitude\":\"174.7313564369237\",\"timeStamp\":\"2016-04-21T15:23:21.273+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":161,\"driving_method\":\"1\"},\"161\":{\"latitude\":\"-36.78714131836049\",\"longitude\":\"174.7312568981164\",\"timeStamp\":\"2016-04-21T15:23:22.279+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":162,\"driving_method\":\"1\"},\"162\":{\"latitude\":\"-36.78722000594812\",\"longitude\":\"174.73115940838542\",\"timeStamp\":\"2016-04-21T15:23:23.276+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":163,\"driving_method\":\"1\"},\"163\":{\"latitude\":\"-36.78730343699034\",\"longitude\":\"174.73105237517555\",\"timeStamp\":\"2016-04-21T15:23:24.279+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":164,\"driving_method\":\"1\"},\"164\":{\"latitude\":\"-36.787391490325675\",\"longitude\":\"174.7309396810897\",\"timeStamp\":\"2016-04-21T15:23:25.287+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"4\",\"id\":165,\"driving_method\":\"1\"},\"165\":{\"latitude\":\"-36.787009\",\"longitude\":\"174.7313075\",\"timeStamp\":\"2016-04-21T15:23:38.107+12:00\",\"trailId\":\"1\",\"eventId\":\"12\",\"type\":\"5\",\"id\":166,\"driving_method\":\"0\"},\"166\":{\"latitude\":\"-36.79240926952555\",\"longitude\":\"174.72441280448933\",\"timeStamp\":\"2016-04-21T15:24:43.036+12:00\",\"trailId\":\"1\",\"eventId\":\"13\",\"type\":\"4\",\"id\":167,\"driving_method\":\"1\"},\"167\":{\"latitude\":\"-36.7992089\",\"longitude\":\"174.7237789\",\"timeStamp\":\"2016-04-21T15:25:58.734+12:00\",\"trailId\":\"1\",\"eventId\":\"13\",\"type\":\"5\",\"id\":168,\"driving_method\":\"0\"},\"168\":{\"latitude\":\"-36.79951067241539\",\"longitude\":\"174.7171801016124\",\"timeStamp\":\"2016-04-21T15:26:41.765+12:00\",\"trailId\":\"1\",\"eventId\":\"14\",\"type\":\"4\",\"id\":169,\"driving_method\":\"1\"},\"169\":{\"latitude\":\"-36.79689252580822\",\"longitude\":\"174.7139010101737\",\"timeStamp\":\"2016-04-21T15:27:13.239+12:00\",\"trailId\":\"1\",\"eventId\":\"14\",\"type\":\"5\",\"id\":170,\"driving_method\":\"0\"},\"170\":{\"latitude\":\"-36.7962747\",\"longitude\":\"174.7133922\",\"timeStamp\":\"Thu Apr 21 16:29:19 NZST 2016\",\"trailId\":\"1\",\"eventId\":\"16\",\"type\":\"2\",\"id\":171,\"driving_method\":\"1\"},\"171\":{\"latitude\":\"-36.7962747\",\"longitude\":\"174.7133922\",\"timeStamp\":\"2016-04-21T04:29:20.505Z\",\"trailId\":\"1\",\"eventId\":\"16\",\"type\":\"2\",\"id\":172,\"driving_method\":\"1\"},\"172\":{\"latitude\":\"-36.7962977\",\"longitude\":\"174.713412\",\"timeStamp\":\"Thu Apr 21 16:29:33 NZST 2016\",\"trailId\":\"1\",\"eventId\":\"18\",\"type\":\"2\",\"id\":173,\"driving_method\":\"1\"},\"173\":{\"latitude\":\"-36.7962977\",\"longitude\":\"174.713412\",\"timeStamp\":\"2016-04-21T04:29:34.754Z\",\"trailId\":\"1\",\"eventId\":\"18\",\"type\":\"2\",\"id\":174,\"driving_method\":\"1\"},\"174\":{\"latitude\":\"-36.7962977\",\"longitude\":\"174.713412\",\"timeStamp\":\"Thu Apr 21 16:29:40 NZST 2016\",\"trailId\":\"1\",\"eventId\":\"20\",\"type\":\"2\",\"id\":175,\"driving_method\":\"1\"},\"175\":{\"latitude\":\"-36.7962977\",\"longitude\":\"174.713412\",\"timeStamp\":\"2016-04-21T04:29:41.816Z\",\"trailId\":\"1\",\"eventId\":\"20\",\"type\":\"2\",\"id\":176,\"driving_method\":\"1\"},\"176\":{\"latitude\":\"-36.7963133\",\"longitude\":\"174.7134031\",\"timeStamp\":\"Thu Apr 21 16:29:56 NZST 2016\",\"trailId\":\"1\",\"eventId\":\"22\",\"type\":\"2\",\"id\":177,\"driving_method\":\"1\"},\"177\":{\"latitude\":\"-36.7963133\",\"longitude\":\"174.7134031\",\"timeStamp\":\"2016-04-21T04:29:57.232Z\",\"trailId\":\"1\",\"eventId\":\"22\",\"type\":\"2\",\"id\":178,\"driving_method\":\"1\"},\"178\":{\"latitude\":\"-36.796269\",\"longitude\":\"174.7133937\",\"timeStamp\":\"Thu Apr 21 16:54:58 NZST 2016\",\"trailId\":\"1\",\"eventId\":\"24\",\"type\":\"2\",\"id\":179,\"driving_method\":\"1\"},\"179\":{\"latitude\":\"-36.796269\",\"longitude\":\"174.7133937\",\"timeStamp\":\"2016-04-21T04:54:58.617Z\",\"trailId\":\"1\",\"eventId\":\"24\",\"type\":\"2\",\"id\":180,\"driving_method\":\"1\"},\"180\":{\"latitude\":\"-36.796269\",\"longitude\":\"174.7133937\",\"timeStamp\":\"2016-04-21T04:54:58.617Z\",\"trailId\":\"1\",\"eventId\":\"25\",\"type\":\"1\",\"id\":181,\"driving_method\":\"1\"}}";
            events = new JSONObject(jsonString);
            returnedMetadata = trailManager20.ProcessMetadata(events, 0, mTrailId);
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
           
        }
       
        
    @Test
    public void TestThatMetadatacanBeRetrievedWithCorrectOrder() {
        String id = retrieve.CreateNewUser("Josiah", "7873", "23", "M", "1", "testing@gmail.com", "1234567");
        mTrailId = retrieve.SaveTrail("OOOHRAA", "just testing yo", id);
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
            gps2.put("driving_method", "0");
            gps2.put("latitude", "-39.250156");
            gps2.put("longitude", "176.707978");
            gps2.put("eventId", "1");
            gps2.put("type", "4");
            // event change
            JSONObject eventChange = new JSONObject();
            eventChange.put("driving_method", "0");
            eventChange.put("latitude", "-39.254256");
            eventChange.put("longitude", "176.736919");
            eventChange.put("eventId", "1");
            eventChange.put("type", "5");
            
            JSONObject walkingEvent1 = new JSONObject();
            walkingEvent1.put("driving_method", "0");
            walkingEvent1.put("latitude", "-39.253900");
            walkingEvent1.put("longitude", "176.741899");
            walkingEvent1.put("eventId", "1");
            walkingEvent1.put("type", "4");
            
             JSONObject walkingEvent2 = new JSONObject();
            walkingEvent2.put("driving_method", "0");
            walkingEvent2.put("latitude", "-39.255749");
            walkingEvent2.put("longitude", "176.745133");
            walkingEvent2.put("eventId", "1");
            walkingEvent2.put("type", "4");
            
            JSONObject walkingEvent3 = new JSONObject();
            walkingEvent3.put("driving_method", "0");
            walkingEvent3.put("latitude", "-39.261117");
            walkingEvent3.put("longitude", "176.751300");
            walkingEvent3.put("eventId", "1");
            walkingEvent3.put("type", "4");
            
            JSONObject walkingEvent4 = new JSONObject();
            walkingEvent4.put("driving_method", "0");
            walkingEvent4.put("latitude", "-39.261389");
            walkingEvent4.put("longitude", "176.758212");
            walkingEvent4.put("eventId", "1");
            walkingEvent4.put("type", "4");
            
            JSONObject walkingEvent5 = new JSONObject();
            walkingEvent5.put("driving_method", "0");
            walkingEvent5.put("latitude", "-39.259891");
            walkingEvent5.put("longitude", "176.762655");
            walkingEvent5.put("eventId", "1");
            walkingEvent5.put("type", "4");
            
            JSONObject walkingEvent6 = new JSONObject();
            walkingEvent6.put("driving_method", "0");
            walkingEvent6.put("latitude", "-39.261888");
            walkingEvent6.put("longitude", "176.770343");
            walkingEvent6.put("eventId", "1");
            walkingEvent6.put("type", "4");
            
            JSONObject finishEvent = new JSONObject();
            finishEvent.put("driving_method", "0");
            finishEvent.put("latitude", "-39.261888");
            finishEvent.put("longitude", "176.770343");
            finishEvent.put("eventId", "1");
            finishEvent.put("type", "3");
            
            JSONObject events = new JSONObject();
            events.put("0", event);
            events.put("1", walkingEvent);
            events.put("2", walkingEvent1);
            events.put("3", walkingEvent2);
            events.put("4", walkingEvent3);
            events.put("5", walkingEvent4);
            events.put("6", walkingEvent5);
            events.put("7", walkingEvent6);
            events.put("8", finishEvent);
            returnedMetadata = trailManager20.ProcessMetadata(events, 0, mTrailId);
            
            events = new JSONObject();
            events.put("3", walkingEvent2);
            events.put("4", walkingEvent3);
            events.put("5", walkingEvent4);
            events.put("6", walkingEvent5);
            events.put("7", walkingEvent6);
            System.out.println("working");
            returnedMetadata = trailManager20.ProcessMetadata(events, 3, mTrailId);

            trailManager20.SaveMetadata(returnedMetadata, Integer.parseInt(mTrailId));
        JSONObject metadata = new JSONObject(trailManager20.FetchMetadataFromTrail(mTrailId));
        System.out.println(metadata.toString());
        
    }
        
}
