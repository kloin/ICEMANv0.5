package com.teamunemployment.breadcrumbs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Josiah Kendall
 */
public class User {
    @SerializedName("Email")
    @Expose
    private String email;
    @SerializedName("ActiveTrail")
    @Expose
    private String activeTrail;
    @SerializedName("FacebookLoginId")
    @Expose
    private String facebookLoginId;
    @SerializedName("Sex")
    @Expose
    private String sex;
    @SerializedName("Nationality")
    @Expose
    private String nationality;
    @SerializedName("About")
    @Expose
    private String about;
    @SerializedName("CoverPhotoId")
    @Expose
    private String coverPhotoId;
    @SerializedName("Pin")
    @Expose
    private String pin;
    @SerializedName("Username")
    @Expose
    private String username;
    @SerializedName("Web")
    @Expose
    private String web;
    @SerializedName("ProfilePicId")
    @Expose
    private String profilePicId;
    @SerializedName("GcmId")
    @Expose
    private String gcmId;
    @SerializedName("Id")
    @Expose
    private String id;
    @SerializedName("Age")
    @Expose
    private String age;

    /**
     *
     * @return
     * The email
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * @param email
     * The Email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *
     * @return
     * The activeTrail
     */
    public String getActiveTrail() {
        return activeTrail;
    }

    /**
     *
     * @param activeTrail
     * The ActiveTrail
     */
    public void setActiveTrail(String activeTrail) {
        this.activeTrail = activeTrail;
    }

    /**
     *
     * @return
     * The facebookLoginId
     */
    public String getFacebookLoginId() {
        return facebookLoginId;
    }

    /**
     *
     * @param facebookLoginId
     * The FacebookLoginId
     */
    public void setFacebookLoginId(String facebookLoginId) {
        this.facebookLoginId = facebookLoginId;
    }

    /**
     *
     * @return
     * The sex
     */
    public String getSex() {
        return sex;
    }

    /**
     *
     * @param sex
     * The Sex
     */
    public void setSex(String sex) {
        this.sex = sex;
    }

    /**
     *
     * @return
     * The nationality
     */
    public String getNationality() {
        return nationality;
    }

    /**
     *
     * @param nationality
     * The Nationality
     */
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    /**
     *
     * @return
     * The about
     */
    public String getAbout() {
        return about;
    }

    /**
     *
     * @param about
     * The About
     */
    public void setAbout(String about) {
        this.about = about;
    }

    /**
     *
     * @return
     * The coverPhotoId
     */
    public String getCoverPhotoId() {
        return coverPhotoId;
    }

    /**
     *
     * @param coverPhotoId
     * The CoverPhotoId
     */
    public void setCoverPhotoId(String coverPhotoId) {
        this.coverPhotoId = coverPhotoId;
    }

    /**
     *
     * @return
     * The pin
     */
    public String getPin() {
        return pin;
    }

    /**
     *
     * @param pin
     * The Pin
     */
    public void setPin(String pin) {
        this.pin = pin;
    }

    /**
     *
     * @return
     * The username
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @param username
     * The Username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     * @return
     * The web
     */
    public String getWeb() {
        return web;
    }

    /**
     *
     * @param web
     * The Web
     */
    public void setWeb(String web) {
        this.web = web;
    }

    /**
     *
     * @return
     * The profilePicId
     */
    public String getProfilePicId() {
        return profilePicId;
    }

    /**
     *
     * @param profilePicId
     * The ProfilePicId
     */
    public void setProfilePicId(String profilePicId) {
        this.profilePicId = profilePicId;
    }

    /**
     *
     * @return
     * The gcmId
     */
    public String getGcmId() {
        return gcmId;
    }

    /**
     *
     * @param gcmId
     * The GcmId
     */
    public void setGcmId(String gcmId) {
        this.gcmId = gcmId;
    }

    /**
     *
     * @return
     * The id
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     * The Id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The age
     */
    public String getAge() {
        return age;
    }

    /**
     *
     * @param age
     * The Age
     */
    public void setAge(String age) {
        this.age = age;
    }

}