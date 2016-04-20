package com.teamunemployment.breadcrumbs.BackgroundServices;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.teamunemployment.breadcrumbs.Weather.WeatherAlarmReceiver;

import java.util.Random;

/**
 * Created by jek40 on 13/04/2016.
 */
public class DayCounterService {

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private Context mContext;
    public DayCounterService(Context context) {
        mContext = context;
    }

    private void setupLaunchForDayCounter() {
        // Create random time for when to launch the app
        Random r = new Random();

        // We thrown an alarm intent, so the recievr in the Weather package does the service sending and saving.
        alarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, WeatherAlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);

        // We only fire when the user is using their phone
        alarmManager.setInexactRepeating(AlarmManager.RTC, 0, AlarmManager.INTERVAL_DAY, alarmIntent);
    }


}
