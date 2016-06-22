package com.teamunemployment.breadcrumbs.SaveCrumb;

import android.graphics.Bitmap;

/**
 * @author Josiah Kendall.
 */
public interface SaveCrumbActivityContract {

    public interface ICrumbDisplay {
        void setBitmap(Bitmap bitmap);
        void setPlaceName(String text);
        void setDescription(String text);
        void showMessage(String message);
        void setEditTextDescriptionVisibility(int visibility);
        void setTextViewDescriptionVisibility(int visibility);
    }
}
