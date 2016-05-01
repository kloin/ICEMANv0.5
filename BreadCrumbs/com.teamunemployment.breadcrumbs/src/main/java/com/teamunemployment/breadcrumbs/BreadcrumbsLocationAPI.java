package com.teamunemployment.breadcrumbs;

import android.location.LocationListener;

import com.teamunemployment.breadcrumbs.BackgroundServices.MessageObjects.GPSStartEvent;
import com.teamunemployment.breadcrumbs.BackgroundServices.MessageObjects.GPSStopEvent;
import com.teamunemployment.breadcrumbs.BackgroundServices.GeofenceRemoval;
import com.teamunemployment.breadcrumbs.BackgroundServices.MessageObjects.SingleGPSRequest;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by jek40 on 23/03/2016.
 */
public class BreadcrumbsLocationAPI {

    EventBus bus = EventBus.getDefault();
    // We use eventBus to send the GpsStartEvent to the service.
    public void StartLocationService() {
        GPSStartEvent event = new GPSStartEvent(600);
        bus.post(event);
    }

    public void StopLocationService() {
        GPSStopEvent event = new GPSStopEvent();
        bus.post(event);
    }

    public void RemoveGeofences() {
        GeofenceRemoval gr = new GeofenceRemoval();
        bus.post(gr);
    }

    public void updateGpsTimer(int newTime) {

    }

    public void singleAccurateGpsRequest(LocationListener listener) {
        SingleGPSRequest request = new SingleGPSRequest(listener);
        bus.post(request);
    }
}
