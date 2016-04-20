package com.teamunemployment.breadcrumbs.client.Maps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.teamunemployment.breadcrumbs.Framework.JsonHandler;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UpdateViewElementWithProperty;
import com.teamunemployment.breadcrumbs.Trails.MyCurrentTrailDisplayManager;
import com.teamunemployment.breadcrumbs.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.ITALIC;
import static android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
 * View model for the map. This shows all the points on the map, and loads up the photos when clicked.
 * Also due for a rework.
 */
public class MapViewer extends Activity implements
		OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener {
	private GoogleMap mMap;
	private JSONObject json;
	private String TAG = "MapViewer";
	private AsyncDataRetrieval clientRequestProxy;
	private boolean requestingImage = false;
    private MyCurrentTrailDisplayManager myCurrentTrailManager;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_map);
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		setListenersAndLoaders();
		mContext = this;

	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "Begining onStart");
		// TrailManagerWorker is used for drawing trails and shit on the map.
	}

	// This is a method that wraps up all the startup shit done in onStart() and
	private void setListenersAndLoaders() {
		createCurrentTrailManager(mMap);

		// Button listeners, do as they say.
		setToggleSatellite();
		setBackButtonListener();

		/* Grab the trail Id, which is used to load the details of the map and */
		String trailId = this.getIntent().getStringExtra("TrailId");
		getBaseDetailsForATrail(trailId);
		myCurrentTrailManager.DisplayCrumbs(trailId);
		myCurrentTrailManager.GetAndDisplayTrailOnMap(trailId);
		addViewToTrail(trailId);
	}

	private void addViewToTrail(String trailId) {
		clientRequestProxy  = new AsyncDataRetrieval(LoadBalancer.RequestServerAddress() +
				"/rest/TrailManager/AddTrailView/"+trailId,
				new AsyncDataRetrieval.RequestListener() {
			@Override
			public void onFinished(String result) {
				// Dont actually need to do anything with this result, so I just log it.
				Log.i("MapViewer.ViewUpdate", "Successfully added view to map. Status : " + result);
			}
		}, this);
		clientRequestProxy.execute();
	}

	private void createCurrentTrailManager(GoogleMap map) {
		myCurrentTrailManager = new MyCurrentTrailDisplayManager(map, this);
	}

	private void setTrailClickHandlers(final String userId) {
		CardView authorCard = (CardView) findViewById(R.id.author_view);
		authorCard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Load the users profile page.
				Intent intent = new Intent();
				intent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.NavMenu.Profile.ProfilePageViewer");
				intent.putExtra("userId", userId);
				//intent.putExtra("name", name);
				startActivity(intent);

			}
		});
	}

	private void getBaseDetailsForATrail(final String trailId) {
		String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetBaseDetailsForATrail/"+trailId;
		AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
			@Override
			public void onFinished(String result) {
					if (result != null) {
					try {
						JsonHandler jsonHandler = new JsonHandler();
						json = jsonHandler.convertJsonStringToJsonObject(result);
						setTrailHeader(json.getString("trailName"));
						setUserNames(json.getString("userId"));
						setTrailClickHandlers(json.getString("userId"));

						setTrailDescription(json.getString("description"));
						setTrailDetails(trailId);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}, this);
		asyncDataRetrieval.execute();
	}


	// This is where I want to set the duration, distance, number of views and followers
	private void setTrailDetails(String trailId) {
		UpdateViewElementWithProperty updateViewElementWithProperty = new UpdateViewElementWithProperty();

		TextView duration = (TextView) findViewById(R.id.duration_details);
		TextView distance = (TextView) findViewById(R.id.distance_details);
		TextView followers = (TextView) findViewById(R.id.followers_details);
		String followerCountUrl = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetNumberOfFollowersForATrail/"+trailId;
		String durationUrl = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetDurationOfTrailInDays/"+ trailId;
		updateViewElementWithProperty.UpdateTextViewWithElementAndExtraString(distance, trailId, "Distance", " km",mContext );
		updateViewElementWithProperty.UpdateTextElementWithUrlAndAdditionalString(duration, durationUrl, "Days", mContext);
		updateViewElementWithProperty.UpdateTextElementWithUrlAndAdditionalString(followers, followerCountUrl, "Followers", mContext);
	}

	private void setTrailDescription(String trailDescription) {
		TextView textView = (TextView) findViewById(R.id.about_trail_overlay);
		if (trailDescription.equals(" ")) {
			textView.setText("No description given.");
			textView.setTypeface(null, Typeface.ITALIC);
		} else {
			textView.setText(trailDescription);
		}
	}

	private void SetUpMapTests() {
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.8696, 151.2094), 10));

		IconGenerator iconFactory = new IconGenerator(this);
		addIcon(iconFactory, "Default", new LatLng(-33.8696, 151.2094));

		iconFactory.setColor(Color.CYAN);
		addIcon(iconFactory, "Custom color", new LatLng(-33.9360, 151.2070));

		iconFactory.setRotation(90);
		iconFactory.setStyle(IconGenerator.STYLE_RED);
		addIcon(iconFactory, "Rotated 90 degrees", new LatLng(-33.8858, 151.096));

		iconFactory.setContentRotation(-90);
		iconFactory.setStyle(IconGenerator.STYLE_PURPLE);
		addIcon(iconFactory, "Rotate=90, ContentRotate=-90", new LatLng(-33.9992, 151.098));

		iconFactory.setRotation(0);
		iconFactory.setContentRotation(90);
		iconFactory.setStyle(IconGenerator.STYLE_GREEN);
		addIcon(iconFactory, "ContentRotate=90", new LatLng(-33.7677, 151.244));

		iconFactory.setRotation(0);
		iconFactory.setContentRotation(0);
		iconFactory.setStyle(IconGenerator.STYLE_ORANGE);
		addIcon(iconFactory, makeCharSequence(), new LatLng(-33.77720, 151.12412));
	}



	private void addIcon(IconGenerator iconFactory, CharSequence text, LatLng position) {
		MarkerOptions markerOptions = new MarkerOptions().
				icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("Day 1"))).
				position(position).
				anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

		mMap.addMarker(markerOptions);
	}

	private CharSequence makeCharSequence() {
		String prefix = "Mixing ";
		String suffix = "different fonts";
		String sequence = prefix + suffix;
		SpannableStringBuilder ssb = new SpannableStringBuilder(sequence);
		ssb.setSpan(new StyleSpan(ITALIC), 0, prefix.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.setSpan(new StyleSpan(BOLD), prefix.length(), sequence.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
		return ssb;
	}

	private void setUserNames(String userId) {
		TextView userName = (TextView) findViewById(R.id.username_map_overlay);
		TextView author = (TextView) findViewById(R.id.author_overlay);
		ArrayList<TextView> arrayList = new ArrayList<>();
		arrayList.add(userName);
		arrayList.add(author);
		UpdateViewElementWithProperty updateViewElementWithProperty = new UpdateViewElementWithProperty();
		updateViewElementWithProperty.UpdateMultipleViews(arrayList, userId, "Username", mContext);
	}

	private void setTrailHeader(String trailName) {
		TextView trailTitle = (TextView) findViewById(R.id.map_trail_header);
		trailTitle.setText(trailName);
	}

	private void setToggleSatellite() {
//		ImageButton imageButton = (ImageButton) findViewById(R.id.sattellite_toggle);
//		imageButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (mMap.getMapType() == 1) {
//					mMap.setMapType(2);
//				} else {
//					mMap.setMapType(1);
//				}
//			}
//		});
	}


	private void setBackButtonListener() {
		ImageView backButton = (ImageView) findViewById(R.id.backButtonCapture);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Close this activity, which will exit the screen
				finish();
			}
		});
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
		return true;
	}


	@Override
	public void onMapClick(LatLng point) {
		// TODO Auto-generated method stub
		SlidingUpPanelLayout slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
		slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
	}

	@Override
	public void onMapLongClick(LatLng point) {
		// TODO Auto-generated method stub

	}

}
