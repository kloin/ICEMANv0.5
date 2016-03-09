package com.teamunemployment.breadcrumbs.client.Cards;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Written by Josiah Kendall.
 *
 * Adapter for the gridview that we use to show images. This was designed with the
 * image selector in mind
 */
public class ImageChooserGridViewAdapter extends BaseAdapter {
    private ArrayList<String> imageIds;
    private Context context;

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

            Glide.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+id+".jpg").centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);
           // Glide.with(context).load("http://placehold.it/350x150").centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);
        } else {
            gridView = (View) convertView;
            ImageView imageView = (ImageView) gridView
                    .findViewById(R.id.grid_image);
            //Glide.with(context).load("http://placehold.it/350x150").centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);
            Glide.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+id+".jpg").centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);
        }

        return gridView;
    }
}
