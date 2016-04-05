package com.teamunemployment.breadcrumbs.caching;

import android.os.AsyncTask;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;

/**
 * Created by jek40 on 30/11/2015.
 */
public class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
    private DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB

    @Override
    protected Void doInBackground(File... params) {
        synchronized (mDiskCacheLock) {
            File cacheDir = params[0];
            try {
                mDiskLruCache = DiskLruCache.open(cacheDir, 1, DISK_CACHE_SIZE, DISK_CACHE_SIZE);
                mDiskCacheStarting = false; // Finished initialization
                mDiskCacheLock.notifyAll(); // Wake any waiting threads
            } catch (IOException e) {
                // This is an issue, need to log this
                e.printStackTrace();
                Log.e("CACHE", "Failed to open the LRU cache. Check doInBackground of InitDiskCacheTask class.");
            }
        }
        return null;
    }
}
