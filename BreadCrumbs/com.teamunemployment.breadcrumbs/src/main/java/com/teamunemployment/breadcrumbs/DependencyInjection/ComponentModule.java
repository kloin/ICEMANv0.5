package com.teamunemployment.breadcrumbs.DependencyInjection;

import android.app.Application;
import android.content.Context;

import com.teamunemployment.breadcrumbs.Album.AlbumModel;
import com.teamunemployment.breadcrumbs.Album.AlbumModelPresenterContract;
import com.teamunemployment.breadcrumbs.Album.AlbumPresenter;
import com.teamunemployment.breadcrumbs.Album.AlbumPresenterViewContract;
import com.teamunemployment.breadcrumbs.Album.repo.LocalAlbumRepo;
import com.teamunemployment.breadcrumbs.Album.repo.RemoteAlbumRepo;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.RESTApi.AlbumService;
import com.teamunemployment.breadcrumbs.RESTApi.FileManager;
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
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(LoadBalancer.RequestServerAddress() + "/rest/")
                .build();
        return retrofit;
    }

    @Provides
    AlbumService provideAlbumService(Retrofit retrofit) {
        return  retrofit.create(AlbumService.class);
    }

    @Provides
    RemoteAlbumRepo provideRemoteAlbumRepo(AlbumService albumService) {
       return new RemoteAlbumRepo(albumService);
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
    AlbumModel provideAlbumModel(RemoteAlbumRepo remoteAlbumRepo, LocalAlbumRepo localAlbumRepo,
                                 Context context,
                                 FileManager fileManager) {
        return new AlbumModel(remoteAlbumRepo, localAlbumRepo, context, fileManager);
    }

    @Provides
    AlbumPresenter provideAlbumPresenter(AlbumModel model, Application application) {
        return new AlbumPresenter(model, application.getApplicationContext());
    }
}
