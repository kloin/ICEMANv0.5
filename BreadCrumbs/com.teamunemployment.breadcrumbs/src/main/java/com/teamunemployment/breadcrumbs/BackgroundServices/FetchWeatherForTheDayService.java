package com.teamunemployment.breadcrumbs.BackgroundServices;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.teamunemployment.breadcrumbs.Weather.WeatherAlarmReceiver;

import java.util.Random;

/**
 * Created by jek40 on 24/03/2016.
 */
public class FetchWeatherForTheDayService {
    private long minMillis = 21600000;
    private long maxMillis = 64800000;
    private long launchTimeForWeatherRequest;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    private Context mContext;

    private FetchWeatherForTheDayService(Context context) {
        setLaunchTimeForWeatherRequest();
        mContext = context;
    }

    public static void setLaunchTimeForWeatherRequest(Context mContext) {
        new FetchWeatherForTheDayService(mContext);
    }

    private void setLaunchTimeForWeatherRequest() {
        // Create random time for when to launch the app
        Random r = new Random();
        launchTimeForWeatherRequest = minMillis + (maxMillis - minMillis) * r.nextLong();

        // We thrown an alarm intent, so the recievr in the Weather package does the service sending and saving.
        alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, WeatherAlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);

        // We only fire when the user is using their phone
        alarmMgr.setInexactRepeating(AlarmManager.RTC, launchTimeForWeatherRequest, AlarmManager.INTERVAL_DAY, alarmIntent);
    }
}
