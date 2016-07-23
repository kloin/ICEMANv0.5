package com.teamunemployment.breadcrumbs.Trips;

import android.util.Log;

import com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils;

import org.json.JSONException;

/**
 * @author Josiah Kendall
 */
public class TripRepo {
    private static final String TAG = "TripRepo";
    private SimpleTripService simpleTripService;

    public TripRepo() {
        simpleTripService = new SimpleTripService();
    }
    public TripRepo(SimpleTripService simpleTripService) {
        this.simpleTripService = simpleTripService;
    }
    public void SaveCoverPhotoId(String trailId, String coverPhotoId) {
        simpleTripService.SaveCoverPhoto(coverPhotoId, trailId);
    }

    public void DeleteTrip(final String tripId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Deleting node");
                simpleTripService.DeleteNode(tripId);
            }
        }).start();
    }
}
