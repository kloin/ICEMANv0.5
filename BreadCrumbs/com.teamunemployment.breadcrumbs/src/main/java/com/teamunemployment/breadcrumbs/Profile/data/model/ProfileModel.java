package com.teamunemployment.breadcrumbs.Profile.data.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Preferences.Preferences;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
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

    // User id is the owner of the page.
    private long userId;

    // This is the user id of the person visiting the page, i.e the user.
    private long visitorId;

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

        this.visitorId = Long.parseLong(new PreferencesAPI(context).GetUserId());
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

    public void setUserState(long loggedInUserId) {
        if (userId != loggedInUserId) {
            presenter.setUserReadOnly();
        } else {
            presenter.setUserEditable();
        }
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
        if (trips != null) {
            presenter.setUserTripsCount(trips.size());
            presenter.setUserTrips(trips);
        }
    }

    @Override
    public void setUserProfilePicId(String id) {
            presenter.setProfilePicture(id);
    }

    @Override
    public void setUseFollowingStatus(boolean isFollowing) {
        presenter.setUserFollowingState(isFollowing);
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

    public void FollowUser() {
        // do user save
        new Thread(new Runnable() {
            @Override
            public void run() {
                repository.setUserFollowingAnotherUser(userId, visitorId);
            }
        }).start();
    }

    public void UnFollowUser() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                repository.setUserNotFollowingAnotherUser(userId, visitorId);
            }
        }).start();
    }

    public void LoadUserFollowing() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                repository.getFollowingStatus(userId, visitorId, contract);
            }
        }).start();
    }
}
