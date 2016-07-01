package com.teamunemployment.breadcrumbs.Profile.data;

/**
 * Created by jek40 on 30/06/2016.
 */
public class ProfileRepository {

    // Contract is used to pass things back to the server multiple times.
    private RepositoryResponseContract responseContract;
    private LocalProfileRepository localProfileRepository;
    private RemoteProfileRepository remoteProfileRepository;

    public ProfileRepository(RepositoryResponseContract contract, LocalProfileRepository localProfileRepository, RemoteProfileRepository remoteProfileRepository) {
        responseContract = contract;
        this.localProfileRepository = localProfileRepository;
        this.remoteProfileRepository = remoteProfileRepository;
    }

    /**
     * Get the username for a given user.
     * @param userId
     */
    public void getUserName(long userId) {
        String userName = localProfileRepository.getUserName(userId);
        if (userName !=  null) {
            responseContract.setUserName(userName);
        }

        // Currently, I fetch from the server everytime. This could be a bad battery drain - maybe I should
        String remoteUserName = remoteProfileRepository.getUserName(userId);
        if (userName == null || !userName.equals(remoteUserName) ) {
            responseContract.setUserName(remoteUserName);

            // Lets save some network requests and do it
            localProfileRepository.saveUserName(remoteUserName, userId);
        }
    }

    /**
     * Get the about section for a user.
     * @param userId
     */
    public void getUserAbout(long userId) {
        String userAbout = localProfileRepository.getUserAbout(userId);
        if (userAbout !=  null) {
            responseContract.setAbout(userAbout);
        }

        // Currently, I fetch from the server everytime. This could be a bad battery drain - maybe I should
        String remoteUserAbout = remoteProfileRepository.getUserAbout(userId);
        if (userAbout == null || !userAbout.equals(remoteUserAbout) ) {
            responseContract.setAbout(remoteUserAbout);

            // Lets save some network requests and do it
            localProfileRepository.saveUserAbout(remoteUserAbout, userId);
        }
    }

    /**
     * Get the web section for a user.
     * @param userId
     */
    public void getUserWeb(long userId) {
        String web = localProfileRepository.getUserWeb(userId);
        if (web !=  null) {
            responseContract.setUserWeb(web);
        }

        // Currently, I fetch from the server everytime. This could be a bad battery drain - maybe I should
        String remote = remoteProfileRepository.getUserWeb(userId);
        if (web == null || !web.equals(remote) ) {
            responseContract.setUserWeb(remote);

            // Lets save some network requests and do it
            localProfileRepository.saveUserWeb(remote, userId);
        }
    }

    /**
     * Get the profile picture id for a user.
     * @param userId
     */
    public void getProfilePictureId(long userId) {
        String local = localProfileRepository.getProfilePictureId(userId);
        if (local !=  null) {
            responseContract.setUserProfilePicId(local);
        }

        // Currently, I fetch from the server everytime. This could be a bad battery drain - maybe I should
        String remote = remoteProfileRepository.getProfilePictureId(userId);
        if (local == null || !local.equals(remote) ) {
            responseContract.setUserProfilePicId(remote);

            // Lets save some network requests and do it
            localProfileRepository.saveProfilePictureId(remote, userId);
        }
    }

    public void getUserTrailIds(long userId) {

    }

    public void saveUserName(String username, long userId) {

    }

    public void saveUserAbout(String about, long userId) {

    }


    public void saveUserWeb(String website, long userId) {

    }

    public void saveProfilePictureId(String profilePicId, long userId) {

    }
}
