package com.teamunemployment.breadcrumbs.SaveCrumb;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.teamunemployment.breadcrumbs.BackgroundServices.SaveCrumbService;
import com.teamunemployment.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.teamunemployment.breadcrumbs.Location.PlaceManager;
import com.teamunemployment.breadcrumbs.Location.SimpleGps;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils;
import com.teamunemployment.breadcrumbs.Trails.TrailManagerWorker;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.database.DatabaseController;


/**
 * @author Josiah Kendall
 */
public class SaveCrumbModel {
    private SimpleGps simpleGps;
    private CrumbToSaveDetails details;
    private SaveCrumbPresenter presenter;

    private String description;
    private Location location;
    private String placeName;

    private float descriptionPositionX;
    private float descriptionPositionY;


    public void setDescriptionPosition(float x, float y) {
        descriptionPositionX = x;
        descriptionPositionY = y;
    }

    public interface MediaLoader {
        void loadMedia();
    }

    public SaveCrumbModel(SimpleGps simpleGps, CrumbToSaveDetails details, SaveCrumbPresenter presenter) {
        this.simpleGps = simpleGps;
        this.details = details;
        this.presenter = presenter;
    }

    public void load(final MediaLoader loaderContract) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (loaderContract != null) {
                    loaderContract.loadMedia();
                }
            }
        }).start();

        setLocation();
    }

    private void setLocation() {
        location = fetchLocation();
        if (validateLocation(location)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    placeName = fetchLocationName(location);
                    presenter.setLocation(placeName);
                }
            }).start();
        } else {
            simpleGps.FetchFineLocation(onLocationFound());
        }
    }

    public SimpleGps.Callback onLocationFound() {
        return new SimpleGps.Callback() {
            @Override
            public void doCallback(Location location) {
                placeName = fetchLocationName(location);
                presenter.setLocation(placeName);
            }
        };
    }

    // check if our location is less than 1 min old. If it isnt, we need to get
    public boolean validateLocation(Location location) {
        if (location == null) {
            return false;
        }
        return location.getTime() > System.currentTimeMillis() - 60000;
    }

    public Bitmap loadBitmap() {
        GlobalContainer globalContainer = GlobalContainer.GetContainerInstance();
        Bitmap bitmapResult = globalContainer.GetBitMap();
        // rotate bitmap to correctOrientation
        if (bitmapResult != null) {
            Bitmap bm = Utils.AdjustBitmapToCorrectOrientation(!details.IS_SELFIE, bitmapResult);
            if (bm != null){
                globalContainer.SetBitMap(bm.copy(Bitmap.Config.ARGB_8888, false));
                return bm;
            }
        }

        return null;
    }

    public Location fetchLocation() {
        Location location = simpleGps.GetInstantLocation();
        return location;
    }

    /**
     * Fetch a place name using a given location.
     * @param location The {@link Location} to use.
     * @return The suburb, city, or country name as a string.
     */
    public String fetchLocationName(Location location) {
        if (location == null) {
            presenter.showMessage("Failed to find location");
            return "";
        }

        if (location.getProvider().equals("BC_MOCK")) {
            return "MOCK";
        }

        // Fetch address using our location.
        Address address = simpleGps.FetchLocationAddress(location);

        // Get our place name
        return simpleGps.FetchPlaceNameForLocation(address);
    }

    public void setDescription(String description) {
        if (description.length() > 140) {
            presenter.showMessage("Descriptions are limited to 140 characters");
            // Re set description back to 140 characters.
            presenter.setDescriptionText(this.description);
        } else {
            this.description = description;
        }
    }

    public String getDescription() {
        return description;
    }

    /**
     * This is pretty shit because its not really testable.
     * @param context need the context to start the service.
     * @param preferencesAPI
     */
    public void SaveCrumb(Context context, PreferencesAPI preferencesAPI) {
        saveCrumb(context, preferencesAPI);
    }

    private void saveCrumb(Context context, PreferencesAPI preferencesAPI) {
        //Currently we will only be creating a crumb. In the future we will be able to create both.
        String trailId = Integer.toString(preferencesAPI.GetLocalTrailId());

        // grab event Id.
        final int eventId = preferencesAPI.GetEventId();

        // Save image to disk.
        if (details.IS_PHOTO) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    saveBitmap(eventId);
                }
            }).start();
        }

        // Need to launch intent service to save object.
        Intent saveCrumbService = new Intent(context, SaveCrumbService.class);
        saveCrumbService.putExtra("IsPhoto", details.IS_PHOTO);
        saveCrumbService.putExtra("EventId", eventId);
        saveCrumbService.putExtra("Description", description);
        saveCrumbService.putExtra("PlaceName", placeName);
        saveCrumbService.putExtra("PositionX", descriptionPositionX);
        saveCrumbService.putExtra("PositionY", descriptionPositionY);

        context.startService(saveCrumbService);
        preferencesAPI.SetEventId(eventId+1); // This is a flag - should be saved in the DB because user can clear shared preferences
    }

    private void saveBitmap(int eventId) {
        String fileName =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/"+eventId;
        com.teamunemployment.breadcrumbs.caching.Utils.SaveBitmap(fileName, GlobalContainer.GetContainerInstance().GetBitMap());
    }

    public void CleanUp() {
        GlobalContainer.GetContainerInstance().SetBitMap(null);
    }
}
