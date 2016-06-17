package com.teamunemployment.breadcrumbs.Map;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import com.teamunemployment.breadcrumbs.BasePresenter;
import com.teamunemployment.breadcrumbs.BaseView;
import com.teamunemployment.breadcrumbs.client.Maps.MapDisplayManager;
import com.teamunemployment.breadcrumbs.data.BreadcrumbsPolyline;
import com.teamunemployment.breadcrumbs.data.TripDetails;
import com.teamunemployment.breadcrumbs.data.TripPath;

/**
 * @author Josiah Kendall
 * Specifies the contract between the view and the presenter.
 */
public interface MapContract  {

    interface View extends BaseView<Presenter> {

        void DrawPath(TripPath tripPath);
        void DrawStandardPolyline(BreadcrumbsPolyline polyline);
        void DrawDashedPolyline(BreadcrumbsPolyline polyline);
        void SetTripDetails(TripDetails tripDetails);
        void ZoomOnLocation(Location location);
        void setBottomSheetFabColor(int color);
        void setBottomSheetFabDrawable(int drawable);

    }

    interface Presenter extends BasePresenter {

        void result(int requestCode, int resultCode, Intent data);
        void loadTripPath(String tripId);
        void loadTripThumbnails(String tripId);
        void loadTripDetails(String tripId);
        void stop();
        void setPlayButton(FloatingActionButton playButton);
        void setBackButton(FloatingActionButton backButton);
        void setEditButton(FloatingActionButton editFab);
        void setLocateMeButton(FloatingActionButton findMeFab);
        void setBottomSheet(CoordinatorLayout coordinatorLayout, android.view.View bottomSheet, Context context);

        // The map display handles the display of the map - the drawing of the map and the crumb
        // display etc.I am really not sure about this I am just doing it for speed. I think it
        // needs to be rewritten This is a bit of a complex beast.
        void setMapDisplayManager(MapDisplayManager mapDisplayManager);

        // We need to also need to know what the model is, so that we can change its properties to update the view.
        void setBoundModel();
    }
}
