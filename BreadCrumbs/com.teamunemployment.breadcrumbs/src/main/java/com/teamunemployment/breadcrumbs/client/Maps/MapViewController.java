package com.teamunemployment.breadcrumbs.client.Maps;

import android.os.Bundle;

import com.teamunemployment.breadcrumbs.client.FragmentMaster;
import com.teamunemployment.breadcrumbs.R;
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

import org.json.JSONObject;

import java.util.ArrayList;

public class MapViewController  extends FragmentMaster implements
OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener {
	private ArrayList<String> subscriptionIds = new ArrayList<String>();
	private ArrayList<JSONObject> crumbCache = new ArrayList<JSONObject>();
	private GoogleMap mMap;
	//private AsyncDataRetrieval imageFetcher = new AsyncDataRetrieval();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.home_map);
	}
	
	// Load crumbs
	public void AddTrail(String trailId) {
		//Check which we already have, and fetch + append those that we dont
		subscriptionIds.add(trailId);
		//imageFetcher.GetRequestForData(LoadBalancer.RequestServerAddress()+"/breadcrumbs/rest/login/getAllCrumbsForATrail/" + trailId, this);
	}
	// Load crumb
	// Display map
	
	// Show all the points on the map
		private void displayData(JSONObject node1) {
			double Latitude = 0;
			double Longitude = 0;
			String Title = "";
			String crumbId = "";

			try {
				Latitude = Double.valueOf(-36.84846);
				Longitude = Double.valueOf(174.763332);
				//Title = node1.getString("Title");
				//crumbId = node1.getString("crumbId");
			} catch (NumberFormatException e) {
				e.printStackTrace();
			//} catch (JSONException e) {
				//e.printStackTrace();
			}
			
			LatLng theBurn = new LatLng(Latitude, Longitude);
			//mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
			mMap.setOnMarkerClickListener(this);
			// Show the shit on the map
			mMap.setMyLocationEnabled(true);

			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(theBurn, 13));
			mMap.addMarker(new MarkerOptions().position(theBurn).title(crumbId)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.highhills)));
		}

		public void Notify(JSONObject jsonResponse) {
			try {
				//crumbCache.add(jsonResponse);
				//System.out.println(crumbCache.toString());
				displayData(null);
			} catch (NullPointerException ex) {
				System.out.println("Error thown at notify in trailmapviewer");
				ex.printStackTrace();
			}
		}
		
		private void parseAndDisplayData() {
			
			
				//Iterator<String> jsonKeyIterator = json.keys();
				//while (jsonKeyIterator.hasNext()) {
				//	JSONObject node;
					
				//	try {
	//			/		node = json.getJSONObject(jsonKeyIterator.next());
			
		//				if (requestingImage == true) {
		//					requestingImage = false;
		//					createImage(node);
		//				} else {
		//					displayData(node);
		//				}
		//			} catch (JSONException e) {
						// TODO Auto-generated catch block
			//			e.printStackTrace();
		//			}
				//}
		}

		@Override
		public boolean onMarkerClick(Marker marker) {
			// TODO Auto-generated method stub
			return false;
		}



		@Override
		public void onMapLongClick(LatLng point) {
			// TODO Auto-generated method stub
			
		}



		@Override
		public void onMapClick(LatLng point) {
			// TODO Auto-generated method stub
			
		}
}
