package com.breadcrumbs.client;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.breadcrumbs.R;

/**
 * Created by aDirtyCanvas on 9/13/2015.
 */
public class EditExistingTrail extends AppCompatActivity {

    private Context context;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_trail);
        context = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.edit_trail_toolbar);
        setSupportActionBar(toolbar);
        String trailId = getIntent().getStringExtra("TrailId");
        mRecyclerView = (RecyclerView) findViewById(R.id.edit_trail_recycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.edit_trail_collapsable_toolbar_holder);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CodeFont);

    }
}
