package com.teamunemployment.breadcrumbs.Location.PathSense.Activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.widget.TextView;
import android.widget.Toast;

import com.pathsense.android.sdk.location.PathsenseDetectedActivities;
import com.pathsense.android.sdk.location.PathsenseDetectedActivity;
import com.pathsense.android.sdk.location.PathsenseDeviceHolding;
import com.pathsense.android.sdk.location.PathsenseLocationProviderApi;
import com.teamunemployment.breadcrumbs.Location.BreadcrumbsLocationProvider;
import com.teamunemployment.breadcrumbs.R;

import java.util.List;

/**
 * Written by Josiah Kendall 2016.
 *
 * This is a wrapper class around PathSense's Activity API. This is designed to allow our locationManager
 * know what the users current state is, and make decisions based on that information.
 */
public class PathSenseActivityManager {

    private static final String TAG = PathSenseActivityManager.class.getName();
    // Messages
    private static final int MESSAGE_ON_ACTIVITY_CHANGE = 1;
    private static final int MESSAGE_ON_ACTIVITY_UPDATE = 2;
    private static final int MESSAGE_ON_DEVICE_HOLDING = 3;

    //
    private InternalActivityChangeReceiver mActivityChangeReceiver;
    private InternalActivityUpdateReceiver mActivityUpdateReceiver;
    private InternalDeviceHoldingReceiver mDeviceHoldingReceiver;
    private InternalHandler mHandler;
    private PathsenseLocationProviderApi mApi;
    private TextView mTextDetectedActivity0;
    private TextView mTextDetectedActivity1;
    private TextView mTextDeviceHolding;
    private Context mContext;

    // Public constructor for now.
    public PathSenseActivityManager(Context context) {
        mContext = context;
    }

    public void StartPathSenseActivityManager() {
        mHandler = new InternalHandler(this);
        // receivers
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        mActivityChangeReceiver = new InternalActivityChangeReceiver(this);
        localBroadcastManager.registerReceiver(mActivityChangeReceiver, new IntentFilter("activityChange"));
        mActivityUpdateReceiver = new InternalActivityUpdateReceiver(this);
        localBroadcastManager.registerReceiver(mActivityUpdateReceiver, new IntentFilter("activityUpdate"));
        mDeviceHoldingReceiver = new InternalDeviceHoldingReceiver(this);
        localBroadcastManager.registerReceiver(mDeviceHoldingReceiver, new IntentFilter("deviceHolding"));
        // location api
        mApi = PathsenseLocationProviderApi.getInstance(mContext);
        mApi.requestActivityChanges(PathsenseActivityChangeBroadcastReceiver.class);
        mApi.requestActivityUpdates(PathsenseActivityUpdateBroadcastReceiver.class);
        mApi.requestDeviceHolding(PathsenseDeviceHoldingBroadcastReceiver.class);
    }

    private static class InternalActivityChangeReceiver extends BroadcastReceiver {
        PathSenseActivityManager mActivity;
        //
        InternalActivityChangeReceiver(PathSenseActivityManager activity) {
            mActivity = activity;
        }
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final PathSenseActivityManager activity = mActivity;
            final InternalHandler handler = activity != null ? activity.mHandler : null;
            //
            if (activity != null && handler != null) {
                PathsenseDetectedActivities detectedActivities = (PathsenseDetectedActivities) intent.getSerializableExtra("detectedActivities");
                Message msg = Message.obtain();
                msg.what = MESSAGE_ON_ACTIVITY_CHANGE;
                msg.obj = detectedActivities;
                handler.sendMessage(msg);
            }
        }
    }

    private static class InternalActivityUpdateReceiver extends BroadcastReceiver {
        PathSenseActivityManager mActivity;
        //
        InternalActivityUpdateReceiver(PathSenseActivityManager activity)
        {
            mActivity = activity;
        }
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final PathSenseActivityManager activity = mActivity;
            final InternalHandler handler = activity != null ? activity.mHandler : null;
            //
            if (activity != null && handler != null)
            {
                PathsenseDetectedActivities detectedActivities = (PathsenseDetectedActivities) intent.getSerializableExtra("detectedActivities");
                Message msg = Message.obtain();
                msg.what = MESSAGE_ON_ACTIVITY_UPDATE;
                msg.obj = detectedActivities;
                handler.sendMessage(msg);
            }
        }
    }

    private static class InternalDeviceHoldingReceiver extends BroadcastReceiver {
        PathSenseActivityManager mActivity;
        //
        InternalDeviceHoldingReceiver(PathSenseActivityManager activity)
        {
            mActivity = activity;
        }
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final PathSenseActivityManager activity = mActivity;
            final InternalHandler handler = activity != null ? activity.mHandler : null;
            //
            if (activity != null && handler != null) {
                PathsenseDeviceHolding deviceHolding = (PathsenseDeviceHolding) intent.getSerializableExtra("deviceHolding");
                Message msg = Message.obtain();
                msg.what = MESSAGE_ON_DEVICE_HOLDING;
                msg.obj = deviceHolding;
                handler.sendMessage(msg);
            }
        }
    }

    private static class InternalHandler extends Handler {
        PathSenseActivityManager mActivity;
        //
        InternalHandler(PathSenseActivityManager activity)
        {
            mActivity = activity;
        }
        @Override
        public void handleMessage(Message msg) {
            final PathSenseActivityManager activity = mActivity;
            final PathsenseLocationProviderApi api = activity != null ? activity.mApi : null;

            //
            if (activity != null && api != null) {
                switch (msg.what) {
                    case MESSAGE_ON_ACTIVITY_CHANGE: {
                        PathsenseDetectedActivities detectedActivities = (PathsenseDetectedActivities) msg.obj;
                        PathsenseDetectedActivity mostProbableActivity = detectedActivities.getMostProbableActivity();

                        if (mostProbableActivity != null) {
                            StringBuilder detectedActivityString = new StringBuilder(mostProbableActivity.getDetectedActivity().name());
//							if (mostProbableActivity.isStationary())
//							{
//								detectedActivityString.append(" STATIONARY");
//							}
                            activity.updateCurrentActivityStatus(mostProbableActivity.getDetectedActivity().toString());
                            Toast.makeText(mActivity.mContext, detectedActivityString.toString(), Toast.LENGTH_LONG).show();
                            //textDetectedActivity1.setText(detectedActivityString.toString());
                        } else {
                            //textDetectedActivity1.setText("");
                        }
                        break;
                    }
                    case MESSAGE_ON_ACTIVITY_UPDATE: {
                        PathsenseDetectedActivities detectedActivities = (PathsenseDetectedActivities) msg.obj;
                        PathsenseDetectedActivity mostProbableActivity = detectedActivities.getMostProbableActivity();
                        if (mostProbableActivity != null) {
                            activity.updateCurrentActivityStatus(mostProbableActivity.getDetectedActivity().toString());
                            List<PathsenseDetectedActivity> detectedActivityList = detectedActivities.getDetectedActivities();
                            int numDetectedActivityList = detectedActivityList != null ? detectedActivityList.size() : 0;
                            if (numDetectedActivityList > 0) {
                                StringBuilder detectedActivityString = new StringBuilder();
                                for (int i = 0; i < numDetectedActivityList; i++) {
                                    PathsenseDetectedActivity detectedActivity = detectedActivityList.get(i);
                                    if (i > 0) {
                                        detectedActivityString.append("<br />");
                                    }
                                    detectedActivityString.append(detectedActivity.getDetectedActivity().name() + " " + detectedActivity.getConfidence());
                                }
                                //Toast.makeText(mActivity.mContext, detectedActivityString.toString(), Toast.LENGTH_LONG).show();

                                //textDetectedActivity0.setText(Html.fromHtml(detectedActivityString.toString()));
                            }
                        } else {
                            //textDetectedActivity0.setText("");
                        }
                        break;
                    } case MESSAGE_ON_DEVICE_HOLDING: {
                        PathsenseDeviceHolding deviceHolding = (PathsenseDeviceHolding) msg.obj;
                        if (deviceHolding != null) {
                            //textDeviceHolding.setText(deviceHolding.isHolding() ? "Holding" : "Not Holding");

                        } else {
                            //textDeviceHolding.setText("");
                        }
                        break;
                    }
                }
            }
        }
    }

    // We don't what to change the activity, because it may be wrong - so we put in some checks to make sure.
    private void updateCurrentActivityStatus(String currentActivity) {
        final String SAVED_ACTIVITY_KEY = "RECORDING_ACTIVITY";
        final String PENDING_ACTIVITY_KEY = "PENDING_ACTIVITY";
        final String PENDING_ACTIVITY_COUNT_KEY = "PENDING_ACTIVITY_COUNT";

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        // This is the activity we are currently recording against.
        String savedActivity = preferences.getString(SAVED_ACTIVITY_KEY, null);


        // First time use case for the app.
        if (savedActivity == null) {
            preferences.edit().putString(SAVED_ACTIVITY_KEY, currentActivity).commit();

            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
            NotificationCompat.Builder noti = new NotificationCompat.Builder(mContext);
            noti.setContentTitle("BreadCrumbs");
            noti.setContentText("Current Activity : "+ currentActivity);
            noti.setSmallIcon(R.drawable.bc64);
            notificationManager.notify(123, noti.build());
            return;
        }

        // If we already have that as our pending activity, don't bother doing anything.
        if (!currentActivity.equals(savedActivity)) {
            // In this case we need to do some work to figure out if we should update
            String pendingActivity = preferences.getString(PENDING_ACTIVITY_KEY, null);

            if (pendingActivity == null) {
                preferences.edit().putString(PENDING_ACTIVITY_KEY, currentActivity).commit();
                preferences.edit().putInt(PENDING_ACTIVITY_COUNT_KEY, 0).commit();
                return;
            }

            // If the current pending activity is different from the last one, we need to wipe the pending stuff and start again
            if (!pendingActivity.equals(currentActivity)) {
                preferences.edit().putInt(PENDING_ACTIVITY_COUNT_KEY, 0).commit();
                preferences.edit().putString(PENDING_ACTIVITY_KEY, currentActivity).commit();
                return;
            }

            // If our pending has been pending last update, we need to check if we need to update our activity that we are recording against
            if (pendingActivity.equals(currentActivity)) {
                int numberOfChecks = preferences.getInt(PENDING_ACTIVITY_COUNT_KEY, 0);
                // If we have checked on 2 previous occasions, we need to update our activity.
                if (numberOfChecks >= 1) {
                    preferences.edit().remove(PENDING_ACTIVITY_KEY).commit();
                    preferences.edit().remove(PENDING_ACTIVITY_COUNT_KEY).commit();
                    preferences.edit().putString(SAVED_ACTIVITY_KEY, currentActivity).commit();

                    NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
                    NotificationCompat.Builder noti = new NotificationCompat.Builder(mContext);
                    noti.setContentTitle("BreadCrumbs");
                    noti.setContentText("Current Activity : "+ currentActivity);
                    noti.setSmallIcon(R.drawable.bc64);
                    notificationManager.notify(1234, noti.build());
                } else {
                    numberOfChecks += 1;
                    preferences.edit().putInt(PENDING_ACTIVITY_COUNT_KEY, numberOfChecks).commit();
                }
            }
        }
    }

    private String getMostConfidentActivity(List<PathsenseDetectedActivity> activities) {
        String mostConfidentActivity = null;
        double confidenceLevel = 0.0;
        for (PathsenseDetectedActivity activity : activities) {
            if (activity.getConfidence() > confidenceLevel) {
                mostConfidentActivity = activity.getDetectedActivity().toString();
            }
        }
        return mostConfidentActivity;
    }
}
