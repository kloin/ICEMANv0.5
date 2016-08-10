package com.teamunemployment.breadcrumbs.Album;

/**
 * @author Josiah Kendall.
 */
public interface AlbumPresenterViewContract {

    void setImageVisibility(int visible);

    void setVideoVisibility(int invisible);

    void setImageUrl(String id);

    void setScreenMessage(String message, float posX, float posY);

    void setProfilePicture(String url);

    void showMessage(String message);

    void setBuffering(int visibility);
}
