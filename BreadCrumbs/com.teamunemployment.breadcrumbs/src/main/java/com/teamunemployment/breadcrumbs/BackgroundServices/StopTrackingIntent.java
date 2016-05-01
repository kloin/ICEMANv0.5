package com.teamunemployment.breadcrumbs.BackgroundServices;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.widget.Toast;

import com.teamunemployment.breadcrumbs.BreadcrumbsActivityAPI;
import com.teamunemployment.breadcrumbs.BreadcrumbsLocationAPI;

/**
 * Created by jek40 on 25/04/2016.
 */
public class StopTrackingIntent extends IntentService {


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public StopTrackingIntent() {
        super("StopLocationIntent");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Toast.makeText(this, "WORKDING", Toast.LENGTH_LONG).show();
        BreadcrumbsLocationAPI locationAPI = new BreadcrumbsLocationAPI();
        BreadcrumbsActivityAPI activityAPI = new BreadcrumbsActivityAPI();
        String action = intent.getAction();
        if (action.equals("STOPGPS")) {
            locationAPI.StartLocationService();
            final NotificationManager systemService = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            systemService.cancel(2222);
            //activityAPI.ListenToUserActivityChanges();
        }
    }
}
