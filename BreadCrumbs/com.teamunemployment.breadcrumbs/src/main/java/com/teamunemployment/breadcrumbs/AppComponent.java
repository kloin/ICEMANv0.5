package com.teamunemployment.breadcrumbs;

import com.teamunemployment.breadcrumbs.Camera.CameraModel;
import com.teamunemployment.breadcrumbs.Camera.CameraPresenter;
import com.teamunemployment.breadcrumbs.Camera.CameraView;
import com.teamunemployment.breadcrumbs.Camera.CameraViewObjectContract;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author Josiah Kendall.
 *
 * Interface for dagger.
 */

@Component(modules = AppModule.class)
@Singleton
public interface AppComponent {
    CameraPresenter getCameraPresenter();
    CameraModel getCameraModel();
}
