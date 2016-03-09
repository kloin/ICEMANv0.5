package com.teamunemployment.breadcrumbs.GCM;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.SelectedEventViewerBase;
import com.google.android.gms.gcm.GcmListenerService;

import java.util.ArrayList;

/**
 * Created by jek40 on 16/12/2015.
 */
public class BreadcrumbsGcmReceiver extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String type = data.getString("type");
        String crumbId = data.getString("crumbId");
        String trailId = data.getString("trailId");
        String userId = data.getString("userId");
        String trailName = data.getString("trailName");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        if (type != null && type.equals("crumb")) {
            SendCrumbCommentNotification(message, userId, crumbId, trailId);
        }
            else {
            sendNotification(message);
        }
        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
       // s
        // [END_EXCLUDE]
    }
    // [END receive_message]


    private void SendCrumbCommentNotification(String message, String userId, String crumbId, String trailId) {
        Intent intent = new Intent(this, SelectedEventViewerBase.class);
        final ArrayList<String> crumbsIds = new ArrayList<>();
        crumbsIds.add(crumbId);
        intent.putStringArrayListExtra("IdArray", crumbsIds);
        intent.putExtra("TrailId", trailId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Breadcrumbs")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        String[] splitArray = message.split(",");
        Intent intent = new Intent(this, SelectedEventViewerBase.class);
        final ArrayList<String> crumbsIds = new ArrayList<>();
        crumbsIds.add(splitArray[0]);
        intent.putStringArrayListExtra("IdArray", crumbsIds);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Breadcrumbs")
                .setContentText(splitArray[1])
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}


