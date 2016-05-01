package com.teamunemployment.breadcrumbs.client.StoryBoard;

import android.content.Context;
import android.widget.ImageView;
import android.widget.ProgressBar;

//import com.squareup.picasso.Picasso;
import com.teamunemployment.breadcrumbs.client.Cards.CrumbCardDataObject;
import com.teamunemployment.breadcrumbs.client.Maps.DisplayCrumb;

/**
 * Created by jek40 on 26/04/2016.
 */
public class StoryBoardModel {
    public int DataIndex;
    public CrumbCardDataObject CrumbData;
    public ImageView ImageView;
    public ProgressBar ProgressBar;
    public boolean FinishedLoadingImages = false;

    public StoryBoardModel(int index, CrumbCardDataObject crumbData, ImageView imageView, ProgressBar progressBar) {
        DataIndex = index;
        CrumbData = crumbData;
        ImageView = imageView;
        ProgressBar = progressBar;
    }




}
