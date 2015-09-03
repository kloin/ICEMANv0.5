package com.breadcrumbs.client.tabs;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import com.breadcrumbs.client.tabs.subtabs.LocalTab;
import com.breadcrumbs.client.tabs.subtabs.PinnedTab;

/**
 * Created by aDirtyCanvas on 4/30/2015.
 *
 * This is the tab host for the subscription manager. It is my attempt at tab inside a tab.
 */
public class SubscriptionTabHolder extends FragmentPagerAdapter {

    public SubscriptionTabHolder(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {

        switch (index){
            //case 1:
                //return new LocalTab();

          //  case 2:
          //      return new ExploreTab();
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Pinned";
            case 1:
                return "Community";
          //  case 2:
           //     return "Explore";
        }
        return "tab brokened";
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 2;
    }

}
