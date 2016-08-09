package com.teamunemployment.breadcrumbs.Album;

/**
 * @author Josiah Kendall.
 */
public interface AlbumPresenterViewContract {


    void setImageVisibility(int visible);

    void setVideoVisibility(int invisible);

    void setImageUrl(String id);

    void showMessage(String message);
}
