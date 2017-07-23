package com.teamunemployment.breadcrumbs.RESTApi;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.teamunemployment.breadcrumbs.FileManager.MediaRecordModel;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;

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
    // Resolutions defined by this.
    public static final int LOW_QUALITY = 0;
    public static final int MEDIUM_QUALITY = 1;
    public static final int HIGH_QUALITY = 2;

    public static final String hiddenFolder = "/Android/data/com.teamunemployment.breadcrumbs/cache/cantseeme";
    private static final String TAG = "FileManager";
    private Context context;
    public FileManager(Context context) {
        this.context = context;
    }

    /**
     * @param res The target resolution
     * @param id Server side frame id of this object/file.
     * @param extension The format. Will probably almost always be .mp4. JPG should use {@link com.squareup.picasso.Picasso}
     * @return A Media record object of the file that was downloaded.
     */
    public MediaRecordModel DownloadMP4FileFromTheServer(String id, String extension, String res) {
        checkSufficientSpace();
        String fileURL = LoadBalancer.RequestCurrentDataAddress() + "/images/"+id + "." + res + extension;
        final String cacheDir = context.getExternalCacheDir().getAbsolutePath();
        createNoMediaFile(cacheDir);
        String filename = cacheDir + "/" + id + extension;
        try {
            File rootFile = new File(cacheDir);
            if (!rootFile.exists()) {
                rootFile.mkdir();
            }
            // File root = Environment.getExternalStorageDirectory();
            URL u = new URL(fileURL);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            FileOutputStream f = new FileOutputStream(new File(filename));
            InputStream in = c.getInputStream();
            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();
            return new MediaRecordModel(id, len1);
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to host.");
            e.printStackTrace();
        }
        return null;
    }

    private void createNoMediaFile(String cacheDir) {
        File dir = new File(cacheDir);
        File output = new File(dir, ".nomedia");
        try {
            boolean fileCreated = output.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Download and save a local file. Think this should be in the album model.
     * @param id The id of the target frame.
     * @return A record of the saved object
     */
    public MediaRecordModel DownloadAndSaveLocalFile(String id, String targetResolution, String ext) {
        // First ensure we have space
        // then do the download.
        if (ext.equals(".mp4")) {
            return DownloadMP4FileFromTheServer(id, ext, targetResolution);
        }
        return DownloadJPGFromServer(id, ext);
    }

    private MediaRecordModel DownloadJPGFromServer(String id, String ext) {
        checkSufficientSpace();
        String fileURL = LoadBalancer.RequestCurrentDataAddress()+"/images/"+id  + ext;
        final String cacheDir = context.getExternalCacheDir().getAbsolutePath();
        String filename = cacheDir + "/" + id + ext;

        try {
            File rootFile = new File(cacheDir);
            if (!rootFile.exists()) {
                rootFile.mkdir();
            }
            // File root = Environment.getExternalStorageDirectory();
            URL u = new URL(fileURL);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            FileOutputStream f = new FileOutputStream(new File(filename));
            InputStream in = c.getInputStream();
            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();
            return new MediaRecordModel(id, len1);
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to host.");
            e.printStackTrace();
        }
        return null;
    }

    public void deleteFile(String fileId) {
        // do delete.
    }

    /**
     * Check the available space left on our external storage
     * @return The amount of remaining space.
     */
    private long checkSufficientSpace() {
        File externalStorageDir = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(externalStorageDir.getAbsolutePath());
        long blocks = statFs.getAvailableBlocks();
        long free = (blocks * statFs.getBlockSize()) / 1024 / 1024;
        Log.d(TAG, "Free space pre dl: " + free);
        return free;
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
     * Return the size of a directory in bytes
     */
    public long dirSize(File dir) {
        if (dir.exists()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            for(int i = 0; i < fileList.length; i++) {
                // Recursive call if it's a directory
                if(fileList[i].isDirectory()) {
                    result += dirSize(fileList [i]);
                } else {
                    // Sum the file size in bytes
                    result += fileList[i].length();
                }
            }
            return result; // return the file size
        }
        return 0;
    }
}
