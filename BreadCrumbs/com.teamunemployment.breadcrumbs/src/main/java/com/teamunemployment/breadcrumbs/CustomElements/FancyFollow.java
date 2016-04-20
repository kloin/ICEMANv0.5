package com.teamunemployment.breadcrumbs.CustomElements;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.caching.TextCaching;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by jek40 on 9/04/2016.
 */
public class FancyFollow {
    private final String mFollowKey;

    private boolean mIsOwnProfile = false;
    private final Context mContext;
    private final FancyButton mFollowButton;
    private final String mCurrentUserId;
    private final String mTargetUserId;
    private final String TAG = "FollowButton";
    private final TextCaching mTextCaching;

    public FancyFollow(String currentUserId, String itemOwnerId, FancyButton fancyFollow, Context context) {
        mFollowButton = fancyFollow;
        if (currentUserId.equals(itemOwnerId)) {
            mIsOwnProfile = true;
        }

        mTargetUserId = itemOwnerId;
        mCurrentUserId = currentUserId;
        mFollowKey = mCurrentUserId + "FOLLOW" + mTargetUserId;
        mContext = context;
        mTextCaching = new TextCaching(context);
    }

    public void init() {
        if (mIsOwnProfile) {
            Log.d(TAG, "Is own profile - hiding follow button");
            mFollowButton.setVisibility(View.GONE);
        } else {
            /*
             Check if we are following. If we are, make unfollow an option. If we are not,. make follow the option.
             Need seperate click handlers for each.
             If we are going to save the follower, it would be faster to add it to the cache. Then ONLY
             if it is not in the cache we need to fetch it from the network
             */
            mFollowButton.setVisibility(View.VISIBLE);

            String alreadyFollowed = mTextCaching.FetchCachedText(mFollowKey);
            if (alreadyFollowed == null || alreadyFollowed.equals("N")) {
                // Set up listeners for the follow click
                setUpFollowButton(mCurrentUserId, mFollowButton);
            } else{
                // Set up for unfollowing
                setUpUnfollowButton(mCurrentUserId, mFollowButton);
            }
        }
    }

    private void setUpUnfollowButton(final String currentUserId, final FancyButton followButton) {
        followButton.setVisibility(View.VISIBLE);
        followButton.setText("Following");
        followButton.setTextColor(Color.WHITE);
        followButton.setBackgroundColor(mContext.getResources().getColor(R.color.ColorPrimary));
        unfollowUserOnClickHandler(followButton, currentUserId);
    }

    private void setUpFollowButton(final String currentUserId, final FancyButton followButton) {
        followButton.setVisibility(View.VISIBLE);
        followButton.setText("Follow");
        followButton.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        followButton.setTextColor(mContext.getResources().getColor(R.color.ColorPrimary));
        // Need to cache whether the user is following the other user or not
        followUserOnClickHandler(followButton, currentUserId);
    }

    private void followUserOnClickHandler(final FancyButton followButton, final String currentUserId) {
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send request to follow user
                Log.d(TAG, "Sending follow request to server");
                if (!currentUserId.equals("-1")) {
                    //followButton.setTextColor(getResources().getColor(R.color.accent));
                    followButton.setText("Following");
                    followButton.setTextColor(Color.WHITE);
                    followButton.setBackgroundColor(mContext.getResources().getColor(R.color.ColorPrimary));
                    Log.d(TAG, "User with Id: " + currentUserId + " is following user with ID: " + mTargetUserId);
                    String followUserUrl = LoadBalancer.RequestServerAddress() + "/rest/User/PinUserForUser/" + currentUserId + "/" + mTargetUserId;
                    AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(followUserUrl, new AsyncDataRetrieval.RequestListener() {
                        @Override
                        public void onFinished(String result) {
                            // need to check its legit here though
                            Log.d(TAG, "Follow request responded: " + result);
                            mTextCaching.CacheText(mFollowKey, "Y");
                        }
                    }, mContext);
                    asyncDataRetrieval.execute();
                    unfollowUserOnClickHandler(followButton, currentUserId);
                }
            }
        });
    }

    private void unfollowUserOnClickHandler(final FancyButton followButton, final String currentUserId) {
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentUserId.equals("-1")) {
                    followButton.setText("Follow");
                    followButton.setTextColor(mContext.getResources().getColor(R.color.ColorPrimary));
                    followButton.setBackgroundColor(Color.WHITE);
                    String followUserUrl = LoadBalancer.RequestServerAddress() + "/rest/User/UnPinUserForUser/" + currentUserId + "/" + mTargetUserId;
                    AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(followUserUrl, new AsyncDataRetrieval.RequestListener() {
                        @Override
                        public void onFinished(String result) {
                            // need to check its legit here though
                            mTextCaching.CacheText(mFollowKey, "N");
                        }
                    }, mContext);
                    asyncDataRetrieval.execute();
                    followUserOnClickHandler(followButton, currentUserId);
                }
            }
        });
    }
}
