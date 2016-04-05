package com.teamunemployment.breadcrumbs.Weather;

import android.location.Location;

import java.util.Date;

/**
 * Created by Josiah Kendall on 12/03/2016.
 *
 * This is a weather object that is returned by weather requests to the
 */
public class Weather {

    /*
        I have gone for public varables here as this object is (currently) all about holding these
        variables for easy use in other classes. Encapsulation with getters and setters
        is a tidier way of doing this but I dont know how well the JIT works with getters
        and setters, so at the moment I am using publics (TL;DR they are more efficient).

        If this class grows in use and complexity, make sure that we move to getters and setters,
        as the efficiency of them will increase with newer versions of android.
     */
    public String description;
    public Date dateTime;
    public Location location;
    public Double humidity;
    public int temperature;


    public Weather(String description, Date dateTime, Location location, Double humidity,  int temp) {
        this.dateTime = dateTime;
        this.description = description;
        this.humidity = humidity;
        this.location = location;
        this.temperature = temp;
    }
}
