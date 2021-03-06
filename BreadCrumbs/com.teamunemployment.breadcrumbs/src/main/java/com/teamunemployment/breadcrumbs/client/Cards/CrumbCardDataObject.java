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
    private String placeName;
    private String description;
    private String placeId;
    private Double latitude;
    private Double longitude;
    private int isLocal; // 0 is local, 1 is server
    private float descXPos;
    private float descYPos;

    public CrumbCardDataObject(String dataType, String crumbId, String placeId, Double latitude, Double longitude,
                               int isLocal, String placeName, String description, float descXpos, float descYpos) {
        this.dataType = dataType;
        this.crumbId = crumbId;
        this.placeName = placeName;
        this.description = description;
        this.placeId = placeId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.isLocal = isLocal;
        this.descXPos = descXpos;
        this.descYPos = descYpos;
    }

    protected CrumbCardDataObject(Parcel in) {
        dataType = in.readString();
        crumbId = in.readString();
        placeName = in.readString();
        description = in.readString();
        placeId = in.readString();
        longitude = in.readDouble();
        latitude = in.readDouble();
        isLocal = in.readInt();
        descXPos = in.readFloat();
        descYPos = in.readFloat();
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

    public int GetIsLocal() {return isLocal; }

    public String GetPlaceName() {
        return placeName;
    }

    public String GetDescripton() {
        return description;
    }

    public float GetDescriptionXPosition() {
        return descXPos;
    }

    public float GetDescriptionYPosition() {
        return descYPos;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dataType);
        dest.writeString(crumbId);
        dest.writeString(placeName);
        dest.writeString(description);
        dest.writeString(placeId);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(isLocal);
        dest.writeFloat(descXPos);
        dest.writeFloat(descYPos);
    }
}
