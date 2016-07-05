package com.teamunemployment.breadcrumbs.Profile.data;

import android.util.Log;

import com.teamunemployment.breadcrumbs.Trails.Trip;

import java.util.ArrayList;

/**
 * Created by jek40 on 30/06/2016.
 */
public class ProfileRepository {

    private static final String TAG = "ProfileRepo";
    private LocalProfileRepository localProfileRepository;
    private RemoteProfileRepository remoteProfileRepository;

    public ProfileRepository(LocalProfileRepository localProfileRepository, RemoteProfileRepository remoteProfileRepository) {
        this.localProfileRepository = localProfileRepository;
        this.remoteProfileRepository = remoteProfileRepository;
    }

    /**
     * Get the username for a given user.
     * @param userId
     */
    public void getUserName(long userId, RepositoryResponseContract responseContract) {
        String userName = localProfileRepository.getUserName(userId);
        if (userName !=  null) {
            responseContract.setUserName(userName);
        }

        // Currently, I fetch from the server everytime. This could be a bad battery drain - maybe I should
        String remoteUserName = remoteProfileRepository.getUserName(userId);
        if (userName == null || !userName.equals(remoteUserName) ) {

            if (remoteUserName != null) {
                responseContract.setUserName(remoteUserName);
                // Lets save some network requests and do it
                localProfileRepository.saveUserName(remoteUserName, userId);
            }

        }
    }

    /**
     * Get the about section for a user.
     * @param userId
     */
    public void getUserAbout(long userId, RepositoryResponseContract responseContract) {
        String userAbout = localProfileRepository.getUserAbout(userId);
        if (userAbout !=  null) {
            responseContract.setAbout(userAbout);
        }

        // Currently, I fetch from the server everytime. This could be a bad battery drain - maybe I should
        String remoteUserAbout = remoteProfileRepository.getUserAbout(userId);
        if (userAbout == null || !userAbout.equals(remoteUserAbout) ) {
            if (remoteUserAbout != null && userId != -1) {

                responseContract.setAbout(remoteUserAbout);
                localProfileRepository.saveUserAbout(remoteUserAbout, userId);
            }
        }
    }

    /**
     * Get the web section for a user.
     * @param userId
     */
    public void getUserWeb(long userId, RepositoryResponseContract responseContract) {
        String web = localProfileRepository.getUserWeb(userId);
        if (web !=  null) {
            responseContract.setUserWeb(web);
        }

        // Currently, I fetch from the server everytime. This could be a bad battery drain - maybe I should
        String remote = remoteProfileRepository.getUserWeb(userId);
        if (web == null || !web.equals(remote) ) {
            if (remote != null && userId != -1) {
                responseContract.setUserWeb(remote);
                // Lets save some network requests and do it
                localProfileRepository.saveUserWeb(remote, userId);
            }
        }
    }

    /**
     * Get the profile picture id for a user.
     * @param userId
     */
    public void getProfilePictureId(long userId,  RepositoryResponseContract responseContract) {
        String local = localProfileRepository.getProfilePictureId(userId);
        if (local !=  null) {
            Log.d(TAG, "found local data: " + local);
            responseContract.setUserProfilePicId(local);
        }

        // Currently, I fetch from the server everytime. This could be a bad battery drain - maybe I should
        String remote = remoteProfileRepository.getProfilePictureId(userId);
        if (local == null || !local.equals(remote) ) {
            if (remote != null && userId != -1) {
                responseContract.setUserProfilePicId(remote);

                // Lets save some network requests and do it
                localProfileRepository.saveProfilePictureId(remote, userId);
            }
        }
    }

    public void getUserTrips(long userId,  RepositoryResponseContract responseContract) {
        ArrayList<Trip> trips = localProfileRepository.getUserTrips(userId);
        if (trips != null) {
            responseContract.setUserTrips(trips);
        }

        // Currently, I fetch from the server everytime. This could be a bad battery drain - maybe I should
        ArrayList<Trip> remoteTrips = remoteProfileRepository.getUserTrips(userId);
        if (trips == null || !trips.equals(remoteTrips) ) {
            responseContract.setUserTrips(remoteTrips);

            // Lets save some network requests and do it
            //localProfileRepository.save(remote, userId);
        }
    }

    public void saveUserName(String username, long userId) {
        localProfileRepository.saveUserName(username, userId);
        remoteProfileRepository.saveUserName(username, userId);
    }

    public void saveUserAbout(String about, long userId) {
        localProfileRepository.saveUserAbout(about, userId);
        remoteProfileRepository.saveUserAbout(about, userId);
    }


    public void saveUserWeb(String website, long userId) {
        localProfileRepository.saveUserWeb(website, userId);
        remoteProfileRepository.saveUserWeb(website, userId);
    }

    public void saveProfilePictureId(String profilePicId, long userId) {
        localProfileRepository.saveProfilePictureId(profilePicId, userId);
        remoteProfileRepository.saveProfilePictureId(profilePicId, userId);
    }
}
