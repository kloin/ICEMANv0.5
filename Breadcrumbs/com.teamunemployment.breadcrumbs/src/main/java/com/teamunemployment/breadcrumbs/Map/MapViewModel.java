package com.teamunemployment.breadcrumbs.Map;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

/**
 *
 */
public class MapViewModel extends BaseObservable {

    private Context context;
    private String trailId;
    private boolean isLocal;
    private String tripName;

    public MapViewModel(Context context, String trailId, boolean isLocal) {
        this.context = context;
        this.trailId = trailId;
        this.isLocal = isLocal;
    }







}
