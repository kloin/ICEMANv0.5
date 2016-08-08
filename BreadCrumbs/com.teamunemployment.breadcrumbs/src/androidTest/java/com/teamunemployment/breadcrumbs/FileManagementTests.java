package com.teamunemployment.breadcrumbs;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.test.RenamingDelegatingContext;

import com.teamunemployment.breadcrumbs.FileManager.MediaRecordModel;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the file management stuff.
 *
 * @author Josiah Kendall.
 */
public class FileManagementTests {
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
    public void TestThatWeCanSaveAndFetchTheRecord() {
        MediaRecordModel mediaRecordModel = new MediaRecordModel("1", 123.123f);
        databaseController.SaveMediaFileRecord(mediaRecordModel);

        MediaRecordModel mediaRecordModel1 = databaseController.GetMediaFileRecord(mediaRecordModel.getId());
        Assert.assertTrue(mediaRecordModel.getSize() == mediaRecordModel1.getSize());
    }

    @Test
    public void TestThatWeCanDeleteMediaRecord() {
        MediaRecordModel mediaRecordModel = new MediaRecordModel("1", 123.123f);
        databaseController.SaveMediaFileRecord(mediaRecordModel);

        MediaRecordModel mediaRecordModel1 = databaseController.GetMediaFileRecord(mediaRecordModel.getId());
        Assert.assertTrue(mediaRecordModel.getSize() == mediaRecordModel1.getSize());

        databaseController.DeleteMediaFileRecord(mediaRecordModel.getId());
        MediaRecordModel mediaRecordModel2 = databaseController.GetMediaFileRecord(mediaRecordModel.getId());
        Assert.assertTrue(mediaRecordModel2 == null);
    }


}
