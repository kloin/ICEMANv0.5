package com.teamunemployment.breadcrumbs.Weather;

import android.location.Location;

import org.json.JSONObject;

/**
 * Created by Josiah Kendall on 12/03/2016.
 *
 * WeatherManager provides the app with a way of fetching a Weather object of the weather
 * by giving a Location object. The weather Object
 */
public class WeatherManager {
    private final static String wunderGroundKeyId = "30ff0330ab4311d7";
    private final static String yahooWeatherClientSecret = "e365ac99c8e5f81e00ee9f3658db6bf525955211";

    // Fetch the weather for a given location. Returns our custom weather object.
    public Weather GetWeatherForGivenLocation(Location location) {
        JSONObject jsonWeatherRespnose = getWeatherUsingLocation(location);

        return null;
    }

    private JSONObject getWeatherUsingLocation(Location location) {

        return null;
    }
}
