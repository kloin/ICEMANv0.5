package com.teamunemployment.breadcrumbs.caching;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Stolen from Platonius on stackOverflow, cheers mate.
 *
 * For more reference hit up:  http://stackoverflow.com/questions/10185898/using-disklrucache-in-android-4-0-does-not-provide-for-opencache-method
 */
public class Utils {
    public static final int IO_BUFFER_SIZE = 8 * 1024;

    private Utils() {};

    public static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    public static File getExternalCacheDir(Context context) {
        if (hasExternalCacheDir()) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    public static File GetExternalFacebookCacheDir(Context context) {
        if (locationExists(context.getExternalCacheDir().toString()+"/Facebook/")) {
            Log.d("UTILS", "Found facebook cache exists");
            return new File(context.getExternalCacheDir().toString()+"/Facebook/");
        }

        // Before Froyo we need to construct the external cache dir ourselves
        Log.d("UTILS", "Found facebook cache does not exist - creating it now");
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/Facebook/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    private static boolean locationExists(String location) {
        File file = new File(location);
        return file.exists();
    }

    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

}
