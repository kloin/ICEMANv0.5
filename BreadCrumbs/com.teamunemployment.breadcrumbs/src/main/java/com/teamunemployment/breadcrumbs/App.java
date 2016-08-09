package com.teamunemployment.breadcrumbs;

import android.app.Application;

import com.teamunemployment.breadcrumbs.DependencyInjection.AppComponent;
import com.teamunemployment.breadcrumbs.DependencyInjection.AppModule;
import com.teamunemployment.breadcrumbs.DependencyInjection.ComponentModule;
import com.teamunemployment.breadcrumbs.DependencyInjection.DaggerAppComponent;

/**
 * @author Josiah Kendall
 * Application class
 */
public class App extends Application {
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        appComponent = DaggerAppComponent.builder()
                // list of modules that are part of this component need to be created here too
                .appModule(new AppModule(this)) // This also corresponds to the name of your module: %component_name%Module
                .componentModule(new ComponentModule())
                .build();

        // If a Dagger 2 component does not have any constructor arguments for any of its modules,
        // then we can use .create() as a shortcut instead:
        //  mNetComponent = com.codepath.dagger.components.DaggerNetComponent.create();
    }

    public AppComponent getNetComponent() {
        return appComponent;
    }
}
