package com.teamunemployment.breadcrumbs.caching;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.BuildConfig;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncRetrieveImage;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Josiah Kendall on 30/11/2015.
 *
 * CURRENTLY USING GLIDE RATHER THAN THIS
 *
 * Class which handles caching and fetching of bitmaps. Might need to abstract some sort of dev friendly
 * interface from this mess, as everything is currently at the same level.
 */
@Deprecated
public class BitmapCachingMaster {
    private DiskLruCache mDiskLruCache;
    private LruCache<String, Bitmap> mMemoryCache;
    private static BitmapCachingMaster bitmapCachingMasterInstance;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "images";
    // Standard singleton stuff
    private BitmapCachingMaster(Context context) {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

        // Start up disk cache on async thread
        File cacheDir = getDiskCacheDir(context, DISK_CACHE_SUBDIR);
        new InitDiskCacheTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cacheDir);
    }

    public static BitmapCachingMaster GetBitmapCachingMasterInstance(Context context) {

        if (bitmapCachingMasterInstance == null) {
            bitmapCachingMasterInstance = new BitmapCachingMaster(context);
        }

        return bitmapCachingMasterInstance;
    }
    // END OF SINGLETON PATTERN *********************************************************

    // What we need right here.
    public void LoadBitmap(ProgressBar progressBar, String nodeId, ImageView imageView) {
        /*
            Order of priority:
                - Memory Cache
                - Disk Cache
                - Server
         */

        final Bitmap bitmap = getBitmapFromMemCache(nodeId);
        if (bitmap != null) {
            imageView.setImageResource(R.drawable.ben);
            //imageView.setImageBitmap(bitmap);
            // Also chuck it in our disk cache (if it doesnt already exist).
            put(nodeId, bitmap);
            imageView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else {
            //Try get from disk and if that fails, the server
            BitmapWorkerTask task = new BitmapWorkerTask(imageView, nodeId, progressBar);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /*
        Task used to load a bitmap. Attemps to load from mem cache first, then disk, then finally
        from network.
        Doing this inside an async as not to freeze UI thread. Network request is also async.
     */
    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {

        private ImageView imageView;
        private String serverId;
        private String imageKey;
        private ProgressBar progressBar;

        public BitmapWorkerTask(ImageView imageView, String nodeKey, ProgressBar progressBar){
            this.imageView = imageView;
            this.progressBar = progressBar;
            this.serverId = nodeKey;
            this.imageKey = nodeKey;
        }

        @Override
        protected void onPostExecute(Bitmap image) {
            if (image == null) {
                // Fetch our image from the server
                AsyncRetrieveImage asyncFetch = new AsyncRetrieveImage(serverId, new AsyncRetrieveImage.RequestListener() {
                    @Override
                    public void onFinished(Bitmap result) {
                        //imageView.setImageBitmap(result);
                        imageView.setImageResource(R.drawable.ben);
                        addBitmapToCache(imageKey, result);
                        imageView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                });
                asyncFetch.execute();
            } else {
                imageView.setImageBitmap(image);
            }
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {

            // Check disk cache in background thread
            Bitmap bitmap = getBitmapFromDiskCache(imageKey);

            if (bitmap == null) { // Not found in disk cache
                return null;
            }

            // Add final bitmap to caches
            addBitmapToCache(imageKey, bitmap);

            return bitmap;
        }
    }


    /*
        Class used to create disk cache.
     */
    class InitDiskCacheTask extends AsyncTask<File, Void, Void> {

        @Override
        protected Void doInBackground(File... params) {
            synchronized (mDiskCacheLock) {
                File cacheDir = params[0];
                try {
                    mDiskLruCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE);
                    mDiskCacheStarting = false; // Finished initialization
                    mDiskCacheLock.notifyAll(); // Wake any waiting threads
                } catch (IOException e) {
                    // This is an issue, need to log this
                    e.printStackTrace();
                    Log.e("D CACHE", "Failed to open the LRU cache. Check doInBackground of InitDiskCacheTask class.");
                }
            }
            return null;
        }
    }

    // Add to the memCache and disk cache in synchronised fashion.
    public void addBitmapToCache(String key, Bitmap bitmap) {
        // Add to memory cache as before
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }

        // Also add to disk cache
        synchronized (mDiskCacheLock) {
            try {
                if (mDiskLruCache != null && mDiskLruCache.get(key) == null) {
                    put(key, bitmap);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap getBitmapFromDiskCache(String key) {
        synchronized (mDiskCacheLock) {
            // Wait while disk cache is started from background thread
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                    Log.e("D CACHE","Failed to retrieve from DISKCACHE");
                }
            }
            if (mDiskLruCache != null) {
                return getBitmap(key);

            }
        }
        return null;
    }

    // Add to memCache.
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    // get from memCache.
    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    private boolean writeBitmapToFile( Bitmap bitmap, DiskLruCache.Editor editor )
            throws IOException, FileNotFoundException {
        OutputStream out = null;

        // These are basically just for reference as to what the numbers are.
        Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG; // For now
        int mCompressQuality = 100;
        try {
            out = new BufferedOutputStream( editor.newOutputStream( 0 ), Utils.IO_BUFFER_SIZE );
            return bitmap.compress( mCompressFormat, mCompressQuality, out );
        } finally {
            if ( out != null ) {
                out.close();
            }
        }
    }

    private File getDiskCacheDir(Context context, String uniqueName) {

        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Utils.isExternalStorageRemovable() ?
                        Utils.getExternalCacheDir(context).getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    public void put( String key, Bitmap data ) {

        DiskLruCache.Editor editor = null;
        try {
            editor = mDiskLruCache.edit( key );
            if ( editor == null ) {
                return;
            }

            if( writeBitmapToFile( data, editor ) ) {
                mDiskLruCache.flush();
                editor.commit();
                if ( BuildConfig.DEBUG ) {
                    Log.d( "cache_test_DISK_", "image put on disk cache " + key );
                }
            } else {
                editor.abort();
                if ( BuildConfig.DEBUG ) {
                    Log.d( "cache_test_DISK_", "ERROR on: image put on disk cache " + key );
                }
            }
        } catch (IOException e) {
            if ( BuildConfig.DEBUG ) {
                Log.d( "cache_test_DISK_", "ERROR on: image put on disk cache " + key );
            }
            try {
                if ( editor != null ) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }
        }

    }

    public Bitmap getBitmap( String key ) {

        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot = null;
        try {

            snapshot = mDiskLruCache.get( key );
            if ( snapshot == null ) {
                return null;
            }
            final InputStream in = snapshot.getInputStream( 0 );
            if ( in != null ) {
                final BufferedInputStream buffIn =
                        new BufferedInputStream( in, Utils.IO_BUFFER_SIZE );
                bitmap = BitmapFactory.decodeStream(buffIn);
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        if ( BuildConfig.DEBUG ) {
            Log.d( "cache_test_DISK_", bitmap == null ? "" : "image read from disk " + key);
        }

        return bitmap;

    }

    public boolean containsKey( String key ) {

        boolean contained = false;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskLruCache.get( key );
            contained = snapshot != null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        return contained;

    }

    public void clearCache() {
        if ( BuildConfig.DEBUG ) {
            Log.d("cache_test_DISK_", "disk cache CLEARED");
        }
        try {
            mDiskLruCache.delete();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public File getCacheFolder() {
        return mDiskLruCache.getDirectory();
    }
}


