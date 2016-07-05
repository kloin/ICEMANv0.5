package com.teamunemployment.breadcrumbs.Profile.data.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Profile.data.LocalProfileRepository;
import com.teamunemployment.breadcrumbs.Profile.data.Presenter.ProfileContract;
import com.teamunemployment.breadcrumbs.Profile.data.ProfileRepository;
import com.teamunemployment.breadcrumbs.Profile.data.RemoteProfileRepository;
import com.teamunemployment.breadcrumbs.Profile.data.RemoteRepositoryFactory;
import com.teamunemployment.breadcrumbs.Profile.data.RepositoryResponseContract;
import com.teamunemployment.breadcrumbs.RESTApi.NodeService;
import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import java.util.ArrayList;


/**
 * @author Josiah Kendall
 */
public class ProfileModel implements RepositoryResponseContract {

    private static final String TAG = "ProfileModel";

    private long userId;

    private RepositoryResponseContract contract;
    private boolean editable = false;
    private boolean aboutIsDirty = false;
    private boolean webIsDirty = false;

    private String aboutText;
    private String webText;

    // Load properties
    private ProfileContract.PresenterContract presenter;
    private Context context;

    private ProfileRepository repository;
    public ProfileModel(ProfileContract.PresenterContract presenter, Context context, DatabaseController databaseController, long userId) {
        contract = this;
        this.presenter = presenter;
        this.context = context;
        this.userId = userId;

        // Build our repo.
        RemoteProfileRepository remoteProfileRepository = RemoteRepositoryFactory.GetRemoteProfileRepository();
        LocalProfileRepository localProfileRepository = new LocalProfileRepository(databaseController);
        repository = new ProfileRepository(localProfileRepository, remoteProfileRepository);
    }

    /**
     * Toggles the readonly / edit state for a user.
     * @return False if the user is in read only, true if the new result is edit mode.
     */
    public boolean ToggleEditReadOnlyButton() {
        // Edit to read only state
        if (editable) {
            editable = false;
            // Save any dirty data
            saveDirtyData();
        } else {
            editable = true;
        }
        return editable;
    }

    private void saveDirtyData() {
        if (aboutIsDirty) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    repository.saveUserAbout(aboutText, userId);
                }
            }).start();
            aboutIsDirty = false;
        }

        if (webIsDirty) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    repository.saveUserWeb(webText, userId);
                }
            }).start();
            webIsDirty = false;
        }
    }

    public void LoadTripIds(final long userId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                repository.getUserTrips(userId, contract);
            }
        }).start();
    }

    public void LoadUserName(final long userId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                repository.getUserName(userId, contract);
            }
        }).start();
    }

    public void LoadUserAbout(final long userId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                repository.getUserAbout(userId, contract);
            }
        }).start();
    }

    public void LoadUserWebsite(final long userId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                repository.getUserWeb(userId, contract);
            }
        }).start();
    }

    public void LoadUserProfileId(final long userId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                repository.getProfilePictureId(userId, contract);
            }
        }).start();
    }

    @Override
    public void setUserName(String userName) {
        presenter.setUserName(userName);
    }

    @Override
    public void setAbout(String about) {
        presenter.setUserAbout(about);
    }

    @Override
    public void setUserWeb(String website) {
        presenter.setUserWeb(website);
    }

    @Override
    public void setUserTrips(ArrayList<Trip> trips) {
        presenter.setUserTrips(trips);
    }

    @Override
    public void setUserProfilePicId(String id) {
            presenter.setProfilePicture(id);
    }

    public boolean getEditable() {
        return editable;
    }

    public void SetUserAboutText(String about) {
        if (!about.isEmpty()) {
            aboutIsDirty = true;
            aboutText = about;
            presenter.setUserAbout(about);
        }
    }

    public void SetUserWebText(String web) {
        if (!web.isEmpty()) {
            webIsDirty = true;
            webText = web;
            presenter.setUserWeb(web);
        }

    }

    public void SaveNewProfilePicId(final long userId, final String coverPhotoPic) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                repository.saveProfilePictureId(coverPhotoPic, userId);
            }
        }).start();
    }
}
