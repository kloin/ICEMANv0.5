package com.teamunemployment.breadcrumbs.DependencyInjection;

import android.app.Activity;

import com.teamunemployment.breadcrumbs.Album.AlbumView;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author Josiah Kendall.
 *
 */

@Singleton
@Component(modules = {AppModule.class, ComponentModule.class})
public interface AppComponent {
    void inject(Activity albumView);
}
