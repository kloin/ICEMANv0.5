package com.teamunemployment.breadcrumbs.client.tabs;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.client.Cards.HomeCardAdapter;

/**
 * Created by jek40 on 4/03/2016.
 */
public class BaseCardViewList extends Fragment {

    public RecyclerView mRecyclerView;
    public RecyclerView.LayoutManager mLayoutManager;
    public HomeCardAdapter mAdapter;
    public SwipeRefreshLayout mSwipeRefreshLayout;
    public AsyncDataRetrieval clientRequestProxy;
    public GlobalContainer globalContainer;
    public Context context;
    public View rootView;
    public Activity activityContext;

    public void onAttach(Activity activity) {
        activityContext= activity;
        super.onAttach(activity);
    }
}
