package com.teamunemployment.breadcrumbs.Album.LocalAlbumSummary;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * @author Josiah Kendall
 */
public interface LocalAlbumSummaryPresenterViewContract {

    void setBitmapCoverPhoto(Bitmap bitmapCoverPhoto);
    void setAlbumTitle(String title);
    void setAlbumPublic(boolean isPublic);

    void launchPhotoEditor(String fileId);

    void setImageBitmapFromUri(Uri resultUri);
    String getAlbumTitle();

    void startPublishingNotification();

    void showPublishConfirmationDialog();
}
