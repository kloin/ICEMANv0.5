package com.teamunemployment.breadcrumbs.data.source.RemoteRepository;

import android.content.Context;
import android.os.Handler;
import android.os.Trace;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.teamunemployment.breadcrumbs.Location.SimpleGps;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.data.BreadcrumbsPolyline;
import com.teamunemployment.breadcrumbs.data.Trip;
import com.teamunemployment.breadcrumbs.data.TripDetails;
import com.teamunemployment.breadcrumbs.data.TripPath;
import com.teamunemployment.breadcrumbs.data.source.TripDataSource;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Josiah Kendall.
 */
public class RemoteTripDataSource implements TripDataSource {

    private final static String TAG = "RemoteTripDataSource";
    private Context context;

    public RemoteTripDataSource(Context context) {
        this.context = context;
    }

    @Override
    public void getTripDetails(@NonNull final LoadTripDetailsCallback callback, final String id) {
        // Send request to the server to get the details from the server.
        String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetMapDetails/"+ id;
        AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) throws JSONException {
                // Need to parse the Result here so that we have
                // result = views,duration,trailname
                String [] results = result.split(",");
                TripDetails details = new TripDetails(id, results[2],  results[1]+" days", results[0] + " views");
                callback.onTripDetailsLoaded(details);
            }
        }, context);
        asyncDataRetrieval.execute();

    }

    @Override
    public void getTripPath(@NonNull final LoadTripPathCallback callback, String id) {
        // Get the trip path.
        String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetSavedPath/"+ id;
        AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) throws JSONException {
                JSONObject resultObject = new JSONObject(result);
                ArrayList<BreadcrumbsPolyline> resultArray = processResult(resultObject);
                TripPath tripPath = new TripPath(resultArray);
                callback.onTripPathLoaded(tripPath);
            }
        }, context);
        asyncDataRetrieval.execute();
    }

    private ArrayList<BreadcrumbsPolyline> processResult(final JSONObject resultObject) {
        try {
            return processResultRunnable(resultObject);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        // Just blank shit. Not sure if it will be better to do the catching in the process result method,
        // but either way It should never happen. If it does that will probably mean that we have a bug in the server.
        return new ArrayList<>();
    }

    // Runnable method that occurs off of the main UI Thread.
    private ArrayList<BreadcrumbsPolyline> processResultRunnable(JSONObject jsonObject) throws JSONException {
        ArrayList<BreadcrumbsPolyline> polylines = new ArrayList<>();
        int count = 0;
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            keys.next();
            JSONObject tempObject = jsonObject.getJSONObject(Integer.toString(count));
            boolean isEncoded = tempObject.getBoolean("IsEncoded");
            String polyline = tempObject.getString("Polyline");
            List<LatLng> listOfPoints;
            if (isEncoded) {
                listOfPoints = PolyUtil.decode(polyline);
                BreadcrumbsPolyline line = new BreadcrumbsPolyline(true, (ArrayList<LatLng>) listOfPoints);
                polylines.add(line);
                //listOfPoints = addLastPointToList(listOfPoints);

            } else {
                listOfPoints = parseNonEncodedPolyline(polyline);
                BreadcrumbsPolyline line = new BreadcrumbsPolyline(false, (ArrayList<LatLng>) listOfPoints);
                polylines.add(line);
            }

            count += 1;
        }

        return polylines;
    }

    public List<LatLng> parseNonEncodedPolyline(String polylineString) {

        ArrayList<LatLng> listOfPoints = new ArrayList<>();
        //if (lastPoint != null) {
        //listOfPoints.add(0,lastPoint);
        //}
        String[] pointsString = polylineString.split("\\|");
        for (int index = 0; index < pointsString.length; index += 1 ) {
            try {
                // Points are store in string array like lat,long so we need to spit it again for each point.
                String[] latAndLong = pointsString[index].split(",");
                LatLng latLng = new LatLng(Double.parseDouble(latAndLong[0]), Double.parseDouble(latAndLong[1]));
                listOfPoints.add(latLng);
            } catch(NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
        return listOfPoints;
    }



    @Override
    public void getCurrentTrip(@NonNull LoadCurrentTripCallback callback, String id) {

    }

    @Override
    public void saveTrip(@NonNull Trip trip) {

    }

    @Override
    public void refreshTrips() {

    }

    @Override
    public void deleteTrip(@NonNull String tripId) {

    }
}
