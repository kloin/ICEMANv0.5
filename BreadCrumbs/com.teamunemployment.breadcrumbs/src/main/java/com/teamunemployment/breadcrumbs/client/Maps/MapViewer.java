package com.teamunemployment.breadcrumbs.client.Maps;

import android.app.Activity;
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
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;
import com.teamunemployment.breadcrumbs.Framework.JsonHandler;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncPost;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UpdateViewElementWithProperty;
import com.teamunemployment.breadcrumbs.Preferences.Preferences;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils;
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
import com.teamunemployment.breadcrumbs.Trails.TrailManagerWorker;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.Cards.CrumbCardDataObject;
import com.teamunemployment.breadcrumbs.client.StoryBoard.StoryBoardActivity;
import com.teamunemployment.breadcrumbs.client.StoryBoard.StoryBoardItemData;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.ITALIC;
import static android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/*
 * View model for the map. This shows all the points on the map, and loads up the photos when clicked.
 * Also due for a rework.
 */
public class MapViewer extends Activity implements
		OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener {

	private boolean HAVE_SCALED_IMAGE = false;

	private int TRAIL_COVER_PHOTO_HEIGHT;
	private int SCROLLABLE_HEIGHT = 0;
	private int BOTTOM_SHEET_STATE = 0;

	private boolean WE_LIKE = false;
	private static final int EDIT_MODE = 1;
	private static final int READ_ONLY_MODE = 0;
	private boolean IS_OWN_TRAIL = false;

	private Context context;
	private GoogleMap mMap;
	private JSONObject json;
	private String TAG = "MapViewer";
	private AsyncDataRetrieval clientRequestProxy;
	private boolean requestingImage = false;
    private MyCurrentTrailDisplayManager myCurrentTrailManager;
	private Activity mContext;
	private ArrayList<DisplayCrumb> mCrumbs;
	private String trailId;

	private CoordinatorLayout coordinatorLayout;
	private View bottomSheet;
	private CardView bottomSheetToolbar;
	private RelativeLayout imageCover;
	private ImageView trailCoverPhoto;

	// Fabs
	private FloatingActionButton editToggleBottomSheetFab;
	private FloatingActionButton playFab;
	private NestedScrollView bottomSheetScrollView;

	// variables for storyboard.
	private int storyboardIndex = 0;
	private int storyboardTimerTime = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_map);
		context = this;
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mContext = this;
		setListenersAndLoaders();
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			Slide fade = new Slide();
			fade.setDuration(1000);
			getWindow().setExitTransition(fade);
		}
		setUpBottomSheet();
		scaleImage(bottomSheet);
		setVisibilityOfItems();
	}

	/**
	 * Method to set Up the bottom sheet, and set listeners for state changes.
	 */
	private void setUpBottomSheet() {
		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.map_root_view);
		bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
		bottomSheetToolbar = (CardView) bottomSheet.findViewById(R.id.bottom_sheet_header);

		BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
		behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				// React to state change
				onBottomSheetChanged(bottomSheet, newState);
			}
			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {

				onBottomSheetSlide(bottomSheet, slideOffset);
				// React to dragging events
			}
		});
	}

	private void setVisibilityOfItems() {
		if (!IS_OWN_TRAIL) {
			findViewById(R.id.settings_my_trail).setVisibility(View.GONE);
			findViewById(R.id.toggle_tracking).setVisibility(View.GONE);
			findViewById(R.id.publish_trail).setVisibility(View.GONE);
		}
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

	private void setExpandedBottomSheetState(View bottomSheet) {
		setScrollingBehaviour(bottomSheet);
		// Set it up here so it works at runtime.
		if (editToggleBottomSheetFab == null) {
			editToggleBottomSheetFab = (FloatingActionButton) bottomSheet.findViewById(R.id.edit_toggle_fab);
			setUpEditFabButtonToggle();

			// Load details
			UpdateViewElementWithProperty viewSetter = new UpdateViewElementWithProperty();

			TextView days = (TextView) bottomSheet.findViewById(R.id.duration_details);
			viewSetter.UpdateTextElementWithUrlAndAdditionalString(days, LoadBalancer.RequestServerAddress()+ "/rest/TrailManager/GetDurationOfTrailInDays/"+trailId, "Days", context);
			TextView pois = (TextView) bottomSheet.findViewById(R.id.number_of_crumbs_details);
			viewSetter.UpdateTextElementWithUrlAndAdditionalString(pois, LoadBalancer.RequestServerAddress() + "rest/TrailManager/GetAllSavedCrumbIdsForATrail" + trailId, "Points of Interest", context);
		}

		// shrink our fab.
		if (editToggleBottomSheetFab.getVisibility() != View.VISIBLE) {
			SimpleAnimations.ExpandFab(editToggleBottomSheetFab, 75);
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
		if (imageScrollPercent > 0.7 && scrollY > oldScrollY && editToggleBottomSheetFab.getVisibility() == View.VISIBLE) {
			SimpleAnimations.ShrinkFab(editToggleBottomSheetFab, 75);
			editToggleBottomSheetFab.setVisibility(View.INVISIBLE);
		}
		// THis means that we are scrolling back up the page, so we will need to redisplay our fab.
		else if (imageScrollPercent < 0.7 && scrollY < oldScrollY && editToggleBottomSheetFab.getVisibility() == View.INVISIBLE) {
			SimpleAnimations.ExpandFab(editToggleBottomSheetFab, 75);
			editToggleBottomSheetFab.setVisibility(View.VISIBLE);

		}

	}


	private void setUpEditFabButtonToggle() {

		if (!IS_OWN_TRAIL) {
			if(!WE_LIKE) {
				editToggleBottomSheetFab.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
				editToggleBottomSheetFab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp));
				WE_LIKE = true;
			} else {
				// Unlike
				editToggleBottomSheetFab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF4081")));
				editToggleBottomSheetFab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_border_white_24dp));
				WE_LIKE = false;
			}
		}

		editToggleBottomSheetFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (IS_OWN_TRAIL) {
					switch (BOTTOM_SHEET_STATE) {
						case EDIT_MODE:
							BOTTOM_SHEET_STATE = READ_ONLY_MODE;
							setUpReadOnlyMode();
							break;
						case READ_ONLY_MODE:
							BOTTOM_SHEET_STATE = EDIT_MODE;
							setUpEditMode();
							break;
					}
				} else {
					// do llike
					if(!WE_LIKE) {
						editToggleBottomSheetFab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF4081")));
						editToggleBottomSheetFab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_border_white_24dp));
						WE_LIKE = true;
					} else {
						// Unlike
						editToggleBottomSheetFab.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
						editToggleBottomSheetFab.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp));
						WE_LIKE = false;
					}


				}

			}
		});
	}


	/**
	 *
	 */
	private void setUpEditMode() {
		Activity act = (Activity) context;
		EditText trailName = (EditText) act.findViewById(R.id.trail_title_input);
		trailName.setEnabled(true);

		TextView tellThemToSelectACoverPhoto = (TextView) act.findViewById(R.id.cover_photo_prompt);
		SimpleAnimations.FadeInView(tellThemToSelectACoverPhoto);

		SimpleAnimations.ShrinkToggleAFab(editToggleBottomSheetFab, "#00E676", context.getResources().getDrawable(R.drawable.ic_action_accept));
	}

	private void setUpReadOnlyMode() {
		Activity act = (Activity) context;
		EditText trailName = (EditText) act.findViewById(R.id.trail_title_input);
		trailName.setEnabled(false);

		TextView tellThemToSelectACoverPhoto = (TextView) act.findViewById(R.id.cover_photo_prompt);
		SimpleAnimations.FadeOutView(tellThemToSelectACoverPhoto);

		SimpleAnimations.ShrinkToggleAFab(editToggleBottomSheetFab, "#FF4081", context.getResources().getDrawable(R.drawable.ic_edit_white_24dp));
	}

	/**
	 *
	 * @param bottomSheet
	 * @param slideOffset
     */
	private void onBottomSheetSlide(View bottomSheet, float slideOffset) {
		float newAlpha = 1 - slideOffset*2; //
		if (newAlpha < 0) {
			newAlpha = 0;
		}

		//bottomSheetToolbar.setAlpha(newAlpha);
		if (newAlpha == 1) {
			bottomSheetToolbar.setAlpha(1);
		} else {
			bottomSheetToolbar.setAlpha(0);
		}
		imageCover.setAlpha(newAlpha);

		if (playFab == null) {
			setUpPlayButton();
		}
		if (playFab.getVisibility() == View.VISIBLE) {
			SimpleAnimations.ShrinkFab(playFab, 75);
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

	private void setCollapsedToolbarState() {
		// Expand play button
		if (playFab == null) {
			setUpPlayButton();
		}
		SimpleAnimations.ExpandFab(playFab, 75);

		if (editToggleBottomSheetFab == null) {
			editToggleBottomSheetFab = (FloatingActionButton) bottomSheet.findViewById(R.id.edit_toggle_fab);
		}

		editToggleBottomSheetFab.setVisibility(View.INVISIBLE);
		// show text view
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
		createCurrentTrailManager(mMap);

		// Button listeners, do as they say.
		setToggleSatellite();
		setBackButtonListener();
		setUpPlayButton();
		/* Grab the trail Id, which is used to load the details of the map and */
		trailId = this.getIntent().getStringExtra("TrailId");
		getBaseDetailsForATrail(trailId);
		myCurrentTrailManager.StartCrumbDisplay(trailId);
		myCurrentTrailManager.GetAndDisplayTrailOnMap(trailId);
		addViewToTrail(trailId);
	}

	private void setUpPlayButton() {
		playFab = (FloatingActionButton) findViewById(R.id.play_button);
		playFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Load at first crumb.
				Intent viewCrumbsIntent = new Intent(mContext, StoryBoardActivity.class);
				ArrayList<CrumbCardDataObject> crumbObjects = myCurrentTrailManager.getDataObjects();
				viewCrumbsIntent.putExtra("StartingObject", crumbObjects.get(0));
				viewCrumbsIntent.putExtra("Index", storyboardIndex);
				viewCrumbsIntent.putParcelableArrayListExtra("CrumbArray", crumbObjects); // Note - this is currently using serializable - shoiuld use parcelable for speed
				viewCrumbsIntent.putExtra("TrailId", trailId);
				viewCrumbsIntent.putExtra("Timer", storyboardTimerTime);
				mContext.startActivityForResult(viewCrumbsIntent, 1);
			}
		});
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
			Toast.makeText(mContext, "Finished", Toast.LENGTH_LONG).show();
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
								.zoom(17)                   // Sets the zoom
								.bearing(90)                // Sets the orientation of the camera to east
								.tilt(40)                   // Sets the tilt of the camera to 30 degrees
								.build();
				mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			}

			// Then we passed data back, need to focus on this now.
		}
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
		private MapDisplayManager mapDisplayManager;
		private float currentZoom = -1;
		private boolean alreadyFocused = false;
		private int mDayOfYear = 0;
		private ArrayList<StoryBoardItemData> mStoryBoardItems;
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

		// Redraw the map
		public void redraw() {
			DisplayTrailOnMap(GlobalContainer.GetContainerInstance().GetTrailsJSON().toString());
		}

		public void GetAndDisplayTrailOnMap(String trailId) {
			setOnMapCameraChangedListener();
			// First construct our url that we want:
			mapDisplayManager = new MapDisplayManager(map, context, trailId);

			// fetch metadata first
			final String fetchMetadataUrl = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/FetchMetadata/" + trailId;
			clientRequestProxy = new AsyncDataRetrieval(fetchMetadataUrl, new AsyncDataRetrieval.RequestListener() {
				@Override
				public void onFinished(String result) throws JSONException {
					JSONObject jsonObject = new JSONObject(result);
					displayMetadata(jsonObject);
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
				// testIfNeedToDrawDay(nodeString);
				// This is to stop the bug where the different lines dont connect up.
				endOfLastPolyline = drawOnMap(node, endOfLastPolyline);
				drawNodeOnMap(node);
				index += 1;
				keys.next();
			}
			// Get fisrt object and display it when we are ready to animate over the photos.
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
				map.addMarker(new MarkerOptions()
						.position(location)
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.player_sprite)));
			}
			else if (eventType == TrailManagerWorker.CRUMB) {
				// Draw crumb type on the map.

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
					drawDashedPoly(listOfPoints);
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

		private List<LatLng> parseNonEncodedPolyline(String polylineString) {
			ArrayList<LatLng> listOfPoints = new ArrayList<>();
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

			map.addPolyline(options);
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
			//setMapListener();
			url = url.replaceAll(" ", "%20");

			// This creates the async request with a callback method of what I want completed when the
			// request is finished.
			clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
				@Override
				public void onFinished(String result) {
					//Initialise our object, and attempt to construct it from the string.
					JSONObject returnedCrumbs = null;
					try {
						returnedCrumbs = new JSONObject(result);

						// All the crumbs are under the object that has the id "Title".
						JSONArray crumbListJSON = new JSONArray(returnedCrumbs.getString("Title"));
						JSONObject next = null;
						for (int index=0; index<crumbListJSON.length(); index += 1 ) {
							// The next node to get data from and Draw.
							next =  crumbListJSON.getJSONObject(index);
							//mStoryBoardItems.add(buildStoryBoardItem(next));

							mapDisplayManager.DrawCrumbFromJson(next, false);
						}

						// Now that we are done, we want to set the focus to the last crumb added
						Double Latitude = next.getDouble("Latitude");
						Double Longitude = next.getDouble("Longitude");

						CameraPosition position = new CameraPosition(new LatLng(Latitude, Longitude), 10, 72, 0);
						CameraUpdate first = CameraUpdateFactory.newCameraPosition(position);

						CameraUpdate center=
								CameraUpdateFactory.newLatLng(new LatLng(Latitude,
										Longitude));
						CameraUpdate zoom=CameraUpdateFactory.zoomTo(12);
						// Move/animate camera to location
						map.moveCamera(first);
						map.animateCamera(zoom);

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						Log.e("BC.MAP", "A JsonException was thrown during the display process for a crumb." +
								"This probably means that you are missing a field on your json. Stacktrace follows");
						e.printStackTrace();
					}
				}
			}, context);

			clientRequestProxy.execute();
			Log.i("MAP", "Finished Loading crumbs and displaying them on the map");
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
			final DatabaseController dbc = new DatabaseController(context);
			// But how will I actually know this?
			final String localTrailId = Integer.toString(mPreferencesApi.GetLocalTrailId());

			// Dont want to be saving a non existent trail - I will do other shit with it first
			if (!localTrailId.equals("-1")) {
				JSONObject jsonObject = dbc.getAllSavedTrailPoints(localTrailId);
				HTTPRequestHandler saver = new HTTPRequestHandler();

				// Save our trails.
				String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/SaveTrailPoints/";
				AsyncPost post = new AsyncPost(url, new AsyncPost.RequestListener() {
					@Override
					public void onFinished(String result) {
						dbc.DeleteAllSavedTrailPoints(localTrailId);
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

}
