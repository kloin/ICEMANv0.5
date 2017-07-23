package com.teamunemployment.breadcrumbs.Explore.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;
import com.teamunemployment.breadcrumbs.Explore.Model;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.User;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Josiah Kendall. Half card representation for the RecyclerView of the Feed tab.
 */
public class HalfCard  extends RecyclerView.ViewHolder implements ExploreCardContract  {
    public static final int HALF_CARD = 1;
    private static final String EVENT_FLAG = "HALF_CARD";
    @Bind(R.id.Title) TextView title;
    @Bind(R.id.main_photo)  ImageView mainPhoto;

    private Trip trip;
    private FirebaseAnalytics analytics;
    private AppCompatActivity context;
    public RelativeLayout CardInner;

    public HalfCard(View v, AppCompatActivity context) {
        super(v);
        ButterKnife.bind(this, v);
        CardInner = (RelativeLayout)v;
        this.context = context;
        analytics = FirebaseAnalytics.getInstance(context);
    }

    @Override
    public int getViewType() {
        return 0;
    }

    @Override
    public RelativeLayout getCardInner() {
        return CardInner;
    }

    @Override
    public void bindTrip(final Trip trip, Model model) {
        this.trip = trip;
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                title.setText(trip.getTrailName());
                Picasso.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+ trip.getCoverPhotoId()+".jpg").into(mainPhoto);
            }
        });
    }

    @Override
    public void bindUser(User user) {
        // Not required for this card type

    }

    @OnClick(R.id.main_photo) void openTrip() {
        addClickToAnalytics();
        Intent TrailViewer = new Intent();
        TrailViewer.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.MapViewer");
        Bundle extras = new Bundle();
        extras.putString("TrailId", trip.getId());
        TrailViewer.putExtras(extras);
        context.startActivity(TrailViewer);
    }

    private void addClickToAnalytics() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, trip.getId());
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, trip.getTrailName());
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "trip");
        analytics.logEvent(EVENT_FLAG, bundle);
    }
    public TextView getTitle() {
        return this.title;
    }
    public ImageView getMainPhoto() {
        return this.mainPhoto;
    }
}
