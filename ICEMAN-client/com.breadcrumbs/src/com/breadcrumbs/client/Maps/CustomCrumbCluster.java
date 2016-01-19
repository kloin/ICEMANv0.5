package com.breadcrumbs.client.Maps;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.R;
import com.breadcrumbs.ServiceProxy.UpdateViewElementWithProperty;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.pkmmte.view.CircularImageView;

/**
 * Created by aDirtyCanvas on 8/5/2015.
 */
public class CustomCrumbCluster extends DefaultClusterRenderer<DisplayCrumb> {
    private ImageView mImageView;
    private final IconGenerator mIconGenerator;
    private final IconGenerator mClusterIconGenerator;
    private CircularImageView circularImageView;
    private Context context;
    private GoogleMap map;
    private View multiProfile;
    private String crumbId = null;
    public CustomCrumbCluster(Activity context, GoogleMap map, ClusterManager<DisplayCrumb> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        this.map = map;
        mIconGenerator = new IconGenerator(context);
        mClusterIconGenerator = new IconGenerator(context);
        multiProfile = context.getLayoutInflater().inflate(R.layout.crumb_cluster_layout, null);
        mImageView = new ImageView(context);
        circularImageView = (CircularImageView) multiProfile.findViewById(R.id.crumb_cluster_image);
        mIconGenerator.setContentView(multiProfile);
        Drawable drawable = ContextCompat.getDrawable(context.getApplicationContext(),R.drawable.background);
        mIconGenerator.setBackground(makeClusterBackground());
        // Do more custom stuff here
    }

    @Override
    protected void onBeforeClusterItemRendered(DisplayCrumb crumb, MarkerOptions markerOptions) {
        // Draw a single person.
        // Set the info window to show their name.
        // mImageView.setImageResource(R.drawable.background);
        circularImageView.setImageBitmap(crumb.getThumbNail());

        Bitmap icon = mIconGenerator.makeIcon();
        crumbId = crumb.getId();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<DisplayCrumb> cluster, MarkerOptions markerOptions) {
        super.onBeforeClusterRendered(cluster, markerOptions);
        // create a background process that alternates showing the images
    }

    @Override
    protected void onClusterItemRendered(DisplayCrumb clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);
        //circularImageView.setImageResource(R.drawable.background3);
    }

    // Make the background white
    private LayerDrawable makeClusterBackground() {
       // Drawable drawable = ContextCompat.getDrawable(context.getApplicationContext(),R.drawable.background);
       // drawable.setColorFilter(0x333 , PorterDuff.Mode.DARKEN);
        UpdateViewElementWithProperty updateViewElementWithProperty = new UpdateViewElementWithProperty();
        //circularImageView.setImageResource(R.drawable.background2);
        ShapeDrawable mColoredCircleBackground = new ShapeDrawable(new OvalShape());
        ShapeDrawable outline = new ShapeDrawable(new OvalShape());
        //updateViewElementWithProperty.UpdateCircularViewWithUrl(circularImageView, LoadBalancer.RequestCurrentDataAddress() + "/images/"+crumbId+".jpg");
       // circularImageView.setImageResource(R.drawable.background2);
        outline.getPaint().setColor(0x00ffffff); // Transparent white.
        LayerDrawable background = new LayerDrawable(new Drawable[]{outline, mColoredCircleBackground});
        int strokeWidth = (int) (15 * 3);
        background.setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        return background;
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        // Always render clusters.
        return cluster.getSize() > 1;
    }

}
