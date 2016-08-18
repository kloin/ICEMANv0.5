package com.teamunemployment.breadcrumbs;

import android.content.Context;
import android.os.Environment;

import java.io.File;

import javax.inject.Inject;

/**
 * @author Josiah Kendall
 *
 * A datasource for an album.
 */
public class AlbumDataSource {

    private Context context;
    private boolean isLocal;
    private String albumId;
    private String dataSourcePath;

    @Inject
    public AlbumDataSource(Context context) {
        this.context = context;
    }

    /**
     * Set the album Id
     * @param albumId The id of the album whom this is the datasource for.
     */
    public void SetAlbumId(String albumId) {
        isLocal = isThisLocal(albumId);

        // if we are local, we want to strip the L from the character and make sure that i
        if (isLocal) {
            this.albumId = albumId.substring(0, albumId.length()-1);
            dataSourcePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();// local shit
        } else {
            this.albumId = albumId;
            File file = context.getExternalCacheDir();
            if (file == null || !file.exists()) {
                file = context.getCacheDir();
            }

            dataSourcePath = file.getAbsolutePath();
        }
    }

    private void initialisationCheck() {
        if (albumId == null) {
            throw new IllegalStateException("Must call setAlbumId(String.class) before any other methods");
        }
    }

    private boolean isThisLocal(String albumId) {
        return albumId.endsWith("L");
    }
    /**
     * Get the album Id.
     * @return The album Id. If the album is local, it will return a value without the local identifier.
     */
    public String GetAlbumId() {
        initialisationCheck();
        return albumId;
    }

    /**
     * Get our datasource. THis is a string representing the base folder for our album.
     * @return The url of the folder where our album is stored.
     */
    public String getDataSource() {
        initialisationCheck();
        return dataSourcePath;
    }

    /**
     * Get the is local variable
     * @return true if this datasource references a local datasource.
     */
    public boolean getIsLocal() {
        initialisationCheck();
        return isLocal;
    }




}
