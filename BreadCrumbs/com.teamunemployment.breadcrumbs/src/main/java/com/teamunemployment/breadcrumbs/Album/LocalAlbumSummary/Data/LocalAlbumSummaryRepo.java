package com.teamunemployment.breadcrumbs.Album.LocalAlbumSummary.Data;

import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

/**
 * @author Josiah Kendall.
 */
public class LocalAlbumSummaryRepo {

    private DatabaseController databaseController;
    private PreferencesAPI preferencesAPI;
    private int localId;

    public LocalAlbumSummaryRepo(DatabaseController databaseController, PreferencesAPI preferencesAPI) {
        this.databaseController = databaseController;
        localId = preferencesAPI.GetLocalTrailId();
    }

    public void SaveAlbumTitle(String title) {
        databaseController.SaveTrailName(title, localId);
    }

    public void SaveAlbumPublicity(boolean isPublic) {
        databaseController.SaveAlbumPublicity(isPublic, localId);
    }

    public void SaveAlbumBitmapReference(String reference) {

    }

    public boolean getIsPublic() {
        return databaseController.getIsPublic();
    }

    public String getAlbumTitle() {
        return databaseController.GetCurrentTrailName();
    }


    public String getAlbumCoverPhotoReference() {
        return "";
    }


}
