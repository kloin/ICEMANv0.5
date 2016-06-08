package com.teamunemployment.breadcrumbs.client.Maps;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.teamunemployment.breadcrumbs.AsyncWorkers.AsyncFetchCrumbList;
import com.teamunemployment.breadcrumbs.AsyncWorkers.GenericAsyncWorker;
import com.teamunemployment.breadcrumbs.Framework.JsonHandler;
import com.teamunemployment.breadcrumbs.Location.BreadcrumbsLocationProvider;
import com.teamunemployment.breadcrumbs.Location.SimpleGps;
import com.teamunemployment.breadcrumbs.Models;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.NetworkConnectivityManager;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncPost;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UpdateViewElementWithProperty;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.teamunemployment.breadcrumbs.Trails.TrailManagerWorker;
import com.teamunemployment.breadcrumbs._HAX.ExceptionHandler;
import com.teamunemployment.breadcrumbs.caching.TextCaching;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.Cards.CrumbCardDataObject;
import com.teamunemployment.breadcrumbs.client.StoryBoard.StoryBoardActivity;
import com.teamunemployment.breadcrumbs.client.StoryBoard.StoryBoardItemData;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.ITALIC;
import static android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/*
 * View model for the map. This shows all the points on the map, and loads up the photos when clicked.
 * Also due for a rework.
 */
public class MapViewer extends Activity implements OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener {

	public static final int EDIT_MODE = 1;
	public static final int READ_ONLY_MODE = 0;
	private static final String FIRST_TIME_FLAG = "FIRST_TIME_BRO";

	public int TRAIL_COVER_PHOTO_HEIGHT;
	public int SCROLLABLE_HEIGHT = 0;
	public int BOTTOM_SHEET_STATE = 0;
	private boolean WE_LIKE = false;
	private boolean IS_OWN_TRAIL = false;

	private boolean HAVE_SCALED_IMAGE = false;
	public MapDisplayManager mapDisplayManager;
	private ArrayList<StoryBoardItemData> mStoryBoardItems;
	public DatabaseController databaseController;

	public Context context;
	public GoogleMap mMap;
	private JSONObject json;
	public final static String TAG = "MapViewer";
	private AsyncDataRetrieval clientRequestProxy;
	private boolean requestingImage = false;
	private Activity mContext;
	private ArrayList<DisplayCrumb> mCrumbs;
	public String trailId;

	private CoordinatorLayout coordinatorLayout;
	public View bottomSheet;
	public CardView bottomSheetToolbar;
	private RelativeLayout imageCover;
	private ImageView trailCoverPhoto;

	private BottomSheetBehavior bottomSheetBehavior;

	// Fabs
	public FloatingActionButton bottomSheetFab;
	private FloatingActionButton playFab;
	private NestedScrollView bottomSheetScrollView;

	// variables for storyboard.
	private int storyboardIndex = 0;
	private int storyboardTimerTime = 0;

	private LatLng lastPoint;
	private LatLng startBase;
	private LatLng startHead;
	// Made public as we need to access this from the local subclass.
	public MyCurrentTrailDisplayManager myCurrentTrailManager;

	public boolean LOOKIING_AT_MAP = true;
	public boolean HAVE_ZOOMED = false;

	private FloatingActionButton locateMe;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_map);
		context = this;
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mContext = this;

		trailId = this.getIntent().getStringExtra("TrailId");
		// HAX
		//Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		//
		if (trailId != null && trailId.endsWith("L")) {
			IS_OWN_TRAIL = true;
			// Get rid of the L
			trailId = trailId.substring(0, trailId.length()-1);
			LOOKIING_AT_MAP = false;
		}
		setLocateMeClickHandler();
		setUpBottomSheet();
		setListenersAndLoaders();
		scaleImage(bottomSheet);
		setUpTrailState();

		final TextCaching caching = new TextCaching(context);
		String firstTime = caching.FetchCachedText(FIRST_TIME_FLAG);
		// What we do if we are looking at our own trail.
		if (firstTime == null) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
				}
			}, 1000);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					Snackbar.make(coordinatorLayout, "Swipe down trip summary to view map", Snackbar.LENGTH_LONG).show();
					caching.CacheText(FIRST_TIME_FLAG, "We have opened our own trail before.");
				}
			}, 1200);
		} else {

			FetchMyLocation();
		}
	}

	private void setUpTrailState() {
		SetUpDetailsItems();
	}

	/**
	 * Method to set Up the bottom sheet, and set listeners for state changes.
	 */
	private void setUpBottomSheet() {
		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.map_root_view);
		bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
		bottomSheetToolbar = (CardView) bottomSheet.findViewById(R.id.bottom_sheet_header);

		bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
		bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				// React to state change
				onBottomSheetChanged(bottomSheet, newState);
			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {
				onBottomSheetSlide(slideOffset);
				// React to dragging events
			}
		});
	}

	/**
	 * Set up the details for the trail. Made public so that we can override for local trails.
	 */
	public void SetUpDetailsItems() {
			findViewById(R.id.settings_my_trail).setVisibility(View.GONE);
			findViewById(R.id.toggle_tracking).setVisibility(View.GONE);
			findViewById(R.id.publish_trail).setVisibility(View.GONE);
	}

	// Handler for when the view is expanded.
	private void onBottomSheetChanged(View bottomSheet, int state) {
		switch (state) {
			case BottomSheetBehavior.STATE_COLLAPSED:
				setCollapsedToolbarState();
				break;
			case BottomSheetBehavior.STATE_DRAGGING:
				// setDragging state - not required atm
				break;
			case BottomSheetBehavior.STATE_EXPANDED:
				setExpandedBottomSheetState(bottomSheet);
				break;
			case BottomSheetBehavior.STATE_HIDDEN:
				// set Hidden behaviour - not enabled
				break;
			case BottomSheetBehavior.STATE_SETTLING:
				// set settliong behaviour
				break;
		}
	}

	/**
	 * Defin what happens when the bottom sheet expands.
	 * @param bottomSheet our bottom sheet view.
     */
	private void setExpandedBottomSheetState(View bottomSheet) {
		setScrollingBehaviour(bottomSheet);
		// Set it up here so it works at runtime.
		if (bottomSheetFab == null) {
			bottomSheetFab = (FloatingActionButton) bottomSheet.findViewById(R.id.edit_toggle_fab);
			SetUpBottomSheetFab();

		}

		// shrink our fab.
		if (bottomSheetFab.getVisibility() != View.VISIBLE) {
			SimpleAnimations.ExpandFab(bottomSheetFab, 75);
		}
	}

	/**
	 * Setup the scrolling behavour for our bottom sheet, namely hiding the action button and
	 * setting the toolbar when  scrolled up
	 * @param bottomSheet
     */
	private void setScrollingBehaviour(View bottomSheet) {
		if (bottomSheetScrollView == null) {
			bottomSheetScrollView = (NestedScrollView) bottomSheet.findViewById(R.id.bottom_sheet_scroller);
		}

		bottomSheetScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
			@Override
			public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				Log.d(TAG, "Scrolling bottom sheet content: ScrollX = " + scrollX + " ScrollY = " + scrollY + " oldScrollX = " + oldScrollX + " oldScrollY = " + oldScrollY );
				if (SCROLLABLE_HEIGHT == 0) {
					DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
					int toolbarHeightInPx = Math.round(60 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
					SCROLLABLE_HEIGHT = TRAIL_COVER_PHOTO_HEIGHT - toolbarHeightInPx;
				}


				float alpha = 0;
				// percentage of the bitmap that we have scrolled to invisible
				float scrollPostion = (float) scrollY / (float) SCROLLABLE_HEIGHT;
				float adjustedScroll = scrollPostion - (float)0.5;
				if (adjustedScroll > 0) {
					alpha = adjustedScroll*2;
				}
				if (alpha >= 0.95) {
					bottomSheetToolbar.setAlpha(1);
				} else {
					bottomSheetToolbar.setAlpha(0);
				}
				if (alpha!= 0) {
					imageCover.setAlpha(alpha);
				}
				checkEditFabState(scrollY, oldScrollY, scrollPostion);
			}
		});
	}

	/**
	 * Check if we should display or hide our edit toggle fab.
	 * @param scrollY The current Scroll position on the Y Axis, as measured in pixels from base position.
	 * @param oldScrollY The precious scroll position on the Y Axis. Use this to determin direction.
	 * @param imageScrollPercent What percentage of the image we have scrolled under our toolbar.
     */
	private void checkEditFabState(int scrollY, int oldScrollY, float imageScrollPercent) {
		// This means that we are scrolling down the page and our fab is moving towards the toolbar.
		if (imageScrollPercent > 0.7 && scrollY > oldScrollY && bottomSheetFab.getVisibility() == View.VISIBLE) {
			SimpleAnimations.ShrinkFab(bottomSheetFab, 75);
			bottomSheetFab.setVisibility(View.INVISIBLE);
		}
		// THis means that we are scrolling back up the page, so we will need to redisplay our fab.
		else if (imageScrollPercent < 0.7 && scrollY < oldScrollY && bottomSheetFab.getVisibility() == View.INVISIBLE) {
			SimpleAnimations.ExpandFab(bottomSheetFab, 75);
			bottomSheetFab.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Set up the edit toggle button. This is not just the edit, but the like button too (depending
	 * on whether it is our trail or not) so I think that really needs a rename.
	 */
	public void SetUpBottomSheetFab() {

		if(!WE_LIKE) {
			bottomSheetFab.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
			bottomSheetFab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp));
			WE_LIKE = false;
		} else {
			// Unlike
			bottomSheetFab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF4081")));
			bottomSheetFab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_border_white_24dp));
			WE_LIKE = true;
		}

		bottomSheetFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DoBottomSheetClick();
			}
		});
	}

	public void DoBottomSheetClick() {

		if(!WE_LIKE) {
			bottomSheetFab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF4081")));
			bottomSheetFab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_border_white_24dp));
			Snackbar.make(coordinatorLayout, "Liked +1", Snackbar.LENGTH_SHORT).show();
			WE_LIKE = true;
		} else {
			// Unlike
			bottomSheetFab.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
			bottomSheetFab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp));
			WE_LIKE = false;
		}

	}



	/**
	 * Define what we do when the bottom sheet is slid up.
	 * @param slideOffset How far we have slid.
     */
	private void onBottomSheetSlide(float slideOffset) {

		// Simple formula to calculate our toolbar visibility. The purpose of this is to make
		// the image start fading into a blue toolbar when it is below half way scrolled.
		float newAlpha = 1 - slideOffset*2; // zero alpha reached when we are at halfway.

		// Safety check.
		if (newAlpha < 0) {
			newAlpha = 0;
		}

		// If else loop to display the bottom sheet card toolbar. This is done because the imageview
		// fades, and when it reaches one we display our card with a shadow. Anything before this we
		// just want to fade the image cover.
		if (newAlpha >= 1) {
			bottomSheetToolbar.setAlpha(1);
		} else {
			bottomSheetToolbar.setAlpha(0);
		}
		imageCover.setAlpha(newAlpha);

		// Safety check as we are initialising these buttons at runtime to speed things up.
		if (playFab == null) {
			setUpPlayButton();
		}
		// If our pay fab is visible, we need to hide it.
		if (playFab.getVisibility() == View.VISIBLE) {
			SimpleAnimations.ShrinkFab(playFab, 75);
			SimpleAnimations.ShrinkFab(locateMe, 75);
		}
	}

	/**
	 * We want our trail cover photo to be the same height as it is width, however we cannot know
	 * this until runtime. I dont know if this should be done at runtime - it might slow things down?
	 * @param bottomSheet The bottom sheet where we can find the imageView.
     */
	private void scaleImage(View bottomSheet) {
		if (trailCoverPhoto == null) {
			trailCoverPhoto = (ImageView) bottomSheet.findViewById(R.id.trail_cover_photo);

			// Set the imageView height to be the same as the width.
			ViewGroup.LayoutParams layoutParams = trailCoverPhoto.getLayoutParams();
			TRAIL_COVER_PHOTO_HEIGHT = calculateScreenWidth();
			layoutParams.height = TRAIL_COVER_PHOTO_HEIGHT;
			trailCoverPhoto.setLayoutParams(layoutParams);
			HAVE_SCALED_IMAGE = true;

			// We also want the image to turn blue as it gets scrolled out of view, so we place this cover.
			imageCover = (RelativeLayout) bottomSheet.findViewById(R.id.image_view_cover);
			ViewGroup.LayoutParams toolbarLayoutParams = imageCover.getLayoutParams();
			toolbarLayoutParams.height = TRAIL_COVER_PHOTO_HEIGHT;
			imageCover.setLayoutParams(toolbarLayoutParams);
		}
	}

	/**
	 * Sewt the state of our toolbar when it is collapsed.
	 */
	public void setCollapsedToolbarState() {
		// Expand play button
		if (playFab == null) {
			setUpPlayButton();
		}
		SimpleAnimations.ExpandFab(playFab, 75);
		SimpleAnimations.ExpandFab(locateMe, 75);
		if (bottomSheetFab == null) {
			bottomSheetFab = (FloatingActionButton) bottomSheet.findViewById(R.id.edit_toggle_fab);
		}

		bottomSheetFab.setVisibility(View.INVISIBLE);
	}

	/**
	 * Simple method which calculates the width of the screen.
	 * @return The screen width of the device in pixels.
     */
	private int calculateScreenWidth() {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int height = displaymetrics.widthPixels;
		return height;
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "Begining onStart");
		// TrailManagerWorker is used for drawing trails and shit on the map.
	}

	// This is a method that wraps up all the startup shit done in onStart() and
	private void setListenersAndLoaders() {

		// UI Thread shit
		setToggleSatellite();
		setBackButtonListener();
		setUpPlayButton();

		// background shit
		createCurrentTrailManager(mMap);
		SetBaseDetailsForATrail(trailId);
		StartCrumbDisplay(trailId);

		Log.d(TAG, "Starting GetAndDisplayTrailOnMap. Time: " + System.currentTimeMillis());
		GetAndDisplayTrailOnMap(trailId);
		Log.d(TAG, "Finished GetAndDisplayTrailOnMap. Time: " + System.currentTimeMillis());
		//addViewToTrail(trailId);
	}

	public void ZoomOnGivenLocation(Location location, int zoom) {
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
				new LatLng(location.getLatitude(), location.getLongitude()), zoom), 500, null);
	}

	/*
        *    Method that triggers the the start of displaying crumbs on the map.
        *    @Param trailId : The id of the trail that we want to view. Used to get all the crumb Ids for
        *    displaying on the map.
        */
	public void StartCrumbDisplay(String trailId) {
		mStoryBoardItems = new ArrayList<>();
		String url = MessageFormat.format("{0}/rest/login/getAllCrumbsForATrail/{1}",
				LoadBalancer.RequestServerAddress(),
				trailId);
		url = url.replaceAll(" ", "%20");
		final String finalUrl = url;
		// This creates the async request with a callback method of what I want completed when the
		// request is finished.
		AsyncFetchCrumbList.IGenericAsync overrides = new AsyncFetchCrumbList.IGenericAsync() {
			@Override
			public JSONArray backgroundTasks() {
				return fetchAndProcessJSONArray(finalUrl);
			}

			@Override
			public void postExecute(JSONArray arrayObject) {
				try {
					DisplayObjects(arrayObject);
				} catch (JSONException e) {
					Log.d(TAG, "Errors displayiing crumbs. Stack trace follows");
					e.printStackTrace();
				}
			}
		};
		AsyncFetchCrumbList genericAsyncWorker = new AsyncFetchCrumbList(overrides);
		genericAsyncWorker.execute();
		Log.d(TAG, "Finished StartCrumbDisplay on main thread. May still be processing client side.");
	}

	private JSONArray fetchAndProcessJSONArray(String url) {
		HTTPRequestHandler requestHandler = new HTTPRequestHandler();
		String networkRequestResult = requestHandler.SendDataRequest(url, context);
		try {
			JSONObject returnedCrumbs = new JSONObject(networkRequestResult);
			JSONArray crumbListJSON = new JSONArray(returnedCrumbs.getString("Title"));
			return crumbListJSON;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// Return a blank to avoid npe.
		return new JSONArray();
	}



	public void FetchMyLocation() {

		SimpleGps simpleGps = new SimpleGps(context);
		Location location = simpleGps.GetInstantLocation();
		if (location == null) {
			return;
		}

 		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
				new LatLng(location.getLatitude(), location.getLongitude()), 13), 500, null);

		mMap.addCircle(new CircleOptions()
				.center(new LatLng(location.getLatitude(), location.getLongitude()))
				.radius(30)
				.strokeColor(Color.BLUE)
				.fillColor(Color.BLUE));
		if (LOOKIING_AT_MAP) {
//			CameraPosition cameraPosition = new CameraPosition.Builder()
//					.target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
//					.zoom(17)                   // Sets the zoom
//					.bearing(90)                // Sets the orientation of the camera to east
//					.tilt(40)                   // Sets the tilt of the camera to 30 degrees
//					.build();                   // Creates a CameraPosition from the builder
//			mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			HAVE_ZOOMED = true;
		}
	}

	public void DisplayObjects (JSONArray objects) throws JSONException {
		JSONObject next = null;
		for (int index=0; index<objects.length(); index += 1 ) {
			// The next node to get data from and Draw.
			next =  objects.getJSONObject(index);
			if (index == 0) {
				Double latitude = next.getDouble(Models.Crumb.LATITUDE);
				Double longitude = next.getDouble(Models.Crumb.LONGITUDE);
				LatLng latLng = new LatLng(latitude, longitude);
				SetMapPosition(latLng);
			}
			mapDisplayManager.DrawCrumbFromJson(next, false);
		}
	}

	public void SetMapPosition(LatLng latLng) {
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 15), 500, null);
	}

	public void GetAndDisplayTrailOnMap(String trailId) {
		// First construct our url that we want:
		mapDisplayManager = new MapDisplayManager(mMap, (Activity) context, trailId);

		// fetch metadata first
		final String fetchMetadataUrl = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetSavedPath/" + trailId;
		clientRequestProxy = new AsyncDataRetrieval(fetchMetadataUrl, new AsyncDataRetrieval.RequestListener() {
			@Override
			public void onFinished(String result) throws JSONException {
				processResult(result);
//				JSONObject jsonObject = new JSONObject(result);
//				displayMetadata(jsonObject);
			}
		}, context);
		clientRequestProxy.execute();
	}

	public void displayMetadata(JSONObject metadata) throws JSONException {
		// Get an iterator of all the event nodes.
		Iterator<String> keys = metadata.keys();
		LatLng endOfLastPolyline = null;
		int index = 0;
		while (keys.hasNext()) {
			String nodeString = metadata.getString(Integer.toString(index));
			JSONObject node = new JSONObject(nodeString);

			// This is to stop the bug where the different lines dont connect up.
			endOfLastPolyline = drawOnMap(node, endOfLastPolyline);
			drawNodeOnMap(node);
			index += 1;
			keys.next();
		}
		// Get fisrt object and display it when we are ready to animate over the photos.
	}

	private void drawNodeOnMap(JSONObject node) throws JSONException {
		String type = node.getString("EventType");
		int eventType = Integer.parseInt(type);
		IconGenerator iconFactory = new IconGenerator(context);
		iconFactory.setColor(Color.CYAN);
		// If we have a rest zone we want to draw a little circle on the map.
		if (eventType == TrailManagerWorker.REST_ZONE) {
			String latitude = node.getString("Latitude");
			String longitude = node.getString("Longitude");

			LatLng location = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
			mMap.addMarker(new MarkerOptions()
					.position(location)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.player_sprite)));
		}
		else if (eventType == TrailManagerWorker.CRUMB) {
			// Draw crumb type on the map.

		}
	}

	private void processResult(String result) throws JSONException {
		JSONObject jsonObject = new JSONObject(result);
		Iterator<String> keys = jsonObject.keys();
		int count = 0;
		while (keys.hasNext()) {
			keys.next();
			JSONObject tempObject = jsonObject.getJSONObject(Integer.toString(count));
			boolean isEncoded = tempObject.getBoolean("IsEncoded");
			String polyline = tempObject.getString("Polyline");
			List<LatLng> listOfPoints;
			if (isEncoded) {
				listOfPoints = PolyUtil.decode(polyline);
				listOfPoints = addLastPointToList(listOfPoints);
				DrawPolyline(listOfPoints, "#03A9F4");
			} else {
				listOfPoints = parseNonEncodedPolyline(polyline);
				listOfPoints = addLastPointToList(listOfPoints);
				DrawDashedPolyline(listOfPoints.get(0), listOfPoints.get(listOfPoints.size()-1), R.color.accent);
				//DrawPolyline(listOfPoints, "#03A9F4");
			}
			count += 1;
		}
	}

	public void DrawDashedPolyline(LatLng latLngOrig, LatLng latLngDest, int color) {

			double difLat = latLngDest.latitude - latLngOrig.latitude;
			double difLng = latLngDest.longitude - latLngOrig.longitude;

			double zoom = mMap.getCameraPosition().zoom;

			double divLat = difLat / (zoom * 2);
			double divLng = difLng / (zoom * 2);

			LatLng tmpLatOri = latLngOrig;

			for(int i = 0; i < (zoom * 2); i++){
				LatLng loopLatLng = tmpLatOri;

				if(i > 0){
					loopLatLng = new LatLng(tmpLatOri.latitude + (divLat * 0.075f), tmpLatOri.longitude + (divLng * 0.075f));
				}

				Polyline polyline = mMap.addPolyline(new PolylineOptions()
						.add(loopLatLng)
						.add(new LatLng(tmpLatOri.latitude + divLat, tmpLatOri.longitude + divLng))
						.color(color)
						.width(5f));

				tmpLatOri = new LatLng(tmpLatOri.latitude + divLat, tmpLatOri.longitude + divLng);
			}

	}

	/**
	 * Add a the last point from the previous polyline so that we dont have those hanging lines.
	 * @param listOfPoints The current polyline.
	 * @return The new polyline points (the line with a point added). Not really neccessary as javas
	 * shallow reference shit will sort it out, but it makes it a bit more readable.
     */
	private List<LatLng> addLastPointToList(List<LatLng> listOfPoints) {

		// This is our first time through use case. In this situation, we want to record the start and
		// end of this line so that we can work out which end was the best to draw from when drawing
		// to the next polyline.
		if (startBase == null && startHead == null && lastPoint == null) {
			startBase = new LatLng(listOfPoints.get(0).latitude, listOfPoints.get(0).longitude);
			startHead = new LatLng(listOfPoints.get(listOfPoints.size()-1).latitude, listOfPoints.get(listOfPoints.size()-1).longitude);
			return listOfPoints;
		}

		// Second time through. Decide which end we should draw from.
		if (lastPoint == null) {
			float lengthFromStart = calculateTheShortestDistanceToAnotherPolylineEndOrStartPoint(listOfPoints, startBase);
			float lengthFromEnd = calculateTheShortestDistanceToAnotherPolylineEndOrStartPoint(listOfPoints, startHead);
			if (lengthFromStart > lengthFromEnd) {
				lastPoint = startHead;
			} else {
				lastPoint = startBase;
			}
		}

		// Calculate which end of the polyline is closest - the head or the start.
		LatLng basePoint = listOfPoints.get(0);
		LatLng headPoint = listOfPoints.get(listOfPoints.size() - 1);
		float[] baseResults = new float[3];
		Location.distanceBetween(lastPoint.latitude, lastPoint.longitude, basePoint.latitude, basePoint.longitude, baseResults);
		float distanceToBase = baseResults[0];
		float[] headFloats = new float[3];
		Location.distanceBetween(lastPoint.latitude, lastPoint.longitude, headPoint.latitude, headPoint.longitude, headFloats);
		float distanceToHead = headFloats[0];

		// This means that we are drawing to the first point in the li
		if (distanceToBase < distanceToHead) {
			listOfPoints.add(0, lastPoint);
			lastPoint = listOfPoints.get(listOfPoints.size()-1);
			return listOfPoints;
		} else {
			listOfPoints.add(listOfPoints.size()-1, lastPoint);
			lastPoint = listOfPoints.get(0);
			return listOfPoints;
		}
	}

	/**
	 * Calculate whether the head or the base is closest. This is used to find what end of the list
	 * we should be drawing the "Joining" line to. This method is used to define the {@link #lastPoint}
	 * when it has not yet been set.
	 * @param listOfPoints The polyline in a list format
	 * @param focusPoint The point we are drawing from.
     * @return The shortest possible distsance.
     */
	private float calculateTheShortestDistanceToAnotherPolylineEndOrStartPoint(List<LatLng> listOfPoints, LatLng focusPoint) {
		// Head and base of our polyline.
		LatLng basePoint = listOfPoints.get(0);
		LatLng headPoint = listOfPoints.get(listOfPoints.size() - 1);

		// Results holders. Should not be more than 3 long, but I am making it 5 to be safe,
		// as documentation was not really clear about how many it can be.
		float[] baseResults = new float[3];
		float[] headFloats = new float[3];

		// Distance between the base and our point in question.
		Location.distanceBetween(focusPoint.latitude, focusPoint.longitude, basePoint.latitude, basePoint.longitude, baseResults);
		float distanceToBase = baseResults[0];

		// Distance between the head of the line and our point in question.
		Location.distanceBetween(focusPoint.latitude, focusPoint.longitude, headPoint.latitude, headPoint.longitude, headFloats);
		float distanceToHead = headFloats[0];
		if (distanceToBase < distanceToHead) {
			return distanceToBase;
		} else {
			return distanceToHead;
		}
	}

	private LatLng drawOnMap(JSONObject node, LatLng endOfLastLine) throws JSONException {

		List<LatLng> listOfPoints;
		if (node.has("PolylineString")) {
			String polyLineString = node.getString("PolylineString");
			if (node.getString("PolylineIsEncoded").equals("0")) {
				listOfPoints = PolyUtil.decode(polyLineString);
			} else {
				listOfPoints = parseNonEncodedPolyline(polyLineString);
				DrawDashedPolyline(listOfPoints.get(0), listOfPoints.get(listOfPoints.size()-1), R.color.black);
				//drawDashedPoly(listOfPoints);
				return listOfPoints.get(0);
			}
			if (endOfLastLine != null) {
				// listOfPoints.add(endOfLastLine);
			}
			DrawPolyline(listOfPoints);
			return listOfPoints.get(0);
		}

		return null;
	}

	private void drawDashedPoly(List<LatLng> list) {
		DrawPolyline(list);
	}

	public List<LatLng> parseNonEncodedPolyline(String polylineString) {

		ArrayList<LatLng> listOfPoints = new ArrayList<>();
		//if (lastPoint != null) {
			//listOfPoints.add(0,lastPoint);
		//}
		String[] pointsString = polylineString.split("\\|");
		for (int index = 0; index < pointsString.length; index += 1 ) {
			try {
				// Points are store in string array like lat,long so we need to spit it again for each point.
				String[] latAndLong = pointsString[index].split(",");
				LatLng latLng = new LatLng(Double.parseDouble(latAndLong[0]), Double.parseDouble(latAndLong[1]));
				listOfPoints.add(latLng);
			} catch(NumberFormatException ex) {
				ex.printStackTrace();
			}
		}
		return listOfPoints;
	}

	public void DrawPolyline(List<LatLng> listOfPoints) {
		PolylineOptions options = new PolylineOptions().width(12).color(Color.parseColor("#E57373")).geodesic(true);
		for (int z = 0; z < listOfPoints.size(); z++) {
			LatLng point = listOfPoints.get(z);
			options.add(point);
		}

		mMap.addPolyline(options);
	}

	public void DrawPolyline(List<LatLng> listOfPoints, String color) {
		PolylineOptions options = new PolylineOptions().width(24).color(Color.parseColor(color)).geodesic(true);
		for (int z = 0; z < listOfPoints.size(); z++) {
			LatLng point = listOfPoints.get(z);
			options.add(point);
		}

//		LatLng first = listOfPoints.get(0);
//                mMap.addCircle(new CircleOptions()
//                        .center(new LatLng(first.latitude, first.longitude))
//                        .radius(30)
//                        .strokeColor(Color.parseColor(color))
//                        .fillColor(Color.parseColor(color)));
		mMap.addPolyline(options);
	}

	/**
	 * setUp our play button.
	 */
	private void setUpPlayButton() {
		playFab = (FloatingActionButton) findViewById(R.id.play_button);
		playFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Load at first crumb.
				playFabClickHandler();
			}
		});
	}

	public void playFabClickHandler() {

		Intent viewCrumbsIntent = new Intent(mContext, StoryBoardActivity.class);

		ArrayList<CrumbCardDataObject> crumbObjects = myCurrentTrailManager.getDataObjects();

		// Just exit quietly.
		if (crumbObjects.size() == 0) {
			return;
		}
		viewCrumbsIntent.putExtra("StartingObject", crumbObjects.get(0));
		viewCrumbsIntent.putExtra("Index", storyboardIndex);
		viewCrumbsIntent.putParcelableArrayListExtra("CrumbArray", crumbObjects);
		viewCrumbsIntent.putExtra("TrailId", trailId);
		boolean isOwnTrail = crumbObjects.get(0).GetIsLocal() == 0;
		viewCrumbsIntent.putExtra("UserOwnsTrail", isOwnTrail);
		viewCrumbsIntent.putExtra("Timer", storyboardTimerTime);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(mContext, playFab, playFab.getTransitionName());
			mContext.startActivityForResult(viewCrumbsIntent , 1, options.toBundle());
		} else {
			mContext.startActivityForResult(viewCrumbsIntent, 1);
		}
	}

	/**
	 * When we view this trail, we need to add a view to it.
	 * @param trailId The trail to add a view to.
     */
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		myCurrentTrailManager = null;
	}

	/*
            For displaying photos we  need to get all the crumb objects, so that we can loop through them all.
         */
	public ArrayList<DisplayCrumb> GetTrailCrumbObjects() {
		if (mCrumbs == null) {
			// This better never happen, but if it does we need to know about it.
			throw new NullPointerException("GetTrailCrumbObjects called too early, object was null");
		}

		return mCrumbs;
	}

	private void createCurrentTrailManager(GoogleMap map) {
		myCurrentTrailManager = new MyCurrentTrailDisplayManager(map, this);
	}

	private void setTrailClickHandlers(final String userId) {
//		CardView authorCard = (CardView) findViewById(R.id.author_view);
//		authorCard.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// Load the users profile page.
//				Intent intent = new Intent();
//				intent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.NavMenu.Profile.ProfilePageViewer");
//				intent.putExtra("userId", userId);
//				//intent.putExtra("name", name);
//				startActivity(intent);
//
//			}
//		});
	}

	public void SetBaseDetailsForATrail(final String trailId) {
		// Load details
		UpdateViewElementWithProperty viewSetter = new UpdateViewElementWithProperty();

		TextView days = (TextView) bottomSheet.findViewById(R.id.duration_details);
		viewSetter.UpdateTextElementWithUrlAndAdditionalString(days, LoadBalancer.RequestServerAddress()+ "/rest/TrailManager/GetDurationOfTrailInDays/"+trailId, "Days", context);
		TextView pois = (TextView) bottomSheet.findViewById(R.id.number_of_crumbs_details);
		viewSetter.UpdateTextElementWithUrlAndAdditionalString(pois, LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetNumberOfCrumbsForATrail/" + trailId, "Points of Interest", context);

		TextView photos = (TextView) bottomSheet.findViewById(R.id.number_of_photos_details);
		viewSetter.UpdateTextElementWithUrlAndAdditionalString(photos, LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetNumberOfPhotosInATrail/" + trailId, "Photos", context);

		TextView videos = (TextView) bottomSheet.findViewById(R.id.number_of_videos_details);
		viewSetter.UpdateTextElementWithUrlAndAdditionalString(videos, LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetNumberOfPhotosInATrail/" + trailId, "Videos", context);

		TextView views = (TextView) bottomSheet.findViewById(R.id.view_count);
		viewSetter.UpdateTextElementWithUrlAndAdditionalString(views, LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetTrailViews/" + trailId, "Views", context);

		EditText trailName = (EditText) bottomSheet.findViewById(R.id.trail_title_input);
		viewSetter.UpdateEditTextElement(trailName, trailId, "TrailName", context);

		TextView textView = (TextView) bottomSheet.findViewById(R.id.bottom_sheet_trail_title);
		viewSetter.UpdateTextViewElement(textView, trailId, "TrailName", context);

		ImageView trailCover = (ImageView) bottomSheet.findViewById(R.id.trail_cover_photo);
		viewSetter.UpdateImageViewElement(trailCover, trailId, "CoverPhotoId", context);
	}

	/**
	 * Zoom the to where the map should be.
	 */
	public void SetFocusForMap() {
		// Load the location where the app should be loaded.

	}


	// This is where I want to set the duration, distance, number of views and followers
	private void setTrailDetails(String trailId) {
		UpdateViewElementWithProperty updateViewElementWithProperty = new UpdateViewElementWithProperty();

		TextView duration = (TextView) findViewById(R.id.duration_details);
		TextView distance = (TextView) findViewById(R.id.distance_details);
//		TextView followers = (TextView) findViewById(R.id.followers_details);
		String followerCountUrl = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetNumberOfFollowersForATrail/"+trailId;
		String durationUrl = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetDurationOfTrailInDays/"+ trailId;
		updateViewElementWithProperty.UpdateTextViewWithElementAndExtraString(distance, trailId, "Distance", " km",mContext );
		updateViewElementWithProperty.UpdateTextElementWithUrlAndAdditionalString(duration, durationUrl, "Days", mContext);
//		updateViewElementWithProperty.UpdateTextElementWithUrlAndAdditionalString(followers, followerCountUrl, "Followers", mContext);
	}

	private void setTrailDescription(String trailDescription) {
//		TextView textView = (TextView) findViewById(R.id.about_trail_overlay);
//		if (trailDescription.equals(" ")) {
//			textView.setText("No description given.");
//			textView.setTypeface(null, Typeface.ITALIC);
//		} else {
//			textView.setText(trailDescription);
//		}
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
		//TextView author = (TextView) findViewById(R.id.author_overlay);
		ArrayList<TextView> arrayList = new ArrayList<>();
		arrayList.add(userName);
	//	arrayList.add(author);
		//UpdateViewElementWithProperty updateViewElementWithProperty = new UpdateViewElementWithProperty();
		//updateViewElementWithProperty.UpdateMultipleViews(arrayList, userId, "Username", mContext);
	}

	private void setTrailHeader(String trailName) {
		//TextView trailTitle = (TextView) findViewById(R.id.map_trail_header);
		//trailTitle.setText(trailName);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			CrumbCardDataObject dataObject = data.getParcelableExtra("LastObject");
			int index = data.getIntExtra("Index", -1);
			int timer = data.getIntExtra("TimerPosition", -1);

			// Set for the next time we hit play.
			if (timer != -1) {
				storyboardTimerTime = timer;
			}
			// Set for next time we hit play.
			if (index != -1) {
				storyboardIndex = index;
			}

			if (dataObject != null) {
				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(new LatLng(dataObject.GetLatitude(), dataObject.GetLongitude()))      // Sets the center of the map to location user
								.zoom(19)                   // Sets the zoom
								.bearing(90)                // Sets the orientation of the camera to east
								.tilt(40)                   // Sets the tilt of the camera to 30 degrees
								.build();
				mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
				mMap.setMapType(2);
			}

			// Then we passed data back, need to focus on this now.
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void setBackButtonListener() {
		FloatingActionButton backButton = (FloatingActionButton) findViewById(R.id.map_back);
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

	private class MyCurrentTrailDisplayManager {
		private GoogleMap map;
		private AsyncDataRetrieval clientRequestProxy;
		private MyCurrentTrailDisplayManager mapContext;
		private Activity context;
		private LinkedList<String> linkedList = new LinkedList<>();
		// Location shit.
		private Location lastCheckedLocation;
		private GoogleApiClient locationclient;
		private LocationRequest locationrequest;
		private Intent mIntentService;
		private PendingIntent mPendingIntent;

		private float currentZoom = -1;
		private boolean alreadyFocused = false;
		private int mDayOfYear = 0;
		private PreferencesAPI mPreferencesApi;
		/*
        Display a single trail and its crumbs
        */
		public MyCurrentTrailDisplayManager(GoogleMap map, Activity context) {
			this.map = map;
			this.context = context;
			mapContext=this;
			mPreferencesApi = new PreferencesAPI(context);
			mapDisplayManager = new MapDisplayManager(map, context,Integer.toString(mPreferencesApi.GetLocalTrailId()));

			linkedList.add(0, "#2196F3");
			UiSettings uiSettings = map.getUiSettings();
			uiSettings.setCompassEnabled(false);
			uiSettings.setMapToolbarEnabled(false);
		}

		public ArrayList<CrumbCardDataObject> getDataObjects() {
			return mapDisplayManager.GetDataObjects();
		}

		private void setOnMapCameraChangedListener() {
			map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
				@Override
				public void onCameraChange(CameraPosition position) {
					if (position.zoom != currentZoom) {
						currentZoom = position.zoom;  // here you get zoom level
						//redraw();
					}
				}
			});
		}



		// Draw a day label on the map.
		private void testIfNeedToDrawDay(String node) {
			try {
				JSONObject jsonObject = new JSONObject(node);
				String timestamp = jsonObject.getString("TimeStamp");
				DateTime dateTime = new DateTime(timestamp);
				if (dateTime.getDayOfYear() > mDayOfYear) {
					double latitude = Double.parseDouble(jsonObject.getString("Latitude"));
					double longitude = Double.parseDouble(jsonObject.getString("Longitude"));
					IconGenerator iconFactory = new IconGenerator(context);
					iconFactory.setColor(Color.CYAN);
					addIcon(iconFactory, "Custom color", new LatLng(latitude, longitude));
					mDayOfYear = dateTime.getDayOfYear();
				}
			} catch (JSONException exception) {
				exception.printStackTrace();
			}
		}

		private void addIcon(IconGenerator iconFactory, CharSequence text, LatLng position) {
			MarkerOptions markerOptions = new MarkerOptions().
					icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("Day 1"))).
					position(position).
					anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
			map.addMarker(markerOptions);
		}

		private void setMapFocus(LatLng lastPoint) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPoint, 10.0f));
		}




		// DOnt think this is used anymroe.
		public void DisplayTrailOnMap(String trailsJSONString) {
			JSONObject trailJSON = null;
			try {
				// Construct our jsonObject for saving
				trailJSON = new JSONObject(trailsJSONString);
				int length = trailJSON.length();
				Iterator<String> nodeKeys = trailJSON.keys();
				// The first two values
				int backindex = 0;
				int frontindex = 1;
				String backNode = "0";
				String frontNode = "1";
				ArrayList<LatLng> list = new ArrayList<>();
				int counter = 0;
				while (counter < length) {
					String key = "Node" + Integer.toString(counter);
					if (trailJSON.has(key)) {
						JSONObject pointNodeBase = trailJSON.getJSONObject(key);
						Double baseLatitude = pointNodeBase.getDouble("latitude");
						Double baseLongitude = pointNodeBase.getDouble("longitude");
						list.add(new LatLng(baseLatitude, baseLongitude));
						counter += 1;
					} else {
						counter += 2;
					} //#E57373 , #00796B, #B71C1C #004D40
				}
				PolylineOptions options = new PolylineOptions().width(15).color(Color.parseColor("#E57373")).geodesic(true);

				map.addPolyline(options);

				// navigate to base of polyline.

				//map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			} catch (JSONException e) {
				Log.e("TRAIL", "Failed to draw trail on the map - Likey an issue finding the correct json Point");
				e.printStackTrace();
			}
		}

		// Simple method to build story board item out of jsonObject.
		private StoryBoardItemData buildStoryBoardItem(JSONObject json) throws JSONException {
			String id = json.getString("Id");
			Double latitude = json.getDouble("Latitude");
			Double Longitude = json.getDouble("Longitude");
			String mime = json.getString("Extension");
			String placeId = json.getString("PlaceId");

			return new StoryBoardItemData(id, placeId, mime, latitude, Longitude);
		}

		public void DisplayCrumbsFromLocalDatabase(JSONObject crumbs) {

			// This creates the async request with a callback method of what I want completed when the
			try {
				// Get the crumb title ??? why do i do this? last crumb?
				Iterator<String> keys = crumbs.keys();
				JSONObject next = null;
				while (keys.hasNext()) {
					// The next node to get data from and Draw.
					//next = crumbs.get
					String id = keys.next();
					next = crumbs.getJSONObject(id);
					mapDisplayManager.DrawLocalCrumbFromJson(next);
					mapDisplayManager.clusterManager.cluster();
				}
				// Now that we are done, we want to set the focus to the last crumb added

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.e("BC.MAP", "A JsonException was thrown during the display process for a crumb." +
						"This probably means that you are missing a field on your json. Stacktrace follows");
				e.printStackTrace();
			}
			Log.i("MAP", "Finished Loading crumbs and displaying them on the map");
		}

		/*
        Method that begins the background tracking of a users trail.
         */
		public boolean CreateTrailAndBeginTracking(String trailTitle) {
			// I am doing this after rather than before because if a trail fails, and I want to retry in
			// the background.
			SendCreateTrailRequest(trailTitle);
			return true;
		}

		public boolean SendCreateTrailRequest(String trailTitle) {
			// Get userId from shared preferences.
			String userId = PreferenceManager.getDefaultSharedPreferences(context).getString("USERID", "-1");
			//store variable to sharedPreferences (and db for backup? It is pretty vital that I dont lose this trail)
			// I love coding high. This source code is like a diary.
			String url = MessageFormat.format(LoadBalancer.RequestServerAddress()+ "/rest/login/saveTrail/{0}/{1}/{2}",trailTitle, "test", userId);
			HTTPRequestHandler saver = new HTTPRequestHandler();
			saver.SendSimpleHttpRequestAndSavePreference(url, context);
			// Save to the database.

			return true;
		}
		/*
        Needs to be moved out at sometime
         */
		public boolean StopTracking() {
			if (locationclient == null) {
				return true;
			}
			if (locationclient.isConnected()) {
				locationclient.disconnect();
			}
			//No fetch data and send it to server.
			fetchTrailPointDataAndSaveItToTheServer();
			return true;
		}

		private void fetchTrailPointDataAndSaveItToTheServer() {
			final String trailPointsId;
			// HACZ - cos im just testing.
			databaseController = new DatabaseController(context);
			// But how will I actually know this?
			final String localTrailId = Integer.toString(mPreferencesApi.GetLocalTrailId());

			// Dont want to be saving a non existent trail - I will do other shit with it first
			if (!localTrailId.equals("-1")) {
				JSONObject jsonObject = databaseController.getAllSavedTrailPoints(localTrailId);
				HTTPRequestHandler saver = new HTTPRequestHandler();

				// Save our trails.
				String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/SaveTrailPoints/";
				AsyncPost post = new AsyncPost(url, new AsyncPost.RequestListener() {
					@Override
					public void onFinished(String result) {
						databaseController.DeleteAllSavedTrailPoints(localTrailId);
					}
				}, jsonObject);

				post.execute();
			}
		}

		/*
        // create bitmap from resource
     Bitmap bm = BitmapFactory.decodeResource(getResources(),
      R.drawable.simple_image);

     // set circle bitmap
     ImageView mImage = (ImageView) findViewById(R.id.image);
     mImage.setImageBitmap(getCircleBitmap(bm));
         */
		private Bitmap getCircleBitmap(Bitmap bitmap) {
			final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
					bitmap.getHeight(), Bitmap.Config.ARGB_8888);
			final Canvas canvas = new Canvas(output);

			final int color = Color.RED;
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			final RectF rectF = new RectF(rect);

			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawOval(rectF, paint);

			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
			canvas.drawBitmap(bitmap, rect, rect, paint);

			bitmap.recycle();

			return output;
		}

		public void DrawPersonalTrailOnMap() {
			// Need to fetch all our metadata and save it to the server. Then we need to get all the
			// photos on the database and display it on the map

		}
	}


	private void setLocateMeClickHandler() {
		locateMe = (FloatingActionButton) findViewById(R.id.where_am_i);
		locateMe.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FetchMyLocation();
			}
		});
	}

}
