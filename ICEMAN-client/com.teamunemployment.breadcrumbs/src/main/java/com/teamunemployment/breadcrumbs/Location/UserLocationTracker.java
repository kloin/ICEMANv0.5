package com.teamunemployment.breadcrumbs.Location;

import android.content.Context;
import android.location.Location;
import android.os.Handler;

import com.google.android.gms.maps.GoogleMap;

public class UserLocationTracker {
/*
 * A class to manage the tracking of a user.
 */
	private long lastCheckedTime = 0;
	private boolean tracking = false;
	//private CanvasLocationManager clm = null;
	private Location lastCheckedPoint = null;
	private Context context = null;
	private Handler handler;
	private int loopCounter= 0;
	private GoogleMap mMap = null;
	public UserLocationTracker(Context context) {
		this.context = context;
	}
	public void StartTracking(final GoogleMap mMap) {
		tracking = true;
		handler = new Handler(context.getMainLooper());
		this.mMap = mMap;
//		final Runnable r = new Runnable(){
//			@Override
//			public void run() {
//						Double lat = clm.GetLatitude();
//						Double lon = clm.GetLongitude();
//						com.teamunemployment.breadcrumbs.Location loc1 = new com.teamunemployment.breadcrumbs.Location("");
//						loc1.setLatitude(lat);
//						loc1.setLongitude(lon);
//						if (lastCheckedPoint == null) {
//							lastCheckedPoint = loc1;
//						}
//						else if (distanceMoreThanTenMeters(loc1)) {
//							//GoogleMap mMap = ((MapFragment) context.get.getFragmentManager().findFragmentById(R.id.map)).getMap();
//							mMap.addPolyline(new PolylineOptions().add(new LatLng(lat, lon), new LatLng(lastCheckedPoint.getLatitude(), lastCheckedPoint.getLongitude())).width(5).color(Color.RED));
//							lastCheckedPoint = loc1;
//							//savePoint(loc1);
//							//Send url to save a point for the map
//						}
//					System.out.println(lastCheckedPoint);
//					loopCounter += 1;
//					System.out.println(loopCounter);
//					handler.postDelayed(this, 10000);
//				}
//		};
//		handler.postDelayed(r, 10000);
//	
	}
	
	public void PauseTracking() {
		
	}
	/*
	public void savePoint(com.teamunemployment.breadcrumbs.Location location) {
		AsyncDataRetrieval asyncDataRetrieval  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
			
			/*
			 * Override for the 
			 */
		/*	@Override
			public void onFinished(String result) {
			
			//I think here i return the id ofthe previous
			//Trail Created
				//trailId = result;
			}
		});
		
		asyncDataRetrieval.execute();
	}*/
	
	private boolean distanceMoreThanTenMeters(Location loc1) {
		// If further than 10 m
			return loc1.distanceTo(lastCheckedPoint) > 10;

	}

	
}
