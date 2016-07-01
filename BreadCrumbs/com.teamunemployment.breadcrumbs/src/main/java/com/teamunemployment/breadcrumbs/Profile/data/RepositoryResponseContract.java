package com.teamunemployment.breadcrumbs.Profile.data;

import java.util.ArrayList;

/**
 * @author Josiah Kendall
 * Simple contract to talk to the
 */
public interface RepositoryResponseContract {
    void setUserName(String userName);
    void setAbout(String about);
    void setUserWeb(String website);
    void setUserTrips(ArrayList<String> ids);
    void setUserProfilePicId(String id);

}
