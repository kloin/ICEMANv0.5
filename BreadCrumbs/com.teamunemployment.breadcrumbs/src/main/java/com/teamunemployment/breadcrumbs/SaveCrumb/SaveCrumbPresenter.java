package com.teamunemployment.breadcrumbs.SaveCrumb;

import android.graphics.Bitmap;

/**
 * @author Josiah Kendall
 */
public class SaveCrumbPresenter {

    private boolean editTextIsEditable = false;
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

    // Toggle the floating description.
    public void toggleEditText() {
        if (editTextIsEditable) {
            editTextIsEditable = false;
        } else {
            editTextIsEditable = true;
        }
        activityView.setEditTextEnabled(editTextIsEditable);
    }

}
