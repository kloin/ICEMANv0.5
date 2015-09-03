package com.breadcrumbs.client.tabs.subtabs;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.breadcrumbs.client.R;

/**
 * I dont know if we really need a contacts tab. This adding contacts stuff would be better soomwhere else
 *
 * Created by aDirtyCanvas on 4/30/2015.
 */
public class LocalTab extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.local_tab, container, false);

        return rootView;
    }

    // Method to load contacts into the tab screen
    private void loadContacts() {

    }
}
