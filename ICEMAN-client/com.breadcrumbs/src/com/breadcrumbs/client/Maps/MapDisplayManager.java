package com.breadcrumbs.client.Maps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.breadcrumbs.Framework.NonSwipeableViewPager;
import com.breadcrumbs.caching.GlobalContainer;
import com.breadcrumbs.client.Cards.CrumbCardAdapter;
import com.breadcrumbs.client.Cards.HomeCardAdapter;
import com.breadcrumbs.client.CrumbViewer.CrumbViewerAdapter;
import com.breadcrumbs.client.CrumbViewer.CrumbViewerFragment;
import com.breadcrumbs.client.R;
import com.breadcrumbs.client.SelectedEventViewerBase;
import com.breadcrumbs.client.tabs.SubscriptionTabHolder;
import com.google.android.gms.drive.internal.t;
import com.google.android.gms.drive.internal.v;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;


import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by aDirtyCanvas on 6/2/2015.
 */
public class MapDisplayManager implements GoogleMap.OnMarkerClickListener {
private GoogleMap mapInstance = null;
    private GlobalContainer globalContainer = GlobalContainer.GetContainerInstance();
    private Activity context;
    private ClusterManager<DisplayCrumb> clusterManager;
    private RecyclerView mRecyclerView;
    private String trailId;

    public MapDisplayManager( GoogleMap map, Activity context, String trailId) {
        this.mapInstance = map;
        this.context = context;
        this.trailId = trailId;
        setUpClusterManager();

    }

    /*
     * Get the map instance. Needed because the map may be null sometimes, so I will need to get the
     * instance from the global container.
     *
     * @ Returns a GoogleMap instance.
     */
    public GoogleMap PassTheMapPlease() {
        if (mapInstance == null) {
            mapInstance = globalContainer.GetGoogleMapInstance();
        }
        return mapInstance;
    }

    /*
     * Set up map - buttons and shit. TODO if needed.
     */
    private void setUpMapInstance() {

    }


    public void setUpClusterManager() {
        clusterManager = new ClusterManager<DisplayCrumb>(context, mapInstance);
        CustomCrumbCluster customCrumbCluster = new CustomCrumbCluster(context, mapInstance, clusterManager);
        //clusterManager.setRenderer(customCrumbCluster); // Used to draw our custom icons.
        mapInstance.setOnCameraChangeListener(clusterManager);
        mapInstance.setOnMarkerClickListener(clusterManager);
        clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<DisplayCrumb>() {

            @Override
            public boolean onClusterClick(Cluster<DisplayCrumb> displayCrumbCluster) {
                Intent viewCrumbsIntent = new Intent(context, SelectedEventViewerBase.class);
                ArrayList<DisplayCrumb> crumbs = (ArrayList<DisplayCrumb>) displayCrumbCluster.getItems();
                ArrayList<String> crumbsIds = new ArrayList<>();
                Iterator<DisplayCrumb> crumbIterator = crumbs.iterator();
                while (crumbIterator.hasNext()) {
                    DisplayCrumb next = crumbIterator.next();
                    crumbsIds.add(next.getId());
                }

                viewCrumbsIntent.putStringArrayListExtra("IdArray", crumbsIds); // Note - this is currently using serializable - shoiuld use parcelable for speed
                viewCrumbsIntent.putExtra("TrailId", trailId);
                context.startActivity(viewCrumbsIntent);
                return true;
            }
        });

        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<DisplayCrumb>() {
            @Override
            public boolean onClusterItemClick(DisplayCrumb clusterItem) {
                // Get the id, and add it to the array to be passed to the
                ArrayList<String> idArray = new ArrayList<>();
                idArray.add(clusterItem.getId());
                Intent viewCrumbsIntent = new Intent(context, SelectedEventViewerBase.class);
                ArrayList<String> crumbs = new ArrayList<>();
                crumbs.add(clusterItem.getId());
                viewCrumbsIntent.putStringArrayListExtra("IdArray", crumbs);
                viewCrumbsIntent.putExtra("TrailId", trailId);
                context.startActivity(viewCrumbsIntent);

                // Consumes it?
                return false;
            }
        });

       // clusterManager.setOnClusterItemClickListener();
    }

    private void loadCrumbs(Cluster<DisplayCrumb> cluster) {

        ArrayList<String> idArrayList = new ArrayList<String>();
    }

    // Get all the ids of the crumbs we want to display in this intent.
    private ArrayList<String> getCrumbIds(Cluster<DisplayCrumb> cluster) {

        ArrayList<String> ids = new ArrayList<String>();
        Object[] crumbCollection = globalContainer.GetCluster().getItems().toArray();

        // Get all the ids for the crumbs that we need to display
        for (int index = 0; index < cluster.getSize(); index+=1) {

            DisplayCrumb crumb = (DisplayCrumb) crumbCollection[index];
            //Temp

            ids.add(crumb.getId());


        }
        // Return our ids
        return ids;
    }
    /*
     *  Draw the crumbs onto the map using the object given.
     *
     * @Throws a JSONException, because the crumb should always have the fields we are asking of it.
     */
    public void DrawCrumbFromJson(JSONObject crumb) throws JSONException {
        //We need to test that we have a map instance
        mapInstance = PassTheMapPlease();

        // Get our variables
        Double Latitude = crumb.getDouble("Latitude");
        Double Longitude = crumb.getDouble("Longitude");
        String id = crumb.getString("Id");
        String mediaType = crumb.getString("Extension");
        // Construct the location, set our clickListener (handled in here).

        // Show the shit on the map
        mapInstance.setMyLocationEnabled(true);

        DisplayCrumb displayCrumb = new DisplayCrumb(Latitude, Longitude, mediaType, id, R.drawable.wine_glass);
        clusterManager.addItem(displayCrumb);
    }

    /*
     * Load up the crumb viewing intent.
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        // This is overridden by the call to the ClusterMarker, but we still need to have this here
        // or the class complains
        return true;
    }



}

