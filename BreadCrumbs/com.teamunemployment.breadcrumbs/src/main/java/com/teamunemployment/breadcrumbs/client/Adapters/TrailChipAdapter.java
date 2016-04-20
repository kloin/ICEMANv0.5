package com.teamunemployment.breadcrumbs.client.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jek40 on 15/12/2015.
 */
public class TrailChipAdapter extends BaseAdapter {

    private ArrayList<String> trailIds;
    private Context context;

    public TrailChipAdapter(ArrayList<String> myDataset, Context context) {
        trailIds = myDataset;
        this.context = context;
    }
    @Override
    public int getCount() {
        return trailIds.size();
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
        String id = trailIds.get(position);
        View chipView = null;

        if (convertView == null) {

            chipView = new View(context);

            chipView = inflater.inflate(R.layout.trail_chip, null);

            String url = LoadBalancer.RequestServerAddress() +"/rest/TrailManager/GetBaseDetailsForATrail/" + id;
            final View finalChipView = chipView;
            AsyncDataRetrieval fetchCardDetails = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {

                @Override
                public void onFinished(String result) {
                    //Result is our card details. We need to go though and fetch these
                    try {
                        // Fetch our data, then
                        JSONObject resultJSON = new JSONObject(result);
                        // These four should all exist. The fifth we have to check for.
                        String desc = resultJSON.get("description").toString();
                        String title = resultJSON.get("trailName").toString();
                        final String userName = resultJSON.get("userName").toString();
                       // String coverId = resultJSON.get("coverPhotoId").toString();
                        createTrailChip(title, desc, null, finalChipView);
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
            }, context);
            fetchCardDetails.execute();
            // Glide.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+id+".jpg").centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);
            // Glide.with(context).load("http://placehold.it/350x150").centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);

        } else {
           // chipView = (View) convertView;
            //Glide.with(context).load("http://placehold.it/350x150").centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);
            //Glide.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+id+".jpg").centerCrop().placeholder(Color.GRAY).crossFade().into(imageView);

        }

        return chipView;
    }

    private void createTrailChip(String title, String description, String coverId, View finalChipView) {
        final TextView primaryText = (TextView) finalChipView.findViewById(R.id.trail_chip_main_title);
        primaryText.setText(title);
        final TextView secondaryText = (TextView) finalChipView.findViewById(R.id.trail_chip_secondary_title);
        secondaryText.setText(description);
    }
}
