package com.teamunemployment.breadcrumbs;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.test.RenamingDelegatingContext;

import com.teamunemployment.breadcrumbs.Album.data.Comment;
import com.teamunemployment.breadcrumbs.Album.data.FrameDetails;
import com.teamunemployment.breadcrumbs.Album.data.MimeDetails;
import com.teamunemployment.breadcrumbs.RESTApi.FileManager;
import com.teamunemployment.breadcrumbs.caching.TextCaching;
import com.teamunemployment.breadcrumbs.data.BreadcrumbsEncodedPolyline;
import com.teamunemployment.breadcrumbs.data.TripPath;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

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

    @Test
    public void TestThatWeCanLoadFrameDetailsFromCrumb() {
        databaseController.SaveCrumb("0123454657", "This is a test", "0", 1234567, 1234.123, 1234.1234, ".jpg", "1234", "sjht", "ccxfgdfasewr", "Birkdale", "auckland", "newZeand",0.1f, 0.2f);
        FrameDetails frameDetails = databaseController.GetFrameDetails("1234567");
        Assert.assertTrue(frameDetails.getChat().equals("This is a test"));
        Assert.assertTrue(frameDetails.getCity().equals("auckland"));
    }

    @Test
    public void TestThatWeCanHandleNotFindingFrameDetails() {
        FrameDetails frameDetails = databaseController.GetFrameDetails("-1234");
        Assert.assertTrue(frameDetails == null);
    }

    @Test
    public void TestThatWeCanLoadMimeDetailsFromLocal() {
        databaseController.SaveCrumb("112233", "This is a test", "0", 1234567, 1234.123, 1234.1234, ".jpg", "1234", "sjht", "ccxfgdfasewr", "Birkdale", "auckland", "newZeand",0.1f, 0.2f);
        databaseController.SaveCrumb("112233", "This is a test", "0", 12345678, 1234.123, 1234.1234, ".mp4", "1234", "sjht", "ccxfgdfasewr", "Birkdale", "auckland", "newZeand",0.1f, 0.2f);

        ArrayList<MimeDetails> mimeDetails = databaseController.LoadMimeDetails("112233");
        Assert.assertTrue(mimeDetails.get(0).getExtension().equals(".jpg"));
        Assert.assertTrue(mimeDetails.get(1).getExtension().equals(".mp4"));
        Assert.assertTrue(mimeDetails.get(0).getId().equals("1234567"));
        Assert.assertTrue(mimeDetails.get(1).getId().equals("12345678"));
    }

    @Test
    public void TestThatWeCanCreateNoMediaFolderIfItDoesNotExist() {
        FileManager fileManager = new FileManager(context);
        boolean doesFolderExist = fileManager.DoesOurHiddenFolderExist();
        if (!doesFolderExist) {
            fileManager.CreateOurHiddenFolder();
        }
        doesFolderExist = fileManager.DoesOurHiddenFolderExist();
        Assert.assertTrue(doesFolderExist);
    }

    @Test
    public void TestThatWeCanCreateFileUsingCaching() {
        TextCaching textCaching = new TextCaching(context);
        textCaching.CreateNoMediaFile();
    }

    @Test
    public void TestThatWeCanSaveAndFetchCommentObject() {
        Comment commentPreSave = new Comment();
        commentPreSave.setId("12345");
        commentPreSave.setEntityId("1");
        commentPreSave.setUserId("123");
        commentPreSave.setCommentText("This is a comment");
        long rst = databaseController.SaveComment(commentPreSave);
        assertTrue(rst != -1);
        Comment commentPostSave = databaseController.GetCommentById(commentPreSave.getId());
        assertTrue(commentPostSave != null);
        assertTrue(commentPostSave.getCommentText().equals(commentPreSave.getCommentText()));
    }

    @Test
    public void TestThatWeCanDeleteACommentObject() {
        Comment commentPreSave = new Comment();
        commentPreSave.setId("123455");
        commentPreSave.setEntityId("2");
        commentPreSave.setUserId("1234");
        commentPreSave.setCommentText("This is a comment");
        long rst = databaseController.SaveComment(commentPreSave);

        databaseController.DeleteComment(commentPreSave.getId());
        Comment nullComment = databaseController.GetCommentById(commentPreSave.getId());
        assertTrue(nullComment==null);
    }

    @Test
    public void TestThatWeCanLoadAllCommentsForAFrame() {
        Comment commentPreSave = new Comment();
        commentPreSave.setId("123455");
        commentPreSave.setEntityId("55");
        commentPreSave.setUserId("1234");
        commentPreSave.setCommentText("This is a comment");
        long rst = databaseController.SaveComment(commentPreSave);

        Comment commentPreSave1 = new Comment();
        commentPreSave1.setId("123455");
        commentPreSave1.setEntityId("55");
        commentPreSave1.setUserId("1234");
        commentPreSave1.setCommentText("This is a comment2");
        long rst1 = databaseController.SaveComment(commentPreSave1);

        Comment commentPreSave2 = new Comment();
        commentPreSave2.setId("123455");
        commentPreSave2.setEntityId("55");
        commentPreSave2.setUserId("1234");
        commentPreSave2.setCommentText("This is a comment3");
        long rst2 = databaseController.SaveComment(commentPreSave2);

        ArrayList<Comment> comments = databaseController.GetAllCommentsForAnAlbum(commentPreSave.getEntityId());
        assertTrue(comments.size() == 3);
    }

    @Test
    public void TestThatWeDoNotSaveTheSaveCommentTwice() {
        Comment commentPreSave = new Comment();
        commentPreSave.setId("1");
        commentPreSave.setEntityId("45");
        commentPreSave.setUserId("1234");
        commentPreSave.setCommentText("This is a comment");
        long rst = databaseController.SaveComment(commentPreSave);
        Comment commentPreSave2 = new Comment();
        commentPreSave2.setId("1");
        commentPreSave2.setEntityId("45");
        commentPreSave2.setUserId("1234");
        commentPreSave2.setCommentText("This is a comment");
        long rst2 = databaseController.SaveComment(commentPreSave);
        ArrayList<Comment> comments = databaseController.GetAllCommentsForAnAlbum(commentPreSave.getEntityId());
        assertTrue(comments.size() == 1);
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
