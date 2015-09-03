package com.breadcrumbs.client.tabs;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.breadcrumbs.ServiceProxy.MasterProxy;
import com.breadcrumbs.Trails.MyCurrentTrailManager;
import com.breadcrumbs.caching.GlobalContainer;
import com.breadcrumbs.client.R;

/**
 * Created by aDirtyCanvas on 6/2/2015.
 */
public class TestFragment  extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_activity, container, false);

    }
}
