package com.breadcrumbs.client;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;

import com.breadcrumbs.caching.GlobalContainer;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

/*
    This is where UI thread methods are run
 */
public class RunOnUiThreads {
    private GoogleMap map;
    private Location location;
    private Context context;
    private Location lastCheckedPoint;
    private GlobalContainer container;
    private Handler handler;
    public RunOnUiThreads() {
        container = GlobalContainer.GetContainerInstance();
    }
    public void DrawLineOnMap(final Location location, Context context, final Location lastCheckedPoint) {

        this.context = context;
        this.location = location;
        this.map = container.GetGoogleMapInstance();
        this.lastCheckedPoint = lastCheckedPoint;
        handler = new Handler(context.getMainLooper());
        final Runnable r = new Runnable(){
            @Override
            public void run() {
                Double lat = location.getLatitude();
                Double lon = location.getLongitude();



                    //GoogleMap mMap = ((MapFragment) context.get.getFragmentManager().findFragmentById(R.id.map)).getMap();
                    map.addPolyline(new PolylineOptions().add(new LatLng(lat, lon), new LatLng(lastCheckedPoint.getLatitude(), lastCheckedPoint.getLongitude())).width(5).color(Color.GREEN));

                    //savePoint(loc1);
                    //Send url to save a point for the map
                    handler.post(this);
                try
                {
                    Thread.sleep( 5 );
                }
                catch( InterruptedException e )
                {
                    //_running = false;
            /* at least we tried */
                }


            }
        };
        handler.post(r);
    }
}
