package com.teamunemployment.breadcrumbs.Explore;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.teamunemployment.breadcrumbs.Explore.Adapter.ExploreCardContract;
import com.teamunemployment.breadcrumbs.Explore.Data.ExploreLocalRepository;
import com.teamunemployment.breadcrumbs.Explore.Data.ExploreRemoteRepository;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.RESTApi.NodeService;
import com.teamunemployment.breadcrumbs.RESTApi.UserService;
import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.User;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Josiah Kendall.
 */
public class Model {

    private ExploreRemoteRepository remoteRepository;
    private ExploreLocalRepository localRepository;

    private ArrayList<ExploreCardModel> cardModels = new ArrayList<>();

    public Model(ExploreLocalRepository localRepo, ExploreRemoteRepository remoteRepo) {
        this.remoteRepository = remoteRepo;
        this.localRepository = localRepo;
    }

    public ArrayList<ExploreCardModel> LoadModels(long userId) {
        cardModels.add(new ExploreCardModel(ExploreCardModel.TRENDING_HEADER, "Trending", R.drawable.ic_location_on_white_24dp));
        ArrayList<String> trendingTrips = remoteRepository.LoadPopularTrips(3);
        cardModels.addAll(addModels(trendingTrips,ExploreCardModel.TRENDING_CARD ));
        ArrayList<String> trips = LoadFollowingTrips(userId, 10);
        if (trips.size() > 0) {
            cardModels.add(new ExploreCardModel(ExploreCardModel.FOLLOWING_HEADER, "Following", R.drawable.ic_account_plus_grey600_24dp));
            cardModels.addAll(addModels(trips, ExploreCardModel.FOLLOWING_CARD));
        }
        return cardModels;
    }

    public ArrayList<String> LoadFollowingTrips(long userId, int maxTrips) {
        return remoteRepository.LoadFollowingTrips(userId, maxTrips);
    }

    public ArrayList<String> LoadUpToTwentyPopularAlbumIdsFromAroundTheGlobe() {
        return remoteRepository.LoadTwentyGlobalTripIds();
    }

    public ArrayList<Trip> LoadThreeUpdatedFollowingTrips(long userId) {

        return remoteRepository.LoadTop3FollowTrips(userId);
    }

    public ArrayList<String> LoadThreeUpdatedFollowingTripIds(long userId) {
        return remoteRepository.LoadTopThreeTripIds(userId);
    }

    public ArrayList<Trip> LoadUpToTwentyPopularAlbumsFromAroundTheGlobe() {
        return remoteRepository.LoadGlobalTrips();
    }

    private ArrayList<ExploreCardModel> addModels(ArrayList<String> ids, int viewType) {
        ArrayList<ExploreCardModel> models = new ArrayList<>();
        for (String id: ids) {
            models.add(new ExploreCardModel(viewType, id));
        }

        return models;
    }

    public void LoadSingleTrip(final ExploreCardModel cardModel, ExploreCardContract contract) {
        long tripId = Long.parseLong(cardModel.getData());
        Trip localTrip = localRepository.LoadTrip(tripId);
        if (localTrip != null) {
            contract.bindTrip(localTrip, this);
        }
        Trip remoteTrip = remoteRepository.LoadTrip(tripId);

        if (localTrip == null || (remoteTrip !=  null && !localTrip.equals(remoteTrip))) {
            contract.bindTrip(remoteTrip, this);
            localRepository.SaveTrip(remoteTrip, tripId);
        }
    }

    public void LoadUserDetailsForCard(String userId, ExploreCardContract contract) {
        User localUser = localRepository.GetUser(userId);
        if (localUser != null) {
            contract.bindUser(localUser);
        }
        User user = remoteRepository.LoadUser(userId);
        if (localUser == null || (user != null && !localUser.equals(user))) {
            contract.bindUser(user);
            localRepository.SaveUser(user);
        }
    }
}
