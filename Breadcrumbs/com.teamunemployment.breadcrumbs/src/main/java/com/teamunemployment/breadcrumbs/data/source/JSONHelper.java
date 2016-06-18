package com.teamunemployment.breadcrumbs.data.source;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.teamunemployment.breadcrumbs.data.BreadcrumbsEncodedPolyline;
import com.teamunemployment.breadcrumbs.data.BreadcrumbsPolyline;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Josiah Kendall
 *
 * Helper class to help with the processing of JSON
 */
public class JSONHelper {
    public ArrayList<BreadcrumbsEncodedPolyline> processResultIntoPolylines(final JSONObject resultObject) {
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
    private ArrayList<BreadcrumbsEncodedPolyline> processResultRunnable(JSONObject jsonObject) throws JSONException {
        ArrayList<BreadcrumbsEncodedPolyline> polylines = new ArrayList<>();
        int count = 0;
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            keys.next();
            JSONObject tempObject = jsonObject.getJSONObject(Integer.toString(count));
            boolean isEncoded = tempObject.getBoolean("IsEncoded");
            String polyline = tempObject.getString("Polyline");
            BreadcrumbsEncodedPolyline encodedPolyline = new BreadcrumbsEncodedPolyline(isEncoded, polyline);
            polylines.add(encodedPolyline);
            count += 1;
        }

        return polylines;
    }

}
