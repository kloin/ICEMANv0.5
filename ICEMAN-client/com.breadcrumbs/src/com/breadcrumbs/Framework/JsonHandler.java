package com.breadcrumbs.Framework;

import org.json.JSONException;
import org.json.JSONObject;

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
            jsonResponse = new JSONObject(json);
            return jsonResponse;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
