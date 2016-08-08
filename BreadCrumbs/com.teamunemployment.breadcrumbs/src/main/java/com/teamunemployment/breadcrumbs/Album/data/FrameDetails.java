package com.teamunemployment.breadcrumbs.Album.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Josiah Kendall.
 */

public class FrameDetails {
    @SerializedName("PlaceId")
    @Expose
    private String placeId;
    @SerializedName("Suburb")
    @Expose
    private String suburb;
    @SerializedName("Latitude")
    @Expose
    private String latitude;
    @SerializedName("City")
    @Expose
    private String city;
    @SerializedName("Longitude")
    @Expose
    private String longitude;
    @SerializedName("DescPosY")
    @Expose
    private String descPosY;
    @SerializedName("TimeStamp")
    @Expose
    private String timeStamp;
    @SerializedName("DescPosX")
    @Expose
    private String descPosX;
    @SerializedName("Extension")
    @Expose
    private String extension;
    @SerializedName("UserId")
    @Expose
    private String userId;
    @SerializedName("Chat")
    @Expose
    private String chat;
    @SerializedName("Country")
    @Expose
    private String country;
    @SerializedName("Icon")
    @Expose
    private String icon;
    @SerializedName("Id")
    @Expose
    private String id;
    @SerializedName("TrailId")
    @Expose
    private String trailId;

    /**
     * @return The placeId
     */
    public String getPlaceId() {
        return placeId;
    }

    /**
     * @param placeId The PlaceId
     */
    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    /**
     * @return The suburb
     */
    public String getSuburb() {
        return suburb;
    }

    /**
     * @param suburb The Suburb
     */
    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    /**
     * @return The latitude
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     * @param latitude The Latitude
     */
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    /**
     * @return The city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city The City
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return The longitude
     */
    public String getLongitude() {
        return longitude;
    }

    /**
     * @param longitude The Longitude
     */
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    /**
     * @return The descPosY
     */
    public String getDescPosY() {
        return descPosY;
    }

    /**
     * @param descPosY The DescPosY
     */
    public void setDescPosY(String descPosY) {
        this.descPosY = descPosY;
    }

    /**
     * @return The timeStamp
     */
    public String getTimeStamp() {
        return timeStamp;
    }

    /**
     * @param timeStamp The TimeStamp
     */
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * @return The descPosX
     */
    public String getDescPosX() {
        return descPosX;
    }

    /**
     * @param descPosX The DescPosX
     */
    public void setDescPosX(String descPosX) {
        this.descPosX = descPosX;
    }

    /**
     * @return The extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * @param extension The Extension
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * @return The userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId The UserId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return The chat
     */
    public String getChat() {
        return chat;
    }

    /**
     * @param chat The Chat
     */
    public void setChat(String chat) {
        this.chat = chat;
    }

    /**
     * @return The country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country The Country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @return The icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param icon The Icon
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The Id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The trailId
     */
    public String getTrailId() {
        return trailId;
    }

    /**
     * @param trailId The TrailId
     */
    public void setTrailId(String trailId) {
        this.trailId = trailId;
    }
}
