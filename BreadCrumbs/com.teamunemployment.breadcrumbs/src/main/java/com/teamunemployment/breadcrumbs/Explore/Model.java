package com.teamunemployment.breadcrumbs.Explore;

import com.teamunemployment.breadcrumbs.Explore.Adapter.ExploreCardContract;
import com.teamunemployment.breadcrumbs.Explore.Data.ExploreLocalRepository;
import com.teamunemployment.breadcrumbs.Explore.Data.ExploreRemoteRepository;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.User;

import java.util.ArrayList;

/**
 * @author Josiah Kendall.
 *
 * The model for the "Explore" or "Feed" page.
 */
public class Model {

    private ExploreRemoteRepository remoteRepository;
    private ExploreLocalRepository localRepository;

    private ArrayList<ExploreCardModel> cardModels = new ArrayList<>();

    public Model(ExploreLocalRepository localRepo, ExploreRemoteRepository remoteRepo) {

        // Dependencies.
        this.remoteRepository = remoteRepo;
        this.localRepository = localRepo;
    }

    /**
     * Load the ids that we want to display. Does as it souns. Each of the cards that we display
     * on the recycler view has a corresponding id. That is loaded intially here. At runtime we use
     * this Id to load the data associated with the trip.
     * @param userId The user id of the user currently logged in. Not sure how neccessary this param is.
     * @return AN Array list of {@link ExploreCardModel} for use in the RecyclerView adapter.
     */
    public ArrayList<ExploreCardModel> LoadIdsForAllTheAlbumsWeWantToDisplay(long userId) {

        // This is a master list of ids to ensure theat we dont add the same trip twice.
        ArrayList<String> ids = new ArrayList<>();

        // Load up to ten trips that we are following.
        ArrayList<String> trips = LoadFollowingTrips(userId, 4);

        // Load following trips.
        if (trips.size() > 0) {
            cardModels.add(new ExploreCardModel(ExploreCardModel.FOLLOWING_HEADER, "Following", R.drawable.ic_account_plus_grey600_24dp));
            cardModels.addAll(addModels(trips, ExploreCardModel.FOLLOWING_CARD, ids));
        }

        // add trending banner
        cardModels.add(new ExploreCardModel(ExploreCardModel.TRENDING_HEADER, "Trending", R.drawable.ic_location_on_white_24dp));

        // Loading trending trips
        ArrayList<String> trendingTrips = remoteRepository.LoadPopularTrips(3);
        cardModels.addAll(addModels(trendingTrips,ExploreCardModel.TRENDING_CARD, ids));

        // Add these trips to the master list
        ids.addAll(trendingTrips);

        // Load a bunch of global trip
        ArrayList<String> global = remoteRepository.LoadTwentyGlobalTripIds();
        if (global.size() > 0) {
            cardModels.add(new ExploreCardModel(ExploreCardModel.GLOBAL_HEADER, "Global", R.drawable.ic_account_plus_grey600_24dp));
            cardModels.addAll(addModels(global, ExploreCardModel.GLOBAL_CARD, ids));
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

    // Add the models to our currently existing models, making sure the we dont add the same item twice.
    private ArrayList<ExploreCardModel> addModels(ArrayList<String> ids, int viewType, ArrayList<String> preExistingIds) {
        ArrayList<ExploreCardModel> models = new ArrayList<>();
        for (String id: ids) {
            if (!preExistingIds.contains(id)) {
                models.add(new ExploreCardModel(viewType, id));
            }
        }

        return models;
    }

    // Load the details for a trip, given its id.
    public void LoadSingleTrip(final long tripId, ExploreCardContract contract) {
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
