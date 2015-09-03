package com.breadcrumbs.client;

import android.support.v4.app.FragmentPagerAdapter;

import com.breadcrumbs.client.tabs.HomePageFragment;
import com.breadcrumbs.client.tabs.SubscriptionManagerTab;
import com.breadcrumbs.client.tabs.subtabs.LocalTab;

public class TabsAdapter extends FragmentPagerAdapter {


    public TabsAdapter(android.support.v4.app.FragmentManager fm) {
        super(fm);
    }

    @Override
	public android.support.v4.app.Fragment getItem(int index) {

		switch (index) {
		case 0:
           // return new ProfilePageViewer();
		case 1:
            return new  SubscriptionManagerTab();
		case 2:
            return new LocalTab();
		}
        // Will cause an exception. SHould never happen
        return null;

	}

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Profile";
            case 1:
                return "Pinned";
              case 2:
                 return "Explore";
        }
        return "tab brokened";
    }

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return 3;
	}

}
