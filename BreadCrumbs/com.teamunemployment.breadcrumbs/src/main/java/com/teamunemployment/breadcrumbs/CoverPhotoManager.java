package com.teamunemployment.breadcrumbs;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UploadFile;
import com.teamunemployment.breadcrumbs.caching.Utils;

/**
 * @author Josiah Kendall
 */
public class CoverPhotoManager {

    public void SaveCoverPhotoToLocal(Bitmap coverBitmap) {
        String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/coverphoto.jpg";
        Utils.SaveBitmap(fileName, coverBitmap);
    }

    public void SaveCoverPhotoToServer(Bitmap coverBitmap) {
        // save bitmap to server.

        // save and grab id
        String id = "1"; // this isour saved id.

        // Save our bitmap here.
        String url = LoadBalancer.RequestServerAddress() + "/rest/Crumb/SaveImageToDatabase/" + id;
        UploadFile uploadFile = new UploadFile(url, null, coverBitmap);
        uploadFile.execute();

    }
}
