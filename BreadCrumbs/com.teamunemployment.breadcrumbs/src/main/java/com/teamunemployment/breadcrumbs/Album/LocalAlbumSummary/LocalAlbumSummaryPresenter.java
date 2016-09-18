package com.teamunemployment.breadcrumbs.Album.LocalAlbumSummary;

import android.content.Intent;
import android.graphics.Bitmap;

import javax.inject.Inject;

import retrofit2.http.Path;

/**
 * @author Josiah Kendall
 */
public class LocalAlbumSummaryPresenter {

    public LocalAlbumModel model;

    @Inject
    public LocalAlbumSummaryPresenter(LocalAlbumModel model) {
        this.model = model;
    }

    public LocalAlbumSummaryPresenterViewContract viewContract;

    public void setViewContract(LocalAlbumSummaryPresenterViewContract viewContract) {
        this.viewContract = viewContract;
    }

    /**
     * Load our info, such as the album title, cover photo etc.
     */
    public void loadInfo() {
        String title = model.LoadTitle();
        viewContract.setAlbumTitle(title);
        Bitmap bitmap = model.LoadCoverBitmap();
        viewContract.setBitmapCoverPhoto(bitmap);
        boolean isPublic = model.LoadPublicity();
        viewContract.setAlbumPublic(isPublic);
    }

    /**
     * Save our public or private setting for the users album
     * @param isPublic true or false setting.
     */
    public void SavePublicity(boolean isPublic) {
        model.SavePublicity(isPublic);
    }

    /**
     * Save a cover photo. NOt sure how this will work yet.
     */
    public void SaveCoverPhoto(){}

    /**
     * Save the albums title.
     * @param title The title of our album.
     */
    public void SaveTitle(String title) {
        model.SaveTitle(title);
    }

    public void LaunchPhotoSelector() {
        // launch a photo selector. When we finish, we want to launch the photo editor.

    }

    public void onPhotoSelectedResult(int requestCode, int resultCode, Intent data) {
        //
    }





}
