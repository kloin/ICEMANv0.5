package com.teamunemployment.breadcrumbs;

import android.location.LocationListener;

import com.teamunemployment.breadcrumbs.BackgroundServices.GPSStartEvent;
import com.teamunemployment.breadcrumbs.BackgroundServices.GPSStopEvent;
import com.teamunemployment.breadcrumbs.BackgroundServices.SingleGPSRequest;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by jek40 on 23/03/2016.
 */
public class BreadcrumbsLocationAPI {

    EventBus bus = EventBus.getDefault();

    // We use eventBus to send the GpsStartEvent to the service.
    public void StartLocationService() {
        GPSStartEvent event = new GPSStartEvent(200);
        bus.post(event);
    }

    public void StopLocationService() {
        GPSStopEvent event = new GPSStopEvent();
        bus.post(event);
    }

    public void updateGpsTimer(int newTime) {

    }

    public void singleAccurateGpsRequest(LocationListener listener) {
        SingleGPSRequest request = new SingleGPSRequest(listener);
        bus.post(request);
    }


    //public
}
