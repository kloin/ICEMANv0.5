package com.teamunemployment.breadcrumbs;

import android.graphics.Bitmap;
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
            return mCrumb.getString(Models.Crumb.EVENT_ID);
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter EventId");
            e.printStackTrace();
        }

        return null;
    }

    public Double GetLatitude() {
        try {
            return mCrumb.getDouble(Models.Crumb.LATITUDE);
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter Latitude");
            e.printStackTrace();
        }
        return null;
    }

    public Double GetLongitude() {
        try {
            return mCrumb.getDouble(Models.Crumb.LONGITUDE);
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter Longitude");
            e.printStackTrace();
        }
        return null;
    }

    public String GetTimestamp() {
        try {
            return mCrumb.getString(Models.Crumb.TIMESTAMP);
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter TimeStamp");
            e.printStackTrace();
        }
        return null;
    }

    public String GetDescription() {
        try {
            return mCrumb.getString(Models.Crumb.DESCRIPTION);
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
            return mCrumb.getString("UserId");
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter UserId");
            e.printStackTrace();
        }
        return null;
    }

    public String GetPlaceId() {
        try {
            return mCrumb.getString(Models.Crumb.PLACEID);
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter PlaceId");
            e.printStackTrace();
        }
        return null;
    }

    public String GetSuburb() {
        try {
            return mCrumb.getString(Models.Crumb.SUBURB);
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter Suburb");
            e.printStackTrace();
        }
        return null;
    }

    public String GetCity() {
        try {
            return mCrumb.getString(Models.Crumb.CITY);
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter City");
            e.printStackTrace();
        }
        return null;
    }

    public String GetCountry() {
        try {
            return mCrumb.getString(Models.Crumb.COUNTRY);
        } catch (JSONException e) {
            Log.d("CrumbModel", "Failed to find the parameter Country");
            e.printStackTrace();
        }
        return null;
    }

    public String GetMediaType() {
        try {
            return mCrumb.getString(Models.Crumb.EXTENSION);
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

    public double GetDescPosX() {
        try {
            return mCrumb.getDouble("DescPosX");
        } catch (JSONException ex) {
            Log.d("CrumbModel", "Failed to find the parameter DescPosX");
        }
        return -1;
    }

    public double GetDescPosY() {
        try {
            return mCrumb.getDouble("DescPosY");
        } catch (JSONException ex) {
            Log.d("CrumbModel", "Failed to find the parameter DescPosX");
        }
        return -1;
    }

    public int GetOrientation() {
        try {
            return mCrumb.getInt("Orientation");
        } catch (JSONException ex) {
            Log.d("CrumbModel", "Failed to find the parameter Orientation");
        }

        return 0;
    }
}
