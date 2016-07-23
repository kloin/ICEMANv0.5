package com.teamunemployment.breadcrumbs.Explore;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.User;
import com.teamunemployment.breadcrumbs.client.Cards.HomeCardAdapter;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author Josiah Kendall
 */
public class ExploreRecyclerViewAdapter extends RecyclerView.Adapter<ExploreRecyclerViewAdapter.ViewHolder> implements RecyclerViewAdapterContract {

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
    public void bindTripToCard(final Trip trip, View card, ExploreCardModel cardModel) {
        final ImageView imageView = (ImageView) card.findViewById(R.id.main_photo);
        final TextView viewCount = (TextView) card.findViewById(R.id.view_count);
        final TextView trailName = (TextView) card.findViewById(R.id.Title);
        appCompatActivityContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Picasso.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+ trip.getCoverPhotoId()+".jpg").into(imageView);
                viewCount.setText(trip.getViews());
                trailName.setText(trip.getTrailName());
            }
        });

        model.LoadUserDetailsForCard(trip.getUserId(), (RelativeLayout) card, instance);

    }

    @Override
    public void bindUserDetailsToTripCard(final User user, View card) {
        final TextView belongsTo = (TextView) card.findViewById(R.id.belongs_to);
        final CircleImageView profilePic = (CircleImageView) card.findViewById(R.id.profilePicture);
        appCompatActivityContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                belongsTo.setText(user.getUsername());
                Picasso.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+ user.getProfilePicId()+"T.jpg").into(profilePic);
            }
        });

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Each data item is just a string in this case
        public RelativeLayout CardInner;
        public ViewHolder(View v) {
            super(v);
            CardInner = (RelativeLayout)v;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return dataset.get(position).getViewType();
    }

    @Override
    public ExploreRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        return createExploreCardView(parent, viewType);
    }

    private ViewHolder createExploreCardView(ViewGroup parent, int viewType) {
        if (viewType == ExploreCardModel.FOLLOWING_CARD) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.explore_following_card, parent, false);
//            CardView card = (CardView) v.findViewById(R.id.card_view);
//            card.setPreventCornerOverlap(false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        // Else make devider
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.explore_header, parent, false);
       // CardView card = (CardView) v.findViewById(R.id.card_view);
        //card.setPreventCornerOverlap(false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ExploreRecyclerViewAdapter.ViewHolder holder, int position) {
        // Load details for the card. Not 100 on how this is going to work yet.

        final ExploreCardModel cardModel = dataset.get(position);

        int viewType = cardModel.getViewType();

        switch (viewType) {
            case ExploreCardModel.FOLLOWING_CARD:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        model.LoadSingleTrip(cardModel, holder.CardInner, instance);
                    }
                }).start();
                break;
            case ExploreCardModel.HEADER_CARD:
                bindHeader(holder, cardModel);
                break;
        }
    }

    private void bindHeader(ViewHolder holder, ExploreCardModel cardModel) {

    }



    @Override
    public int getItemCount() {
        return dataset.size();
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        RelativeLayout card = holder.CardInner;
        ImageView imageView = (ImageView) card.findViewById(R.id.main_photo);
        if (imageView != null) {
            imageView.setImageDrawable(null);
            TextView title = (TextView) card.findViewById(R.id.Title);
            title.setText(null);
            TextView userName = (TextView) card.findViewById(R.id.belongs_to);
            userName.setText("");
            CircleImageView profilePic = (CircleImageView) card.findViewById(R.id.profilePicture);
            profilePic.setImageResource(R.drawable.profileblank);
        }


    }
}
