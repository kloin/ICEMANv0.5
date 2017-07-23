package com.teamunemployment.breadcrumbs.client.Cards;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils;

import java.io.File;
import java.util.ArrayList;

/**
 * Written by Josiah Kendall.
 *
 * Adapter for the gridview that we use to show images. This was designed with the
 * image selector in mind
 */
public class ImageChooserGridViewAdapter extends BaseAdapter {
    public ArrayList<String> imageIds;
    public Context context;

    public ImageChooserGridViewAdapter(ArrayList<String> myDataset, Context context) {
        imageIds = myDataset;
        this.context = context;
    }

    @Override
    public int getCount() {
        return imageIds.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String id = imageIds.get(position);
        View gridView;

        if (convertView == null) {
            gridView = new View(context);
            gridView = inflater.inflate(R.layout.grid_image_layout, null);

            // set image based on selected text
            ImageView imageView = (ImageView) gridView
                    .findViewById(R.id.grid_image);

            if (id.endsWith("L")) {
                id =  id.substring(0, id.length()-1);
                File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File image = new File(sd, id + ".jpg");
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                imageView.setImageBitmap(bitmap);
                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                params.height = params.width;
                imageView.setLayoutParams(params);
            } else {
                Picasso.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+id+".jpg").fit().centerCrop().into(imageView);
            }

        } else {
            gridView = (View) convertView;
            ImageView imageView = (ImageView) gridView
                    .findViewById(R.id.grid_image);
            if (id.endsWith("L")) {
                id =  id.substring(0, id.length()-1);
                File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File image = new File(sd, id + ".jpg");
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                imageView.setImageBitmap(bitmap);
                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                params.height = params.width;
                imageView.setLayoutParams(params);

            } else {
                Picasso.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+id+".jpg").fit().centerCrop().into(imageView);
            }
        }

        return gridView;
    }

}
