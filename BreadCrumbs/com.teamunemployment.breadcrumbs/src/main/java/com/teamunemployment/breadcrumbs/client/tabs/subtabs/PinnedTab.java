package com.teamunemployment.breadcrumbs.client.tabs.subtabs;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teamunemployment.breadcrumbs.R;

/**
 * Created by aDirtyCanvas on 4/30/2015.
 */
public class PinnedTab extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.pinned_tab, container, false);

       /* rootView.findViewById(R.id.pink_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(rootView.getContext(), "Clicked pink Floating Action Button", Toast.LENGTH_SHORT).show();
            }
        });*/

        return rootView;
    }
}
