package com.teamunemployment.breadcrumbs.BackgroundServices.MessageObjects;

/**
 * Created by jek40 on 23/03/2016.
 */
public class GPSStartEvent {

    public final int duration;
    public final int minDistance;

    public GPSStartEvent(int duration) {
        this.duration = duration;
        this.minDistance = 200; // by default atm.
    }


}
