package com.teamunemployment.breadcrumbs.Profile.data;

import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import java.util.ArrayList;

/**
 * @author Josiah Kendall
 */
public class LocalProfileRepository implements ProfileRepositoryContract {

    private DatabaseController databaseController;

    public LocalProfileRepository(DatabaseController dbc) {
        databaseController = dbc;
    }

    @Override
    public String getUserName(long userId) {
        return databaseController.GetUserName(userId);
    }

    @Override
    public String getUserAbout(long userId) {
        return databaseController.GetUserAbout(userId);
    }

    @Override
    public String getUserWeb(long userId) {
        return databaseController.getUserWeb(userId);
    }

    @Override
    public String getProfilePictureId(long userId) {
        return databaseController.GetUserProfilePic(userId);
    }

    @Override
    public ArrayList<Trip> getUserTrips(long userId) {
        //databaseController.GetThreeTrips();
        return null;
    }


    @Override
    public void saveUserName(String username, long userId) {
        databaseController.SaveUserName(userId, username);
    }

    @Override
    public void saveUserAbout(String about, long userId) {
        databaseController.SaveUserAboutField(userId, about);
    }

    @Override
    public void saveUserWeb(String website, long userId) {
        databaseController.SaveUserWebField(userId, website);
    }

    @Override
    public void saveProfilePictureId(String profilePicId, long userId) {
        databaseController.SaveUserProfilePicId(userId, profilePicId);
    }

    @Override
    public void saveUserTrips(ArrayList<Trip> trips) {
        databaseController.SaveUserTrips(trips);
    }

    public void setUserFolliwingAnotherUser(long broadcastUserId, long followingUserId) {
            databaseController.SetUserFollowingAnotherUser(broadcastUserId, followingUserId);
    }

    public void setUserNotFollowingAnotherUser(long broadcastUserId, long followingUserId) {
        databaseController.RemoveBroadcaster(broadcastUserId, followingUserId);
    }

    public boolean isUserFollowingOtherUser(long userId, long visitorId) {
        return databaseController.isUserFollowingOtherUser(userId, visitorId);
    }
}
