package com.teamunemployment.breadcrumbs.caching;

import android.content.Context;

import com.teamunemployment.breadcrumbs.Framework.JsonHandler;

import org.json.JSONObject;

/**
 * Written by Josiah kendall 2015.
 *
 * This class is a dev friendly API to retrieve text that we want to display to the user such as
 * comments, photo descriptions, names etc. This class handles all the caching and network requests
 * so that we can just use a single request.
 */
public class TextCachingInterface {

    private Context context;

    public TextCachingInterface(Context context) {
        this.context = context;
    }

    public void CacheText(String url, String textTocache) {
        TextCaching textCaching = new TextCaching(context);
        textCaching.CacheText(url, textTocache);
    }

    public JSONObject FetchDataInJSONFormat(String url) {

        TextCaching textCaching = new TextCaching(context);
        String dataToFetch = textCaching.FetchCachedText(url);

        // In this use case, we have found cached data and can return it without any need for a network request.
        if (dataToFetch != null) {
            JsonHandler jsonHandler = new JsonHandler();
            JSONObject jsonResponse = jsonHandler.convertJsonStringToJsonObject(dataToFetch);

            // this is a check to see if the JSON has been safely converted. If it has not safely converted,
            // that indicates possible data issues which would be a serious bug, or perhaps we using the wrong method.
            if (jsonResponse == null) {
                throw new NullPointerException("Cached data was found but converting said data to JSON failed." +
                        "If you need to retrieve a string rather than a JSON object, use FetchDataInStringFormat(String key, String url");
            }

            // Successfully found and converted cached JSON in this case.
            return jsonResponse;
        }

        // If we get this far it means that we did not find any cached data. Therefore we need to send a
        // need to send a network request to fetch our data


        return null;
    }


    public String FetchDataInStringFormat(String key) {
        TextCaching textCaching = new TextCaching(context);
        String dataToFetch = textCaching.FetchCachedText(key);
        return dataToFetch;
    }
}
