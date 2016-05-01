package com.teamunemployment.breadcrumbs;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A Human friendly interface to interact with the crumb json objects
 */
public class Crumb {

    private JSONObject mCrumb;
    public Crumb(JSONObject crumb) {
        this.mCrumb = crumb;
    }

    public String GetEventId() {
        try {
            return mCrumb.getString("eventId");
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter EventId");
            e.printStackTrace();
        }

        return null;
    }

    public Double GetLatitude() {
        try {
            return mCrumb.getDouble("latitude");
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter Latitude");
            e.printStackTrace();
        }
        return null;
    }

    public Double GetLongitude() {
        try {
            return mCrumb.getDouble("longitude");
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter Longitude");
            e.printStackTrace();
        }
        return null;
    }

    public String GetTimestamp() {
        try {
            return mCrumb.getString("timeStamp");
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter TimeStamp");
            e.printStackTrace();
        }
        return null;
    }

    public String GetDescription() {
        try {
            return mCrumb.getString("description");
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter description");
            e.printStackTrace();
        }
        return null;
    }

    // Not using this yet, but will in the future.
    public String GetIcon() {
           throw new UnsupportedOperationException("Icon not supported");
    }

    public String GetUserId() {
        try {
            return mCrumb.getString("userId");
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter UserId");
            e.printStackTrace();
        }
        return null;
    }

    public String GetPlaceId() {
        try {
            return mCrumb.getString("placeId");
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter PlaceId");
            e.printStackTrace();
        }
        return null;
    }

    public String GetSuburb() {
        try {
            return mCrumb.getString("suburb");
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter Suburb");
            e.printStackTrace();
        }
        return null;
    }

    public String GetCity() {
        try {
            return mCrumb.getString("city");
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter City");
            e.printStackTrace();
        }
        return null;
    }

    public String GetCountry() {
        try {
            return mCrumb.getString("country");
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter Country");
            e.printStackTrace();
        }
        return null;
    }

    public String GetMediaType() {
        try {
            return mCrumb.getString("mime");
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter Mime");
            e.printStackTrace();
        }
        return null;
    }

    public int GetIndex() {
        try {
            return mCrumb.getInt("index");
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter Mime");
            e.printStackTrace();
        }
        return -1;
    }
}
