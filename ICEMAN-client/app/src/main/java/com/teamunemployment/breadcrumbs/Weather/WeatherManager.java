package com.teamunemployment.breadcrumbs.Weather;

import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;

import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.model.City;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;
import com.teamunemployment.breadcrumbs.Location.BreadcrumbsLocationProvider;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by Josiah Kendall on 12/03/2016.
 *
 * WeatherManager provides the app with a way of fetching a Weather object of the weather
 * by giving a Location object. The weather Object
 */
public class WeatherManager {
    private final static String wunderGroundKeyId = "30ff0330ab4311d7";
    private final static String yahooWeatherClientSecret = "e365ac99c8e5f81e00ee9f3658db6bf525955211";
    private WeatherClient weatherClient;
    private final static String TAG = "WEATHER";
    private Context mContext;

    public WeatherManager(Context context) {
        mContext = context;
        WeatherContext weatherContext = WeatherContext.getInstance();
        weatherClient = weatherContext.getClient(context);
    }


    // Fetch our current city
    public Weather FetchCurrentCity(Location location, final WeatherClient.WeatherEventListener listener) {

        // Fetch our weather for the city. We do this by searching by latitude/longitude
        weatherClient.searchCity(location.getLatitude(), location.getLongitude(), new WeatherClient.CityEventListener() {
            @Override
            public void onCityListRetrieved(List<City> cities) {
                // The data is ready
                Log.d(TAG, "Found city(s): " + cities);
                String id = cities.get(0).getId();
                getWeatherForCity(id, listener);
            }

            @Override
            public void onWeatherError(WeatherLibException e) {
                // Error on geting data
            }

            @Override
            public void onConnectionError(Throwable throwable) {
                // Connection error
            }
        });
        return null;
    }

    private void getWeatherForCity(String id, WeatherClient.WeatherEventListener listener) {
        weatherClient.getCurrentCondition(new WeatherRequest(id), listener);

    }

    @Nullable
    public Weather GetWeatherUsingLocation(Location location, WeatherClient.WeatherEventListener listener) {
        if (location == null) {
            return null;
        }
        FetchCurrentCity(location, listener);
        return null;
    }


}
