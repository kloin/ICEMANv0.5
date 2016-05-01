package com.teamunemployment.breadcrumbs.Location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.teamunemployment.breadcrumbs.Network.NetworkConnectivityManager;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by jek40 on 28/04/2016.
 */
public class PlaceManager {

    public static Address GetPlace(Context context, Double latitude, Double longitude) {
        if (NetworkConnectivityManager.IsNetworkAvailable(context)) {
            Geocoder gcd = new Geocoder(context, Locale.getDefault());
            try {
                final List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
                if (addresses.size() > 0) {
                    return addresses.get(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException ex) {
                // Happens when we have a Latitude of more than 90 or less than -90
                // Happens when we have a longitude of more than 180/-180
                Log.d("BC/Places", "Places request given bad coordinates : Latitude = " + latitude.toString() + " Longitude = " + longitude.toString());
            } catch (IndexOutOfBoundsException outOfBounds) {
                // WE dont have any addresses, so get fails.
                Log.d("BC/Places", "Failed to find any places - stack trace follows.");
                outOfBounds.printStackTrace();
            }
        }

        return null;
    }

    public static String GetSuburb(Address address) {
        if (address != null) {
            return address.getSubLocality();
        }

        return " "; // return an empty string which will be sent to the server.
    }

    public static String GetCity(Address address) {
        if (address != null) {
            return address.getLocality();
        }
        return " "; // return an empty string which will be sent to the server.
    }

    public static String GetCountry(Address address) {
        if (address != null) {
            return address.getCountryName();
        }
        return " "; // return an empty string which will be sent to the server.
    }
}
