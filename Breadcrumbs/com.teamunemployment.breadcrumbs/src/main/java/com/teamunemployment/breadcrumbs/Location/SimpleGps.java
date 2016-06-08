package com.teamunemployment.breadcrumbs.Location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.teamunemployment.breadcrumbs.caching.TextCaching;

import java.util.List;

/**
 * Created by jek40 on 25/05/2016.
 */
public class SimpleGps {

    private Context context;
    private LocationManager locationManager;

    public SimpleGps(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.context = context;
    }

    public interface Callback {
        public void doCallback(Location location);
    }

    /**
     * Returns a {@link Location} object that is most recent.
     * @return
     */
    @Nullable
    public Location GetInstantLocation() {
        Location bestResult = null;
        float bestAccuracy = 500;
        long bestTime = 0;
        List<String> matchingProviders = locationManager.getAllProviders();
        for (String provider : matchingProviders) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // We cannot call for permission now because we are in the backgorund.
                // TODO have some better logic here to raise awareness to the user that they are making it impossible for the app to do its work.
                return null;
            }
            Location location = locationManager.getLastKnownLocation(provider); // This will never throw errors because i ask for permission
            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if ((time > bestTime && accuracy < bestAccuracy)) {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                } else if (time < bestTime &&
                        bestAccuracy == Float.MAX_VALUE && time > bestTime) {
                    bestResult = location;
                    bestTime = time;
                }
            }
        }

        return bestResult;
    }

    public void FetchCoarseLocation(Callback callback) {
        Criteria locationCriteria = new Criteria();
        locationCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        TextCaching caching = new TextCaching(context);
        caching.CacheText("COARSE", "Fetching coarse location");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
          // TODO - logic here. Tell the user they are an idiot.
            return;
        }

        // Do a single request for a coarse location.
        locationManager.requestSingleUpdate(locationCriteria, getCustomLocationListener(callback), null);
    }

    /**
     * Fetch an accurate location from the gps. May take some time, and may cost gps.
     * @param callback the callback to execute when we have found a location.
     */
    public void FetchFineLocation(Callback callback) {
        Criteria locationCriteria = new Criteria();
        locationCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        TextCaching caching = new TextCaching(context);
        String text = caching.FetchCachedText("GPS_REQUESTS");
        if (text == null) {
            text = "0";
        }
        int countInt = Integer.parseInt(text);
        countInt += 1;
        caching.CacheText("GPS_REQUESTS", Integer.toString(countInt));
        // Send our gps request.
        locationManager.requestSingleUpdate(locationCriteria, getCustomLocationListener(callback), Looper.myLooper());
    }

    private LocationListener getCustomLocationListener(final Callback callback) {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                callback.doCallback(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }




}
