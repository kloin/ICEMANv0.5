package com.teamunemployment.breadcrumbs.Explore;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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
        ArrayList<String> trips = LoadUpToTwentyPopularAlbumIdsFromAroundTheGlobe();
        cardModels.add(new ExploreCardModel(ExploreCardModel.HEADER_CARD, "Around the world", R.drawable.ic_account_plus_grey600_24dp));
        cardModels.addAll(addModels(trips, ExploreCardModel.FOLLOWING_CARD));
        return cardModels;
    }

    //
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

    public void LoadSingleTrip(final ExploreCardModel cardModel, final RelativeLayout cardInner, final RecyclerViewAdapterContract cardViewContract) {
        long tripId = Long.parseLong(cardModel.getData());
        Trip localTrip = localRepository.LoadTrip(tripId);
        if (localTrip != null) {
            cardViewContract.bindTripToCard(localTrip, cardInner, cardModel);
        }
        Trip remoteTrip = remoteRepository.LoadTrip(tripId);

        if (localTrip == null || (remoteTrip !=  null && !localTrip.equals(remoteTrip))) {
            cardViewContract.bindTripToCard(remoteTrip, cardInner, cardModel);
            localRepository.SaveTrip(remoteTrip, tripId);
        }
    }

    public void LoadUserDetailsForCard(String userId, RelativeLayout cardInner, ExploreRecyclerViewAdapter instance) {
        User localUser = localRepository.GetUser(userId);
        if (localUser != null) {
            instance.bindUserDetailsToTripCard(localUser,cardInner);
        }
        User user = remoteRepository.LoadUser(userId);
        if (localUser == null || (user != null && !localUser.equals(user))) {
            instance.bindUserDetailsToTripCard(user, cardInner);
            localRepository.SaveUser(user);
        }
    }
}
