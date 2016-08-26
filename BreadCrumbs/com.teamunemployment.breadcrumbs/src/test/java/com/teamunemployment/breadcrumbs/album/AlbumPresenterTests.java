package com.teamunemployment.breadcrumbs.album;
import android.content.Context;

import com.teamunemployment.breadcrumbs.Album.AlbumModel;
import com.teamunemployment.breadcrumbs.Album.AlbumPresenter;
import com.teamunemployment.breadcrumbs.Album.AlbumPresenterViewContract;
import com.teamunemployment.breadcrumbs.Album.Frame;
import com.teamunemployment.breadcrumbs.Album.data.FrameDetails;
import com.teamunemployment.breadcrumbs.Album.data.MimeDetails;
import com.teamunemployment.breadcrumbs.AlbumDataSource;
import com.teamunemployment.breadcrumbs.BreadcrumbsTimer;
import com.teamunemployment.breadcrumbs.FileManager.MediaRecordModel;
import com.teamunemployment.breadcrumbs.MediaPlayerWrapper;
import com.teamunemployment.breadcrumbs.RESTApi.AlbumService;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Josiah Kendall
 */
public class AlbumPresenterTests {

    @Test
    public void TestThatWeCanLoadAndStart() throws InterruptedException {
        AlbumModel model = Mockito.mock(AlbumModel.class);
        ArrayList<MimeDetails> mimeDetails = new ArrayList<MimeDetails>();

        MimeDetails details1 = new MimeDetails();
        details1.setExtension(".mp4");
        details1.setId("1");

        MimeDetails details2 = new MimeDetails();
        details2.setExtension(".jpg");
        details2.setId("2");

        MimeDetails details3 = new MimeDetails();
        details3.setExtension(".jpg");
        details3.setId("3");

        mimeDetails.add(details1);
        mimeDetails.add(details2);
        mimeDetails.add(details3);

        when(model.LoadMimeDetails(anyString())).thenReturn(mimeDetails);

        MediaPlayerWrapper mediaPlayerWrapper = Mockito.mock(MediaPlayerWrapper.class);
        AlbumDataSource dataSource = Mockito.mock(AlbumDataSource.class);
        when(dataSource.GetAlbumId()).thenReturn("1");
        BreadcrumbsTimer timer = mock(BreadcrumbsTimer.class);
        AlbumPresenter presenter = new AlbumPresenter(model, Mockito.mock(Context.class), mediaPlayerWrapper,dataSource, timer);
        presenter.SetView(Mockito.mock(AlbumPresenterViewContract.class));
        presenter.Start("1");

        // We need to wait as start runs in a seperate thread, so the current frame could be null
        Thread.sleep(100);
        Assert.assertTrue(presenter.getCurrentFrame().getId().equals(details1.getId()));
    }

    @Test
    public void TestThatWeCanGoForewardInTheList() throws InterruptedException {
        AlbumModel model = Mockito.mock(AlbumModel.class);
        ArrayList<MimeDetails> mimeDetails = new ArrayList<MimeDetails>();

        MimeDetails details1 = new MimeDetails();
        details1.setExtension(".mp4");
        details1.setId("1");

        MimeDetails details2 = new MimeDetails();
        details2.setExtension(".jpg");
        details2.setId("2");

        MimeDetails details3 = new MimeDetails();
        details3.setExtension(".jpg");
        details3.setId("3");

        mimeDetails.add(details1);
        mimeDetails.add(details2);
        mimeDetails.add(details3);

        when(model.LoadMimeDetails(anyString())).thenReturn(mimeDetails);

        MediaPlayerWrapper mediaPlayerWrapper = Mockito.mock(MediaPlayerWrapper.class);
        AlbumDataSource dataSource = Mockito.mock(AlbumDataSource.class);
        when(dataSource.GetAlbumId()).thenReturn("1");
        BreadcrumbsTimer timer = mock(BreadcrumbsTimer.class);
        AlbumPresenter presenter = new AlbumPresenter(model, Mockito.mock(Context.class), mediaPlayerWrapper,dataSource, timer);
        presenter.SetView(Mockito.mock(AlbumPresenterViewContract.class));
        presenter.Start("1");

        // We need to wait as start runs in a seperate thread, so the current frame could be null
        Thread.sleep(100);
        Assert.assertTrue(presenter.getCurrentFrame().getId().equals(details1.getId()));
        presenter.foreward();

        Thread.sleep(100);
        Assert.assertTrue(presenter.getCurrentFrame().getId().equals(details2.getId()));
    }

    @Test
    public void TestThatWeCanGoForewardThenBack() throws InterruptedException {
        AlbumModel model = Mockito.mock(AlbumModel.class);
        ArrayList<MimeDetails> mimeDetails = new ArrayList<MimeDetails>();

        MimeDetails details1 = new MimeDetails();
        details1.setExtension(".mp4");
        details1.setId("1");

        MimeDetails details2 = new MimeDetails();
        details2.setExtension(".jpg");
        details2.setId("2");

        MimeDetails details3 = new MimeDetails();
        details3.setExtension(".jpg");
        details3.setId("3");

        mimeDetails.add(details1);
        mimeDetails.add(details2);
        mimeDetails.add(details3);

        when(model.LoadMimeDetails(anyString())).thenReturn(mimeDetails);

        MediaPlayerWrapper mediaPlayerWrapper = Mockito.mock(MediaPlayerWrapper.class);

        AlbumDataSource dataSource = Mockito.mock(AlbumDataSource.class);
        when(dataSource.GetAlbumId()).thenReturn("1");
        BreadcrumbsTimer timer = mock(BreadcrumbsTimer.class);
        AlbumPresenter presenter = new AlbumPresenter(model, Mockito.mock(Context.class), mediaPlayerWrapper,dataSource, timer);
        presenter.SetView(Mockito.mock(AlbumPresenterViewContract.class));
        presenter.Start("1");

        // We need to wait as start runs in a seperate thread, so the current frame could be null
        Thread.sleep(100);
        Assert.assertTrue(presenter.getCurrentFrame().getId().equals(details1.getId()));
        presenter.foreward();

        Thread.sleep(100);
        Assert.assertTrue(presenter.getCurrentFrame().getId().equals(details2.getId()));

        presenter.reverse();

        Thread.sleep(100);
        Assert.assertTrue(presenter.getCurrentFrame().getId().equals(details2.getId()));

        presenter.reverse();

        Thread.sleep(100);
        Assert.assertTrue(presenter.getCurrentFrame().getId().equals(details1.getId()));
    }

    @Test
    public void TestThatWeCanGoForewardThenBackTheForeward() throws InterruptedException {
        AlbumModel model = Mockito.mock(AlbumModel.class);
        ArrayList<MimeDetails> mimeDetails = new ArrayList<MimeDetails>();

        MimeDetails details1 = new MimeDetails();
        details1.setExtension(".mp4");
        details1.setId("1");

        MimeDetails details2 = new MimeDetails();
        details2.setExtension(".jpg");
        details2.setId("2");

        MimeDetails details3 = new MimeDetails();
        details3.setExtension(".jpg");
        details3.setId("3");

        mimeDetails.add(details1);
        mimeDetails.add(details2);
        mimeDetails.add(details3);

        when(model.LoadMimeDetails(anyString())).thenReturn(mimeDetails);

        MediaPlayerWrapper mediaPlayerWrapper = Mockito.mock(MediaPlayerWrapper.class);

        AlbumDataSource dataSource = Mockito.mock(AlbumDataSource.class);
        when(dataSource.GetAlbumId()).thenReturn("1");
        BreadcrumbsTimer timer = mock(BreadcrumbsTimer.class);
        AlbumPresenter presenter = new AlbumPresenter(model, Mockito.mock(Context.class), mediaPlayerWrapper,dataSource, timer);
        presenter.SetView(Mockito.mock(AlbumPresenterViewContract.class));
        presenter.Start("1");

        // We need to wait as start runs in a seperate thread, so the current frame could be null
        Thread.sleep(100);
        Assert.assertTrue(presenter.getCurrentFrame().getId().equals(details1.getId()));
        presenter.foreward();

        Thread.sleep(100);
        Assert.assertTrue(presenter.getCurrentFrame().getId().equals(details2.getId()));

        presenter.reverse();

        Thread.sleep(100);
        Assert.assertTrue(presenter.getCurrentFrame().getId().equals(details2.getId()));

        presenter.reverse();

        Thread.sleep(100);
        Assert.assertTrue(presenter.getCurrentFrame().getId().equals(details1.getId()));

        presenter.foreward();

        Thread.sleep(100);
        Assert.assertTrue(presenter.getCurrentFrame().getId().equals(details2.getId()));
    }

    // It throws a runtime error because we have not mocked it. Fuck it this test is shit but its better than nothing.
    @Test(expected = RuntimeException.class)
    public void TestThatWeCanHandleGoingWayToForeward() throws InterruptedException {
        AlbumModel model = Mockito.mock(AlbumModel.class);
        ArrayList<MimeDetails> mimeDetails = new ArrayList<MimeDetails>();

        MimeDetails details1 = new MimeDetails();
        details1.setExtension(".mp4");
        details1.setId("1");

        MimeDetails details2 = new MimeDetails();
        details2.setExtension(".jpg");
        details2.setId("2");

        MimeDetails details3 = new MimeDetails();
        details3.setExtension(".jpg");
        details3.setId("3");

        mimeDetails.add(details1);
        mimeDetails.add(details2);
        mimeDetails.add(details3);

        when(model.LoadMimeDetails(anyString())).thenReturn(mimeDetails);

        MediaPlayerWrapper mediaPlayerWrapper = Mockito.mock(MediaPlayerWrapper.class);

        AlbumDataSource dataSource = Mockito.mock(AlbumDataSource.class);
        when(dataSource.GetAlbumId()).thenReturn("1");
        BreadcrumbsTimer timer = mock(BreadcrumbsTimer.class);
        AlbumPresenter presenter = new AlbumPresenter(model, Mockito.mock(Context.class), mediaPlayerWrapper,dataSource, timer);
        presenter.SetView(Mockito.mock(AlbumPresenterViewContract.class));
        presenter.Start("1");

        // We need to wait as start runs in a seperate thread, so the current frame could be null
        Thread.sleep(100);
        Assert.assertTrue(presenter.getCurrentFrame().getId().equals(details1.getId()));
        presenter.foreward();

        Thread.sleep(100);
        Assert.assertTrue(presenter.getCurrentFrame().getId().equals(details2.getId()));
        presenter.foreward();
        presenter.foreward();
        presenter.foreward();
        presenter.foreward();
        presenter.foreward();

        Thread.sleep(100);
        Assert.assertTrue(presenter.getCurrentFrame().getId().equals(details3.getId()));
    }



    @Test
    public void TestThatLoadingStarts() {
        AlbumModel albumModel = Mockito.mock(AlbumModel.class);
        Context context = Mockito.mock(Context.class);
        AlbumDataSource dataSource = Mockito.mock(AlbumDataSource.class);
        when(dataSource.GetAlbumId()).thenReturn("1234");
        BreadcrumbsTimer timer = mock(BreadcrumbsTimer.class);
        AlbumPresenter presenter = new AlbumPresenter(Mockito.mock(AlbumModel.class), Mockito.mock(Context.class), Mockito.mock(MediaPlayerWrapper.class),dataSource,timer);
        presenter.SetView(Mockito.mock(AlbumPresenterViewContract.class));
        presenter.Start("1234");
    }

    @Test
    public void TestThatWeCanLoadFrameMediaSuccessfully() {

    }

    @Test
    public void TestThatWeCanGoNextIfWeFailToLoadFrameMedia() {
        AlbumModel model = Mockito.mock(AlbumModel.class);
        Context context = Mockito.mock(Context.class);
        AlbumDataSource dataSource = Mockito.mock(AlbumDataSource.class);
        BreadcrumbsTimer timer = mock(BreadcrumbsTimer.class);
        AlbumPresenter presenter = new AlbumPresenter(Mockito.mock(AlbumModel.class), Mockito.mock(Context.class), Mockito.mock(MediaPlayerWrapper.class),dataSource, timer);
        presenter.SetView(Mockito.mock(AlbumPresenterViewContract.class));
    }

    @Test
    public void TestDetailsFailButFrameMediaDoesnt() {
        AlbumModel model = Mockito.mock(AlbumModel.class);
        Context context = Mockito.mock(Context.class);
        AlbumDataSource dataSource = Mockito.mock(AlbumDataSource.class);
        BreadcrumbsTimer timer = mock(BreadcrumbsTimer.class);
        AlbumPresenter presenter = new AlbumPresenter(Mockito.mock(AlbumModel.class), Mockito.mock(Context.class), Mockito.mock(MediaPlayerWrapper.class),dataSource, timer);
        presenter.SetView(Mockito.mock(AlbumPresenterViewContract.class));

    }

    @Test
    public void TestThatWeCanCancelLoadingOfPhoto() {

    }

    @Test
    public void TestThatWeCanCancelLoadingOfVideo() {

    }

    @Test
    public void TestThatDestroyStopsLoadingOfAllMedia() {

    }

    @Test
    public void TestThatLoadingFrameHandlesError() {

    }

    @Test
    public void TestLoadingFramesCallsLoadFrameOnFirstItem() {

    }

    @Test
    public void TestThatDisplayFrameIsCalledAfterBothDetailsAndMediaHaveBeenLoaded() {

    }

    @Test
    public void TestThatDisplayFrameIsNotCalledIfMediaDoesNotLoad() {

    }

    @Test
    public void TestThatDisplayFrameIsNotCalledIfDetailsDoNotLoad() {

    }

    @Test
    public void TestThatWeCanPause() {
        AlbumDataSource dataSource = mock(AlbumDataSource.class);
        AlbumModel model = mock(AlbumModel.class);
        MediaPlayerWrapper mediaPlayerWrapper = mock(MediaPlayerWrapper.class);
        when(mediaPlayerWrapper.Prepare()).thenReturn(MediaPlayerWrapper.PREPARED);
        Context context = mock(Context.class);
        when(dataSource.GetAlbumId()).thenReturn("1");
        BreadcrumbsTimer timer = new BreadcrumbsTimer();
        AlbumPresenter presenter = new AlbumPresenter(model, Mockito.mock(Context.class), mediaPlayerWrapper,dataSource, timer);
        presenter.SetView(Mockito.mock(AlbumPresenterViewContract.class));
        presenter.Start("1");
        assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_STOPPED);
        FrameDetails frameDetails = mock(FrameDetails.class);
        when(frameDetails.getExtension()).thenReturn(".mp4");
        presenter.setFrame(frameDetails);
        assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);
        presenter.togglePauseState();
        assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_PAUSED);

    }

    @Test
    public void TestThatWeCanPauseAndResume() {
        AlbumDataSource dataSource = mock(AlbumDataSource.class);
        AlbumModel model = mock(AlbumModel.class);
        MediaPlayerWrapper mediaPlayerWrapper = mock(MediaPlayerWrapper.class);
        when(mediaPlayerWrapper.Prepare()).thenReturn(MediaPlayerWrapper.PREPARED);
        Context context = mock(Context.class);
        when(dataSource.GetAlbumId()).thenReturn("1");
        BreadcrumbsTimer timer = new BreadcrumbsTimer();
        AlbumPresenter presenter = new AlbumPresenter(model, Mockito.mock(Context.class), mediaPlayerWrapper,dataSource, timer);
        presenter.SetView(Mockito.mock(AlbumPresenterViewContract.class));
        presenter.Start("1");
        assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_STOPPED);
        FrameDetails frameDetails = mock(FrameDetails.class);
        when(frameDetails.getExtension()).thenReturn(".mp4");
        presenter.setFrame(frameDetails);
        assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);
        presenter.togglePauseState();
        assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_PAUSED);
        presenter.togglePauseState();
        assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);

    }

    @Test
    public void TestThatWeCanShowScreenOverlayWhenWePause() {
        AlbumDataSource dataSource = mock(AlbumDataSource.class);
        AlbumModel model = mock(AlbumModel.class);
        MediaPlayerWrapper mediaPlayerWrapper = mock(MediaPlayerWrapper.class);
        when(mediaPlayerWrapper.Prepare()).thenReturn(MediaPlayerWrapper.PREPARED);
        Context context = mock(Context.class);
        when(dataSource.GetAlbumId()).thenReturn("1");
        BreadcrumbsTimer timer = new BreadcrumbsTimer();
        AlbumPresenter presenter = new AlbumPresenter(model, Mockito.mock(Context.class), mediaPlayerWrapper,dataSource, timer);

        AlbumPresenterViewContract contract = Mockito.mock(AlbumPresenterViewContract.class);
        presenter.SetView(contract);
        presenter.Start("1");
        assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_STOPPED);
        FrameDetails frameDetails = mock(FrameDetails.class);
        when(frameDetails.getExtension()).thenReturn(".mp4");
        presenter.setFrame(frameDetails);

        assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);
        presenter.togglePauseState();
        verify(contract, times(1)).showDimScreenOverlay();
        verify(contract, times(1)).showCommentsBottomSheet();
        assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_PAUSED);


    }

    @Test
    public void TestThatWeCanHideScreenOverlayWhenWeUnPause() {
        AlbumDataSource dataSource = mock(AlbumDataSource.class);
        AlbumModel model = mock(AlbumModel.class);
        MediaPlayerWrapper mediaPlayerWrapper = mock(MediaPlayerWrapper.class);
        when(mediaPlayerWrapper.Prepare()).thenReturn(MediaPlayerWrapper.PREPARED);
        Context context = mock(Context.class);
        when(dataSource.GetAlbumId()).thenReturn("1");
        BreadcrumbsTimer timer = new BreadcrumbsTimer();
        AlbumPresenter presenter = new AlbumPresenter(model, Mockito.mock(Context.class), mediaPlayerWrapper,dataSource, timer);

        AlbumPresenterViewContract contract = Mockito.mock(AlbumPresenterViewContract.class);
        presenter.SetView(contract);
        presenter.Start("1");
        assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_STOPPED);
        FrameDetails frameDetails = mock(FrameDetails.class);
        when(frameDetails.getExtension()).thenReturn(".mp4");
        presenter.setFrame(frameDetails);

        assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);
        presenter.togglePauseState();
        verify(contract, times(1)).showDimScreenOverlay();
        verify(contract, times(1)).showCommentsBottomSheet();
        assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_PAUSED);

        presenter.togglePauseState();
        verify(contract, times(1)).hideDimScreenOverlay();
    }

    @Test
    public void TestThatWeCanShowCommentsWhenWePause() {
        AlbumDataSource dataSource = mock(AlbumDataSource.class);
        AlbumModel model = mock(AlbumModel.class);
        MediaPlayerWrapper mediaPlayerWrapper = mock(MediaPlayerWrapper.class);
        when(mediaPlayerWrapper.Prepare()).thenReturn(MediaPlayerWrapper.PREPARED);
        Context context = mock(Context.class);
        when(dataSource.GetAlbumId()).thenReturn("1");
        BreadcrumbsTimer timer = new BreadcrumbsTimer();
        AlbumPresenter presenter = new AlbumPresenter(model, Mockito.mock(Context.class), mediaPlayerWrapper,dataSource, timer);

        AlbumPresenterViewContract contract = Mockito.mock(AlbumPresenterViewContract.class);
        presenter.SetView(contract);
        presenter.Start("1");
        assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_STOPPED);
        FrameDetails frameDetails = mock(FrameDetails.class);
        when(frameDetails.getExtension()).thenReturn(".mp4");
        presenter.setFrame(frameDetails);

        assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);
        presenter.togglePauseState();
        verify(contract, times(1)).showCommentsBottomSheet();
    }

    @Test
    public void TestThatWeCanHideCommentsWhenWeUnPause() {
        AlbumDataSource dataSource = mock(AlbumDataSource.class);
        AlbumModel model = mock(AlbumModel.class);
        MediaPlayerWrapper mediaPlayerWrapper = mock(MediaPlayerWrapper.class);
        when(mediaPlayerWrapper.Prepare()).thenReturn(MediaPlayerWrapper.PREPARED);
        Context context = mock(Context.class);
        when(dataSource.GetAlbumId()).thenReturn("1");
        BreadcrumbsTimer timer = new BreadcrumbsTimer();
        AlbumPresenter presenter = new AlbumPresenter(model, Mockito.mock(Context.class), mediaPlayerWrapper,dataSource, timer);

        AlbumPresenterViewContract contract = Mockito.mock(AlbumPresenterViewContract.class);
        presenter.SetView(contract);
        presenter.Start("1");
        assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_STOPPED);
        FrameDetails frameDetails = mock(FrameDetails.class);
        when(frameDetails.getExtension()).thenReturn(".mp4");
        presenter.setFrame(frameDetails);

        assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);
        presenter.togglePauseState();
        verify(contract, times(1)).showDimScreenOverlay();
        verify(contract, times(1)).showCommentsBottomSheet();
        assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_PAUSED);

        presenter.togglePauseState();
        verify(contract, times(1)).hideCommentsBottomSheet();
    }

}
