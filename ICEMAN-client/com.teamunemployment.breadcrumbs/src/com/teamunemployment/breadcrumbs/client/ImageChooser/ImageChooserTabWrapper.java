package com.teamunemployment.breadcrumbs.client.ImageChooser;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.ViewPagerAdapter;
import com.teamunemployment.breadcrumbs.client.tabs.ExploreTabFragment;
import com.teamunemployment.breadcrumbs.client.tabs.HomeTabFragment;
import com.teamunemployment.breadcrumbs.client.tabs.TestFragment;

/**
 * Copyright Breadcrumbs 2016.
 *
 * Class that is made to enable the choosing of images to use as a profile pic/other means.
 * Images currently come from 3 sources, which each have 1 tab
 *    * facebook
 *    * breadcrumbs
 *    * local
 */
public class ImageChooserTabWrapper extends AppCompatActivity {
    private ViewPager viewPager;
    private Context context;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_selector_tab_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.image_selector_toolbar);
        toolbar.setTitleTextAppearance(this, R.style.HeaderFont);
        setSupportActionBar(toolbar);
        viewPager = (ViewPager) findViewById(R.id.image_selector_viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.image_selector_tabs);
        tabLayout.setupWithViewPager(viewPager);
        context = this;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GridImageSelector(), "Uploaded");
        adapter.addFragment(new GridImageSelector(), "Facebook");
        adapter.addFragment(new GridImageSelector(), "Local");
        viewPager.setAdapter(adapter);
    }
}
