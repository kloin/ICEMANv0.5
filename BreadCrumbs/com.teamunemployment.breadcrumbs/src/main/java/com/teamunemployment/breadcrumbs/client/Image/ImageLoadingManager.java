package com.teamunemployment.breadcrumbs.client.Image;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jek40 on 7/04/2016.
 */
public class ImageLoadingManager {

    private Context mContext;

    public ImageLoadingManager(Context context) {
            mContext = context;
    }

    public Bitmap correctOrientation(Uri uri) {
        try {
            final BitmapFactory.Options thumbOpts = new BitmapFactory.Options();
            thumbOpts.inSampleSize = 2;
            Bitmap bm = getThumbnail(uri);

            // NEED TO TEST THIS ON PRE V19
            int rotation = getOrientation(mContext, uri);
            //int rotationInDegrees = Utils.exifToDegrees(rotation);
            Matrix matrix = new Matrix();
            if (rotation != 0) {
                matrix.preRotate(rotation);
            }
            final Bitmap adjustedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            return adjustedBitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public Bitmap correctOrientationForBitmap(Bitmap bm, Uri uri) {


            // NEED TO TEST THIS ON PRE V19
            int rotation = getOrientation(mContext, uri);
            //int rotationInDegrees = Utils.exifToDegrees(rotation);
            Matrix matrix = new Matrix();
            if (rotation != 0) {
                matrix.preRotate(rotation);
            }
            final Bitmap adjustedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            return adjustedBitmap;

    }

    public int getOrientation(Context context, Uri photoUri) {
    /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        int result = -1;
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                result = cursor.getInt(0);
            }
            cursor.close();
        }

        return result;
    }

    public void doImageWork(final Uri uri, final ImageView imageView) {
        // Need to do this shit in a thread
        new Thread(new Runnable() {
            public void run() {

                final BitmapFactory.Options thumbOpts = new BitmapFactory.Options();
                thumbOpts.inSampleSize = 2;

                final Bitmap adjustedBitmap = correctOrientation(uri);
                //final Bitmap bm= Bitmap.createScaledBitmap(adjustedBitmap, 120, 120, false);
                Activity contextAct = (Activity) mContext;
                contextAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(adjustedBitmap);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                });
            }
        }).start();
    }

    public Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException{
        InputStream input = mContext.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > 240) ? (originalSize / 240) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither=true;//optional
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        input = mContext.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    public Bitmap GetFull720Bitmap(Uri uri) throws IOException{
        InputStream input = mContext.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > 720) ? (originalSize / 720) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither=true;//optional
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        input = mContext.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        bitmap = correctOrientationForBitmap(bitmap, uri);
        input.close();
        return bitmap;
    }

    private int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }

}
