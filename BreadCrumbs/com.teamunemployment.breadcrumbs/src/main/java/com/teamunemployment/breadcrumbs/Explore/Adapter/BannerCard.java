package com.teamunemployment.breadcrumbs.Explore.Adapter;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.Explore.Model;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.User;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by jek40 on 24/07/2016.
 */
public class BannerCard  extends RecyclerView.ViewHolder implements ExploreCardContract {
    public static final int BANNER_CARD = 0;

    @Bind(R.id.header) TextView header;
    @Bind(R.id.header_icon) ImageView headerIcon;
    @Bind(R.id.header_wrapper) RelativeLayout wrapper;

    private AppCompatActivity context;
    private RelativeLayout cardInner;
    public BannerCard(View v, AppCompatActivity context) {
        super(v);
        ButterKnife.bind(this, v);
        cardInner = (RelativeLayout)v;
        this.context = context;
    }

    @Override
    public int getViewType() {
        return 0;
    }

    @Override
    public RelativeLayout getCardInner() {
        return cardInner;
    }

    @Override
    public void bindTrip(Trip trip, Model model) {

    }

    @Override
    public void bindUser(User user) {

    }

    public ImageView getHeaderIcon() {
        return headerIcon;
    }

    public RelativeLayout getWrapper() {
        return wrapper;
    }

    public TextView getHeader() {
        return header;
    }
}
