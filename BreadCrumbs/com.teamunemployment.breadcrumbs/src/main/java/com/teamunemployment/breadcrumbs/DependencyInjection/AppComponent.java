package com.teamunemployment.breadcrumbs.DependencyInjection;


import com.teamunemployment.breadcrumbs.Album.AlbumView;
import com.teamunemployment.breadcrumbs.Album.LocalAlbumSummary.LocalAlbumView;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author Josiah Kendall.
 *
 */

@Singleton
@Component(modules = {AppModule.class, ComponentModule.class})
public interface AppComponent {
    void inject(AlbumView albumView);
    void injectView(LocalAlbumView localAlbumView);
}
