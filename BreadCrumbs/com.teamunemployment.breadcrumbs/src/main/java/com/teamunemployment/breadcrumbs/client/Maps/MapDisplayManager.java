package com.teamunemployment.breadcrumbs.client.Maps;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.LatLngBounds;
import com.teamunemployment.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.teamunemployment.breadcrumbs.Models;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncFetchThumbnail;
import com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.Cards.CrumbCardDataObject;
import com.teamunemployment.breadcrumbs.client.SelectedEventViewerBase;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.teamunemployment.breadcrumbs.client.StoryBoard.StoryBoardActivity;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by aDirtyCanvas on 6/2/2015.
 */
public class MapDisplayManager implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {
    private GoogleMap mapInstance = null;
    private GlobalContainer globalContainer = GlobalContainer.GetContainerInstance();
    private Activity context;
    public ClusterManager<DisplayCrumb> clusterManager;
    private RecyclerView mRecyclerView;
    private String trailId;
    private ArrayList<CrumbCardDataObject> mDataObjects;
    private final static String TAG = "MapDisplayManager";

    public MapDisplayManager( GoogleMap map, Activity context, String trailId) {
        this.mapInstance = map;
        this.context = context;
        this.trailId = trailId;
        mDataObjects = new ArrayList<>();
        setUpClusterManager();

    }

    public ArrayList<CrumbCardDataObject> GetDataObjects() {
        return mDataObjects;
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

    private String tryMatchSuburb(ArrayList<DisplayCrumb> crumbList) {
        String location = "";
        for (DisplayCrumb crumb : crumbList) {
            if (location.equals("")) {
                location = crumb.getSuburb();
            } else {
                if (location.equals(crumb.getSuburb())) {
                    //Continue
                } else {
                    location = "";
                    break;
                }
            }
        }
        return location;
    }

    private String tryMatchCity(ArrayList<DisplayCrumb> crumbList) {
        String location = "";
        for (DisplayCrumb crumb : crumbList) {
            if (location.equals("")) {
                location = crumb.getCity();
            } else {
                if (location.equals(crumb.getCity())) {
                    //Continue
                } else {
                    location = "";
                    break;
                }
            }
        }
        return location;
    }
    /*
        Algorithm used to find the best fit for the location tag
     */
    private String getBestFitLocationStringForCluster(ArrayList<DisplayCrumb> crumbList) {
        String result = tryMatchSuburb(crumbList);
        if (result.equals("")) {
            result = tryMatchCity(crumbList);
            if (result.equals("")) {
                result = "New Zealand"; // For now.
            }
        }
        return result;
    }

    private LatLngBounds getBounds(Cluster<DisplayCrumb> displayCrumbCluster) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (DisplayCrumb crumb :  displayCrumbCluster.getItems()) {
            builder.include(crumb.getPosition());
        }
        LatLngBounds bounds = builder.build();
        return bounds;
    }

    public void setUpClusterManager() {
        clusterManager = new ClusterManager<DisplayCrumb>(context, mapInstance);
        CustomCrumbCluster customCrumbCluster = new CustomCrumbCluster(context, mapInstance, clusterManager);
        clusterManager.setRenderer(customCrumbCluster); // Used to draw our custom icons.
        mapInstance.setOnCameraChangeListener(clusterManager);
        mapInstance.setOnMarkerClickListener(clusterManager);
        clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<DisplayCrumb>() {

            @Override
            public boolean onClusterClick(Cluster<DisplayCrumb> displayCrumbCluster) {
                LatLngBounds markerBounds = getBounds(displayCrumbCluster);
                int padding = 400; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(markerBounds, padding);
                mapInstance.animateCamera(cu);
                return true;
            }
        });

        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<DisplayCrumb>() {
            @Override
            public boolean onClusterItemClick(final DisplayCrumb clusterItem) {
                // Get the id, and add it to the array to be passed to the
                if (clusterItem.getId().equals("-1")) {
                    return false;
                }
                ArrayList<String> idArray = new ArrayList<>();
                idArray.add(clusterItem.getId());

                Intent viewCrumbsIntent = new Intent(context, StoryBoardActivity.class);
                ArrayList<CrumbCardDataObject> crumbs = new ArrayList<>();

                CrumbCardDataObject tempCard = new CrumbCardDataObject(clusterItem.getExtension(), clusterItem.getId(), clusterItem.getPlaceId(), clusterItem.getPosition().latitude, clusterItem.getPosition().longitude, clusterItem.GetIsLocal(), clusterItem.getSuburb());
                crumbs.add(tempCard);
                viewCrumbsIntent.putExtra("StartingObject", new CrumbCardDataObject(clusterItem.getExtension(), clusterItem.getId(), clusterItem.getPlaceId(),clusterItem.getPosition().latitude, clusterItem.getPosition().longitude, clusterItem.GetIsLocal(), clusterItem.getSuburb()));
                viewCrumbsIntent.putParcelableArrayListExtra("CrumbArray", mDataObjects);
                boolean isOwnTrail = clusterItem.GetIsLocal() == 0;
                viewCrumbsIntent.putExtra("UserOwnsTrail", isOwnTrail);
                viewCrumbsIntent.putExtra("TrailId", trailId);

                FloatingActionButton playFab = (FloatingActionButton) context.findViewById(R.id.play_button);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context, playFab, playFab.getTransitionName());
                    context.startActivityForResult(viewCrumbsIntent , 1, options.toBundle());
                } else {
                    context.startActivityForResult(viewCrumbsIntent, 1);
                }
                final String placeId = clusterItem.getPlaceId();
                final String suburb = clusterItem.getSuburb();
                BreadCrumbsFusedLocationProvider locationProvider = new BreadCrumbsFusedLocationProvider(context);

                locationProvider.GetPlaceNameFromId(placeId, new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        try {
                            Place place = places.get(0);
                            if (place != null) {
                                // TextView mapOverLayTitle = (TextView) context.findViewById(R.id.map_overlay_title);
                                // mapOverLayTitle.setText(place.getName() + ", " + suburb );
                            }
                        } catch (IllegalStateException ex) {
                            Log.d("MAP", "Failed to get place. It probably doesnt exist");
                        }

                    }
                });

                // Consumes it?
                return false;
            }
        });

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
    public void DrawCrumbFromJson(JSONObject crumb, boolean local) throws JSONException {
        //We need to test that we have a map instance
        mapInstance = PassTheMapPlease(); // Shit code

        String url = "";
        // Get our variables
        // TODO Move this so that only variables required at this point are here. Others we can fetch when looking at the object.
        final Double Latitude = crumb.getDouble("Latitude"); // required
        final Double Longitude = crumb.getDouble("Longitude"); // Required
        final String id = crumb.getString("Id"); // required - The actual database node id.
        final String mediaType = crumb.getString("Extension"); // Required (?)
        final String placeId = crumb.getString("PlaceId"); // Not required
        final String suburb = crumb.getString("Suburb"); // Not required
        final String city = crumb.getString("City"); // Not required
        final String country = crumb.getString("Country"); // Not required
        final String timeStamp = crumb.getString("TimeStamp"); // Not required
        final String description = crumb.getString("Chat"); //Not required

        String placeName = suburb;
        if (suburb == null || suburb.isEmpty() || suburb.equals("null")) {
            placeName = city;
            if (placeName == null || placeName.isEmpty() || placeName.equals("null")) {
                placeName = country;
            }
        }

        mDataObjects.add(new CrumbCardDataObject(mediaType, id, placeId, Latitude, Longitude, 1, placeName));

        if (!local) {
            AsyncFetchThumbnail asyncDataRetrieval = new AsyncFetchThumbnail(id, new AsyncFetchThumbnail.RequestListener() {
                @Override
                public void onFinished(Bitmap result) {
                    //mapInstance.setMyLocationEnabled(false);
                    DisplayCrumb displayCrumb = new DisplayCrumb(Latitude, Longitude, mediaType, id, R.drawable.wine_glass, placeId,suburb, city, country, timeStamp, description, result, 1);
                    clusterManager.addItem(displayCrumb);
                    clusterManager.cluster();
                }
            });
            asyncDataRetrieval.execute();
        }
        else {
            // Local trail should always have an eventId.
            final String eventId = crumb.getString("eventId");
            Bitmap bitmap = fetchBitmapFromLocalFile(eventId, mediaType);

            DisplayCrumb displayCrumb = new DisplayCrumb(Latitude, Longitude, mediaType, id, R.drawable.wine_glass, placeId,suburb, city, country, timeStamp, description, bitmap, 0);
            clusterManager.addItem(displayCrumb);
        }
    }

    private String fetchStringPropertyFromJSON(String property, JSONObject crumb) throws JSONException {
        if (crumb.has(property)) {
            return crumb.getString(property);
        }
        return null;
    }

    private Double fetchDoublePropertyFromJSON(String property, JSONObject crumb) throws JSONException {
        if (crumb.has(property)) {
            return crumb.getDouble(property);
        }
        return null;
    }

    public void DrawLocalCrumbFromJson(JSONObject crumb) throws JSONException {
        mapInstance = PassTheMapPlease(); // Shit code
        Log.d(TAG, "Starting to draw crumb. Time : " + System.currentTimeMillis());
        String url = "";
        // Get our variables
        final String id = crumb.getString(Models.Crumb.ID);
        final Double Latitude = crumb.getDouble(Models.Crumb.LATITUDE);
        final Double Longitude = crumb.getDouble(Models.Crumb.LONGITUDE);
        final String mediaType = crumb.getString(Models.Crumb.EXTENSION);
        final String eventId = fetchStringPropertyFromJSON(Models.Crumb.EVENT_ID, crumb);
        final String placeId = fetchStringPropertyFromJSON(Models.Crumb.PLACEID, crumb);
        final String suburb = fetchStringPropertyFromJSON(Models.Crumb.SUBURB, crumb);
        final String city = fetchStringPropertyFromJSON(Models.Crumb.CITY, crumb);
        final String country = fetchStringPropertyFromJSON(Models.Crumb.COUNTRY, crumb);
        final String timeStamp = fetchStringPropertyFromJSON(Models.Crumb.TIMESTAMP, crumb);
        final String description = fetchStringPropertyFromJSON(Models.Crumb.DESCRIPTION, crumb);

        String placeName = suburb;
        if (suburb == null || suburb.isEmpty() || suburb.equals("null")) {
            placeName = city;
            if (placeName == null || placeName.isEmpty() || placeName.equals("null")) {
                placeName = country;
            }
        }
        mDataObjects.add(new CrumbCardDataObject(mediaType, eventId, placeId, Latitude, Longitude, 0, placeName));
        Bitmap bitmap = fetchBitmapFromLocalFile(eventId, mediaType); // Needs to be async
        DisplayCrumb displayCrumb = new DisplayCrumb(Latitude, Longitude, mediaType, id, R.drawable.wine_glass, placeId,suburb, city, country, timeStamp, description, bitmap, 0);
        clusterManager.addItem(displayCrumb);

        //Cluster manager doesnt normally update unless you change the map. This method forces it to update.
        clusterManager.cluster();
        Log.d(TAG, "Finished drawing crumb. Time : " + System.currentTimeMillis());

    }

    /*
        Grab a bitmap to display on the map as a thumbnail. Returns null if shit goes wrong
     */
    @Nullable
    private Bitmap fetchBitmapFromLocalFile(String eventId, String mediaType) {
        Bitmap bitmap = null;
        // If video, idk what we are going to do.
        if (mediaType.equals(".mp4")) {
            // show a default thumbnail
            String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/"+eventId + ".mp4";

            long id = Utils.FetchContentIdFromFilePath(fileName, context.getContentResolver());
            ContentResolver crThumb = context.getContentResolver();
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize = 1;
            bitmap = MediaStore.Video.Thumbnails.getThumbnail(crThumb, id, MediaStore.Video.Thumbnails.MICRO_KIND, options);
        } else {
            // Grab the
            String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/"+eventId + ".jpg";

            bitmap = Utils.FetchScaledBitmapFromFile(fileName, 60, 60);

        }

        return bitmap;

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


    @Override
    public void onMapClick(LatLng latLng) {
        SlidingUpPanelLayout slidingUpPanelLayout = (SlidingUpPanelLayout) context.findViewById(R.id.sliding_layout);

        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }
}