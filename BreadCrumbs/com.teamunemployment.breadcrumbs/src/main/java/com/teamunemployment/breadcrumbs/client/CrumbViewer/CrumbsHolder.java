package com.teamunemployment.breadcrumbs.client.CrumbViewer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.teamunemployment.breadcrumbs.Framework.NonSwipeableViewPager;
import com.teamunemployment.breadcrumbs.R;

/**
 * Created by aDirtyCanvas on 6/3/2015.
 */
public class CrumbsHolder extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.test_view_pager_holder);
        setUpViewPager();
    }

    private void setUpViewPager() {
        // Get our ViewPager
        ViewPager viewPager = (ViewPager) findViewById(R.id.crumbs_holder);
        // Get the adapter for controlling the tabs and set it on the ViewPager.
        CrumbViewerAdapter mAdapter = new CrumbViewerAdapter(getFragmentManager());
        viewPager.setAdapter(mAdapter);
        viewPager.setOffscreenPageLimit(0);
        viewPager.setCurrentItem(0);
        // We only want one two tabs running  so that we can minimise cpu/battery/memory etc...
        viewPager.setOffscreenPageLimit(1);
        viewPager.setPageMarginDrawable(R.color.background_material_dark);
        viewPager.setOnPageChangeListener(new NonSwipeableViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected( int position) {
                try {
                    // on changing the page
                    // make respected tab selected
                    // actionBar.setSelectedNavigationItem(position);
                } catch(Exception ex) {
                    System.out.println("And exception has been thrown by ViewPager: " + ex);
                }
            }

            @Override //Should not happen - have removed ability for pages to scroll with custom view pager.
            public void onPageScrollStateChanged(int arg0) {
                //Do some shit geee - Do we want to load the data in here?
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // or here?
            }
        });
    }

}
