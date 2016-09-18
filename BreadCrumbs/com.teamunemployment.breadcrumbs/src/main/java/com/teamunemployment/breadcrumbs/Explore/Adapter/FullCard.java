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
import com.teamunemployment.breadcrumbs.Explore.ExploreCardModel;
import com.teamunemployment.breadcrumbs.Explore.Model;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.User;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author Josiah Kendall.
 * This class represents a full card on the feed recyclerview. This handles the binding callbacks,
 * button clicks.
 */
public class FullCard extends RecyclerView.ViewHolder implements ExploreCardContract {
    public static final int FULL_CARD = 2;
    private static final String EVENT_FLAG = "FULL_CARD";

    @Bind(R.id.profilePicture) CircleImageView profilePicture;
    @Bind(R.id.belongs_to) TextView belongsTo;
    @Bind(R.id.main_photo) ImageView mainPhoto;
    @Bind(R.id.view_count) TextView viewCount;
    @Bind(R.id.Title) TextView title;

    private FirebaseAnalytics analytics;
    private AppCompatActivity context;
    private String userId;
    private String tripId;
    public RelativeLayout CardInner;
    private Trip trip;

    public FullCard(View v, AppCompatActivity context) {
        super(v);
        ButterKnife.bind(this, v);
        CardInner = (RelativeLayout)v;
        this.context = context;
        analytics = FirebaseAnalytics.getInstance(context);
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public int getViewType() {
        return ExploreCardModel.FOLLOWING_CARD;
    }

    @Override
    public RelativeLayout getCardInner() {
        return CardInner;
    }

    @Override
    public void bindTrip(Trip trip1, Model model) {
        this.trip = trip1;
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Picasso.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+ trip.getCoverPhotoId()+".jpg").into(mainPhoto);
                viewCount.setText(trip.getViews());
                title.setText(trip.getTrailName());
            }
        });

        model.LoadUserDetailsForCard(trip.getUserId(), this);
    }

    @Override
    public void bindUser(final User user) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                belongsTo.setText(user.getUsername());
                Picasso.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+ user.getProfilePicId()+"T.jpg")
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.profileblank)
                        .into(profilePicture);
            }
        });
    }

    private void addClickToAnalytics() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, trip.getId());
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, trip.getTrailName());
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "trip");
        analytics.logEvent(EVENT_FLAG, bundle);
    }

    @OnClick(R.id.main_photo) void openTrip() {
        addClickToAnalytics();
        Intent TrailViewer = new Intent();
        TrailViewer.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.Album.AlbumView");
        Bundle extras = new Bundle();
        extras.putString("AlbumId", trip.getId());
        TrailViewer.putExtras(extras);
        context.startActivity(TrailViewer);
    }

    @OnClick(R.id.profilePicture) void openProfilePage() {
        Intent profileIntent = new Intent();
        profileIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.Profile.data.View.ProfileActivity");
        profileIntent.putExtra("UserId", Long.parseLong(trip.getUserId()));
        context.startActivity(profileIntent);
    }
}

