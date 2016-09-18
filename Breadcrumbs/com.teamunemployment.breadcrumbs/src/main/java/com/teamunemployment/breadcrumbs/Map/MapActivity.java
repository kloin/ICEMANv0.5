package com.teamunemployment.breadcrumbs.Map;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.teamunemployment.breadcrumbs.Location.SimpleGps;
import com.teamunemployment.breadcrumbs.client.Cards.CrumbCardDataObject;
import com.teamunemployment.breadcrumbs.client.Maps.MapDisplayManager;
import com.teamunemployment.breadcrumbs.client.StoryBoard.StoryBoardActivity;
import com.teamunemployment.breadcrumbs.data.BreadcrumbsPolyline;
import com.teamunemployment.breadcrumbs.data.TripDetails;
import com.teamunemployment.breadcrumbs.data.TripPath;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.data.source.LocalRepository.TripLocalDataSource;
import com.teamunemployment.breadcrumbs.data.source.RemoteRepository.RemoteTripDataSource;
import com.teamunemployment.breadcrumbs.data.source.TripRepository;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Josiah Kendall
 *
 * Our view for the Map.
 */
public class MapActivity extends AppCompatActivity implements MapContract.View , OnMapReadyCallback{
    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    public void DrawPath(TripPath tripPath) {

    }

    @Override
    public void DrawStandardPolyline(BreadcrumbsPolyline polyline) {

    }

    @Override
    public void DrawDashedPolyline(BreadcrumbsPolyline polyline) {

    }

    @Override
    public void SetTripDetails(TripDetails tripDetails) {

    }

    @Override
    public void ZoomOnLocation(Location location) {

    }

    @Override
    public void setBottomSheetFabColor(int color) {

    }

    @Override
    public void setBottomSheetFabDrawable(int drawable) {

    }

    @Override
    public void setPresenter(MapContract.Presenter presenter) {

    }

//    private static final String TAG = "MapView";
//    private MapContract.Presenter presenter;
//    private GoogleMap map;
//    private HomeMapBinding binding;
//    private AppCompatActivity context;
//    private MapDisplayManager mapDisplayManager;
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        presenter.start();
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        presenter.result(requestCode, resultCode, data);
//    }
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        context = this;
//        binding = DataBindingUtil.setContentView(this, R.layout.home_map);
//        // Initialise our map.
//        ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
//        String trailId = this.getIntent().getStringExtra("TrailId");
//
//        // Build our repositories
//        DatabaseController databaseController = new DatabaseController(this);
//        TripLocalDataSource localDataSource = new TripLocalDataSource(databaseController);
//        RemoteTripDataSource remoteTripDataSource = new RemoteTripDataSource(this);
//        TripRepository repository = new TripRepository(localDataSource, remoteTripDataSource);
//        // Create our presenter
//        if (presenter == null) {
//            presenter = new MapPresenter(this, repository);
//        }
//
//        // Dont really want this here however, it works for now.
//        mapDisplayManager = new MapDisplayManager(map, context,trailId);
//        presenter.loadTripPath(trailId);
//        presenter.loadTripDetails(trailId);
//        // This fetches all the data needed to display the thumbnails.
//        presenter.loadTripThumbnails(trailId);
//
//        FloatingActionButton playButton = (FloatingActionButton) findViewById(R.id.play_button);
//        presenter.setPlayButton(playButton);
//        setLocateMeClickHandler();
//        setUpBottomSheet();
//        SetPlayButtonClickHandler(trailId);
//        // Set the bottom sheet up.
//    }
//
//
//    private void setLocateMeClickHandler() {
//        FloatingActionButton locateMe = (FloatingActionButton) findViewById(R.id.where_am_i);
//        locateMe.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FetchMyLocation();
//            }
//        });
//    }
//
//    public void FetchMyLocation() {
//        SimpleGps simpleGps = new SimpleGps(context);
//        Location location = simpleGps.GetInstantLocation();
//        if (location == null) {
//            return;
//        }
//
//        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                new LatLng(location.getLatitude(), location.getLongitude()), 13), 500, null);
//
//        map.addCircle(new CircleOptions()
//                .center(new LatLng(location.getLatitude(), location.getLongitude()))
//                .radius(30)
//                .strokeColor(Color.BLUE)
//                .fillColor(Color.BLUE));
//    }
//
//
//    private void setUpBottomSheet() {
//        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.map_root_view);
//        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
//
//        // Dont like this at all - we need a better solution.
//        presenter.setBottomSheet(coordinatorLayout, bottomSheet, context);
//    }
//
//    /**
//     * This here draws the path onto google maps. This should probably be written off of the UI Thread.
//     *
//     * @param tripPath The polyline list to draw.
//     */
//    @Override
//    public void DrawPath(TripPath tripPath) {
////        ArrayList<BreadcrumbsPolyline> polylines = tripPath.getTripPolyline();
////        for (BreadcrumbsPolyline polyline : polylines) {
////            drawPolyline(polyline);
////        }
//    }
//
//    @Override
//    public void DrawStandardPolyline(BreadcrumbsPolyline polyline) {
//        drawPolyline(polyline);
//    }
//
//    @Override
//    public void DrawDashedPolyline(BreadcrumbsPolyline polyline) {
//
//    }
//
//
//    private void drawPolyline(BreadcrumbsPolyline polyline) {
//        if (polyline.isEncoded) {
//            drawEncodedPolyline(polyline.points, "#03A9F4");
//        } else {
//            drawDashedPolyline(polyline.points.get(0), polyline.points.get(polyline.points.size() - 1), getResources().getColor(R.color.bb_darkBackgroundColor));
//        }
//    }
//
//    // Needs a re write this is shithouse
//    private void drawDashedPolyline(LatLng latLngOrig, LatLng latLngDest, int color) {
//
//        double difLat = latLngDest.latitude - latLngOrig.latitude;
//        double difLng = latLngDest.longitude - latLngOrig.longitude;
//
//        double zoom = map.getCameraPosition().zoom;
//
//        double divLat = difLat / (zoom * 2);
//        double divLng = difLng / (zoom * 2);
//
//        LatLng tmpLatOri = latLngOrig;
//
//        for (int i = 0; i < (zoom * 2); i++) {
//            LatLng loopLatLng = tmpLatOri;
//
//            if (i > 0) {
//                loopLatLng = new LatLng(tmpLatOri.latitude + (divLat * 0.075f), tmpLatOri.longitude + (divLng * 0.075f));
//            }
//
//            Polyline polyline = map.addPolyline(new PolylineOptions()
//                    .add(loopLatLng)
//                    .add(new LatLng(tmpLatOri.latitude + divLat, tmpLatOri.longitude + divLng))
//                    .color(color)
//                    .width(5f));
//
//            tmpLatOri = new LatLng(tmpLatOri.latitude + divLat, tmpLatOri.longitude + divLng);
//        }
//    }
//
//    // This should run off of the UI Thread
//    private void drawEncodedPolyline(List<LatLng> listOfPoints, String color) {
//        final PolylineOptions options = new PolylineOptions().width(24).color(Color.parseColor(color)).geodesic(true);
//        for (int z = 0; z < listOfPoints.size(); z++) {
//            LatLng point = listOfPoints.get(z);
//            options.add(point);
//        }
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                map.addPolyline(options);
//            }
//        });
//    }
//
//    @Override
//    public void SetTripDetails(TripDetails tripDetails) {
//        // Here , we want to just set the viewModel so that the databinding can do the rest.
//        binding.setTripDetails(tripDetails);
//    }
//
//    @Override
//    public void ZoomOnLocation(Location location) {
//
//    }
//
//    @Override
//    public void setBottomSheetFabColor(int color) {
//
//    }
//
//    @Override
//    public void setBottomSheetFabDrawable(int drawable) {
//
//    }
//
//    @Override
//    public void setPresenter(MapContract.Presenter presenter) {
//        this.presenter = presenter;
//    }
//
//
//
//
//    private void SetPlayButtonClickHandler(final String trailId) {
//        FloatingActionButton playBtton = (FloatingActionButton) findViewById(R.id.play_button);
//        assert playBtton != null;
//        playBtton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                playFabClickHandler(trailId, v);
//            }
//        });
//    }
//
//    public void playFabClickHandler(String trailId, View playFab) {
//        Intent viewCrumbsIntent = new Intent(context, StoryBoardActivity.class);
//        ArrayList<CrumbCardDataObject> crumbObjects = mapDisplayManager.GetDataObjects();
//        if (crumbObjects.size() == 0) {
//            Log.d(TAG, "Cant open anything, we have no data.");
//            return;
//        }
//        viewCrumbsIntent.putExtra("StartingObject", crumbObjects.get(0));
//        viewCrumbsIntent.putExtra("Index", 0);
//        viewCrumbsIntent.putParcelableArrayListExtra("CrumbArray", crumbObjects);
//        viewCrumbsIntent.putExtra("TrailId", trailId);
//        boolean isOwnTrail = crumbObjects.get(0).GetIsLocal() == 0;
//        viewCrumbsIntent.putExtra("UserOwnsTrail", isOwnTrail);
//        viewCrumbsIntent.putExtra("Timer", 0);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context, playFab, playFab.getTransitionName());
//            context.startActivityForResult(viewCrumbsIntent , 1, options.toBundle());
//        } else {
//            context.startActivityForResult(viewCrumbsIntent, 1);
//        }
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        map = googleMap;
//    }
}

