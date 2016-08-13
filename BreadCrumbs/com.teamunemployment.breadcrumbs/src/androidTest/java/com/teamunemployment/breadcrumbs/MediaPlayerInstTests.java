package com.teamunemployment.breadcrumbs;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.test.InstrumentationRegistry;
import android.test.RenamingDelegatingContext;

import com.teamunemployment.breadcrumbs.database.DatabaseController;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by jek40 on 13/08/2016.
 */
public class MediaPlayerInstTests {

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
    public void TestSimpleStart() {

        // NEEDS TO EXIST LOCALLY FOR TEST TO RUN
        String testPath = context.getExternalCacheDir().getAbsolutePath() + "/24718.mp4";
        // create wrapper
        MediaPlayerWrapper mediaPlayerWrapper = new MediaPlayerWrapper(new MediaPlayer());
        mediaPlayerWrapper.SetTrack(testPath);
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.INITIALIZED);
        mediaPlayerWrapper.Prepare();

        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARED ||
                        mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARING);
        mediaPlayerWrapper.Play();
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.STARTED);
    }

    @Test
    public void TestStartThenStopThenStart() {
        // NEEDS TO EXIST LOCALLY FOR TEST TO RUN
        String testPath = context.getExternalCacheDir().getAbsolutePath() + "/24718.mp4";
        // create wrapper
        MediaPlayerWrapper mediaPlayerWrapper = new MediaPlayerWrapper(new MediaPlayer());
        mediaPlayerWrapper.SetTrack(testPath);
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.INITIALIZED);
        mediaPlayerWrapper.Prepare();

        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARED ||
                mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARING);
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARED ||
                mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARING);
        mediaPlayerWrapper.Play();
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.STARTED);
        mediaPlayerWrapper.Stop();
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.STOPPED);
        mediaPlayerWrapper.Play();
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.STARTED);
    }

    @Test
    public void TestThatWeCanPauseThenPlayAgain() {
        // NEEDS TO EXIST LOCALLY FOR TEST TO RUN
        String testPath = context.getExternalCacheDir().getAbsolutePath() + "/24718.mp4";
        // create wrapper
        MediaPlayerWrapper mediaPlayerWrapper = new MediaPlayerWrapper(new MediaPlayer());
        mediaPlayerWrapper.SetTrack(testPath);
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.INITIALIZED);
        mediaPlayerWrapper.Prepare();

        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARED ||
                mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARING);
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARED ||
                mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARING);
        mediaPlayerWrapper.Play();
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.STARTED);
        mediaPlayerWrapper.Pause();
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PAUSED);
        mediaPlayerWrapper.Play();
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.STARTED);
    }

    @Test
    public void TestThatWeCanChangeTrack() {
        // NEEDS TO EXIST LOCALLY FOR TEST TO RUN
        String testPath = context.getExternalCacheDir().getAbsolutePath() + "/24718.mp4";

        // create wrapper
        MediaPlayerWrapper mediaPlayerWrapper = new MediaPlayerWrapper(new MediaPlayer());
        mediaPlayerWrapper.SetTrack(testPath);
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.INITIALIZED);
        mediaPlayerWrapper.Prepare();

        // Check that we have successfully prepared.
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARED ||
                mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARING);
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARED ||
                mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARING);

        // Play pause play
        mediaPlayerWrapper.Play();
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.STARTED);
        mediaPlayerWrapper.Pause();
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PAUSED);
        mediaPlayerWrapper.Play();
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.STARTED);

        // Change Track
        mediaPlayerWrapper.Stop();
        mediaPlayerWrapper.Reset();
        mediaPlayerWrapper.SetTrack(testPath);
        mediaPlayerWrapper.Prepare();
        mediaPlayerWrapper.Play();

        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.STARTED);
    }

    @Test(expected = IllegalStateException.class)
    public void TestThatChangingTrackWithoutResettingThrowsIllegalStateException() {
        // NEEDS TO EXIST LOCALLY FOR TEST TO RUN
        String testPath = context.getExternalCacheDir().getAbsolutePath() + "/24718.mp4";

        // create wrapper
        MediaPlayerWrapper mediaPlayerWrapper = new MediaPlayerWrapper(new MediaPlayer());
        mediaPlayerWrapper.SetTrack(testPath);
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.INITIALIZED);
        mediaPlayerWrapper.Prepare();

        // Check that we have successfully prepared.
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARED ||
                mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARING);
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARED ||
                mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARING);

        mediaPlayerWrapper.Play();
        mediaPlayerWrapper.SetTrack(testPath);
    }

    @Test(expected = IllegalStateException.class)
    public void TestChangingTrackWithoutResettingThrowsExceptionIllegalStateExceptionV2() {
        // NEEDS TO EXIST LOCALLY FOR TEST TO RUN
        String testPath = context.getExternalCacheDir().getAbsolutePath() + "/24718.mp4";

        // create wrapper
        MediaPlayerWrapper mediaPlayerWrapper = new MediaPlayerWrapper(new MediaPlayer());
        mediaPlayerWrapper.SetTrack(testPath);
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.INITIALIZED);
        mediaPlayerWrapper.Prepare();

        // Check that we have successfully prepared.
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARED ||
                mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARING);
        Assert.assertTrue(mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARED ||
                mediaPlayerWrapper.getCurrentState() == MediaPlayerWrapper.PREPARING);

        mediaPlayerWrapper.Play();
        mediaPlayerWrapper.Stop();
        mediaPlayerWrapper.SetTrack(testPath);
    }
}
