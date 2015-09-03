package com.breadcrumbs.client.CrumbViewer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;

import com.breadcrumbs.caching.GlobalContainer;
import com.breadcrumbs.client.Maps.DisplayCrumb;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Created by aDirtyCanvas on 6/2/2015.
 */
public class CrumbViewerAdapter extends FragmentPagerAdapter {
    private Object[] crumbCollection;
    public CrumbViewerAdapter(FragmentManager fm) {
        super(fm);
        GlobalContainer globalContainer = GlobalContainer.GetContainerInstance();
        crumbCollection = globalContainer.GetCluster().getItems().toArray();
    }

    @Override
    public Fragment getItem(int index) {
        CrumbViewerFragment myFragment = new CrumbViewerFragment();

        DisplayCrumb itemToDisplay = (DisplayCrumb) crumbCollection[index];
        Bundle args = new Bundle();

        // Add the shit we need to display the crumb
        args.putString("Id", itemToDisplay.getId());
        args.putDouble("Latitude", itemToDisplay.getPosition().latitude);
        args.putDouble("Longitude", itemToDisplay.getPosition().longitude);
        args.putString("Extension", itemToDisplay.getExtension());
        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return crumbCollection.length;
    }

}
