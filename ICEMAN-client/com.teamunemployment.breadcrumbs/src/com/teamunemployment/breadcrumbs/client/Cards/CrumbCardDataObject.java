package com.teamunemployment.breadcrumbs.client.Cards;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Written By Josiah Kendall, 2016.
 *
 * A class to contain data for the home card adapter. An array of these will be passed to the constructor.
 * @Implements Parcelable - so that I can pass the object through intents.
 */
public class CrumbCardDataObject implements Parcelable{

    private String dataType;
    private String crumbId;

    public CrumbCardDataObject(String dataType, String crumbId) {
        this.dataType = dataType;
        this.crumbId = crumbId;
    }

    protected CrumbCardDataObject(Parcel in) {
        dataType = in.readString();
        crumbId = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dataType);
        dest.writeString(crumbId);
    }
}
