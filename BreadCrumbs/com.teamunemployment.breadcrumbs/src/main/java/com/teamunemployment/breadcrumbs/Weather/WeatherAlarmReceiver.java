package com.teamunemployment.breadcrumbs.Weather;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.City;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.teamunemployment.breadcrumbs.Location.BreadcrumbsLocationProvider;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import java.util.List;

/**
 * Written by Josiah kendall 2016.
 *
 * This is the reciever for the weather alarm. The alarm throws this intent service to handle the fetching and saving of weather for the day.
 * This alarm is thrown once a day, during the day sometime. It will only be thrown while the device is active (RTC);
 */
public class WeatherAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "Weather Reciever";
    private Context mContext;
    private Location mLocation;




    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        final WeatherManager weatherManager = new WeatherManager(context);
        mLocation = BreadcrumbsLocationProvider.getInstance(context).androidLocationManager.getLastKnownLocation("Network");
        if (mLocation == null) {
            // May happen. This will mean we will have to just take it from the most recent point in the db.
            BreadcrumbsLocationProvider.getInstance(context).requestLocationUpdate(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mLocation = location;
                    weatherManager.GetWeatherUsingLocation(location, listener);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
            return;
        }



        // Fetch and save our weather updates.
        weatherManager.GetWeatherUsingLocation(mLocation, listener);

    }

/*    @Override
    protected void onHandleIntent(Intent intent) {
        final WeatherManager weatherManager = new WeatherManager(getApplicationContext());
        mLocation = BreadcrumbsLocationProvider.getInstance(this.getApplicationContext()).androidLocationManager.getLastKnownLocation("Network");
        if (mLocation == null) {
            // May happen. This will mean we will have to just take it from the most recent point in the db.
            BreadcrumbsLocationProvider.getInstance(this.getApplicationContext()).requestLocationUpdate(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mLocation = location;
                    weatherManager.GetWeatherUsingLocation(location, listener);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
            return;
        }



        // Fetch and save our weather updates.
        weatherManager.GetWeatherUsingLocation(mLocation, listener);

    }*/

    /*
        This is what handles our weather after we have successfully retrieved it.
     */
    private WeatherClient.WeatherEventListener listener = new WeatherClient.WeatherEventListener() {
        @Override
        public void onWeatherRetrieved(CurrentWeather currentWeather) {
            float currentTemp = currentWeather.weather.temperature.getTemp();
            String weatherCondition = currentWeather.weather.currentCondition.getCondition();
            int weatherId = currentWeather.weather.currentCondition.getWeatherId();
            String city = currentWeather.weather.location.getCity();

            String travelDayNum = null; // We can work this out server side by knowing the start date of the server
            DatabaseController databaseController = new DatabaseController(mContext);
            databaseController.AddWeather(Integer.toString(weatherId), travelDayNum, weatherCondition, mLocation.getLatitude(), mLocation.getLongitude(), city, Float.toString(currentTemp));

            NotificationManager notificationManager = (NotificationManager)  mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
            NotificationCompat.Builder noti = new NotificationCompat.Builder(mContext);
            noti.setContentTitle("Weather for today");
            noti.setContentText("Weather today is: " + weatherCondition + ". CurrentTemperature: " + currentTemp);
            noti.setSmallIcon(R.drawable.bc64);
            notificationManager.notify(1234567, noti.build());


            Log.d(TAG, "Processed weather event and saved weather to the DB.");
        }

        // When I fail I could just notify the db/Metadata table, so that we know that we have failed
        // and can then possibly "replace" it at a later date. But for now I am not doing shit
        @Override
        public void onWeatherError(WeatherLibException e) {
            Log.d("WL", "Weather Error - parsing data");
            e.printStackTrace();
        }

        @Override
        public void onConnectionError(Throwable throwable) {
            Log.d("WL", "Connection error");
            throwable.printStackTrace();
        }
    };
}