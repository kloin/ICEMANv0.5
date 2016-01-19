package com.breadcrumbs.ServiceProxy;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.R;
import com.bumptech.glide.Glide;
import com.pkmmte.view.CircularImageView;

/**
 * Created by aDirtyCanvas on 7/26/2015.
 */
public class UpdateViewElementWithProperty {

    public void UpdateTextViewElement(final TextView viewElementToUpdate, String nodeId, String nodeProperty) {
        String url = LoadBalancer.RequestServerAddress() +"/rest/login/GetPropertyFromNode/"+nodeId+"/"+nodeProperty;
        AsyncDataRetrieval fetchDescription = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                if (result != null && !result.isEmpty())
              viewElementToUpdate.setText(result);
            }
        });
        fetchDescription.execute();
    }

    public void UpdateTextViewWithElementAndExtraString(final TextView viewElementToUpdate, String nodeId, String nodeProperty, final String extra) {
        String url = LoadBalancer.RequestServerAddress() +"/rest/login/GetPropertyFromNode/"+nodeId+"/"+nodeProperty;
        AsyncDataRetrieval fetchDescription = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                if (result != null && !result.isEmpty())
                    viewElementToUpdate.setText(result + extra);
            }
        });
        fetchDescription.execute();
    }

    // This class fetches the ID of the image using the node as a reference - e.g get the coverId of a trail, then load that coverId into the image using glide
    public void UpdateImageViewElement(final ImageView imageToUpdate, String nodeId, String nodeProperty, final Context context) {
        String url = LoadBalancer.RequestServerAddress() +"/rest/login/GetPropertyFromNode/"+nodeId+"/"+nodeProperty;
        AsyncDataRetrieval fetchDescription = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                if (result != null && !result.isEmpty())
                    Glide.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+result+".jpg").centerCrop().crossFade().into(imageToUpdate);
            }
        });
        fetchDescription.execute();
    }

    public void UpdateEditTextElement(final EditText viewElementToUpdate, String nodeId, String nodeProperty) {
        String url = LoadBalancer.RequestServerAddress() +"/rest/login/GetPropertyFromNode/"+nodeId+"/"+nodeProperty;
        AsyncDataRetrieval fetchDescription = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
               if (result != null && !result.isEmpty()) {
                   viewElementToUpdate.setText(result);
               }
            }
        });
        fetchDescription.execute();
    }

    public void UpdateTextElementWithUrl(final TextView viewElementToUpdate, String url) {
        AsyncDataRetrieval fetchDescription = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                if (result != null) {
                    viewElementToUpdate.setText("("+result+")");
                }
            }
        });
        fetchDescription.execute();
    }

    public void UpdateCircularViewWithUrl(final CircularImageView imageView, String url) {
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
