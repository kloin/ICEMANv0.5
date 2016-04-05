package com.teamunemployment.breadcrumbs.RandomUsefulShit;

import android.media.ExifInterface;

/**
 * Created by jek40 on 10/03/2016.
 */
public class Utils {
    public static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }


}
