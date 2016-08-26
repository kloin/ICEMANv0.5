package com.teamunemployment.breadcrumbs;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.test.RenamingDelegatingContext;

import com.teamunemployment.breadcrumbs.Album.Frame;
import com.teamunemployment.breadcrumbs.Album.data.FrameDetails;
import com.teamunemployment.breadcrumbs.Album.data.MimeDetails;
import com.teamunemployment.breadcrumbs.Album.repo.LocalAlbumRepo;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * @author Josiah Kendall
 */
public class AlbumLocalRepoTests {
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
    public void TestThatWeCanSaveFrame() {
        FrameDetails frameDetails = new FrameDetails();
        frameDetails.setCity("Auckland");
        frameDetails.setId("1");
        frameDetails.setChat("Test");
        frameDetails.setIcon("icon");
        frameDetails.setExtension(".mp4");
        frameDetails.setDescPosX("123");
        frameDetails.setDescPosY("1234");
        frameDetails.setCountry("NZ");
        frameDetails.setLatitude("1234");
        frameDetails.setLongitude("1234");
        frameDetails.setPlaceId("1234");

        databaseController.SaveFrameDetails(frameDetails);
        FrameDetails newFrameDetails = databaseController.GetFrameDetails(frameDetails.getId());
        Assert.assertTrue(newFrameDetails.getChat().equals("Test"));
        Assert.assertTrue(newFrameDetails.getDescPosX().equals("123"));
    }

    /**
     * We need to be able to save mimes as frames, because we download just the mime data for a frame.
     * as we need this before we get to saving
     */
    @Test
    public void TestThatWeCanSaveJustMimeData() {
        FrameDetails frameDetails = new FrameDetails();
        frameDetails.setId("2");
        frameDetails.setExtension(".mp4");

        databaseController.SaveFrameDetails(frameDetails);
        FrameDetails frameDetails1 = databaseController.GetFrameDetails(frameDetails.getId());
        Assert.assertTrue(frameDetails.getExtension().equals(frameDetails1.getExtension()));
        Assert.assertTrue(frameDetails1.getChat() == null);
    }

    @Test
    public void TestThatWeCanUpdateData() {
        FrameDetails frameDetails = new FrameDetails();
        frameDetails.setId("12345");
        frameDetails.setExtension(".mp4");

        databaseController.SaveFrameDetails(frameDetails);
        FrameDetails frameDetails1 = databaseController.GetFrameDetails(frameDetails.getId());
        Assert.assertTrue(frameDetails1.getChat() == null);
        frameDetails1.setChat("This is a test");
        databaseController.UpdateFrameDetails(frameDetails1);
        FrameDetails frameDetails2 = databaseController.GetFrameDetails(frameDetails.getId());
        Assert.assertTrue(frameDetails2.getChat().equals(frameDetails1.getChat()));

        frameDetails2.setChat("This is not a test");
        databaseController.UpdateFrameDetails(frameDetails2);
        FrameDetails frameDetails3 = databaseController.GetFrameDetails(frameDetails.getId());
        Assert.assertTrue(frameDetails3.getChat().equals(frameDetails2.getChat()));

    }
    @Test
    public void TestThatWeCanHandleNotFindingFrameDetails() {
        FrameDetails frameDetails = databaseController.GetFrameDetails("-1234");
        Assert.assertTrue(frameDetails == null);
    }

    @Test
    public void TestThatWeCanSaveFrameMimeList() {
        MimeDetails mimeDetails = new MimeDetails();
        mimeDetails.setExtension(".mp4");
        mimeDetails.setId("1");
        MimeDetails mimeDetails2 = new MimeDetails();
        mimeDetails2.setExtension(".mp4");
        mimeDetails2.setId("2");
        MimeDetails mimeDetails3 = new MimeDetails();
        mimeDetails3.setExtension(".mp4");
        mimeDetails3.setId("3");
        MimeDetails mimeDetails4 = new MimeDetails();
        mimeDetails4.setExtension(".mp4");
        mimeDetails4.setId("4");

        ArrayList<MimeDetails> mimes = new ArrayList<>();
        mimes.add(mimeDetails);
        mimes.add(mimeDetails2);
        mimes.add(mimeDetails3);
        mimes.add(mimeDetails4);

        LocalAlbumRepo localAlbumRepo = new LocalAlbumRepo(databaseController);
        localAlbumRepo.SaveFrameMimeData(mimes);


    }

}
