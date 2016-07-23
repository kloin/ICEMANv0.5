package com.teamunemployment.breadcrumbs.Network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * Created by jek40 on 8/04/2016.
 */
public class NetworkConnectivityManager {

    public static final int FAST_MOBILE_CONNECTION = 2;
    public static final int WIFI_CONNECTION = 1;
    public static final int MEDIUM_MOBILE_CONNECTION = 3;
    public static final int SLOW_MOBILE_CONNECTION = 4;
    public static final int UNKNOWN_MOBILE_CONNECTION = 5;
    private Context context;

    public NetworkConnectivityManager(Context context) {
        this.context = context;
    }

    public static boolean IsNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
    }

    public static boolean IsConnectedToWifi(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo == null) {
            return false;
        }
        return activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean IsOnData(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo == null) {
            return false;
        }
        return activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    public static int getAvailableNetworkType(Context context) {
        int type = ConnectivityManager.TYPE_WIFI; // default
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.getType() != ConnectivityManager.TYPE_WIFI) {
            String typeName = activeNetwork.getSubtypeName();
            type = activeNetwork.getSubtype();
        }
        return type;
    }
}
