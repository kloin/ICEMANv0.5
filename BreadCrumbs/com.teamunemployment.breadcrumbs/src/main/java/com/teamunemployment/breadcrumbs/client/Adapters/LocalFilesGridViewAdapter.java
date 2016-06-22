package com.teamunemployment.breadcrumbs.client.Adapters;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils;
import com.teamunemployment.breadcrumbs.client.Cards.ImageChooserGridViewAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Class that is designed to show the local files on our phone. These can be selected and saved
 * for things like trail cover photos and profile pictures.
 */
public class LocalFilesGridViewAdapter extends ImageChooserGridViewAdapter {
    private ArrayList<String> myDataSet;
    public LocalFilesGridViewAdapter(ArrayList<String> myDataset, Context context) {
        super(myDataset, context);
        this.myDataSet = myDataset;
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String id = imageIds.get(position);
        View gridView;
        final String url = id;
        if (convertView == null) {
            gridView = new View(context);
            gridView = inflater.inflate(R.layout.grid_image_layout, null);

            final ImageView imageView = (ImageView) gridView
                    .findViewById(R.id.grid_image);
            imageView.setImageBitmap(null);

            Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            final Uri uri = Uri.parse(images + "/" + myDataSet.get(position));
            doImageWork(uri, imageView);



            //Glide.with(context).load(url).centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);
            // Glide.with(context).load("http://placehold.it/350x150").centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);
        } else {
            gridView = (View) convertView;

            final ImageView imageView = (ImageView) gridView
                    .findViewById(R.id.grid_image);
            imageView.setImageBitmap(null);

            Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            final Uri uri = Uri.parse(images + "/" + myDataSet.get(position));
            doImageWork(uri, imageView);

            //Glide.with(context).load("http://placehold.it/350x150").centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);
            //Glide.with(context).load(url).centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);
        }
        return gridView;
    }

    private Bitmap correctOrientation(Uri uri) {
        try {
            final BitmapFactory.Options thumbOpts = new BitmapFactory.Options();
            thumbOpts.inSampleSize = 3;
            Bitmap bm = getThumbnail(uri);
            if (bm == null) {
                //Cant load
                return null;
            }
            // NEED TO TEST THIS ON PRE V19
            int rotation = getOrientation(context, uri);
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

    private void doImageWork(final Uri uri, final ImageView imageView) {
        // Need to do this shit in a thread
        new Thread(new Runnable() {
            public void run() {

                final BitmapFactory.Options thumbOpts = new BitmapFactory.Options();
                thumbOpts.inSampleSize = 2;

                final Bitmap adjustedBitmap = correctOrientation(uri);
                //final Bitmap bm= Bitmap.createScaledBitmap(adjustedBitmap, 120, 120, false);
                Activity contextAct = (Activity) context;
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

    private Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException{
        InputStream input = context.getContentResolver().openInputStream(uri);

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
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }
}
