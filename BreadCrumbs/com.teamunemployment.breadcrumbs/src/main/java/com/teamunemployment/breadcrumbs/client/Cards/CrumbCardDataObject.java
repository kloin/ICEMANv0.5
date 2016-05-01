package com.teamunemployment.breadcrumbs.client.Cards;

import android.os.Parcel;
import android.os.Parcelable;

import com.teamunemployment.breadcrumbs.client.Maps.DisplayCrumb;

/**
 * Written By Josiah Kendall, 2016.
 *
 * A class to contain data for the home card adapter. An array of these will be passed to the constructor.
 * @Implements Parcelable - so that I can pass the object through intents.
 */
public class CrumbCardDataObject implements Parcelable{
    private String dataType;
    private String crumbId;
    private String placeId;
    private Double latitude;
    private Double longitude;

    public CrumbCardDataObject(String dataType, String crumbId, String placeId, Double latitude, Double longitude) {
        this.dataType = dataType;
        this.crumbId = crumbId;
        this.placeId = placeId;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    protected CrumbCardDataObject(Parcel in) {
        dataType = in.readString();
        crumbId = in.readString();
        placeId = in.readString();
        longitude = in.readDouble();
        latitude = in.readDouble();
    }

    public static final Creator<CrumbCardDataObject> CREATOR = new Creator<CrumbCardDataObject>() {

        @Override
        public CrumbCardDataObject createFromParcel(Parcel in) {
            return new CrumbCardDataObject(in);
        }

        @Override
        public CrumbCardDataObject[] newArray(int size) {
            return new CrumbCardDataObject[size];
        }
    };

    public String GetDataType() {
        return dataType;
    }

    public String GetCrumbId() {
        return crumbId;
    }

    public String GetPlaceId() {
        return placeId;
    }

    public Double GetLatitude() {
        return latitude;
    }

    public Double GetLongitude() {
        return longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dataType);
        dest.writeString(crumbId);
        dest.writeString(placeId);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
