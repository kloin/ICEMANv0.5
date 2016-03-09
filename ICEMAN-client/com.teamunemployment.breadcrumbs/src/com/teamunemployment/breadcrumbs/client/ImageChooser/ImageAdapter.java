package com.teamunemployment.breadcrumbs.client.ImageChooser;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncRetrieveImage;
import com.teamunemployment.breadcrumbs.R;

import java.util.ArrayList;

/**
 * Created by aDirtyCanvas on 7/2/2015.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> thumbNailUrls;
    private ArrayList<String> thumbNails;
    public ImageAdapter(Context c, ArrayList<String> thumbNailUrls) {
        mContext = c;
        this.thumbNailUrls = thumbNailUrls;
        this.thumbNails = new ArrayList<String>();
    }

    private final int[] icons = {
            R.drawable.bird100,
            R.drawable.bearfootprint100,
            R.drawable.cat100,
            R.drawable.catfootprint100,
            R.drawable.coral100,
            R.drawable.dogbone100,
            R.drawable.dogbowl100,
            R.drawable.doghouse100,
            R.drawable.duck100,
            R.drawable.gorilla100,
            R.drawable.insect100,
            R.drawable.octopus100,
            R.drawable.pig100,
            R.drawable.prawn100,
            R.drawable.runningrabit100
        };

    public int getCount() {
        return icons.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(2, 2, 2, 2);
        } else {
            imageView = (ImageView) convertView;
        }
        // Here I load image based on the url.
        int id = icons[position];
        imageView.setImageResource(id);
        //loadImage(url, imageView);
        return imageView;
    }
// Not used at the moment
    private void loadImage(final String imageId, final ImageView imageView) {
        AsyncRetrieveImage asyncFetch = new AsyncRetrieveImage(imageId, new AsyncRetrieveImage.RequestListener() {
            @Override
            public void onFinished(Bitmap result) {
                imageView.setImageBitmap(result);

            }
        });
        asyncFetch.execute();
    }


}