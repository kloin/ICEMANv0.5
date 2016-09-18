package com.teamunemployment.breadcrumbs.DependencyInjection;

import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;

import com.teamunemployment.breadcrumbs.Album.AlbumModel;
import com.teamunemployment.breadcrumbs.Album.AlbumPresenter;
import com.teamunemployment.breadcrumbs.Album.LocalAlbumSummary.Data.LocalAlbumSummaryRepo;
import com.teamunemployment.breadcrumbs.Album.LocalAlbumSummary.LocalAlbumModel;
import com.teamunemployment.breadcrumbs.Album.LocalAlbumSummary.LocalAlbumSummaryPresenter;
import com.teamunemployment.breadcrumbs.Album.repo.LocalAlbumRepo;
import com.teamunemployment.breadcrumbs.Album.repo.RemoteAlbumRepo;
import com.teamunemployment.breadcrumbs.AlbumDataSource;
import com.teamunemployment.breadcrumbs.BreadcrumbsTimer;
import com.teamunemployment.breadcrumbs.LocalTripRepo;
import com.teamunemployment.breadcrumbs.MediaPlayerWrapper;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.Profile.data.LocalProfileRepository;
import com.teamunemployment.breadcrumbs.Profile.data.RemoteProfileRepository;
import com.teamunemployment.breadcrumbs.RESTApi.AlbumService;
import com.teamunemployment.breadcrumbs.RESTApi.CrumbService;
import com.teamunemployment.breadcrumbs.RESTApi.FileManager;
import com.teamunemployment.breadcrumbs.RESTApi.NodeService;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Josiah Kendall
 *
 * The dagger module that holds our different componenets that we want injected.
 */
@Module
public class ComponentModule {

    @Provides
    DatabaseController provideDatabaseController(Application application) {
        return new DatabaseController(application.getApplicationContext());
    }

    @Provides
    FileManager provideFileManager(Application application) {
        return new FileManager(application.getApplicationContext());
    }

    @Provides
    Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(LoadBalancer.RequestServerAddress() + "/rest/")
                .build();
    }

    @Provides
    AlbumService provideAlbumService(Retrofit retrofit) {
        return  retrofit.create(AlbumService.class);
    }

    @Provides
    CrumbService provideCrumbService(Retrofit retrofit) {
        return retrofit.create(CrumbService.class);
    }

    @Provides
    RemoteAlbumRepo provideRemoteAlbumRepo(AlbumService albumService, NodeService nodeService) {
       return new RemoteAlbumRepo(albumService, nodeService);
    }

    @Provides
    LocalAlbumRepo provideLocalAlbumRepo(DatabaseController databaseController) {
        return new LocalAlbumRepo(databaseController);
    }

    @Provides
    Context provideContext(Application application) {
        return application.getApplicationContext();
    }

    @Provides
    LocalProfileRepository provideLocalProfileRepository(DatabaseController databaseController) {
        return new LocalProfileRepository(databaseController);
    }

    @Provides
    NodeService provideNodeService(Retrofit retrofit) {
        return retrofit.create(NodeService.class);
    }

    @Provides
    RemoteProfileRepository provideRemoteProfileRepository(NodeService nodeService) {
        return new RemoteProfileRepository(nodeService);
    }

    @Provides
    PreferencesAPI providePreferencesApi(Context context) {
        return new PreferencesAPI(context);
    }


    @Provides
    LocalTripRepo provideLocalTripRepo(DatabaseController databaseController) {
        return new LocalTripRepo(databaseController);
    }

    @Provides
    AlbumModel provideAlbumModel(RemoteAlbumRepo remoteAlbumRepo, LocalAlbumRepo localAlbumRepo,
                                 Context context, FileManager fileManager, LocalProfileRepository
                                         localProfileRepository,RemoteProfileRepository remoteProfileRepository,
                                 PreferencesAPI preferencesAPI, LocalTripRepo localTripRepo) {
        return new AlbumModel(remoteAlbumRepo, localAlbumRepo, context, fileManager, localProfileRepository,
                remoteProfileRepository, preferencesAPI, localTripRepo);
    }

    @Provides
    MediaPlayer provideMediaPlayer() {
        return new MediaPlayer();
    }

    @Provides
    MediaPlayerWrapper provideMediaPlayerWrapper(MediaPlayer mediaPlayer) {
        return new MediaPlayerWrapper(mediaPlayer);
    }

    @Provides
    AlbumDataSource provideAlbumDataSource(Context context) {
        return new AlbumDataSource(context);
    }

    @Provides
    AlbumPresenter provideAlbumPresenter(AlbumModel model, Application application, MediaPlayerWrapper mediaPlayerWrapper,
                                         AlbumDataSource albumDataSource, BreadcrumbsTimer timer) {
        return new AlbumPresenter(model, application.getApplicationContext(), mediaPlayerWrapper, albumDataSource, timer);
    }

    @Provides
    BreadcrumbsTimer provideBreadCrumbsTimer() {
        return new BreadcrumbsTimer();
    }


    @Provides
    LocalAlbumSummaryRepo provideLocalAlbumSummaryRepo(DatabaseController databaseController, PreferencesAPI preferencesAPI) {
        return new LocalAlbumSummaryRepo(databaseController, preferencesAPI);
    }

    @Provides
    LocalAlbumModel provideLocalAlbumModel(LocalAlbumSummaryRepo localAlbumSummaryRepo) {
        return new LocalAlbumModel(localAlbumSummaryRepo);
    }

    @Provides
    LocalAlbumSummaryPresenter provideLocalAlbumSummaryPresenter(LocalAlbumModel albumModel) {
        return new LocalAlbumSummaryPresenter(albumModel);
    }
}
