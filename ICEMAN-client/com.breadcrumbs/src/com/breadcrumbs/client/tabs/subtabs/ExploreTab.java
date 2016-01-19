package com.breadcrumbs.client.tabs.subtabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.breadcrumbs.R;

/**
 * Created by aDirtyCanvas on 4/30/2015.
 */
public class ExploreTab extends Fragment{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.explore_tab, container, false);

        return rootView;
    }
}
