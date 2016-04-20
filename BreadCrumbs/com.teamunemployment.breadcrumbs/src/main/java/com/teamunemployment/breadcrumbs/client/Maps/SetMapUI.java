package com.teamunemployment.breadcrumbs.client.Maps;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;

/**
 * Created by jek40 on 13/04/2016.
 */
public class SetMapUI {

    GoogleMap mMap;
    public SetMapUI(GoogleMap map) {
        mMap = map;
    }

    public void SetUpDefaultMapDisplay() {
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setCompassEnabled(false);
        uiSettings.setMapToolbarEnabled(false);
    }
}
