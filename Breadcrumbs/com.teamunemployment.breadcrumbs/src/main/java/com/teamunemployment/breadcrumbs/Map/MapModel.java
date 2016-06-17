package com.teamunemployment.breadcrumbs.Map;

import android.databinding.BaseObservable;
import android.databinding.BindingAdapter;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.teamunemployment.breadcrumbs.Location.SimpleGps;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;

/**
 * The model for our map view.
 */
public class MapModel extends BaseObservable {

    private int visibility = View.VISIBLE;
    // Bindings for View elment changes

    // Visibility on fabs, so when they are set to invisible they animate.


    // variables - like trail name, views, etc.
    //

    // Set Visibility. This will animate, then set the visibility parameter to the visibility setting
    public void setVisibility(int newVisibility) {

    }


}
