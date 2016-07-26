package com.teamunemployment.breadcrumbs.Explore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;
import com.teamunemployment.breadcrumbs.Explore.Adapter.BannerCard;
import com.teamunemployment.breadcrumbs.Explore.Adapter.FullCard;
import com.teamunemployment.breadcrumbs.Explore.Adapter.HalfCard;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.User;
import com.teamunemployment.breadcrumbs.client.Cards.HomeCardAdapter;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author Josiah Kendall
 * The adapter for our feed recyclerview.
 */
public class ExploreRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ExploreCardModel> dataset;
    private Model model;
    private FirebaseAnalytics firebaseAnalytics;
    private Context context;
    private AppCompatActivity appCompatActivityContext;
    private ExploreRecyclerViewAdapter instance;

    public ExploreRecyclerViewAdapter(ArrayList<ExploreCardModel> myDataset, Model model, Context context) {
        dataset = myDataset;
        this.model = model;
        this.context = context;
        this.appCompatActivityContext = (AppCompatActivity) context;
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        instance = this;
    }

    @Override
    public int getItemViewType(int position) {
        return dataset.get(position).getViewType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        return createExploreCardView(parent, viewType);
    }

    private RecyclerView.ViewHolder createExploreCardView(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ExploreCardModel.FOLLOWING_CARD:
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.favourite_half_card, parent, false);
                HalfCard vh = new HalfCard(v, appCompatActivityContext);
                return vh;

            case ExploreCardModel.TRENDING_CARD:
                View view2 = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.explore_following_card, parent, false);
                FullCard fullCard = new FullCard(view2, appCompatActivityContext);
                return fullCard;
            case ExploreCardModel.GLOBAL_CARD:
                View view3 = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.explore_following_card, parent, false);
                FullCard fullCard2 = new FullCard(view3, appCompatActivityContext);
                return fullCard2;
        }

        // Else make banner
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.explore_header, parent, false);
        BannerCard vh = new BannerCard(v, appCompatActivityContext);
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        final ExploreCardModel cardModel = dataset.get(position);

        int viewType = cardModel.getViewType();

        switch (viewType) {
            case ExploreCardModel.FOLLOWING_CARD :
                final HalfCard viewHolder = (HalfCard) holder;
                scaleImage(viewHolder.getMainPhoto());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        model.LoadSingleTrip(Long.parseLong(cardModel.getData()), viewHolder);
                    }
                }).start();
                break;
            case ExploreCardModel.FOLLOWING_HEADER:
                bindHeader(holder, "Favourites", appCompatActivityContext.getResources().getColor(R.color.red_300), R.drawable.ic_favorite_border_white_24dp);
                break;
            case ExploreCardModel.LOCAL_CARD:
                break;
            case ExploreCardModel.TRENDING_HEADER:
                bindHeader(holder, "Trending", appCompatActivityContext.getResources().getColor(R.color.blue_300), R.drawable.ic_trending_up_white_24dp);
                break;
            case ExploreCardModel.TRENDING_CARD:
                final FullCard fullCard = (FullCard) holder;
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) fullCard.itemView.getLayoutParams();
                layoutParams.setFullSpan(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        model.LoadSingleTrip(Long.parseLong(cardModel.getData()), fullCard);
                    }
                }).start();
                break;
            case ExploreCardModel.GLOBAL_HEADER:
                bindHeader(holder, "Around the Globe", appCompatActivityContext.getResources().getColor(R.color.good_to_go), R.drawable.ic_public_white_24dp);
                break;
            case ExploreCardModel.GLOBAL_CARD:
                final FullCard global = (FullCard) holder;
                StaggeredGridLayoutManager.LayoutParams fullSpan = (StaggeredGridLayoutManager.LayoutParams) global.itemView.getLayoutParams();
                fullSpan.setFullSpan(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        model.LoadSingleTrip(Long.parseLong(cardModel.getData()), global);
                    }
                }).start();
                break;
        }
    }

    /**
     * Simple method which calculates the width of the screen.
     * @return The screen width of the device in pixels.
     */
    private int calculateScreenWidth() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        appCompatActivityContext.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.widthPixels /2;
        return height;
    }

    private void scaleImage(ImageView view) {

            // Set the imageView height to be the same as the width.
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            int TRAIL_COVER_PHOTO_HEIGHT = calculateScreenWidth();
            layoutParams.height = TRAIL_COVER_PHOTO_HEIGHT;
            view.setLayoutParams(layoutParams);
    }

    private void bindHeader(RecyclerView.ViewHolder holder, String text, int color, int drawableIconReference) {
        // Get background, and set color.
        // get text, and setColor
        BannerCard headerHolder = (BannerCard) holder;
        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) headerHolder.itemView.getLayoutParams();
        layoutParams.setFullSpan(true);
        headerHolder.getHeader().setText(text);
        headerHolder.getWrapper().setBackgroundColor(color);
        headerHolder.getHeaderIcon().setImageResource(drawableIconReference);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {

//        ImageView imageView = (ImageView) card.findViewById(R.id.main_photo);
//        if (imageView != null) {
//            imageView.setImageDrawable(null);
//            TextView title = (TextView) card.findViewById(R.id.Title);
//            title.setText(null);
//            TextView userName = (TextView) card.findViewById(R.id.belongs_to);
//            userName.setText("");
//            CircleImageView profilePic = (CircleImageView) card.findViewById(R.id.profilePicture);
//            profilePic.setImageResource(R.drawable.profileblank);
//        }
    }
}
