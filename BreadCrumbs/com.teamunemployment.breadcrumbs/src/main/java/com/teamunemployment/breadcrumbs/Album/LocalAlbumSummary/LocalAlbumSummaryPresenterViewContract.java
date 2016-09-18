package com.teamunemployment.breadcrumbs.Album.LocalAlbumSummary;

import android.graphics.Bitmap;

/**
 * @author Josiah Kendall
 */
public interface LocalAlbumSummaryPresenterViewContract {

    void setBitmapCoverPhoto(Bitmap bitmapCoverPhoto);
    void setAlbumTitle(String title);
    void setAlbumPublic(boolean isPublic);
}
