package com.teamunemployment.breadcrumbs.data.source;

import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.util.Log;

import com.teamunemployment.breadcrumbs.Location.SimpleGps;
import com.teamunemployment.breadcrumbs.Models;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.data.BreadcrumbsEncodedPolyline;
import com.teamunemployment.breadcrumbs.data.BreadcrumbsPolyline;
import com.teamunemployment.breadcrumbs.data.TripPath;
import com.teamunemployment.breadcrumbs.data.source.LocalRepository.TripLocalDataSource;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * @author Josiah Kendall
 *
 * Sync class to keep the Current trail updated with the processed path, so we can display that offline,
 * rather than the raw data. These syncs will be ~1kb per day so it should not be a big deal data-wise.
 */
public class SyncManager {


    /**
     * Essentially process the raw data on the server to server to make the map look nicer.
     * @param fetchLocalDataCallback
     * @param localId
     * @param databaseController
     */
    public void Sync(@NonNull final TripDataSource.LoadTripPathCallback fetchLocalDataCallback, final int localId,
                     @NonNull final DatabaseController databaseController, final int index) {

        Runnable syncRunnable = new Runnable() {
            @Override
            public void run() {
                sync(fetchLocalDataCallback, localId, databaseController);
            }
        };

        new Thread(syncRunnable).start();
    }

    private void sync(@NonNull TripDataSource.LoadTripPathCallback fetchLocalDataCallback, int localId,
                      @NonNull DatabaseController databaseController) {

        // Do local fetch
        int index = databaseController.GetSavedIndexForTrail(Integer.toString(localId));
        TripLocalDataSource localDataSource = new TripLocalDataSource(databaseController);
        JSONObject localData = localDataSource.FetchRawTrackingData(localId, index);


        //JSONObject localData = localDataSource.FetchRawTestData();
        if (localData.length() > 0) {
            // Send data to the server for processing and reference our result.
            JSONObject resultData = pushDataToServer(localData);

            // Help with the processing of the json.
            JSONHelper helper = new JSONHelper();
            ArrayList<BreadcrumbsEncodedPolyline> lines = helper.processResultIntoPolylines(resultData);
            // if our data is shit, we need
            if (lines.size() == 0) {
                databaseController.updateTrailSavedPoint(Integer.toString(localId), index);
            }
            TripPath tripPath = new TripPath(lines);
            localDataSource.saveTripPath(tripPath, Integer.toString(localId));
        }

        // Now that we have saved our trip, grab it from the db and display it. This will also happen if we have not
        // processed anything.
        localDataSource.getTripPath(fetchLocalDataCallback, Integer.toString(localId));
    }

    private JSONObject pushDataToServer(JSONObject localData) {
        String url = MessageFormat.format("{0}/rest/TrailManager/CalculatePath/{1}",
                LoadBalancer.RequestServerAddress(), "1");
        url = url.replaceAll(" ", "%20");
        HTTPRequestHandler requestHandler = new HTTPRequestHandler();
        String result = requestHandler.SendJSONRequest(url, localData);
        if (result == null) {
            return new JSONObject();
        }
        try {
            JSONObject resultJSON = new JSONObject(result);
            return resultJSON;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }



}
