package com.teamunemployment.breadcrumbs.client.Adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UpdateViewElementWithProperty;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.caching.TextCachingInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Written by Josiah Kendall, February 2016.
 *
 * This class is an adapter to create and manage editable crumb instances for the EditMyTrail Class
 */
public class CrumbCardEditAdapter extends RecyclerView.Adapter<CrumbCardEditAdapter.ViewHolder> {
    private ArrayList<String> mDataset;
    private Activity mContext;
    private String userId;
    private boolean commentsOpen = false;
    private boolean commentsLoaded = false;
    private GlobalContainer globalContainer;
    private String jsonResult;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout CardInner;

        public ViewHolder(View view) {
            super(view);
            CardInner = (LinearLayout) view;
        }
    }


    public CrumbCardEditAdapter(ArrayList<String> myDataset, Activity context) {
        // This is our JSONObject of trailData (id, name etc).
        mDataset = myDataset;
        mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        // The purpose of this method is to let the onCreateViewHolder of the adapter know if it is the first in the list or not.
        return position;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CrumbCardEditAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        /*
            If it is not the first card in the list, create the standard crumb card. If it is the
            first card in the list we need to create the editable trail card.
      */
        View v = null;
        if (viewType == 0) {
            // Set up the trail card
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_trail_card, parent, false);
        } else {
             v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.edit_crumb_card, parent, false);
        }

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        /*
            Here we need to get the trailId from the array. This is the first one in the list.
            We need this to create a trail header card at the top of the
          */
        if (position != 0) {
            String crumbId = mDataset.get(position);
            FetchAndBindObject(holder.CardInner, crumbId, position);
            setSaveAndDeleteClickHandlers(holder.CardInner, crumbId, position);

        } else {
            // This means it equals 0 - so go through loading an populating the trail card.
            String trailId = mDataset.get(position);
            FetchAndBindTrailObject(holder.CardInner, trailId);
            TextView openMap = (TextView) holder.CardInner.findViewById(R.id.view_my_map);
            openMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.MapHostBase");
                    mContext.startActivity(intent);
                }
            });
        }
    }

    // For the first item in the list - which is th trail
    private void FetchAndBindTrailObject(LinearLayout card, final String trailId) {
        UpdateViewElementWithProperty updateViewElementWithProperty = new UpdateViewElementWithProperty();

        // Get title/description objects
        final EditText trailTitle = (EditText) card.findViewById(R.id.trail_title);
        final EditText trailDescription = (EditText) card.findViewById(R.id.trail_description);

        // Set our already selected title/descriptions.
        updateViewElementWithProperty.UpdateEditTextElement(trailTitle, trailId, "TrailName");
        updateViewElementWithProperty.UpdateEditTextElement(trailDescription, trailId, "Description");

        // Set up click listener for the save button
        TextView saveButton = (TextView) card.findViewById(R.id.save_edited_trail);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = "";
                String description = " ";

                // send save request
                Editable titleEditable = trailTitle.getText();
                Editable descriptionEditable = trailDescription.getText();
                if (titleEditable != null) {
                    title = titleEditable.toString();
                }

                if (descriptionEditable != null) {
                    description = descriptionEditable.toString();
                }

                // Now save our details.
                HTTPRequestHandler requestHandler = new HTTPRequestHandler();
                requestHandler.SaveNodeProperty(trailId, "TrailName", title);
                requestHandler.SaveNodeProperty(trailId, "Description", description);

            }
        });
    }

    private void setSaveAndDeleteClickHandlers(final LinearLayout card, final String crumbId, final int position) {
        TextView saveButton = (TextView) card.findViewById(R.id.save_edited_crumb);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Save description - thats all we need to save
                EditText description = (EditText) card.findViewById(R.id.crumb_description);
                Editable descriptionEditable = description.getText();
                if (descriptionEditable != null) {
                    String descriptionResult = descriptionEditable.toString();
                    // Send save
                    HTTPRequestHandler requestHandler = new HTTPRequestHandler();
                    requestHandler.SaveNodeProperty(crumbId, "Chat", descriptionResult);
                 }
            }
        });

        TextView deleteButton = (TextView) card.findViewById(R.id.delete_crumb);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // First confirm we want to delete.
                showDeleteDialog(crumbId, position);
            }
        });
    }

    private void showDeleteDialog(final String crumbId, final int position) {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.delete_crumb_dialog);
        TextView dialogButton = (TextView) dialog.findViewById(R.id.cancel_dialog_button);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        TextView deleteButton = (TextView) dialog.findViewById(R.id.delete_dialog_button);
        // if button is clicked, close the custom dialog
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                deleteCrumb(crumbId);
                mDataset.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mDataset.size());
            }
        });
        dialog.show();
    }

    private void deleteCrumb(String crumbId) {
        String url = LoadBalancer.RequestServerAddress() + "/rest/Crumb/DeleteCrumb/" + crumbId;
        HTTPRequestHandler requestHandler = new HTTPRequestHandler();
        requestHandler.SendSimpleHttpRequest(url);
        // need to remove it from the list
    }
    @Override
    public void onViewRecycled(ViewHolder holder) {
        ImageView imageView = (ImageView) holder.CardInner.findViewById(R.id.crumb_image);
        imageView.setImageDrawable(null);
    }

    private void setImageWidth(final ImageView imageView) {
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        mContext.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int widthDouble = displaymetrics.widthPixels;
        layoutParams.height = widthDouble;
        imageView.setLayoutParams(layoutParams);
    }
    private void FetchAndBindObject(final LinearLayout card, final String crumbId, int position) {
        final ImageView imageView = (ImageView) card.findViewById(R.id.crumb_image);
        setImageWidth(imageView);
        final TextCachingInterface textCachingInterface = new TextCachingInterface(mContext);
        final String latLongKey = "GetLatitudeAndLogitudeForCrumb" + crumbId;
        String cachedLatLon = textCachingInterface.FetchDataInStringFormat(latLongKey);
        if (cachedLatLon == null) {
            String url = LoadBalancer.RequestServerAddress() + "/rest/Crumb/GetLatitudeAndLogitudeForCrumb/" + crumbId;
            AsyncDataRetrieval fetchCardDetails = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
                @Override
                public void onFinished(String result) {
                    BindCard(result, card);
                    textCachingInterface.CacheText(latLongKey, result);
                }
            });
            fetchCardDetails.execute();
        } else {
            BindCard(cachedLatLon, card);
        }

        // Need to make a base class for loadBitmap, non cards wont work with this.
        Glide.with(mContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/" + crumbId + ".jpg").centerCrop().placeholder(R.drawable.background3).crossFade().into(imageView);
        final String descriptionKey = "chat" + crumbId;
        String description = textCachingInterface.FetchDataInStringFormat(descriptionKey);
        if (description == null) {
            String crumbDescriptionUrl = LoadBalancer.RequestServerAddress() + "/rest/login/GetPropertyFromNode/" + crumbId + "/Chat/";
            AsyncDataRetrieval fetchCrumbDescription = new AsyncDataRetrieval(crumbDescriptionUrl, new AsyncDataRetrieval.RequestListener() {
                @Override
                public void onFinished(String result) {
                    TextView crumbDescription = (TextView) card.findViewById(R.id.crumb_description);
                    crumbDescription.setText(result);
                    textCachingInterface.CacheText(descriptionKey, result);
                }
            });
            fetchCrumbDescription.execute();
        } else {
            TextView crumbDescription = (TextView) card.findViewById(R.id.crumb_description);
            crumbDescription.setText(description);
        }
    }

    private void BindCard(String data, final LinearLayout card) {
        jsonResult = data;
        // Result is our card details. We need to go though and fetch these
        new Thread(new Runnable() {
            public void run() {
                try {
                    // Fetch our data
                    JSONObject resultJSON = new JSONObject(jsonResult);
                    Double Latitude = resultJSON.getDouble("Latitude");
                    Double Longitude = resultJSON.getDouble("Longitude");
                    Geocoder gcd = new Geocoder(mContext, Locale.getDefault());
                    final List<Address> addresses = gcd.getFromLocation(Latitude, Longitude, 1);
                    if (addresses.size() > 0)
                        // Need to get back on the UI thread to update.
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView locality = (TextView) card.findViewById(R.id.location);
                                locality.setText(addresses.get(0).getLocality());
                            }
                        });
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
