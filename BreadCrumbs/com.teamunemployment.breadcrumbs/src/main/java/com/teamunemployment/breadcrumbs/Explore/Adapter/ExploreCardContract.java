package com.teamunemployment.breadcrumbs.Explore.Adapter;

import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;

import com.teamunemployment.breadcrumbs.Explore.Model;
import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.User;

/**
 * @author Josiah Kendall.
 */
public interface ExploreCardContract {

    int getViewType();
    RelativeLayout getCardInner();
    void bindTrip(Trip trip, Model model);
    void bindUser(User user);
}
