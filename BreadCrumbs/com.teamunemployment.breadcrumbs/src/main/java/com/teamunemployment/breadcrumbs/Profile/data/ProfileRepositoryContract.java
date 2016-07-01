package com.teamunemployment.breadcrumbs.Profile.data;

import java.util.ArrayList;

/**
 * Created by jek40 on 30/06/2016.
 */
public interface ProfileRepositoryContract {
    String getUserName(long userId);

    String getUserAbout(long userId);

    String getUserWeb(long userId);

    String getProfilePictureId(long userId);

    ArrayList<String> getUserTrailIds(long userId);

    void saveUserName(String username, long userId);

    void saveUserAbout(String about, long userId);

    void saveUserWeb(String website, long userId);

    void saveProfilePictureId(String profilePicId, long userId);



}
