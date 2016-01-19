package com.breadcrumbs.Framework;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by aDirtyCanvas on 4/28/2015.
 *
 * The goal of this class is to deal with the JSON that we get back from various web sources,
 * both our own and others.
 */
public class JsonHandler {
    private JSONObject jsonResponse;

    //Convert string to object. Pretty straight forward
    public JSONObject convertJsonStringToJsonObject(String json) {

        try {
            if (json != null) {
                jsonResponse = new JSONObject(json);
                return jsonResponse;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
        Covert a JSON String returned by the server into an arrayList of all the jsons values.
     */
    public ArrayList<String> ConvertJSONStringToArrayList(String json) {
        ArrayList<String> stringArrayList = new ArrayList<>();
        JSONObject jsonObject = convertJsonStringToJsonObject(json);

        try {
        // Ensure that we are not going to start throwing Null pointers
            if (jsonObject != null) {
                Iterator<String> it = jsonObject.keys();
                // Loop through all keys and put them and put their objects into the arrayList
                while (it.hasNext()) {
                    String id = it.next();
                    String crumbId = jsonObject.getString(id);
                    stringArrayList.add(crumbId);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSONHANDLE", "Error occurred processing JSON list");
        }

        return stringArrayList;
    }
}
