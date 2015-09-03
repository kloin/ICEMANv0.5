package com.breadcrumbs.client.Maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

/**
 * Created by aDirtyCanvas on 8/5/2015.
 */
public class CustomCrumbCluster extends DefaultClusterRenderer<DisplayCrumb> {
    private ImageView mImageView;
    private final IconGenerator mIconGenerator;
    private final IconGenerator mClusterIconGenerator;

    private Context context;
    private GoogleMap map;
    public CustomCrumbCluster(Context context, GoogleMap map, ClusterManager<DisplayCrumb> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        this.map = map;
        mIconGenerator = new IconGenerator(context);
        mClusterIconGenerator = new IconGenerator(context);
        mImageView = new ImageView(context);
        mIconGenerator.setContentView(mImageView);
        mIconGenerator.setBackground(makeClusterBackground());
        // Do more custom stuff here

    }

    @Override
    protected void onBeforeClusterItemRendered(DisplayCrumb crumb, MarkerOptions markerOptions) {
        // Draw a single person.
        // Set the info window to show their name.
        mImageView.setImageResource(crumb.GetCrumbIcon());
        Bitmap icon = mIconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title("test");
    }

    // Make the background white
    private LayerDrawable makeClusterBackground() {
        ShapeDrawable mColoredCircleBackground = new ShapeDrawable(new OvalShape());
        ShapeDrawable outline = new ShapeDrawable(new OvalShape());
        outline.getPaint().setColor(0x00ffffff); // Transparent white.
        LayerDrawable background = new LayerDrawable(new Drawable[]{outline, mColoredCircleBackground});
        int strokeWidth = (int) (15 * 3);
        background.setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        return background;
    }

}
