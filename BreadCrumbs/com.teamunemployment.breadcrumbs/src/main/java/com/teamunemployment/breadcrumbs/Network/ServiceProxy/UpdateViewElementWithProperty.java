package com.teamunemployment.breadcrumbs.Network.ServiceProxy;

import android.content.Context;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by aDirtyCanvas on 7/26/2015.
 */
public class UpdateViewElementWithProperty {
    private String TAG = "UPDATER";
    public void UpdateMultipleViews(final ArrayList<TextView> views, String nodeId, String nodeProperty, Context context){
        String url = LoadBalancer.RequestServerAddress() +"/rest/login/GetPropertyFromNode/"+nodeId+"/"+nodeProperty;
        AsyncDataRetrieval fetchDescription = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                if (result != null && !result.isEmpty())
                    for (TextView textview: views) {
                        textview.setText(result);
                    }
            }
        }, context);
        fetchDescription.execute();
    }
    public void UpdateTextViewElement(final TextView viewElementToUpdate, String nodeId, String nodeProperty, Context context) {
        String url = LoadBalancer.RequestServerAddress() +"/rest/login/GetPropertyFromNode/"+nodeId+"/"+nodeProperty;
        AsyncDataRetrieval fetchDescription = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                if (result != null && !result.isEmpty())
              viewElementToUpdate.setText(result);
            }
        }, context);
        fetchDescription.execute();
    }

    public void UpdateTextViewWithElementAndExtraString(final TextView viewElementToUpdate, String nodeId, String nodeProperty, final String extra, Context context) {
        String url = LoadBalancer.RequestServerAddress() +"/rest/login/GetPropertyFromNode/"+nodeId+"/"+nodeProperty;
        AsyncDataRetrieval fetchDescription = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                if (result != null && !result.isEmpty())
                    viewElementToUpdate.setText(result + extra);
            }
        }, context);
        fetchDescription.execute();
    }

    // This class fetches the ID of the image using the node as a reference - e.g get the coverId of a trail, then load that coverId into the image using glide
    public void UpdateImageViewElement(final ImageView imageToUpdate, String nodeId, String nodeProperty, final Context context) {
        String url = LoadBalancer.RequestServerAddress() +"/rest/login/GetPropertyFromNode/"+nodeId+"/"+nodeProperty;
        AsyncDataRetrieval fetchDescription = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                if (result != null && !result.isEmpty()) {
                    try {
                        Glide.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+result+".jpg").centerCrop().crossFade().into(imageToUpdate);
                    } catch (IllegalArgumentException ex) {
                        // standard "loading" once destroyed issue
                        Log.e("UPDATER", "Tried to update a view on a destroyed activity");
                    }
                }
            }
        }, context);
        fetchDescription.execute();
    }

    // This class fetches the ID of the image using the node as a reference - e.g get the coverId of a trail, then load that coverId into the image using glide
    public void UpdateImageViewElementAndHidePlaceholder(final ImageView imageToUpdate, String nodeId, String nodeProperty, final Context context, final View placeholder) {
        String url = LoadBalancer.RequestServerAddress() +"/rest/login/GetPropertyFromNode/"+nodeId+"/"+nodeProperty;
        AsyncDataRetrieval fetchDescription = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                if (result != null && !result.isEmpty()) {
                    try {
                        placeholder.setVisibility(View.GONE);
                        Glide.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+result+".jpg").centerCrop().crossFade().into(imageToUpdate);
                    } catch (IllegalArgumentException ex) {
                        // standard "loading" once destroyed issue
                        Log.e("UPDATER", "Tried to update a view on a destroyed activity");
                    }
                }
            }
        }, context);
        fetchDescription.execute();
    }

    public void UpdateEditTextElement(final EditText viewElementToUpdate, String nodeId, String nodeProperty, Context context) {
        String url = LoadBalancer.RequestServerAddress() +"/rest/login/GetPropertyFromNode/"+nodeId+"/"+nodeProperty;
        AsyncDataRetrieval fetchDescription = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
               if (result != null && !result.isEmpty()) {
                   Log.d(TAG, "Updating EditText element: " + viewElementToUpdate.toString() + " with text result: " + result);
                   viewElementToUpdate.setText(result);
               }
            }
        }, context);
        fetchDescription.execute();
    }

    public void UpdateTextElementWithUrl(final TextView viewElementToUpdate, String url, Context context) {
        AsyncDataRetrieval fetchDescription = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                if (result != null) {
                    Log.d(TAG, "Updating Text element: " + viewElementToUpdate.toString() + " with text result: " + result);
                    viewElementToUpdate.setText("("+result+")");
                }
            }
        }, context);
        fetchDescription.execute();
    }

    public void UpdateTextElementWithUrlAndAdditionalString(final TextView viewElementToUpdate, String url, final String extra, Context context) {
        AsyncDataRetrieval fetchDescription = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                if (result != null) {
                    viewElementToUpdate.setText(result + " " + extra);
                }
            }
        }, context);
        fetchDescription.execute();
    }


    public void UpdateCircularViewWithUrl(final CircleImageView imageView, String url) {
        AsyncRetrieveImage fetchDescription = new AsyncRetrieveImage(url, new AsyncRetrieveImage.RequestListener() {
            @Override
            public void onFinished(Bitmap result) {
                if (result != null) {
                    imageView.setImageBitmap(result);
                }
            }
        });
        fetchDescription.execute();
    }
}
