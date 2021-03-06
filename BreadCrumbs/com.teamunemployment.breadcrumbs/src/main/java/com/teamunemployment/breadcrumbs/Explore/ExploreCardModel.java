package com.teamunemployment.breadcrumbs.Explore;

import android.graphics.drawable.Drawable;

/**
 * @author Josiah Kendall.
 */
public class ExploreCardModel {

    public static final int HEADER_CARD = 0;
    public static final int FOLLOWING_CARD = 1;
    public static final int LOCAL_CARD = 2;
    public static final int TRENDING_HEADER = 3;
    public static final int FOLLOWING_HEADER = 4;
    public static final int LOCAL_HEADER = 5;
    public static final int TRENDING_CARD = 6;
    public static final int GLOBAL_HEADER = 7;
    public static final int GLOBAL_CARD = 8;


    private int viewType;
    private String data;
    private int drawableResId;

    public ExploreCardModel(int viewType, String data) {
        this.viewType = viewType;
        this.data = data;
    }

    public ExploreCardModel(int viewType, String data, int drawableResId) {
        this.viewType = viewType;
        this.data = data;
        this.drawableResId = drawableResId;
    }

    public String getData() {
        return data;
    }

    public int getViewType() {
        return viewType;
    }
}
