package com.teamunemployment.breadcrumbs.Location.PathSense.Activity;

import android.util.Log;

import com.pathsense.android.sdk.location.PathsenseDetectedActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jek40 on 22/03/2016.
 */
public class UserActivityFriendlyWrapper {

    private final String TAG = "USER_ACTIVITY_OBJECT";
    public static double DRIVING = 0;
    public static double TILTING = 0;
    public static double SHAKING = 0;
    public static double HOLDING = 0;
    public static double IN_VEHICLE = 0;
    public static double WALKING = 0;

    // Variables we want to get
    private static final String DRIVING_IDENTIFIER = "DRIVING";
    private static final String WALKING_IDENTIFIER = "ON_FOOT";
    private static final String SHAKING_IDENTIFIER = "SHAKING";
    private static final String TILTING_IDENTIFIER = "TILTING";
    private static final String HOLDING_IDENTIFIER = "HOLDING";
    private static final String IN_VEHICLE_IDENTIFIER = "IN_VEHICLE";

    // Not sure about this class it might be quite expensive
    public UserActivityFriendlyWrapper(List<PathsenseDetectedActivity> activities) {
        ArrayList<String> identifiers = new ArrayList<>();
        identifiers.add(DRIVING_IDENTIFIER);
        identifiers.add(WALKING_IDENTIFIER);
        identifiers.add(SHAKING_IDENTIFIER);
        identifiers.add(TILTING_IDENTIFIER);
        identifiers.add(HOLDING_IDENTIFIER);
        identifiers.add(IN_VEHICLE_IDENTIFIER);

        if (activities.contains(DRIVING_IDENTIFIER)) {
            Log.d(TAG, "Found Identifier: "+DRIVING_IDENTIFIER);
            DRIVING = activities.get(activities.indexOf(DRIVING_IDENTIFIER)).getConfidence();
            Log.d(TAG, "Driving Confidence: " + DRIVING);
        }

        if (activities.contains(WALKING_IDENTIFIER)) {
            Log.d(TAG, "Found Identifier: "+WALKING_IDENTIFIER);
            DRIVING = activities.get(0).getConfidence();
            Log.d(TAG, "Driving Confidence: " + DRIVING);
        }
    }
}
