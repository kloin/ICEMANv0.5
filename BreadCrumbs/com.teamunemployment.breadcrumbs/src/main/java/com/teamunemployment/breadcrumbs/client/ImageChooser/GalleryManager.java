package com.teamunemployment.breadcrumbs.client.ImageChooser;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jek40 on 6/04/2016.
 */
public class GalleryManager {
    private Context mContext;

    public GalleryManager(Context context) {
        mContext = context;
    }

    public HashMap<String, GalleryFolder> GetGalleryFolders() {
        HashMap<String, GalleryFolder> galleryFolders = new HashMap<>();
        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN
        };

        // content:// style URI for the "primary" external storage volume
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // Make the query.
        Cursor cur = mContext.getContentResolver().query(images,
                projection, // Which columns to return
                null,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                null        // Ordering
        );

        Log.i("ListingImages", " query count=" + cur.getCount());

        if (cur.moveToFirst()) {
            String bucket;
            String date;

            int bucketColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            int dateColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATE_TAKEN);

            do {
                // Get the field values
                bucket = cur.getString(bucketColumn);
                if (galleryFolders.containsKey(bucket)) {
                    GalleryFolder folder = galleryFolders.get(bucket);
                    String imageId = cur.getString(0);
                    folder.Images.add(imageId);
                    folder.NumberOfPhotos += 1;
                } else {
                    GalleryFolder galleryFolder = new GalleryFolder(1, bucket);
                    String imageId = cur.getString(0);
                    galleryFolder.Images.add(imageId);
                    galleryFolders.put(bucket, galleryFolder);
                }

            } while (cur.moveToNext());

        }
        return galleryFolders;
    }
}
