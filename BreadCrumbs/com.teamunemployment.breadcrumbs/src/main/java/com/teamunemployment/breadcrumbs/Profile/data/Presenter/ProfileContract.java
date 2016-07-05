package com.teamunemployment.breadcrumbs.Profile.data.Presenter;

import android.graphics.Bitmap;

import com.teamunemployment.breadcrumbs.Trails.Trip;

import java.util.ArrayList;

/**
 * Created by jek40 on 2/07/2016.
 */
public interface ProfileContract {

    public interface ViewContract {
        void setUserName(String userName);
        void setUserAbout(String about);
        void setUserWeb(String website);
        void setUserTripsAdapter(ArrayList<Trip> trips);
        void setProfilePicture(String url);
        void setProfileBitmap(Bitmap bitmap);
        void setFabAsGreen();
        void setFabAsWhite();
        void setFabIconAsEdit();
        void setFabIconAsTick();
        void setProfileClickPromptAsVisible();
        void setProfileClickPromptAsGone();
        void setUserAboutAsReadOnly();
        void setUserWebsiteAsReadOnly();
        void setUserWebsiteAsEditable();
        void setUserAboutAsEditable();
        void setUserTripsCount();

    }

    // Not sure I need two contracts here?
    interface PresenterContract {
        void setUserName(String userName);
        void setUserAbout(String about);
        void setUserWeb(String website);
        void setUserTrips(ArrayList<Trip> trips);
        void setProfilePicture(String id);

    }



}
