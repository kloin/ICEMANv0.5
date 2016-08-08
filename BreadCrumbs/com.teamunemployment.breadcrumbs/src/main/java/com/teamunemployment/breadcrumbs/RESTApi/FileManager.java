package com.teamunemployment.breadcrumbs.RESTApi;

import android.content.Context;
import android.os.Environment;

import com.teamunemployment.breadcrumbs.FileManager.MediaRecordModel;
import com.teamunemployment.breadcrumbs.caching.Utils;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Josiah Kendall
 */
public class FileManager {
    public static final String hiddenFolder = "/Android/data/com.teamunemployment.breadcrumbs/cache/cantseeme";

    private Context context;
    private DatabaseController databaseController;

    public FileManager(Context context, DatabaseController databaseController) {
        this.context = context;
        this.databaseController = databaseController;
    }

    /**
     * @param id Server side frame id of this object/file.
     * @param extension The format. Will probably almost always be .mp4. JPG should use {@link com.squareup.picasso.Picasso}
     * @return A Media record object of the file that was downloaded.
     */
    public MediaRecordModel DownloadFileFromServer(String id, String extension) {
        String res = "640";
        String fileURL = "http://104.199.132.109:8080/images/"+id + "." + res + extension;
        final String cacheDir = "/Android/data/com.teamunemployment.breadcrumbs/cache/";
        String filename = cacheDir + "/" + id + extension;
        try {
            File RootFile = new File(cacheDir);
            RootFile.mkdir();
            // File root = Environment.getExternalStorageDirectory();
            URL u = new URL(fileURL);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            FileOutputStream f = new FileOutputStream(new File(RootFile, filename));
            InputStream in = c.getInputStream();
            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();
            return new MediaRecordModel(id, len1);
        } catch (Exception e) {


        }
        return null;
    }

    /**
     * Download and save a local file. Think this should be in the album model.
     * @param id
     * @return
     */
    public MediaRecordModel DownloadAndSaveLocalFile(String id) {
        return DownloadFileFromServer(id, ".mp4");
    }

    public boolean RetrieveLocalFile(String id, String extension) {
        // does file exist?

        return false;
    }

    public boolean SaveFileLocally(File file, String fileName) {
        return false;
    }

    /**
     * Test if we have a folder to add the nomedia file and other mp4 files to.
     * @return
     */
    public boolean DoesOurHiddenFolderExist() {
        final String cacheDir = "/Android/data/com.teamunemployment.breadcrumbs/cache/cantseeme";
        File f = new File(cacheDir);
        if(f.isDirectory()) {
            return true;
        }
        return false;
    }

    public boolean CreateOurHiddenFolder() {
        File dir = new File(hiddenFolder);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return true;
    }

    /**
     * Create the nomedia file that makes the files downloaded not visible.
     * @return True if created the file successfully, false if not.
     */
    public boolean initHiddenFolder() {
        final String cacheDir = "/Android/data/com.teamunemployment.breadcrumbs/cache/cantseeme";
        File f = new File(cacheDir);
        File nomediaFile = new File(cacheDir, ".nomedia");
        try {
            nomediaFile.createNewFile();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Test if our nomedia file has already been created.
     * @return
     */
    public boolean DoesNoMediaFileExist() {
        final String noMediaFileLocation = "/Android/data/com.teamunemployment.breadcrumbs/cache/cantseeme/.nomedia";
        File noMediaFile = new File(noMediaFileLocation);
        return noMediaFile.exists();
    }
}
