package com.teamunemployment.breadcrumbs.Album;

import android.graphics.Bitmap;

/**
 * @author Josiah Kendall.
 */
public interface AlbumPresenterViewContract {

    void setImageVisibility(int visible);

    void setVideoVisibility(int invisible);

    void setImageUrl(String id);
    void setImageBitmap(Bitmap bitmap);

    void setScreenMessage(String message, float posX, float posY);

    void setProfilePicture(String url);

    void showMessage(String message);

    void setBufferingVisible();
    void setBufferingInvisible();

    void setUserName(String userName);
    void setProgressBarState(int position);
    void setProgressBarMax(int max);
    void finishUp();
}
