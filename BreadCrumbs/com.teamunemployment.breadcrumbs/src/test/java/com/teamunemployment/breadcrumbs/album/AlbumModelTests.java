package com.teamunemployment.breadcrumbs.album;

import android.content.Context;
import android.net.ConnectivityManager;

import com.teamunemployment.breadcrumbs.Album.AlbumModel;
import com.teamunemployment.breadcrumbs.Album.AlbumModelPresenterContract;
import com.teamunemployment.breadcrumbs.Album.Frame;
import com.teamunemployment.breadcrumbs.Album.data.Comment;
import com.teamunemployment.breadcrumbs.Album.data.FrameDetails;
import com.teamunemployment.breadcrumbs.Album.data.MimeDetails;
import com.teamunemployment.breadcrumbs.Album.repo.LocalAlbumRepo;
import com.teamunemployment.breadcrumbs.Album.repo.RemoteAlbumRepo;
import com.teamunemployment.breadcrumbs.Explore.Data.ExploreRemoteRepository;
import com.teamunemployment.breadcrumbs.Explore.Model;
import com.teamunemployment.breadcrumbs.FileManager.MediaRecordModel;
import com.teamunemployment.breadcrumbs.MockClient;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.Profile.data.LocalProfileRepository;
import com.teamunemployment.breadcrumbs.Profile.data.RemoteProfileRepository;
import com.teamunemployment.breadcrumbs.RESTApi.AlbumService;
import com.teamunemployment.breadcrumbs.RESTApi.CrumbService;
import com.teamunemployment.breadcrumbs.RESTApi.FileManager;
import com.teamunemployment.breadcrumbs.RESTApi.NodeService;
import com.teamunemployment.breadcrumbs.RESTApi.UserService;
import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import junit.framework.Assert;

import org.json.JSONException;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Josiah Kendall.
 */
public class AlbumModelTests {

    @Test
    public void TestWeCanLoadAlbumIds() {
        //
        Context context = Mockito.mock(Context.class);
        String result = "[{\"Extension\":\".mp4\",\"Id\":\"17804\"},{\"Extension\":\".mp4\",\"Id\":\"17805\"},{\"Extension\":\".mp4\",\"Id\":\"17806\"},{\"Extension\":\".mp4\",\"Id\":\"17807\"},{\"Extension\":\".mp4\",\"Id\":\"17808\"},{\"Extension\":\".jpg\",\"Id\":\"17809\"},{\"Extension\":\".jpg\",\"Id\":\"17810\"},{\"Extension\":\".mp4\",\"Id\":\"17811\"},{\"Extension\":\".mp4\",\"Id\":\"17812\"},{\"Extension\":\".jpg\",\"Id\":\"17813\"},{\"Extension\":\".jpg\",\"Id\":\"17814\"},{\"Extension\":\".mp4\",\"Id\":\"17815\"},{\"Extension\":\".jpg\",\"Id\":\"17816\"},{\"Extension\":\".mp4\",\"Id\":\"17817\"},{\"Extension\":\".mp4\",\"Id\":\"17818\"},{\"Extension\":\".jpg\",\"Id\":\"17819\"},{\"Extension\":\".mp4\",\"Id\":\"17820\"},{\"Extension\":\".mp4\",\"Id\":\"17821\"},{\"Extension\":\".mp4\",\"Id\":\"17822\"},{\"Extension\":\".mp4\",\"Id\":\"17823\"},{\"Extension\":\".mp4\",\"Id\":\"17824\"},{\"Extension\":\".mp4\",\"Id\":\"17825\"},{\"Extension\":\".jpg\",\"Id\":\"17826\"}]";
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(
                        new MockClient(context, result, 200)
                ).build();

        Retrofit retrofit = new Retrofit.Builder()
                // .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(LoadBalancer.RequestServerAddress() + "/rest/")
                .client(okHttpClient)
                .build();

        DatabaseController databaseController = Mockito.mock(DatabaseController.class);
        RemoteAlbumRepo remoteAlbumRepo = new RemoteAlbumRepo(retrofit.create(AlbumService.class), retrofit.create(CrumbService.class));
        LocalAlbumRepo localAlbumRepo = Mockito.mock(LocalAlbumRepo.class);
        AlbumModelPresenterContract contract = Mockito.mock(AlbumModelPresenterContract.class);
        FileManager fileManager = new FileManager(context);
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        PreferencesAPI preferencesAPI = mock(PreferencesAPI.class);
        AlbumModel albumModel = new AlbumModel(remoteAlbumRepo, localAlbumRepo,context, fileManager, localProfileRepository, remoteProfileRepository, preferencesAPI);
        albumModel.setContract(contract);

        ArrayList<MimeDetails> list = albumModel.LoadMimeDetails("Doesnt Matter");
        Assert.assertTrue(list != null);
        ListIterator<MimeDetails> iterator = list.listIterator();
        MimeDetails first = iterator.next();

        Assert.assertTrue(first.getId().equals("17804"));
        Assert.assertTrue(first.getExtension().equals(".mp4"));

        MimeDetails second = iterator.next();
        Assert.assertTrue(second.getId().equals("17805"));
        Assert.assertTrue(second.getExtension().equals(".mp4"));
    }

    @Test
    public void TestThatWeCanLoadWhenWeHaveLocalData() {
        RemoteAlbumRepo remoteAlbumRepo = Mockito.mock(RemoteAlbumRepo.class);
        LocalAlbumRepo localAlbumRepo = Mockito.mock(LocalAlbumRepo.class);

        ArrayList<MimeDetails> results = new ArrayList<>();

        MimeDetails mime1 = new MimeDetails();
        mime1.setId("1");
        mime1.setExtension(".jpg");

        MimeDetails mime2 = new MimeDetails();
        mime1.setId("2");
        mime1.setExtension(".mp4");

        MimeDetails mime3 = new MimeDetails();
        mime1.setId("3");
        mime1.setExtension(".jpg");

        results.add(mime1);
        results.add(mime2);
        results.add(mime3);

        when(localAlbumRepo.LoadMimeDetailsForAnAlbum(any(String.class))).thenReturn(results);

        AlbumModelPresenterContract contract = Mockito.mock(AlbumModelPresenterContract.class);
        Context context = Mockito.mock(Context.class);
        FileManager fileManager = Mockito.mock(FileManager.class);
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        PreferencesAPI preferencesAPI = mock(PreferencesAPI.class);
        AlbumModel albumModel = new AlbumModel(remoteAlbumRepo, localAlbumRepo,context, fileManager, localProfileRepository, remoteProfileRepository, preferencesAPI);
        albumModel.setContract(contract);
        ArrayList<MimeDetails> mimes = albumModel.LoadMimeDetails("dont care");
        Assert.assertTrue(mimes.get(0).equals(mime1));
    }

    @Test
    public void TestThatWeCanRetrieveDataWhenWeHaveNothingLocally() {
        RemoteAlbumRepo remoteAlbumRepo = Mockito.mock(RemoteAlbumRepo.class);
        LocalAlbumRepo localAlbumRepo = Mockito.mock(LocalAlbumRepo.class);

        ArrayList<MimeDetails> results = new ArrayList<>();

        MimeDetails mime1 = new MimeDetails();
        mime1.setId("1");
        mime1.setExtension(".jpg");

        MimeDetails mime2 = new MimeDetails();
        mime1.setId("2");
        mime1.setExtension(".mp4");

        MimeDetails mime3 = new MimeDetails();
        mime1.setId("3");
        mime1.setExtension(".jpg");

        results.add(mime1);
        results.add(mime2);
        results.add(mime3);

        when(localAlbumRepo.LoadMimeDetailsForAnAlbum(any(String.class))).thenReturn(new ArrayList<MimeDetails>());
        when(remoteAlbumRepo.LoadMimeDetailsForAnAlbum(any(String.class))).thenReturn(results);
        AlbumModelPresenterContract contract = Mockito.mock(AlbumModelPresenterContract.class);
        Context context = Mockito.mock(Context.class);

        FileManager fileManager = Mockito.mock(FileManager.class);
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        PreferencesAPI preferencesAPI = mock(PreferencesAPI.class);
        AlbumModel albumModel = new AlbumModel(remoteAlbumRepo, localAlbumRepo,context, fileManager, localProfileRepository, remoteProfileRepository, preferencesAPI);
        albumModel.setContract(contract);
        ArrayList<MimeDetails> mimes = albumModel.LoadMimeDetails("dont care");
        Assert.assertTrue(mimes.get(0).equals(mime1));
    }

    @Test
    public void TestThatWeSaveDataAfterRetrievingFromRemote() {
        RemoteAlbumRepo remoteAlbumRepo = Mockito.mock(RemoteAlbumRepo.class);
        LocalAlbumRepo localAlbumRepo = Mockito.mock(LocalAlbumRepo.class);

        ArrayList<MimeDetails> results = new ArrayList<>();

        MimeDetails mime1 = new MimeDetails();
        mime1.setId("1");
        mime1.setExtension(".jpg");

        MimeDetails mime2 = new MimeDetails();
        mime1.setId("2");
        mime1.setExtension(".mp4");

        MimeDetails mime3 = new MimeDetails();
        mime1.setId("3");
        mime1.setExtension(".jpg");

        results.add(mime1);
        results.add(mime2);
        results.add(mime3);

        when(localAlbumRepo.LoadMimeDetailsForAnAlbum(any(String.class))).thenReturn(new ArrayList<MimeDetails>());
        when(remoteAlbumRepo.LoadMimeDetailsForAnAlbum(any(String.class))).thenReturn(results);
        AlbumModelPresenterContract contract = Mockito.mock(AlbumModelPresenterContract.class);
        Context context = Mockito.mock(Context.class);
        FileManager fileManager = Mockito.mock(FileManager.class);
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        PreferencesAPI preferencesAPI = mock(PreferencesAPI.class);
        AlbumModel albumModel = new AlbumModel(remoteAlbumRepo, localAlbumRepo,context, fileManager, localProfileRepository, remoteProfileRepository, preferencesAPI);
        albumModel.setContract(contract);
        ArrayList<MimeDetails> mimes = albumModel.LoadMimeDetails("dont care");

        verify(localAlbumRepo, times(1)).SaveFrameMimeData(any(ArrayList.class));
        Assert.assertTrue(mimes.get(0).equals(mime1));
    }

    @Test
    public void TestThatWeCanHandleNullResponse() {
        Context context = Mockito.mock(Context.class);

        RemoteAlbumRepo remoteAlbumRepo = Mockito.mock(RemoteAlbumRepo.class);
        LocalAlbumRepo localAlbumRepo = Mockito.mock(LocalAlbumRepo.class);
        AlbumModelPresenterContract contract = Mockito.mock(AlbumModelPresenterContract.class);
        FileManager fileManager = Mockito.mock(FileManager.class);
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        PreferencesAPI preferencesAPI = mock(PreferencesAPI.class);
        AlbumModel albumModel = new AlbumModel(remoteAlbumRepo, localAlbumRepo,context, fileManager, localProfileRepository, remoteProfileRepository, preferencesAPI);
        albumModel.setContract(contract);
        ArrayList<MimeDetails> list = albumModel.LoadMimeDetails("Doesnt Matter");
        Assert.assertTrue(list.size() == 0);
    }

    @Test
    public void TestThatWeCanHandleWeirdResponse() {
        Context context = Mockito.mock(Context.class);
        String result = "34123redsfsfdsaljrlkew45jk3l23412fdsfsadjklfds";
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(
                        new MockClient(context, result, 200)
                ).build();

        Retrofit retrofit = new Retrofit.Builder()
                // .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(LoadBalancer.RequestServerAddress() + "/rest/")
                .client(okHttpClient)
                .build();

        RemoteAlbumRepo remoteAlbumRepo = Mockito.mock(RemoteAlbumRepo.class);
        LocalAlbumRepo localAlbumRepo = Mockito.mock(LocalAlbumRepo.class);
        AlbumModelPresenterContract contract = Mockito.mock(AlbumModelPresenterContract.class);
        FileManager fileManager = Mockito.mock(FileManager.class);
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        PreferencesAPI preferencesAPI = mock(PreferencesAPI.class);
        AlbumModel albumModel = new AlbumModel(remoteAlbumRepo, localAlbumRepo,context, fileManager, localProfileRepository, remoteProfileRepository, preferencesAPI);
        albumModel.setContract(contract);
        ArrayList<MimeDetails> list = albumModel.LoadMimeDetails("Doesnt Matter");

        Assert.assertTrue(list.size() == 0);
    }

    @Test
    public void TestThatWeCanDownlaodAllFiles() {
        RemoteAlbumRepo remoteAlbumRepo = Mockito.mock(RemoteAlbumRepo.class);
        LocalAlbumRepo localAlbumRepo = Mockito.mock(LocalAlbumRepo.class);

        ArrayList<MimeDetails> results = new ArrayList<>();

        MimeDetails mime1 = new MimeDetails();
        mime1.setId("1");
        mime1.setExtension(".mp4");

        MimeDetails mime2 = new MimeDetails();
        mime2.setId("2");
        mime2.setExtension(".mp4");

        MimeDetails mime3 = new MimeDetails();
        mime3.setId("3");
        mime3.setExtension(".mp4");

        results.add(mime1);
        results.add(mime2);
        results.add(mime3);

        when(localAlbumRepo.LoadMimeDetailsForAnAlbum(any(String.class))).thenReturn(new ArrayList<MimeDetails>());
        when(remoteAlbumRepo.LoadMimeDetailsForAnAlbum(any(String.class))).thenReturn(results);
        AlbumModelPresenterContract contract = Mockito.mock(AlbumModelPresenterContract.class);
        Context context = Mockito.mock(Context.class);
        ConnectivityManager manager = Mockito.mock(ConnectivityManager.class);
        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(manager);
        when(manager.getActiveNetworkInfo()).thenReturn(null);
        FileManager fileManager = Mockito.mock(FileManager.class);
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        PreferencesAPI preferencesAPI = mock(PreferencesAPI.class);
        AlbumModel albumModel = new AlbumModel(remoteAlbumRepo, localAlbumRepo,context, fileManager, localProfileRepository, remoteProfileRepository, preferencesAPI);
        albumModel.setContract(contract);
        ArrayList<MimeDetails> mimes = albumModel.LoadMimeDetails("dont care");
        albumModel.StartDownloadingFrames();
        verify(fileManager, times(3)).DownloadAndSaveLocalFile(any(String.class), any(String.class), any(String.class));
    }

    @Test
    public void TestThatWeDoDownloadFilesIfTheyDontAlreadyExist() {
        RemoteAlbumRepo remoteAlbumRepo = Mockito.mock(RemoteAlbumRepo.class);
        LocalAlbumRepo localAlbumRepo = Mockito.mock(LocalAlbumRepo.class);

        ArrayList<MimeDetails> results = new ArrayList<>();

        MimeDetails mime1 = new MimeDetails();
        mime1.setId("1");
        mime1.setExtension(".mp4");

        MimeDetails mime2 = new MimeDetails();
        mime2.setId("2");
        mime2.setExtension(".mp4");

        MimeDetails mime3 = new MimeDetails();
        mime3.setId("3");
        mime3.setExtension(".mp4");

        results.add(mime1);
        results.add(mime2);
        results.add(mime3);

        when(localAlbumRepo.LoadMimeDetailsForAnAlbum(any(String.class))).thenReturn(new ArrayList<MimeDetails>());
        when(remoteAlbumRepo.LoadMimeDetailsForAnAlbum(any(String.class))).thenReturn(results);

        AlbumModelPresenterContract contract = Mockito.mock(AlbumModelPresenterContract.class);
        Context context = Mockito.mock(Context.class);
        FileManager fileManager = Mockito.mock(FileManager.class);
        when(localAlbumRepo.FindMediaFileRecord(any(String.class))).thenReturn(null);
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        PreferencesAPI preferencesAPI = mock(PreferencesAPI.class);
        AlbumModel albumModel = new AlbumModel(remoteAlbumRepo, localAlbumRepo,context, fileManager, localProfileRepository, remoteProfileRepository, preferencesAPI);
        // Be sure that we recieve a call to download a new file, as this one is not locally stored.
        albumModel.DownloadFrame("1", ".mp4", "640");
        verify(fileManager, times(1)).DownloadAndSaveLocalFile(any(String.class), anyString(), anyString());
    }

    @Test
    public void TestThatWeDontDownloadFilesWhenTheyAlreadyExist() {
        RemoteAlbumRepo remoteAlbumRepo = Mockito.mock(RemoteAlbumRepo.class);
        LocalAlbumRepo localAlbumRepo = Mockito.mock(LocalAlbumRepo.class);

        ArrayList<MimeDetails> results = new ArrayList<>();

        MimeDetails mime1 = new MimeDetails();
        mime1.setId("1");
        mime1.setExtension(".mp4");

        MimeDetails mime2 = new MimeDetails();
        mime2.setId("2");
        mime2.setExtension(".mp4");

        MimeDetails mime3 = new MimeDetails();
        mime3.setId("3");
        mime3.setExtension(".mp4");

        results.add(mime1);
        results.add(mime2);
        results.add(mime3);

        when(localAlbumRepo.LoadMimeDetailsForAnAlbum(any(String.class))).thenReturn(new ArrayList<MimeDetails>());
        when(remoteAlbumRepo.LoadMimeDetailsForAnAlbum(any(String.class))).thenReturn(results);

        AlbumModelPresenterContract contract = Mockito.mock(AlbumModelPresenterContract.class);
        Context context = Mockito.mock(Context.class);
        FileManager fileManager = Mockito.mock(FileManager.class);
        when(localAlbumRepo.FindMediaFileRecord(any(String.class))).thenReturn(Mockito.mock(MediaRecordModel.class));
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        PreferencesAPI preferencesAPI = mock(PreferencesAPI.class);
        AlbumModel albumModel = new AlbumModel(remoteAlbumRepo, localAlbumRepo,context, fileManager, localProfileRepository, remoteProfileRepository, preferencesAPI);
        // Be sure that we recieve a call to download a new file, as this one is not locally stored.
        albumModel.DownloadFrame("1", ".mp4", "640");
        verify(fileManager, times(0)).DownloadAndSaveLocalFile(any(String.class), anyString(), anyString());
    }

    @Test
    public void TestThatWeCanStopFilesDownloading() {
        // Not sure how to test this.
    }

    @Test
    public void TestThatWeAttemptToLoadFromLocalFrameDetails() {
        RemoteAlbumRepo remoteAlbumRepo = Mockito.mock(RemoteAlbumRepo.class);
        LocalAlbumRepo localAlbumRepo = Mockito.mock(LocalAlbumRepo.class);

        ArrayList<MimeDetails> results = new ArrayList<>();

        MimeDetails mime1 = new MimeDetails();
        mime1.setId("1");
        mime1.setExtension(".mp4");

        MimeDetails mime2 = new MimeDetails();
        mime2.setId("2");
        mime2.setExtension(".mp4");

        MimeDetails mime3 = new MimeDetails();
        mime3.setId("3");
        mime3.setExtension(".mp4");

        results.add(mime1);
        results.add(mime2);
        results.add(mime3);

        when(localAlbumRepo.LoadMimeDetailsForAnAlbum(any(String.class))).thenReturn(new ArrayList<MimeDetails>());
        when(remoteAlbumRepo.LoadMimeDetailsForAnAlbum(any(String.class))).thenReturn(results);

        AlbumModelPresenterContract contract = Mockito.mock(AlbumModelPresenterContract.class);
        Context context = Mockito.mock(Context.class);
        FileManager fileManager = Mockito.mock(FileManager.class);
        when(localAlbumRepo.FindMediaFileRecord(any(String.class))).thenReturn(Mockito.mock(MediaRecordModel.class));
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        PreferencesAPI preferencesAPI = mock(PreferencesAPI.class);
        AlbumModel albumModel = new AlbumModel(remoteAlbumRepo, localAlbumRepo,context, fileManager, localProfileRepository, remoteProfileRepository, preferencesAPI);
        albumModel.DownloadFrame(mime1.getId(), mime1.getExtension(), "280");
        verify(localAlbumRepo, times(1)).LoadFrameDetails(mime1.getId());
    }

    @Test
    public void TestThatWeDontFetchRemoteDataIfWeHaveLocal() {
        RemoteAlbumRepo remoteAlbumRepo = Mockito.mock(RemoteAlbumRepo.class);
        LocalAlbumRepo localAlbumRepo = Mockito.mock(LocalAlbumRepo.class);

        ArrayList<MimeDetails> results = new ArrayList<>();

        MimeDetails mime1 = new MimeDetails();
        mime1.setId("1");
        mime1.setExtension(".mp4");

        MimeDetails mime2 = new MimeDetails();
        mime2.setId("2");
        mime2.setExtension(".mp4");

        MimeDetails mime3 = new MimeDetails();
        mime3.setId("3");
        mime3.setExtension(".mp4");

        results.add(mime1);
        results.add(mime2);
        results.add(mime3);

        when(localAlbumRepo.LoadMimeDetailsForAnAlbum(any(String.class))).thenReturn(new ArrayList<MimeDetails>());
        when(remoteAlbumRepo.LoadMimeDetailsForAnAlbum(any(String.class))).thenReturn(results);
        when(localAlbumRepo.LoadFrameDetails(anyString())).thenReturn(new FrameDetails());

        AlbumModelPresenterContract contract = Mockito.mock(AlbumModelPresenterContract.class);
        Context context = Mockito.mock(Context.class);
        FileManager fileManager = Mockito.mock(FileManager.class);
        when(localAlbumRepo.FindMediaFileRecord(any(String.class))).thenReturn(Mockito.mock(MediaRecordModel.class));
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        PreferencesAPI preferencesAPI = mock(PreferencesAPI.class);
        AlbumModel albumModel = new AlbumModel(remoteAlbumRepo, localAlbumRepo,context, fileManager, localProfileRepository, remoteProfileRepository, preferencesAPI);
        albumModel.DownloadFrame(mime1.getId(), mime1.getExtension(), "280");
        verify(remoteAlbumRepo, times(0)).LoadFrameDetails(mime1.getId());
    }

    @Test
    public void TestThatWeSaveFrameDetailsLocallyAfterFetching() {
        RemoteAlbumRepo remoteAlbumRepo = Mockito.mock(RemoteAlbumRepo.class);
        LocalAlbumRepo localAlbumRepo = Mockito.mock(LocalAlbumRepo.class);

        ArrayList<MimeDetails> results = new ArrayList<>();

        MimeDetails mime1 = new MimeDetails();
        mime1.setId("1");
        mime1.setExtension(".mp4");

        MimeDetails mime2 = new MimeDetails();
        mime2.setId("2");
        mime2.setExtension(".mp4");

        MimeDetails mime3 = new MimeDetails();
        mime3.setId("3");
        mime3.setExtension(".mp4");

        results.add(mime1);
        results.add(mime2);
        results.add(mime3);

        when(localAlbumRepo.LoadMimeDetailsForAnAlbum(any(String.class))).thenReturn(new ArrayList<MimeDetails>());
        when(remoteAlbumRepo.LoadMimeDetailsForAnAlbum(any(String.class))).thenReturn(results);
        when(localAlbumRepo.LoadFrameDetails(anyString())).thenReturn(null);

        AlbumModelPresenterContract contract = Mockito.mock(AlbumModelPresenterContract.class);
        Context context = Mockito.mock(Context.class);
        FileManager fileManager = Mockito.mock(FileManager.class);
        when(localAlbumRepo.FindMediaFileRecord(any(String.class))).thenReturn(Mockito.mock(MediaRecordModel.class));
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        PreferencesAPI preferencesAPI = mock(PreferencesAPI.class);
        AlbumModel albumModel = new AlbumModel(remoteAlbumRepo, localAlbumRepo,context, fileManager, localProfileRepository, remoteProfileRepository, preferencesAPI);
        albumModel.DownloadFrame(mime1.getId(), mime1.getExtension(), "280");
        verify(localAlbumRepo, times(1)).SaveFrameDetails(any(FrameDetails.class));
        verify(localAlbumRepo, times(1)).LoadFrameDetails(anyString());
        verify(remoteAlbumRepo, times(1)).LoadFrameDetails(anyString());
    }

    @Test
    public void TestThatWeSaveCommentBothLocallyAndRemotely() {
        RemoteAlbumRepo remoteAlbumRepo = Mockito.mock(RemoteAlbumRepo.class);
        LocalAlbumRepo localAlbumRepo = Mockito.mock(LocalAlbumRepo.class);
        AlbumModelPresenterContract contract = Mockito.mock(AlbumModelPresenterContract.class);
        Context context = Mockito.mock(Context.class);
        FileManager fileManager = Mockito.mock(FileManager.class);
        when(localAlbumRepo.FindMediaFileRecord(any(String.class))).thenReturn(Mockito.mock(MediaRecordModel.class));
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        PreferencesAPI preferencesAPI = mock(PreferencesAPI.class);
        AlbumModel albumModel = new AlbumModel(remoteAlbumRepo, localAlbumRepo,context, fileManager, localProfileRepository, remoteProfileRepository, preferencesAPI);

        albumModel.SaveComment("Test1", "1");

        verify(remoteAlbumRepo, times(1)).SaveComment(any(Comment.class));
        verify(localAlbumRepo, times(1)).SaveComment(any(Comment.class));
    }

    @Test
    public void TestLoadingWhenNoCommentsExistLocally() {
        RemoteAlbumRepo remoteAlbumRepo = Mockito.mock(RemoteAlbumRepo.class);
        LocalAlbumRepo localAlbumRepo = Mockito.mock(LocalAlbumRepo.class);
        AlbumModelPresenterContract contract = Mockito.mock(AlbumModelPresenterContract.class);
        Context context = Mockito.mock(Context.class);
        FileManager fileManager = Mockito.mock(FileManager.class);
        when(localAlbumRepo.FindMediaFileRecord(any(String.class))).thenReturn(Mockito.mock(MediaRecordModel.class));
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        PreferencesAPI preferencesAPI = mock(PreferencesAPI.class);
        AlbumModel albumModel = new AlbumModel(remoteAlbumRepo, localAlbumRepo,context, fileManager, localProfileRepository, remoteProfileRepository, preferencesAPI);

        ArrayList<Comment> mockComments = new ArrayList<>();
        Comment comment1 = new Comment();
        comment1.setCommentText("Test");
        comment1.setUserId("1");
        comment1.setEntityId("12");
        comment1.setId("123");

        Comment comment2 = new Comment();
        comment2.setCommentText("Test");
        comment2.setUserId("1");
        comment2.setEntityId("12");
        comment2.setId("223");

        mockComments.add(comment1);
        mockComments.add(comment2);

        when(remoteAlbumRepo.LoadCommentsForFrame(anyString())).thenReturn(mockComments);

        ArrayList<Comment> commentsMockArray = new ArrayList<>();
        when(localAlbumRepo.LoadCommentsForAFrame(anyString())).thenReturn(commentsMockArray);

        AlbumModel.loadedCommentsCallback commentsCallback = mock(AlbumModel.loadedCommentsCallback.class);

        albumModel.GetCommentsForFrame("1", commentsCallback);
        verify(commentsCallback, times(1)).onLoaded(any(ArrayList.class));
    }

    @Test
    public void TestThatWeCanLoadFromLocalAndRemote() {
        RemoteAlbumRepo remoteAlbumRepo = Mockito.mock(RemoteAlbumRepo.class);
        LocalAlbumRepo localAlbumRepo = Mockito.mock(LocalAlbumRepo.class);
        AlbumModelPresenterContract contract = Mockito.mock(AlbumModelPresenterContract.class);
        Context context = Mockito.mock(Context.class);
        FileManager fileManager = Mockito.mock(FileManager.class);
        when(localAlbumRepo.FindMediaFileRecord(any(String.class))).thenReturn(Mockito.mock(MediaRecordModel.class));
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        PreferencesAPI preferencesAPI = mock(PreferencesAPI.class);
        AlbumModel albumModel = new AlbumModel(remoteAlbumRepo, localAlbumRepo,context, fileManager, localProfileRepository, remoteProfileRepository, preferencesAPI);

        ArrayList<Comment> mockComments = new ArrayList<>();
        Comment comment1 = new Comment();
        comment1.setCommentText("Test");
        comment1.setUserId("1");
        comment1.setEntityId("12");
        comment1.setId("123");

        Comment comment2 = new Comment();
        comment2.setCommentText("Test");
        comment2.setUserId("1");
        comment2.setEntityId("12");
        comment2.setId("223");

        mockComments.add(comment1);
        mockComments.add(comment2);

        when(remoteAlbumRepo.LoadCommentsForFrame(anyString())).thenReturn(mockComments);

        ArrayList<Comment> commentsMockArray = new ArrayList<>();
        when(localAlbumRepo.LoadCommentsForAFrame(anyString())).thenReturn(mockComments);

        AlbumModel.loadedCommentsCallback commentsCallback = mock(AlbumModel.loadedCommentsCallback.class);

        albumModel.GetCommentsForFrame("1", commentsCallback);
        verify(commentsCallback, times(2)).onLoaded(any(ArrayList.class));
    }

    @Test
    public void TestThatWeCanHandleFindingNodataAnywhere() {
        RemoteAlbumRepo remoteAlbumRepo = Mockito.mock(RemoteAlbumRepo.class);
        LocalAlbumRepo localAlbumRepo = Mockito.mock(LocalAlbumRepo.class);
        AlbumModelPresenterContract contract = Mockito.mock(AlbumModelPresenterContract.class);
        Context context = Mockito.mock(Context.class);
        FileManager fileManager = Mockito.mock(FileManager.class);
        when(localAlbumRepo.FindMediaFileRecord(any(String.class))).thenReturn(Mockito.mock(MediaRecordModel.class));
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        PreferencesAPI preferencesAPI = mock(PreferencesAPI.class);
        AlbumModel albumModel = new AlbumModel(remoteAlbumRepo, localAlbumRepo,context, fileManager, localProfileRepository, remoteProfileRepository, preferencesAPI);

        ArrayList<Comment> mockComments = new ArrayList<>();
        Comment comment1 = new Comment();
        comment1.setCommentText("Test");
        comment1.setUserId("1");
        comment1.setEntityId("12");
        comment1.setId("123");

        Comment comment2 = new Comment();
        comment2.setCommentText("Test");
        comment2.setUserId("1");
        comment2.setEntityId("12");
        comment2.setId("223");

        mockComments.add(comment1);
        mockComments.add(comment2);


        ArrayList<Comment> commentsMockArray = new ArrayList<>();
        when(remoteAlbumRepo.LoadCommentsForFrame(anyString())).thenReturn(commentsMockArray);
        when(localAlbumRepo.LoadCommentsForAFrame(anyString())).thenReturn(commentsMockArray);

        AlbumModel.loadedCommentsCallback commentsCallback = mock(AlbumModel.loadedCommentsCallback.class);

        albumModel.GetCommentsForFrame("1", commentsCallback);
        verify(commentsCallback, times(0)).onLoaded(any(ArrayList.class));
    }

    /**
     * This should be in the local / remote repo section.
     */
    @Test
    public void TestThatWeCanLoadFrameDetails() {
//        Context context = Mockito.mock(Context.class);
//        String result = getMockJSONFrameDetailsObject();
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(
//                        new MockClient(context, result, 200)
//                ).build();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                // .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//                .addConverterFactory(GsonConverterFactory.create())
//                .baseUrl(LoadBalancer.RequestServerAddress() + "/rest/")
//                .client(okHttpClient)
//                .build();
//
//        AlbumService albumService = retrofit.create(AlbumService.class);
//        AlbumModel albumModel = new AlbumModel(albumService);
//        FrameDetails details = albumModel.DownloadFrame("doesntmatter");
//        Assert.assertTrue(details.getChat().equals("null"));
    }

    @Test
    public void TestThatWeCanHandleBadResponse() {
//        Context context = Mockito.mock(Context.class);
//        String result = getMockJSONFrameDetailsObject();
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(
//                        new MockClient(context, result, 500)
//                ).build();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                // .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//                .addConverterFactory(GsonConverterFactory.create())
//                .baseUrl(LoadBalancer.RequestServerAddress() + "/rest/")
//                .client(okHttpClient)
//                .build();
//
//        AlbumService albumService = retrofit.create(AlbumService.class);
//        AlbumModel albumModel = new AlbumModel(albumService);
//        FrameDetails details = albumModel.LoadFrameDetails("doesntmatter");
//        Assert.assertTrue(details == null);
    }

    @Test
    public void TestThatWeCanHandleBadResponse2() {
//        Context context = Mockito.mock(Context.class);
//        String result = getBlankResponse();
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(
//                        new MockClient(context, result, 500)
//                ).build();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                // .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//                .addConverterFactory(GsonConverterFactory.create())
//                .baseUrl(LoadBalancer.RequestServerAddress() + "/rest/")
//                .client(okHttpClient)
//                .build();
//
//        AlbumService albumService = retrofit.create(AlbumService.class);
//        AlbumModel albumModel = new AlbumModel(albumService);
//        FrameDetails details = albumModel.LoadFrameDetails("doesntmatter");
//        Assert.assertTrue(details == null);
    }

    private String getMockJSONFrameDetailsObject() {
        return "{\"PlaceId\":\" \",\"Suburb\":\"null\",\"Latitude\":\"33.58720590975674\",\"City\":\"Mission Viejo\",\"Longitude\":\"-117.6715514659003\",\"DescPosY\":\"0\",\"TimeStamp\":\" \",\"DescPosX\":\"0\",\"Extension\":\".mp4\",\"UserId\":\"23800\",\"Chat\":\"null\",\"Country\":\"null\",\"Icon\":\" \",\"Id\":\"25554\",\"TrailId\":\"25464\"}";
    }
    private String getBlankResponse() {
        return "{}";
    }




}
