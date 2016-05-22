package com.teamunemployment.breadcrumbs.database.Models;

import com.teamunemployment.breadcrumbs.Crumb;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * @author Josiah Kendall
 *
 * A class to simplify the interaction between the metadata from the local database and the
 * trail summary statistics in {@link com.teamunemployment.breadcrumbs.client.TrailDetailsViewer}.
 * This processes the metadata and makes some nice stats for us. This
 * could be done by recording this info when we publish a trail.
 */
public class LocalTrailModel {
    private int numberOfPhotos = 0;
    private int numberOfVideos = 0;
    private int numberOfPOI = 0;

    /**
     * @param crumbs The crumbs for our local trail, fetched by {@link com.teamunemployment.breadcrumbs.database.DatabaseController#GetAllCrumbs(String)}
     */
    public LocalTrailModel(JSONObject crumbs) {
        processMetadataIntoVars(crumbs);
    }

    /**
     * Process the metadata into the statistics that we need, e.g {@link #numberOfPOI},
     * {@link #numberOfVideos}, {@link #numberOfPhotos}.
     * @param jsonObject
     */
    private void processMetadataIntoVars(JSONObject jsonObject) {
        Iterator<String> keys = jsonObject.keys();
        numberOfPOI = jsonObject.length();

        // Now iterate over and count the amount that have video or jpeg as their mime.
        while (keys.hasNext()) {
            String nextId = keys.next();
            try {
                // Create an object that is easy to deal with.
                JSONObject jsonCrumb = jsonObject.getJSONObject(nextId);
                Crumb crumb = new Crumb(jsonCrumb);

                // increment jpeg
                if (crumb.GetMediaType().equals(".jpg")) {
                    numberOfPhotos += 1;
                }

                // increment mp4
                if (crumb.GetMediaType().equals(".mp4")) {
                    numberOfVideos += 1;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public int getNumberOfPhotos() {
        return numberOfPhotos;
    }

    public int getNumberOfVideos() {
        return numberOfVideos;
    }

    public int getNumberOfPOI() {
        return numberOfPOI;
    }
}
