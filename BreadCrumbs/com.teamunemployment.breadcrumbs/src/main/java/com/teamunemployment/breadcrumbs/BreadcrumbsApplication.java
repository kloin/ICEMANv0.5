//package com.teamunemployment.breadcrumbs;
//
//import android.app.Application;
//import android.content.Context;
//import android.support.multidex.MultiDex;
//
//import com.squareup.leakcanary.LeakCanary;
//
///**
// * Created by jek40 on 5/05/2016.
// */
//public class BreadcrumbsApplication extends Application {
//    @Override public void onCreate() {
//        super.onCreate();
//        LeakCanary.install(this);
//    }
//
//    @Override
//    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(base);
//        MultiDex.install(this);
//    }
//}
