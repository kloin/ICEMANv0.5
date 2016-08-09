package com.teamunemployment.breadcrumbs.album;
import android.content.Context;

import com.teamunemployment.breadcrumbs.Album.AlbumModel;
import com.teamunemployment.breadcrumbs.Album.AlbumPresenter;
import com.teamunemployment.breadcrumbs.Album.AlbumPresenterViewContract;
import com.teamunemployment.breadcrumbs.RESTApi.AlbumService;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import okhttp3.ResponseBody;
import retrofit2.Call;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Josiah Kendall
 */
public class AlbumPresenterTests {

    @Test
    public void TestThatStartLoadsAllFramesForAnAlbum() {
//        AlbumService albumService = Mockito.mock(AlbumService.class);
//        AlbumModel model = new AlbumModel(albumService);
//        AlbumPresenter presenter = new AlbumPresenter(model);
//        when(albumService.GetFrameIdsForAnAlbum(any(String.class))).thenReturn(Mockito.mock(Call.class));
//        //verify(model, times(1)).LoadFrameIds("123");
//        presenter.SetView(Mockito.mock(AlbumPresenterViewContract.class));
//        presenter.Start("123");
//        verify(albumService, times(1)).GetFrameIdsForAnAlbum("123");
    }

    @Test
    public void TestThatLoadingStarts() {
        AlbumModel albumModel = Mockito.mock(AlbumModel.class);
        Context context = Mockito.mock(Context.class);
        AlbumPresenter presenter = new AlbumPresenter(albumModel, context);
        presenter.SetView(Mockito.mock(AlbumPresenterViewContract.class));
        presenter.Start("1234");
        verify(albumModel, times(1)).LoadFrameMedia(any(String.class), any(String.class));

    }

    @Test
    public void TestThatLoadingStopsWhenWeAreOutOfData() {

    }

    @Test
    public void TestThatWeCanLoadFrameMediaSuccessfully() {

    }

    @Test
    public void TestThatWeCanGoNextIfWeFailToLoadFrameMedia() {
        AlbumModel model = Mockito.mock(AlbumModel.class);
        Context context = Mockito.mock(Context.class);
        AlbumPresenter presenter = new AlbumPresenter(model, context);
        presenter.SetView(Mockito.mock(AlbumPresenterViewContract.class));
        Assert.fail();
    }

    @Test
    public void TestDetailsFailButFrameMediaDoesnt() {
        AlbumModel model = Mockito.mock(AlbumModel.class);
        Context context = Mockito.mock(Context.class);
        AlbumPresenter presenter = new AlbumPresenter(model, context);
        presenter.SetView(Mockito.mock(AlbumPresenterViewContract.class));


    }

    @Test
    public void TestThatWeCanCancelLoadingOfPhoto() {

    }

    @Test
    public void TestThatWeCanCancelLoadingOfVideo() {
        Assert.fail();
    }

    @Test
    public void TestThatDestroyStopsLoadingOfAllMedia() {
        Assert.fail();

    }

    @Test
    public void TestThatLoadingFrameHandlesError() {
        Assert.fail();

    }

    @Test
    public void TestLoadingFramesCallsLoadFrameOnFirstItem() {
        Assert.fail();

    }

    @Test
    public void TestThatDisplayFrameIsCalledAfterBothDetailsAndMediaHaveBeenLoaded() {
        Assert.fail();

    }

    @Test
    public void TestThatDisplayFrameIsNotCalledIfMediaDoesNotLoad() {
        Assert.fail();

    }

    @Test
    public void TestThatDisplayFrameIsNotCalledIfDetailsDoNotLoad() {

    }
}
