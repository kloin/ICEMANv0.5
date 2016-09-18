package com.teamunemployment.breadcrumbs;


import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import javax.inject.Inject;

/**
 * @author Josiah Kendall
 */
public class LocalTripRepo {

    private DatabaseController databaseController;

    @Inject
    public LocalTripRepo(DatabaseController databaseController) {
        this.databaseController = databaseController;
    }

    public Trip LoadTrip(long tripId) {
        return databaseController.LoadTrip(tripId);
    }
}
