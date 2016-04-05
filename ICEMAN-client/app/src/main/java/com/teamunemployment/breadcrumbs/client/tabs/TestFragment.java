package com.teamunemployment.breadcrumbs.client.tabs;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teamunemployment.breadcrumbs.R;

/**
 * Created by aDirtyCanvas on 6/2/2015.
 */
public class TestFragment  extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.test_fragment_layout, container, false);

    }
}
