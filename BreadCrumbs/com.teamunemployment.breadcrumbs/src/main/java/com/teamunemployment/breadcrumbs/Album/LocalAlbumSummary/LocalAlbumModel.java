package com.teamunemployment.breadcrumbs.Album.LocalAlbumSummary;

import android.graphics.Bitmap;

import com.teamunemployment.breadcrumbs.Album.LocalAlbumSummary.Data.LocalAlbumSummaryRepo;
import com.teamunemployment.breadcrumbs.Album.repo.LocalAlbumRepo;
import com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils;

import javax.inject.Inject;

/**
 * @author Josiah Kendall
 */
public class LocalAlbumModel {

    @Inject
    LocalAlbumSummaryRepo localAlbumSummaryRepo;

    public LocalAlbumModel() {
        // default constructor.
    }

    public LocalAlbumModel(LocalAlbumSummaryRepo localAlbumSummaryRepo) {
        this.localAlbumSummaryRepo = localAlbumSummaryRepo;
    }

    public Bitmap LoadCoverBitmap() {
        return Utils.FetchRawBitmapFromFile("");
    }

    public void SaveBitmap(Bitmap bitmap) {
        // save our coverphoto to file.
    }

    public String LoadTitle() {
        return localAlbumSummaryRepo.getAlbumTitle();
    }

    public boolean LoadPublicity() {
        return localAlbumSummaryRepo.getIsPublic();
    }

    public void SaveTitle(String title) {
        localAlbumSummaryRepo.SaveAlbumTitle(title);
    }

    public void SavePublicity(boolean isPublic) {
        localAlbumSummaryRepo.SaveAlbumPublicity(isPublic);
    }
}

