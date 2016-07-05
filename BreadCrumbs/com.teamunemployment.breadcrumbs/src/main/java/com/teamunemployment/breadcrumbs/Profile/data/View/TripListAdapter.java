package com.teamunemployment.breadcrumbs.Profile.data.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Trails.Trip;

/**
 * @author Josiah Kendall
 *
 * Adapter for a list of trails. This was written specifically for the profile page.
 */
public class TripListAdapter extends ArrayAdapter<Trip> {
    private final Context context;
    private final Trip[] values;

    public TripListAdapter(Context context,  Trip[] objects) {
        super(context, -1, objects);
        this.context = context;
        this.values = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.trip_list_item, parent, false);
        TextView title = (TextView) rowView.findViewById(R.id.trip_title);
        ImageView coverPhoto = (ImageView) rowView.findViewById(R.id.trip_cover_photo);

        Trip trip = values[position];

        // Set our values
        title.setText(trip.getTrailName());
        Picasso.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+ trip.getCoverPhotoId()+".jpg").into(coverPhoto);
        return rowView;
    }
}
