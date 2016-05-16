package com.teamunemployment.breadcrumbs.caching;

import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;

/**
 * Created by jek40 on 4/05/2016.
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
