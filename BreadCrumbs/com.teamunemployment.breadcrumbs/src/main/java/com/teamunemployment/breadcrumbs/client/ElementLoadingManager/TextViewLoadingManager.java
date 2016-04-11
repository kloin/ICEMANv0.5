package com.teamunemployment.breadcrumbs.client.ElementLoadingManager;

import android.content.Context;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.caching.TextCaching;

import org.json.JSONException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by jek40 on 9/04/2016.
 */
public class TextViewLoadingManager {

    public static void LoadTextView(final String url, final TextView textView, Context context) {
        boolean textSet = false;
        final TextCaching textCaching = new TextCaching(context);
        String text = textCaching.FetchCachedText(url);
        if (text!= null) {
            //set our
            textView.setText(text);
            textSet = true;
        }
        /*
            This part is a bit wierd. I want this method static so there is no class initialisation,
            so this is all done in a static method. If we have set the text, we want to just cache.
            If we have not set the textview text yet then we need to display the result too.
         */
        if (textSet) {
            AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
                @Override
                public void onFinished(String result) throws JSONException {
                    // Here we need to cache
                    textCaching.CacheText(url, result);
                }
            });
            asyncDataRetrieval.execute();
        } else {
            // Fetch the latest value from the db. This will replace the cache so next time we will be updated.
            AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
                @Override
                public void onFinished(String result) throws JSONException {
                    // Here we need to cache
                    textCaching.CacheText(url, result);
                    textView.setText(result);

                }
            });
            asyncDataRetrieval.execute();
        }
    }

    public static void LoadCircularImageView(final String url, final CircleImageView imageView, final Context context) {
        boolean imageSet = false;
        final TextCaching textCaching = new TextCaching(context);
        String[] splitString = url.split("/");
        final String key = splitString[splitString.length-1] + splitString[splitString.length-2];
        String id = textCaching.FetchCachedText(key);
        if (id!= null) {
            //Create a url using the id we just got.
            String imageUrl = LoadBalancer.RequestCurrentDataAddress() + "/images/"+id+"T.jpg";
            Picasso.with(context).load(imageUrl).placeholder(R.drawable.profileblank).into(imageView);
            imageSet = true;
        }
        /*
            This part is a bit wierd. I want this method static so there is no class initialisation,
            so this is all done in a static method. If we have set the text, we want to just cache.
            If we have not set the textview text yet then we need to display the result too.
         */
        if (imageSet) {
            AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
                @Override
                public void onFinished(String result) throws JSONException {
                    // Here we need to cache
                    textCaching.CacheText(key, result);
                }
            });
            asyncDataRetrieval.execute();
        } else {
            // Fetch the latest value from the db. This will replace the cache so next time we will be updated.
            AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
                @Override
                public void onFinished(String result) throws JSONException {
                    // Here we need to cache
                    textCaching.CacheText(key, result);
                    String imageUrl = LoadBalancer.RequestCurrentDataAddress() + "/images/"+result+"T.jpg";
                    Picasso.with(context).load(imageUrl).placeholder(R.drawable.profileblank).into(imageView);
                }
            });
            asyncDataRetrieval.execute();
        }

    }
}
