package com.teamunemployment.breadcrumbs;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.teamunemployment.breadcrumbs.Location.PathSense.Activity.PathSenseActivityManager;


/**
 * Created by jek40 on 17/04/2016.
 */
public class PreferencesAPI {

    private final String SAVED_ACTIVITY_KEY = "RECORDING_ACTIVITY";
    private Context mContext;
    private SharedPreferences mPreferences;
    private static PreferencesAPI mPreferencesApi;
    public PreferencesAPI(Context context) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isWalking() {
        return mPreferences.getString(SAVED_ACTIVITY_KEY, null).equals(PathSenseActivityManager.WALKING);
    }

    public boolean isDriving() {
        String currentActivity = mPreferences.getString(SAVED_ACTIVITY_KEY, null);
        if (currentActivity == null) {
            return false;
        }
        return currentActivity.equals(PathSenseActivityManager.DRIVING);
    }

    // save our local trail Id
    public void SaveCurrentLocalTrailId(int id) {
        mPreferences.edit().putInt("LOCAL_TRAIL", id).commit();
    }

    public int GetLocalTrailId() {
        return mPreferences.getInt("LOCAL_TRAIL", -1);
    }

    public int GetServerTrailId() {
        return mPreferences.getInt("SERVER_TRAIL", -1);
    }
    // save our local trail Id
    public void SaveCurrentServerTrailId(int id) {
        mPreferences.edit().putInt("SERVER_TRAIL", id).commit();
    }

    public void DeleteCurrentUserState() {
        mPreferences.edit().remove("STATE").commit();
    }

    public void SetTracking(boolean tracking) {
        mPreferences.edit().putBoolean("TRACKING", tracking).commit();
    }

    public boolean isTracking() {
        return mPreferences.getBoolean("TRACKING", false);
    }

    public void SetUserTracking(boolean tracking) {
        mPreferences.edit().putBoolean("USER_TRACKING", tracking).commit();
    }

    public boolean isTrackingEnabledByUser() {
        return mPreferences.getBoolean("USER_TRACKING", false);
    }

    public String GetUserId() {
        return mPreferences.getString("USERID", null);
    }

    public void SetUserId(String userId) {
        mPreferences.edit().putString("USERID", userId).commit();
    }

    public int GetCurrentIndex() {
        return mPreferences.getInt("CURRENT_INDEX", 0);
    }

    public void SetCurrentIndex(int index) {
        mPreferences.edit().putInt("CURRENT_INDEX", index).commit();
    }

    public int GetTransportMethod() {
        return mPreferences.getInt("TRANSPORT_METHOD", 1); // walking by default
    }

    public void SetTransportMethod(int transportMethod) {
        mPreferences.edit().putInt("TRANSPORT_METHOD", transportMethod).commit();
    }

    public void SaveTrailNameString(String trailName) {
        mPreferences.edit().putString("TRAIL_NAME", trailName).commit();
    }

    public String GetTrailName() {
        return mPreferences.getString("TRAIL_NAME", "");
    }

    public void RemoveActivityBasedValues() {
        mPreferences.edit().remove("TRANSPORT_METHOD").commit();
        //mPreferences.edit().remove("STATE").commit();
    }

    public void SetLastSavedMediaCrumbIndex(int index) {
        mPreferences.edit().putInt("CRUMB_INDEX", index).commit();
    }

    public int GetLastSavedMediaCrumbIndex() {
        return mPreferences.getInt("CRUMB_INDEX", -1);
    }

    public void RemoveTrailBasedValues() {
        // Remove all these
        mPreferences.edit().remove("CRUMB_INDEX").apply();
        mPreferences.edit().remove("TRANSPORT_METHOD").apply();
        mPreferences.edit().remove("TRAIL_NAME").apply();
        mPreferences.edit().remove("CURRENT_INDEX").apply();
        mPreferences.edit().remove("USER_TRACKING").apply();
        mPreferences.edit().remove("TRACKING").apply();
        mPreferences.edit().remove("SERVER_TRAIL").apply();
        mPreferences.edit().remove("LOCAL_TRAIL").apply();
        mPreferences.edit().remove("STATE").apply();
        mPreferences.edit().remove("TrailCoverPhoto").apply();
    }

    public String GetCurrentTrailCoverPhoto() {
        return mPreferences.getString("TrailCoverPhoto", null);
    }

    public void SetCurrentTrailCoverPhoto(String id) {
        mPreferences.edit().putString("TrailCoverPhoto", id).commit();
    }

    public int GetEventId() {
        return mPreferences.getInt("EVENTID", 0);
    }

    public void SetEventId(int eventId) {
        mPreferences.edit().putInt("EVENTID", eventId).commit();
    }

    public int GetVideoId() {
        return mPreferences.getInt("VIDEOID", 0);
    }

    public void SetVideoId(int videoId) {
        mPreferences.edit().putInt("VIDEOID", videoId).commit();
    }

    public void SetUserName(String userName) {
        mPreferences.edit().putString("UserName", userName).commit();
    }

    public void SetIsUploading(boolean isUploading) {
        mPreferences.edit().putBoolean("IsUploading", isUploading).commit();
    }

    public boolean IsUploading() {
        return mPreferences.getBoolean("IsUploading", false);
    }

    @Nullable
    public String GetUserName() {
        return mPreferences.getString("USERNAME", null);
    }

    public int GetLastActivity() {
        return mPreferences.getInt("LastActivity", -1);
    }

    public void SetLastActivity(int lastActivity) {
        mPreferences.edit().putInt("LastActivity", lastActivity).commit();
    }

    public void SetCurrentTab(int tab) {
        mPreferences.edit().putInt("HOME_TAB", tab).commit();
    }

    public int GetCurrentTab() {
        return mPreferences.getInt("HOME_TAB",-1);
    }

    public void SetUserCoverPhoto(String s) {
        mPreferences.edit().putString("PersonalCoverPhoto",s).commit();
    }

    public String GetUserCoverPhoto() {
        return mPreferences.getString("PersonalCoverPhoto", null);
    }
}
