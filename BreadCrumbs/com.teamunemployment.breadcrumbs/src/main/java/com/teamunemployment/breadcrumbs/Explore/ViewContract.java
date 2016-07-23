package com.teamunemployment.breadcrumbs.Explore;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by jek40 on 21/07/2016.
 */
public interface ViewContract {

    void ShowMessage(String message);
    void SetRecyclerViewAdapter(RecyclerView.Adapter adapter);
}
