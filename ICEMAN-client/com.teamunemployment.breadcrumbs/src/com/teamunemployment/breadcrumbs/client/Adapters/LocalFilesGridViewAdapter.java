package com.teamunemployment.breadcrumbs.client.Adapters;

import android.app.Activity;
import android.content.Context;
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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Class that is designed to show the local files on our phone. These can be selected and saved
 * for things like trail cover photos and profile pictures.
 */
public class LocalFilesGridViewAdapter extends ImageChooserGridViewAdapter {

    public LocalFilesGridViewAdapter(ArrayList<String> myDataset, Context context) {
        super(myDataset, context);
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
            final Uri uri = Uri.parse("file://" + url);

            // Need to do this shit in a thread
            new Thread(new Runnable() {
                public void run() {

                        final BitmapFactory.Options thumbOpts = new BitmapFactory.Options();
                        thumbOpts.inSampleSize = 2;

                        final Bitmap adjustedBitmap = correctOrientation(url);
                        // bm = Bitmap.createScaledBitmap(bm, 120, 120, false);
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

            //Glide.with(context).load(url).centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);
            // Glide.with(context).load("http://placehold.it/350x150").centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);
        } else {
            gridView = (View) convertView;

            final ImageView imageView = (ImageView) gridView
                    .findViewById(R.id.grid_image);
            imageView.setImageBitmap(null);

            final Uri uri = Uri.parse("file://" + url);

            new Thread(new Runnable() {
                public void run() {
                    BitmapFactory.Options thumbOpts = new BitmapFactory.Options();
                    thumbOpts.inSampleSize = 5;
                     final Bitmap bm2 = correctOrientation(url);
                    Activity contextAct = (Activity) context;
                    contextAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bm2);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                    });
                }

            }).start();

            //Glide.with(context).load("http://placehold.it/350x150").centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);
            //Glide.with(context).load(url).centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);
        }
        return gridView;
    }

    private Bitmap correctOrientation(String url) {
        try {
            final BitmapFactory.Options thumbOpts = new BitmapFactory.Options();
            thumbOpts.inSampleSize = 3;
            Bitmap bm = BitmapFactory.decodeFile(url, thumbOpts);
            ExifInterface exif = null;
            exif = new ExifInterface(url);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = Utils.exifToDegrees(rotation);
            Matrix matrix = new Matrix();
            if (rotation != 0f) {
                matrix.preRotate(rotationInDegrees);
            }
            final Bitmap adjustedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            return adjustedBitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
