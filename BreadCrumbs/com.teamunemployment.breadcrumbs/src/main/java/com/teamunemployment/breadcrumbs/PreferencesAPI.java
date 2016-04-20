package com.teamunemployment.breadcrumbs;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
    private PreferencesAPI(Context context) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public static PreferencesAPI GetInstance(Context context) {
        if (mPreferencesApi == null) {
            mPreferencesApi =  new PreferencesAPI(context);
        }
        return mPreferencesApi;
    }

    public boolean isWalking() {
        return mPreferences.getString(SAVED_ACTIVITY_KEY, null).equals(PathSenseActivityManager.WALKING);
    }

    public boolean isDriving() {
        String currentActivity = mPreferences.getString(SAVED_ACTIVITY_KEY, null);
        if (currentActivity == null) {
            mPreferences.edit().putString(SAVED_ACTIVITY_KEY, "REST");
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
            NotificationCompat.Builder noti = new NotificationCompat.Builder(mContext);
            noti.setContentTitle("Activity");
            noti.setContentText("was null");
            noti.setSmallIcon(R.drawable.bc64);
            notificationManager.notify(123456789, noti.build());
            return false;
        }
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
        NotificationCompat.Builder noti = new NotificationCompat.Builder(mContext);
        noti.setContentTitle("activity");
        noti.setContentText("wasnt null");
        noti.setSmallIcon(R.drawable.bc64);
        notificationManager.notify(123456789, noti.build());
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


}
