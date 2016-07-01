package com.teamunemployment.breadcrumbs.Profile.data;

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
    public ArrayList<String> getUserTrailIds(long userId) {
        return null;
    }

    @Override
    public void saveUserName(String username, long userId) {

    }

    @Override
    public void saveUserAbout(String about, long userId) {

    }

    @Override
    public void saveUserWeb(String website, long userId) {

    }

    @Override
    public void saveProfilePictureId(String profilePicId, long userId) {

    }
}
