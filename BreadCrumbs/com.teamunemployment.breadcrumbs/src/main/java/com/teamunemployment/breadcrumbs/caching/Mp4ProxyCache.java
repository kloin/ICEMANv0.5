package com.teamunemployment.breadcrumbs.caching;

import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;

/**
 * Singleton for getting our proxy cache server for caching videos.
 */
public class Mp4ProxyCache {

    private static HttpProxyCacheServer proxyCacheServer;

    public static HttpProxyCacheServer GetProxy(Context context) {

        if (proxyCacheServer == null) {
            proxyCacheServer = new HttpProxyCacheServer(context);
        }

        return proxyCacheServer;
    }

}
