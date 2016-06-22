package com.teamunemployment.breadcrumbs.SaveCrumb;

/**
 * Created by jek40 on 22/06/2016.
 */
public class CrumbToSaveDetails {
    public final boolean IS_SELFIE;
    public final String LOCAL_ID;
    public final boolean IS_PHOTO;

    public CrumbToSaveDetails(boolean isSelfie, String localId, boolean isPhoto) {
        IS_SELFIE = isSelfie;
        LOCAL_ID = localId;
        IS_PHOTO = isPhoto;
    }
}
