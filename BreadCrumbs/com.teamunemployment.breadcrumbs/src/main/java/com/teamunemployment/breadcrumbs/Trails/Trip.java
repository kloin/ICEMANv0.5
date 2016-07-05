package com.teamunemployment.breadcrumbs.Trails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jek40 on 4/07/2016.
 */
public class Trip {

    @SerializedName("StartDate")
    @Expose
    private String startDate;
    @SerializedName("CoverPhotoId")
    @Expose
    private String coverPhotoId;
    @SerializedName("Views")
    @Expose
    private String views;
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("UserId")
    @Expose
    private String userId;
    @SerializedName("TrailName")
    @Expose
    private String trailName;
    @SerializedName("Id")
    @Expose
    private String id;
    @SerializedName("Distance")
    @Expose
    private String distance;

    /**
     *
     * @return
     *     The startDate
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     *
     * @param startDate
     *     The StartDate
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     *
     * @return
     *     The coverPhotoId
     */
    public String getCoverPhotoId() {
        return coverPhotoId;
    }

    /**
     *
     * @param coverPhotoId
     *     The CoverPhotoId
     */
    public void setCoverPhotoId(String coverPhotoId) {
        this.coverPhotoId = coverPhotoId;
    }

    /**
     *
     * @return
     *     The views
     */
    public String getViews() {
        return views;
    }

    /**
     *
     * @param views
     *     The Views
     */
    public void setViews(String views) {
        this.views = views;
    }

    /**
     *
     * @return
     *     The description
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     *     The Description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     *     The userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     *
     * @param userId
     *     The UserId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     *
     * @return
     *     The trailName
     */
    public String getTrailName() {
        return trailName;
    }

    /**
     *
     * @param trailName
     *     The TrailName
     */
    public void setTrailName(String trailName) {
        this.trailName = trailName;
    }

    /**
     *
     * @return
     *     The id
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     *     The Id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     *     The distance
     */
    public String getDistance() {
        return distance;
    }

    /**
     *
     * @param distance
     *     The Distance
     */
    public void setDistance(String distance) {
        this.distance = distance;
    }

}
