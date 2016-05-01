package com.teamunemployment.breadcrumbs.BackgroundServices.MessageObjects;

import android.location.LocationListener;

/**
 * Written by Josiah kendall 2016.
 */
public class SingleGPSRequest {
    public final LocationListener listener;

    public SingleGPSRequest(LocationListener listener) {
        this.listener = listener;
    }
}
