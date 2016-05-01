package com.teamunemployment.breadcrumbs.client.StoryBoard;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by jek40 on 26/04/2016.
 */
public class StoryBoardItemData {
    public String CrumbId;
    public String PlaceId;
    public String Extension;
    public LatLng location;

    public StoryBoardItemData(String crumbId, String placeId, String extension, Double latitude, Double longitude) {
        CrumbId = crumbId;
        PlaceId = placeId;
        Extension = extension;
        location = new LatLng(latitude, longitude);
    }
}
