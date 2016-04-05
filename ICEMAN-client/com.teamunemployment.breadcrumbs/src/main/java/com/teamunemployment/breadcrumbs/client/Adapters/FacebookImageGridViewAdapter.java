package com.teamunemployment.breadcrumbs.client.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.Cards.ImageChooserGridViewAdapter;

import java.util.ArrayList;

/**
 * The adapter to load up all the facebook images.
 */
public class FacebookImageGridViewAdapter extends ImageChooserGridViewAdapter {
    public FacebookImageGridViewAdapter(ArrayList<String> myDataset, Context context) {
        super(myDataset, context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String id = imageIds.get(position);
        View gridView;
        String url =  "https://graph.facebook.com/"+id+"/picture?type=large";
        if (convertView == null) {
            gridView = new View(context);
            gridView = inflater.inflate(R.layout.grid_image_layout, null);

            // set image based on selected text
            ImageView imageView = (ImageView) gridView
                    .findViewById(R.id.grid_image);

            Glide.with(context).load(url).centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);
            // Glide.with(context).load("http://placehold.it/350x150").centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);
        } else {
            gridView = (View) convertView;
            ImageView imageView = (ImageView) gridView
                    .findViewById(R.id.grid_image);
            //Glide.with(context).load("http://placehold.it/350x150").centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);
           imageView.setImageBitmap(null);
        }

        return gridView;
    }
}
