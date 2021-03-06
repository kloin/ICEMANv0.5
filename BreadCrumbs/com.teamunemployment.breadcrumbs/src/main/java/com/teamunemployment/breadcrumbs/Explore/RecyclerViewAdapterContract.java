package com.teamunemployment.breadcrumbs.Explore;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.User;

/**
 * Created by jek40 on 23/07/2016.
 */
public interface RecyclerViewAdapterContract {
    void bindTripToCard(Trip trip, RecyclerView.ViewHolder card, ExploreCardModel cardModel);
    void bindUserDetailsToTripCard(User user,RecyclerView.ViewHolder card);
}
