package com.teamunemployment.breadcrumbs.RandomUsefulShit;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.TypedValue;

import java.io.ByteArrayOutputStream;
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

    public static Bitmap AdjustBitmapToCorrectOrientation(boolean backCameraOpen, Bitmap bm) {
        if (backCameraOpen) {

            if (90 != 0 && bm != null) {
                Matrix m = new Matrix();

                m.setRotate(90, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
                try {
                    Bitmap b2 = Bitmap.createBitmap(
                            bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                    if (bm != b2) {
                        bm.recycle();
                        bm = b2;
                    }
                } catch (OutOfMemoryError ex) {
                    ex.printStackTrace();
                }
            }
        }
        // Otherwise its a front cam shot, rotate the other way.
        else {
            if (bm != null) {
                Matrix m = new Matrix();

                m.setRotate(270, (float) bm.getWidth()/2, (float) bm.getHeight() / 2);
                try {
                    Bitmap b2 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                    if (bm != b2) {
                        bm.recycle();
                        bm = b2;
                    }

                    // Our images keep being flipped?
                    Matrix flipHorizontalMatrix = new Matrix();
                    flipHorizontalMatrix.setScale(-1,1);
                    flipHorizontalMatrix.postTranslate(bm.getWidth(),0);
                    bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), flipHorizontalMatrix, true);
                } catch (OutOfMemoryError ex) {
                    throw ex;
                }
            }
        }

        return  bm;
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

    @Nullable
    public static Bitmap fetchBitmapFromLocalFile(String eventId, String mediaType, Context context) {
        Bitmap bitmap = null;
        // If video, idk what we are going to do.
        if (mediaType.equals(".mp4")) {
            // show a default thumbnail
            String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/"+eventId + ".mp4";

            long id = Utils.FetchContentIdFromFilePath(fileName, context.getContentResolver());
            ContentResolver crThumb = context.getContentResolver();
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize = 1;
            bitmap = MediaStore.Video.Thumbnails.getThumbnail(crThumb, id, MediaStore.Video.Thumbnails.MICRO_KIND, options);
        } else {
            // Grab the
            String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/"+eventId + ".jpg";

            bitmap = Utils.FetchScaledBitmapFromFile(fileName, 60, 60);

        }

        return bitmap;

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

    public int convertPixelToDp(int px, Context context) {
        int convertedDpSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context.getResources().getDisplayMetrics());
        return convertedDpSize;
    }

    public static long FetchContentIdFromFilePath(String path, ContentResolver contentResolver) {
            long videoId = 0; // not sure how we should handle this.
            //Log.d(TAG,"Loading file " + filePath);

            // This returns us content://media/external/videos/media (or something like that)
            // I pass in "external" because that's the MediaStore's name for the external
            // storage on my device (the other possibility is "internal")
            Uri videosUri = MediaStore.Video.Media.getContentUri("external");

            // Log.d(TAG,"videosUri = " + videosUri.toString());

            String[] projection = {MediaStore.Video.VideoColumns._ID};

            // TODO This will break if we have no matching item in the MediaStore.
            Cursor cursor = contentResolver.query(videosUri, projection, MediaStore.Video.VideoColumns.DATA + " LIKE ?", new String[] { path }, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(projection[0]);
            if (cursor.getCount() > 0) {
                videoId = cursor.getLong(columnIndex);
            }

            //Log.d(TAG,"Video ID is " + videoId);
            cursor.close();
            return videoId;
        }

    public static byte[] ConvertBitmapToByteArray(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

}
