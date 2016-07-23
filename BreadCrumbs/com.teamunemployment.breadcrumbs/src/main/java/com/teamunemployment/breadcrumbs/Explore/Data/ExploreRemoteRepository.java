package com.teamunemployment.breadcrumbs.Explore.Data;

import android.support.annotation.Nullable;
import android.util.Log;

import com.teamunemployment.breadcrumbs.RESTApi.NodeService;
import com.teamunemployment.breadcrumbs.RESTApi.UserService;
import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.User;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by jek40 on 22/07/2016.
 */
public class ExploreRemoteRepository {
    private static final String TAG = "ExploreRemoteRepo";

    private UserService userService;
    private NodeService nodeService;

    public ExploreRemoteRepository(UserService userService, NodeService nodeService) {
        this.userService = userService;
        this.nodeService = nodeService;
    }

    /**
     * Return three trips that our current user will be interested in.
     * @param userId The userid that we are loading for.
     * @return An array of trips to display to the user.
     */
    @Nullable
    public ArrayList<Trip> LoadTop3FollowTrips(long userId) {
        if(userId == -1) {
            Log.d(TAG, "User id is -1. Serious issue");
        }

        try {
            Call<ArrayList<Trip>> call = userService.GetTopThreeItemsForAUser(Long.toString(userId));
            Response<ArrayList<Trip>> tripsResponse = call.execute();

            if (tripsResponse.code() == 200) {
                return tripsResponse.body();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Load a bunch of trips from around the globe to display to the user. This is the default, but
     * probably wont show the user anything of special interest.
     * @return Up to twenty differnt trips from around the globe sorted by popularity
     */
    @Nullable
    public ArrayList<Trip> LoadGlobalTrips() {
        try {
            Call<ArrayList<Trip>> call = nodeService.getTwentyTrips();
            Response<ArrayList<Trip>> tripsResponse = call.execute();

            if (tripsResponse.code() == 200) {
                return tripsResponse.body();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Grab an array of ids from albums around the globe.
     * @return The ids for the albums.
     */
    public ArrayList<String> LoadTwentyGlobalTripIds() {
            Call<ResponseBody> call = nodeService.getTwentyTripIds();
            try {
                Response<ResponseBody> response = call.execute();
                if (response.code() == 200) {
                    String commaSeperatedIds = response.body().string();
                    String[] ids = commaSeperatedIds.split(",");
                    ArrayList<String> idsArray = new ArrayList<>(Arrays.asList(ids));
                    return idsArray;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        return new ArrayList<>();
    }

    public ArrayList<String> LoadTopThreeTripIds(long userId) {
        Call<ResponseBody> call = userService.GetTopThreeUnreadTripIds(Long.toString(userId));
        try {
            Response<ResponseBody> response = call.execute();
            if (response.code() == 200) {
                String commaSeperatedIds = response.body().string();
                String[] ids = commaSeperatedIds.split(",");
                ArrayList<String> idsArray = new ArrayList<>(Arrays.asList(ids));
                return idsArray;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public Trip LoadTrip(long tripId) {
       Call<Trip> call = nodeService.getTrip(Long.toString(tripId));
        try {
            Response<Trip> response = call.execute();
            if (response.code() == 200) {
                return response.body();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public User LoadUser(String userId) {
        Call<User> call = userService.GetUser(userId);
        try {
            Response<User> response = call.execute();
            if (response.code() == 200) {
                return response.body();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
