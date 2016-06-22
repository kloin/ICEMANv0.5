package com.teamunemployment.breadcrumbs.SaveCrumb;

import android.graphics.Bitmap;

/**
 * Created by jek40 on 22/06/2016.
 */
public class SaveCrumbPresenter {

    private SaveCrumbActivityContract.ICrumbDisplay activityView;

    public SaveCrumbPresenter(SaveCrumbActivityContract.ICrumbDisplay view) {
        activityView = view;
    }

    //
    public void setBitmapDisplay(Bitmap bitmap) {
        activityView.setBitmap(bitmap);
    }

    public void setDescriptionText(String text) {
        activityView.setDescription(text);
    }

    // Set the location tag on a crumb
    public void setLocation(String locationName) {
        activityView.setPlaceName(locationName);
    }

    public void showMessage(String message) {
        activityView.showMessage(message);
    }

    public void setDescriptionTextViewVisibility(int visibility) {
        activityView.setTextViewDescriptionVisibility(visibility);
    }

    public void setDescriptionEditTextVisibility(int visibility) {
        activityView.setEditTextDescriptionVisibility(visibility);
    }
}
