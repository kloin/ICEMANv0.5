package com.teamunemployment.breadcrumbs.Explore.Data;

import com.teamunemployment.breadcrumbs.RESTApi.UserService;
import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.User;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import java.util.ArrayList;

/**
 * Created by jek40 on 22/07/2016.
 */
public class ExploreLocalRepository {

    private DatabaseController databaseController;
    public ExploreLocalRepository(DatabaseController dbc) {
        this.databaseController = dbc;
    }


    public Trip LoadTrip(long tripId) {
        return databaseController.LoadTrip(tripId);
    }

    public void SaveTrip(Trip remoteTrip, long tripId) {
        databaseController.SaveTrip(remoteTrip, tripId);
    }

    public User GetUser(String userId) {
        return databaseController.GetUser(userId);
    }

    public void SaveUser(User user) {
        databaseController.SaveUser(user);
    }
}
