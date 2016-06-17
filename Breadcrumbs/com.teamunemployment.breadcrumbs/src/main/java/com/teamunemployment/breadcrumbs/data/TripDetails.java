package com.teamunemployment.breadcrumbs.data;

/**
 * Created by jek40 on 15/06/2016.
 */
public class TripDetails {

    public String ServerId;
    public String StartDate;
    public String TrailName;
    public String Views;

    public TripDetails(String serverId, String trailName, String startDate) {
        ServerId = serverId;
        TrailName = trailName;
        StartDate = startDate;
    }

    public TripDetails(String serverId, String trailName, String startDate, String views) {
        ServerId = serverId;
        TrailName = trailName;
        StartDate = startDate;
        Views = views;

    }
}
