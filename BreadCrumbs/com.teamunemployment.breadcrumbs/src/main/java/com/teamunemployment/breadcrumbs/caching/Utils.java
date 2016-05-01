package com.teamunemployment.breadcrumbs.caching;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
        if (locationExists(context.getExternalCacheDir().toString() + "/Facebook/")) {
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

    /**
     * Gets the real path from file
     * @param context
     * @param contentUri
     * @return path
     */
    public static String getRealPathFromURI(Context context, Uri contentUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return getPathForV19AndUp(context, contentUri);
        } else {
            return getPathForPreV19(context, contentUri);
        }
    }

    /**
     * Handles pre V19 uri's
     * @param context
     * @param contentUri
     * @return
     */
    public static String getPathForPreV19(Context context, Uri contentUri) {
        String res = null;

        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();

        return res;
    }

    /**
     * Handles V19 and up uri's
     * @param context
     * @param contentUri
     * @return path
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPathForV19AndUp(Context context, Uri contentUri) {
        String wholeID = DocumentsContract.getDocumentId(contentUri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];
        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";
        Cursor cursor = context.getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        column, sel, new String[]{ id }, null);

        String filePath = "";
        int columnIndex = cursor.getColumnIndex(column[0]);
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }

        cursor.close();
        return filePath;
    }

    /*
    ===============================================================================
    *********************   Writing files utils ********************************
    =============================================================================
     */
    // Save vido. Juest taking away some abstraction level here.
    public static boolean SaveVideo(String fileName, byte[] arrayOfShit) throws IOException {
        writeByteArrayToDisk(fileName, arrayOfShit);
        return true;
    }

    // Write a byte array to a file. Used by video and image
    private static boolean writeByteArrayToDisk(String fileName, byte[] arrayOfShit) throws IOException {
        FileOutputStream fos=new FileOutputStream(fileName);
        fos.write(arrayOfShit);
        fos.close();
        return true;
    }


    // Create byte array from bitmap. Saved as PNG format at full quality.
    public static boolean SaveBitmap(String fileName, Bitmap media) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        media.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        try{
            writeByteArrayToDisk(fileName, byteArray);
        } catch (IOException ex) {
            Log.d("FILESAVE", "Saving bitmap failed");
            ex.printStackTrace();
            return false;
        }
        return true;
    }

}
