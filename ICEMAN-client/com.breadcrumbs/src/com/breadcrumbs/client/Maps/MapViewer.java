package com.breadcrumbs.client.Maps;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.ServiceProxy.AsyncDataRetrieval;
import com.breadcrumbs.ServiceProxy.MasterProxy;
import com.breadcrumbs.Trails.MyCurrentTrailManager;
import com.breadcrumbs.client.FragmentMaster;
import com.breadcrumbs.client.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
/*
 * View model for the map. This shows all the points on the map, and loads up the photos when clicked.
 * Also due for a rework.
 */
public class MapViewer extends Activity implements
		OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener {
	private GoogleMap mMap;
	private JSONObject json;
	private AsyncDataRetrieval clientRequestProxy;
	private PopupWindow popUp; 
	private LinearLayout parent;
	private boolean requestingImage = false;
	private AsyncDataRetrieval imageFetcher;
    private MyCurrentTrailManager myCurrentTrailManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.home_map);
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        myCurrentTrailManager = new MyCurrentTrailManager(mMap, this);
		// Start getting the data at the very start.
		//imageFetcher =new AsyncDataRetrieval();
        String trailId = this.getIntent().getStringExtra("TrailId");
        clientRequestProxy  = new AsyncDataRetrieval(LoadBalancer.RequestServerAddress() +"/rest/TrailManager/AddTrailView/"+trailId, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                //Initialise our object, and attempt to construct it from the string.
                Log.i("MapViewer.ViewUpdate", result);
            }
        });
        clientRequestProxy.execute();

		// Get our data
		myCurrentTrailManager.DisplayTrailAndCrumbs(trailId);

	}
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.animator.slide_in_right, R.anim.abc_slide_out_bottom);
    }

	@Override
	public boolean onMarkerClick(Marker marker) {
		// Popup window with image
		setContentView(R.layout.crumb_activity);

		requestingImage = true;
		//imageFetcher =  new AsyncDataRetrieval();
		//imageFetcher.GetRequestForData("192.168.1.7:8080/breadcrumbs/images/media/34816.png", this);
		return true;
	}


	@Override
	public void onMapClick(LatLng point) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapLongClick(LatLng point) {
		// TODO Auto-generated method stub

	}

}
