package com.teamunemployment.breadcrumbs.RandomUsefulShit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by jek40 on 10/03/2016.
 */
public class Utils {
    public static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    public static String writeBitmapToDisk(Bitmap bmp, String file) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }


    /*
        Returns a bitmap from the specified location. Returns null if things go bad. Will most likely
        throw an exception if the filename is not pointing to any file so be wary of that.
     */
    @Nullable
    public static Bitmap FetchRawBitmapFromFile(String fileName) {
        Bitmap bitmap = null;
        File image = new File(fileName);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        return bitmap;
    }


    /*
        Return a 'thumbnail' of a bitmap with a scaled size 120x120. Use alternative scaled method
        if you want to scale the bitmap to custom sizes.

     *  @throws IllegalArgumentException if width is <= 0, or height is <= 0
     */
    @Nullable
    public static Bitmap FetchBitmapThumbnailFromFile(String fileName) {
        Bitmap bitmap = null;
        File image = new File(fileName);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        // Filter by default as I think it creates better results when downsizing.
        return Bitmap.createScaledBitmap(bitmap, 120, 120,true);
    }

    /*
        Fetch a bitmap with specified witdth and height scaling

        @Returns A bitmap or nothing if you give it a bad URL
        @throws IllegalArgumentException if width is <= 0, or height is <= 0
     */
    @Nullable
    public static Bitmap FetchScaledBitmapFromFile(String fileName, int width, int height) {
        Bitmap bitmap = null;
        File image = new File(fileName);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        // Filter by default as I think it creates better results when downsizing.
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    public static String FetchLocalPathToImageFile(String eventId) {
       return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/"+eventId + ".jpg";
    }

    public static String FetchLocalPathToVideoFile(String eventId) {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/"+eventId + ".mp4";
    }

    /**
     * A Simple method to determine if we are currently Running on the Ui Thread.
     * @return True if we are on UI thread, false if we are not.
     */
    public static boolean WeAreRunningOnTheUIThread() {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            return true;
        } else {
            return false;
        }
    }
}
