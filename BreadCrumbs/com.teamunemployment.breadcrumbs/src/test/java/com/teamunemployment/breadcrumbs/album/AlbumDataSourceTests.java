package com.teamunemployment.breadcrumbs.album;

import android.content.Context;

import com.teamunemployment.breadcrumbs.AlbumDataSource;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.File;

/**
 * Created by jek40 on 15/08/2016.
 */
public class AlbumDataSourceTests {

    @Test
    public void TestThatWeCanGetLocalDatasource() {
        Context context = Mockito.mock(Context.class);
        AlbumDataSource albumDataSource = new AlbumDataSource(context);
        albumDataSource.SetAlbumId("1L");
        String local = albumDataSource.getDataSource();
        Assert.assertTrue(albumDataSource.getIsLocal());
        Assert.assertTrue(local.equals(""));
    }

    @Test
    public void TestThatWEeCanGetRemoteFDataSource() {
        Context context = Mockito.mock(Context.class);
        AlbumDataSource albumDataSource = new AlbumDataSource(context);
        albumDataSource.SetAlbumId("1");
        String remote = albumDataSource.getDataSource();
        Assert.assertTrue(remote.equals("1"));
        Assert.assertFalse(albumDataSource.getIsLocal());
    }

    @Test (expected = IllegalStateException.class)
    public void TestThatWeThrowAnExceptionIfWeDontSet() {
        Context context = Mockito.mock(Context.class);
        AlbumDataSource albumDataSource = new AlbumDataSource(context);
        albumDataSource.getDataSource();
    }

    @Test
    public void TestThatWeCanGetLocalId() {
        Context context = Mockito.mock(Context.class);
        AlbumDataSource albumDataSource = new AlbumDataSource(context);
        albumDataSource.SetAlbumId("1L");
        String albumId = albumDataSource.GetAlbumId();
        Assert.assertTrue(albumId.equals("1"));
    }

    @Test
    public void TestThatWeCanGetRemoteId() {
        Context context = Mockito.mock(Context.class);
        AlbumDataSource albumDataSource = new AlbumDataSource(context);
        albumDataSource.SetAlbumId("1");
        String albumId = albumDataSource.GetAlbumId();
        Assert.assertTrue(albumId.equals("1"));
    }

    @Test
    public void TestThatWeCanHandleNoExternalCache() {
        Context context = Mockito.mock(Context.class);
        AlbumDataSource albumDataSource = new AlbumDataSource(context);
        Mockito.when(context.getExternalCacheDir()).thenReturn(null);
        Mockito.when(context.getCacheDir()).thenReturn(new File("test"));
        albumDataSource.SetAlbumId("1");
        String datasource = albumDataSource.getDataSource();
        Assert.assertTrue(datasource.equals("C:\\Users\\jek40\\Desktop\\ICEMANv0.5\\BreadCrumbs\\com.teamunemployment.breadcrumbs\\test"));
    }
}
