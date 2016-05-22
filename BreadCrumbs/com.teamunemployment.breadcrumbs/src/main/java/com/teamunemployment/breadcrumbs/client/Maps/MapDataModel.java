package com.teamunemployment.breadcrumbs.client.Maps;

/**
 * @author Josiah Kendall
 *
 * Model to hold data for a trail. This is the data that will be used on the map page. This model
 * is to streamline loading for multiple sources. As we sometimes load maps from remote sources, and
 * sometimes load from local database we have differing behaviours. This class is to unify that
 * and make one point of entry for the map viewer class. This should reduce the complexity.
 */
public class MapDataModel {
    private String trailName;
    private int views;
    private String userId;
    private String duration;
    private String pointsOfInterest;
    private String numberOfVideos;
    private String numberOfPhotos;
    private String trailId;
}
