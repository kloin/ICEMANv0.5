package com.teamunemployment.breadcrumbs;

import com.teamunemployment.breadcrumbs.Camera.CameraPresenter;
import com.teamunemployment.breadcrumbs.Camera.CameraView;

import dagger.Module;
import dagger.Provides;

/**
 * Created by jek40 on 31/07/2016.
 */

@Module
public class AppModule {

    private App app;

    public AppModule(App app) {
        this.app = app;
    }

//    @Provides
//    public CameraPresenter providePresenter() {
//        return
//    }
}
