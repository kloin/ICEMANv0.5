package com.teamunemployment.breadcrumbs.Map;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.databinding.BaseObservable;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.exoplayer.C;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.Maps.MapDisplayManager;
import com.teamunemployment.breadcrumbs.data.BreadcrumbsPolyline;
import com.teamunemployment.breadcrumbs.data.TripDetails;
import com.teamunemployment.breadcrumbs.data.TripPath;
import com.teamunemployment.breadcrumbs.data.source.TripDataSource;
import com.teamunemployment.breadcrumbs.data.source.TripRepository;

import java.util.ArrayList;

/**
 * @author Josiah Kendall
 *
 */
public class MapPresenter extends BaseObservable implements MapContract.Presenter {

    private TripRepository repository;
    private MapContract.View mapView;
    private FloatingActionButton playFab;
    private int SCROLLABLE_HEIGHT;
    private int TRAIL_COVER_PHOTO_HEIGHT;
    public MapPresenter(MapContract.View mapView, TripRepository repository) {
        this.repository = repository;
        this.mapView = mapView;
    }

    @Override
    public void start() {
        // Load our trip
    }

    @Override
    public void result(int requestCode, int resultCode, Intent data) {
        // DO activity lifecycle shit here.
    }

    @Override
    public void loadTripPath(String tripId) {
        repository.getTripPath(new TripDataSource.LoadTripPathCallback() {
            @Override
            public void onTripPathLoaded(TripPath tripPath) {
                ArrayList<BreadcrumbsPolyline> polylines = tripPath.getTripPolyline();
                for(BreadcrumbsPolyline polyline : polylines) {
                    mapView.DrawStandardPolyline(polyline);
                }
            }
        }, tripId);
    }

    @Override
    public void loadTripThumbnails(String tripId) {

    }

    @Override
    public void loadTripDetails(String tripId) {
        repository.getTripDetails(new TripDataSource.LoadTripDetailsCallback() {
            @Override
            public void onTripDetailsLoaded(TripDetails tripDetails) {
                mapView.SetTripDetails(tripDetails);
            }
        }, tripId);
    }

    @Override
    public void stop() {

    }

    @Override
    public void setPlayButton(FloatingActionButton playButton) {
        playFab = playButton;
    }

    @Override
    public void setBackButton(FloatingActionButton backButton) {

    }

    @Override
    public void setEditButton(FloatingActionButton editFab) {

    }

    @Override
    public void setLocateMeButton(FloatingActionButton findMeFab) {
    }


    // is still required
    @Override
    public void setBottomSheet(CoordinatorLayout coordinatorLayout, View bottomSheet, final Context context) {
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // React to state change
                onBottomSheetChanged(bottomSheet, newState);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                onBottomSheetSlide(slideOffset, bottomSheet);
                // React to dragging events
            }
        });
    }

    /**
     * Define what we do when the bottom sheet is slid up.
     * @param slideOffset How far we have slid.
     */
    private void onBottomSheetSlide(float slideOffset, View bottomSheet) {
        CardView bottomSheetToolbar = (CardView) bottomSheet.findViewById(R.id.bottom_sheet_header);
        ImageView imageCover = (ImageView) bottomSheet.findViewById(R.id.trail_cover_photo);
        // Simple formula to calculate our toolbar visibility. The purpose of this is to make
        // the image start fading into a blue toolbar when it is below half way scrolled.
        float newAlpha = 1 - slideOffset*2; // zero alpha reached when we are at halfway.

        // Safety check.
        if (newAlpha < 0) {
            newAlpha = 0;
        }

        // If else loop to display the bottom sheet card toolbar. This is done because the imageview
        // fades, and when it reaches one we display our card with a shadow. Anything before this we
        // just want to fade the image cover.
        if (newAlpha >= 1) {
            bottomSheetToolbar.setAlpha(1);
        } else {
            bottomSheetToolbar.setAlpha(0);
        }
        imageCover.setAlpha(newAlpha);
        // If our pay fab is visible, we need to hide it.
        if (playFab.getVisibility() == View.VISIBLE) {
            SimpleAnimations.ShrinkFab(playFab, 75);
        }
    }

    @Override
    public void setMapDisplayManager(MapDisplayManager mapDisplayManager) {

    }

    @Override
    public void setBoundModel() {

    }

    // Handler for when the view is expanded.
    private void onBottomSheetChanged(View bottomSheet, int state) {
        switch (state) {
            case BottomSheetBehavior.STATE_COLLAPSED:
                setCollapsedToolbarState(bottomSheet);
                break;
            case BottomSheetBehavior.STATE_DRAGGING:
                // setDragging state - not required atm
                break;
            case BottomSheetBehavior.STATE_EXPANDED:
                setExpandedBottomSheetState(bottomSheet);
                break;
            case BottomSheetBehavior.STATE_HIDDEN:
                // set Hidden behaviour - not enabled
                break;
            case BottomSheetBehavior.STATE_SETTLING:
                // set settliong behaviour
                break;
        }
    }

    /**
     * Set the state of our toolbar when it is collapsed.
     */
    public void setCollapsedToolbarState(View bottomSheet) {
        // Expand play button

        SimpleAnimations.ExpandFab(playFab, 75);

        FloatingActionButton bottomSheetFab = (FloatingActionButton) bottomSheet.findViewById(R.id.edit_toggle_fab);
        bottomSheetFab.setVisibility(View.INVISIBLE);
    }

    /**
     * Defin what happens when the bottom sheet expands.
     * @param bottomSheet our bottom sheet view.
     */
    private void setExpandedBottomSheetState(View bottomSheet) {

        setScrollingBehaviour(bottomSheet);
        // Set it up here so it works at runtime.

        FloatingActionButton  bottomSheetFab = (FloatingActionButton) bottomSheet.findViewById(R.id.edit_toggle_fab);
        SetUpBottomSheetFab(bottomSheetFab);


        // shrink our fab.
        if (bottomSheetFab.getVisibility() != View.VISIBLE) {
            SimpleAnimations.ExpandFab(bottomSheetFab, 75);
        }
    }

    /**
     * Set up the edit toggle button. This is not just the edit, but the like button too (depending
     * on whether it is our trail or not) so I think that really needs a rename.
     */
    public void SetUpBottomSheetFab(final FloatingActionButton bottomSheetFab) {

        if (bottomSheetFab.getBackgroundTintList() == ColorStateList.valueOf(Color.parseColor("#FF4081"))) {
            mapView.setBottomSheetFabDrawable(R.drawable.ic_favorite_border_black_24dp);
            mapView.setBottomSheetFabColor(Color.WHITE);
        } else {
            mapView.setBottomSheetFabDrawable(R.drawable.ic_favorite_border_white_24dp);
            mapView.setBottomSheetFabColor(Color.parseColor("#FF4081"));
        }
        bottomSheetFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoBottomSheetClick(bottomSheetFab);
            }
        });
    }

    public void DoBottomSheetClick(FloatingActionButton bottomSheetFab) {

        // Uodate like button
        if(bottomSheetFab.getBackgroundTintList() == ColorStateList.valueOf(Color.WHITE)) {
            mapView.setBottomSheetFabColor(Color.parseColor("#FF4081"));
            mapView.setBottomSheetFabDrawable(R.drawable.ic_favorite_border_white_24dp);

        } else {
            mapView.setBottomSheetFabColor(Color.WHITE);
            mapView.setBottomSheetFabDrawable(R.drawable.ic_favorite_border_black_24dp);
        }

    }

    /**
     * Setup the scrolling behavour for our bottom sheet, namely hiding the action button and
     * setting the toolbar when  scrolled up
     * @param bottomSheet
     */
    // databind
    private void setScrollingBehaviour(View bottomSheet) {

//        NestedScrollView bottomSheetScrollView = (NestedScrollView) bottomSheet.findViewById(R.id.bottom_sheet_scroller);
//
//
//        bottomSheetScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                if (SCROLLABLE_HEIGHT == 0) {
//                    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
//                    int toolbarHeightInPx = Math.round(60 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
//                    SCROLLABLE_HEIGHT = TRAIL_COVER_PHOTO_HEIGHT - toolbarHeightInPx;
//                }
//
//                float alpha = 0;
//                // percentage of the bitmap that we have scrolled to invisible
//                float scrollPostion = (float) scrollY / (float) SCROLLABLE_HEIGHT;
//                float adjustedScroll = scrollPostion - (float)0.5;
//                if (adjustedScroll > 0) {
//                    alpha = adjustedScroll*2;
//                }
//                if (alpha >= 0.95) {
//                    bottomSheetToolbar.setAlpha(1);
//                } else {
//                    bottomSheetToolbar.setAlpha(0);
//                }
//                if (alpha!= 0) {
//                    imageCover.setAlpha(alpha);
//                }
//                checkEditFabState(scrollY, oldScrollY, scrollPostion);
//            }
//        });
    }
}
