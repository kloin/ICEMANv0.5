package com.teamunemployment.breadcrumbs;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.test.RenamingDelegatingContext;

import com.teamunemployment.breadcrumbs.Trails.TrailManagerWorker;
import com.teamunemployment.breadcrumbs.caching.TextCaching;
import com.teamunemployment.breadcrumbs.data.BreadcrumbsEncodedPolyline;
import com.teamunemployment.breadcrumbs.data.TripPath;
import com.teamunemployment.breadcrumbs.data.source.SyncManager;
import com.teamunemployment.breadcrumbs.data.source.TripDataSource;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * @author Josiah Kendall
 *
 */
public class PersistanceLayer {

    private Context context;
    private DatabaseController databaseController;
    PreferencesAPI preferencesAPI;

    @Before
    public void Before() throws Exception {
        RenamingDelegatingContext context = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        databaseController = new DatabaseController(context);
        preferencesAPI = new PreferencesAPI(context);
        this.context = context;
    }

    @Test
    public void TestThatWeCanSavePolylines() {
        RenamingDelegatingContext context = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        BreadcrumbsEncodedPolyline encodedPolyline = new BreadcrumbsEncodedPolyline(true, "ABC");
        databaseController.SavePolyline(encodedPolyline, "1");
        TripPath path = databaseController.FetchTripPath("1");
        ArrayList<BreadcrumbsEncodedPolyline> encodedPolylines = path.getTripPolyline();
        Assert.assertTrue(encodedPolylines.size() == 1);
        Assert.assertTrue(encodedPolylines.get(0).isEncoded);
        Assert.assertTrue(encodedPolylines.get(0).polyline.equals("ABC"));  // Not worrying about safety / efficiency as it is a simple test.
    }

    @Test
    public void TestThatWeCanSaveWithHeadAndBaseLatitudes() {
        BreadcrumbsEncodedPolyline encodedPolyline = new BreadcrumbsEncodedPolyline(true, "ABC", 0.0, 1.1, 2.3, 4.4);
        BreadcrumbsEncodedPolyline encodedPolyline2 = new BreadcrumbsEncodedPolyline(true, "ABC", 0.0, 1.1, 2.3, 4.4);
        databaseController.SavePolyline(encodedPolyline, "1");

        TripPath path = databaseController.FetchTripPath("1");
        ArrayList<BreadcrumbsEncodedPolyline> encodedPolylines = path.getTripPolyline();
        Assert.assertTrue(encodedPolylines.size() == 1);
        Assert.assertTrue(encodedPolylines.get(0).baseLatitude == 0.0);
        Assert.assertTrue(encodedPolylines.get(0).baseLongitude == 1.1);
        Assert.assertTrue(encodedPolylines.get(0).headLatitude == 2.3);
        Assert.assertTrue(encodedPolylines.get(0).headLongitude == 4.4);
    }

    @Test
    public void TestFetchingActivityData() {
        // Not really sure what will happen with this test - I already have an active trail so it will probably pull that data out too.
        databaseController.SaveActivityPoint(0, 0, 0.0, 0.0,0);
        databaseController.SaveActivityPoint(0, 0, 0.0, 0.0,0);
        databaseController.SaveActivityPoint(0, 0, 0.0, 0.0,0);
        databaseController.SaveActivityPoint(0, 0, 0.0, 0.0,0);

        int localTrailId = preferencesAPI.GetLocalTrailId();
        JSONObject object = databaseController.GetAllActivityData(localTrailId, 0);

        Assert.assertTrue(object.length() == 4);

        databaseController.SaveActivityPoint(0, 0, 0.0, 0.0, 0);

        TextCaching textCaching = new TextCaching(context);
        String id = textCaching.FetchCachedText("TrailActivityIndex");
        JSONObject object1 = databaseController.GetAllActivityData(localTrailId, Integer.parseInt(id));

        // This is the real test that matters.
        Assert.assertTrue(object1.length() == 1);
    }

    @Test
    public void TestThatWeOnlyProcessDataOnce() {
        databaseController.SaveActivityPoint(0, 0, 0.0, 0.0,0);
        databaseController.SaveActivityPoint(0, 0, 0.0, 0.0,0);
        databaseController.SaveActivityPoint(0, 0, 0.0, 0.0,0);
        databaseController.SaveActivityPoint(0, 0, 0.0, 0.0,0);

        int localTrailId = preferencesAPI.GetLocalTrailId();
        JSONObject object = databaseController.GetAllActivityData(localTrailId, 0);

        Assert.assertTrue(object.length() > 0);

        databaseController.SaveActivityPoint(0, 0, 0.0, 0.0, 0);

    }

//    @Test
//    public void TestSyncWorks() {
//        SyncManager syncManager = new SyncManager();
//        TripDataSource.LoadTripPathCallback loadTripPathCallback = new TripDataSource.LoadTripPathCallback() {
//            @Override
//            public void onTripPathLoaded(TripPath tripPath) {
//                Assert.assertTrue(tripPath.getTripPolyline().get(1).polyline.equals("TEST"));
//            }
//        };
//
//        databaseController.SaveActivityPoint(0, 0, 0.0, 0.0,0);
//        databaseController.SaveActivityPoint(0, 0, 0.0, 0.0,0);
//        databaseController.SaveActivityPoint(0, 0, 0.0, 0.0,0);
//        databaseController.SaveActivityPoint(0, 0, 0.0, 0.0,0);
//
//        BreadcrumbsEncodedPolyline encodedPolyline = new BreadcrumbsEncodedPolyline(true, "ABC");
//        databaseController.SavePolyline(encodedPolyline, "1");
//        databaseController.SavePolyline(encodedPolyline, "1");
//
//        // Trip
//        syncManager.Sync(loadTripPathCallback, 1, databaseController, 0);
//    }
}
