package com.breadcrumbs.client.tabs;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.TimeUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.breadcrumbs.Location.CanvasLocationManager;
import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.ServiceProxy.AsyncDataRetrieval;
import com.breadcrumbs.ServiceProxy.AsyncImageFetch;
import com.breadcrumbs.ServiceProxy.MasterProxy;
import com.breadcrumbs.Trails.MyCurrentTrailManager;
import com.breadcrumbs.caching.GlobalContainer;
import com.breadcrumbs.client.BaseViewModel;
import com.breadcrumbs.client.BreadCrumbsImageSelector;
import com.breadcrumbs.R;
import com.breadcrumbs.client.SelectedEventViewerBase;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class SaveEventFragment extends Activity {
    private static final float ROTATE_FROM = 0.0f;
    private static final float ROTATE_TO = 360.0f;// 3.141592654f * 32.0f;

	private MasterProxy clientServiceProxy;
    private GlobalContainer globalContainer;
	private Context context;
	private static ViewPager viewPager;
	private Map<String, String> map = new HashMap<String, String>();
	private static Bitmap media;
	private ImageView iv;
	//private CanvasLocationManager locationManager;
    private MyCurrentTrailManager currentTrailManager;
	private static String crumbMedia;
	private LruCache<String, Bitmap> mMemoryCache;
	private String trailId;
	private String SavedCrumbId;
	private Spinner s;
    private JSONObject editableTrails;
	private HashMap<String, String> trailAndIdMap;
    private AsyncDataRetrieval asyncDataRetrieval;
	BreadCrumbsFusedLocationProvider locationProvider;
	/*
	 * Do as LITTLE as possible in the constructors. If possible, load at run-time
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_screen);
		globalContainer = GlobalContainer.GetContainerInstance();
		addTapListeners();
        //trailId = "15";
        context = this;
        ActionBar actionBar = getActionBar();
//        actionBar.hide();
        SetMedia();
        // use with care.
        currentTrailManager = MyCurrentTrailManager.GetCurrentTrailManagerInstance();
		// Set the height of the camera to be the same as the width so we get a square.
		CardView cardView = (CardView) findViewById(R.id.card_view);
		ViewGroup.LayoutParams layoutParams = cardView.getLayoutParams();
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		layoutParams.height = displaymetrics.widthPixels;
		cardView.setLayoutParams(layoutParams);
		setBackButtonListener();
		locationProvider = new BreadCrumbsFusedLocationProvider(this);
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

	public static void SetCrumbMedia(String crumbMedias) {
		crumbMedia = crumbMedias;
	}
	
	private void addTapListeners() {
		//adding listeners
		/*ImageButton backButton = (ImageButton) findViewById(R.id.backAddScreen);
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
				
			}
		});*/

		//viewPager = (ViewPager) findViewById(R.id.pager);
		/*iv = (ImageView)findViewById(R.id.media);
        final ImageButton cancelSaveButton = (ImageButton) findViewById(R.id.cancelButton);
        cancelSaveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Exit this page
                finish();
            }
        });*/

		final android.support.design.widget.FloatingActionButton newTrailButton =
				(android.support.design.widget.FloatingActionButton) findViewById(R.id.save_content);
			newTrailButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					createNewEvent();
					finish();
				/*	Intent viewCrumbsIntent = new Intent(context, BreadCrumbsImageSelector.class);
					String trailId = PreferenceManager.getDefaultSharedPreferences(context).getString("TRAILID", null);
					if (trailId != null) {
						viewCrumbsIntent.putExtra("TrailId", "5950"); // What is going on here? needs to be sorted out
						context.startActivity(viewCrumbsIntent);
					} else {
						// This really needs to be handled better.
						Toast.makeText(context, "Save failed because we did not find a trail to save to", Toast.LENGTH_SHORT).show();
					}*/

					/*RotateAnimation r; // = new RotateAnimation(ROTATE_FROM, ROTATE_TO);
					r = new RotateAnimation(ROTATE_FROM, ROTATE_TO, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					r.setDuration((long) 2*1500);
					r.setRepeatCount(100);
					newTrailButton.startAnimation(r);*/
				}
			});
        getAllOwnedOrPartOfTrailsForAUser();
		}
	private void getAllOwnedOrPartOfTrailsForAUser() {
        // Get all trails with a url request, convert the result to json
        String userId = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("USERID", "-1");
        String url = MessageFormat.format("{0}/rest/User/GetAllEditibleTrailsForAUser/{1}",
                LoadBalancer.RequestServerAddress(),
                userId);
        asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                //Trail Created
                try {
                    if (result == null) {
                        return;
                    }
                    editableTrails = new JSONObject(result);
                    loadTrailsIntoHashMap(editableTrails);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        asyncDataRetrieval.execute();
    }
	private void loadTrailsIntoHashMap(JSONObject trailsToLoad) {
        // Get all trails Owned/part_of for our user.
        //s = (Spinner) findViewById(R.id.spinner_image);
        //Get all trails
        //Add them to hashMap of string/trail
        trailAndIdMap = new HashMap<String, String>();
		Iterator iterator = trailsToLoad.keys();
		while (iterator.hasNext()) {
			JSONObject trail;
			try {
				trail = trailsToLoad.getJSONObject(iterator.next().toString());
                String title = trail.getString("TrailName");
                String id = trail.getString("Id");
                trailAndIdMap.put(title, id);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        // Load the trails from the hashSet into the spinner adapter.
        ArrayList<String> array = new ArrayList<String>();
        Collection baseList = trailAndIdMap.keySet();
        Iterator<String> listIterator = baseList.iterator();
        while (listIterator.hasNext()) {
            array.add(listIterator.next());
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array); // selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //s.setAdapter(spinnerArrayAdapter);
	}
	
	private void createNewEvent() {
		//Currently we will only be creating a crumb. In the future we will be able to create both.
        EditText chatTextView = (EditText) findViewById(R.id.new_crumb_title);
		final String TrailId = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("TRAILID", "-1");
        if (TrailId.equals("-1")) {
			Toast.makeText(this, "No current active trail. Create a trail from the main menu.", Toast.LENGTH_LONG).show();
            return;
        }

        Location location = locationProvider.GetLastKnownLocation();
		if (location == null) {
			// Need to display and error to the user
			Toast.makeText(this, "Failed to find location. Please ensure you have location services enabled", Toast.LENGTH_SHORT).show();
			return;
		}

		double lat = location.getLatitude();
		double longit = location.getLongitude();
		String suburb = "";
		String city = "";
		String country = "";
		Geocoder gcd = new Geocoder(context, Locale.getDefault());
		try {
			final List<Address> addresses = gcd.getFromLocation(lat, longit, 1);
			addresses.get(0);
			if (addresses.size() > 0) {
				Address address = addresses.get(0);
				suburb = address.getSubLocality();
				city = address.getLocality();
				country = address.getCountryName();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		final String latitude = String.valueOf(lat);
		final String longitude = String.valueOf(longit);
		final String Chat = chatTextView.getText().toString();
		String Icon = "Shitty styff";
		final String UserId = globalContainer.GetUserId();

		Calendar calendar = Calendar.getInstance();
		final String timeStamp = calendar.getTime().toString();
		final String finalSuburb = suburb;
		final String finalCity = city;
		final String finalCountry = country;
		// Just think of this shitty code as a javascript promise
		locationProvider.GetCurrentPlace(new ResultCallback<PlaceLikelihoodBuffer>() {
			@Override
			public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
				String placeId = "";
				PlaceLikelihood placeLikelihood = likelyPlaces.get(0);
				if (placeLikelihood != null ) {
					Place place = placeLikelihood.getPlace();
					if (place!=null) {
						placeId = place.getId();
					}
				}
		/*		for (PlaceLikelihood placeLikelihood : likelyPlaces) {
					Log.i("TEST", String.format("Place '%s' has likelihood: %g",
							placeLikelihood.getPlace().getName(),
							placeLikelihood.getLikelihood()));
							id = placeLikelihood.getPlace().getId();

				}*/
				likelyPlaces.release();
				// We need to wait
				createNewCrumb(Chat, UserId, TrailId, latitude, longitude,  "icon", ".jpg", placeId, finalSuburb, finalCity, finalCountry, timeStamp);
			}
		});
	}
	
	private void saveImage() {
		AsyncImageFetch imagesave = new AsyncImageFetch(media, SavedCrumbId, new AsyncImageFetch.RequestListener() {
			
			/*
			 * Override for the 
			 */
			@Override
			public void onFinished(String result) {
				finish();	
			}
		});
		imagesave.execute();	
	}

	public void SetMedia() {
        //Unpack extras

        media = globalContainer.GetBitMap();
		iv = (ImageView) findViewById(R.id.media);
		iv.setImageBitmap(media);
	}
	// adda photo.
//	private void addMedia() {
//		final ImageView addMediaButton = (ImageView) findViewById(R.id.media);
//		//addMediaButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				BaseViewModel.setViewPager(3);
//			}
//		});
//	}
	
	private void createNewCrumb(String Chat, String UserId,	String TrailId,	String Latitude, String Longitude, String Icon, String Extension, String placeId, String suburb, String city, String country, String timeStamp){
        String url = MessageFormat.format("{0}/rest/login/savecrumb/{1}/{2}/{3}/{4}/{5}/{6}/{7}/{8}/{9}/{10}/{11}/{12}",
                LoadBalancer.RequestServerAddress(),
                Chat,
                UserId,
                TrailId,
                Latitude,
                Longitude,
                Icon,
                Extension,
				placeId,
				suburb,
				city,
				country,
				timeStamp);

        url = url.replaceAll(" ", "%20");

        asyncDataRetrieval  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {

            /*
             * Override for the
             */
            @Override
            public void onFinished(String result) {

                SavedCrumbId = result;
                saveImage();
			}
		});
		
		asyncDataRetrieval.execute();	
		System.out.println("Sending save request to : " + url);	
	}
	
	//Create a new trail based on the data entered in the field.
	private void createNewTrail(String title, String description, String userId) {
		//final ToggleButton toggleButton = (ToggleButton) rootView.findViewById(R.id.trailAndCrumbToggle);
		//Construct our url
		String url = MessageFormat.format("{0}/rest/login/saveTrail/{1}/{2}/{3}",
				LoadBalancer.RequestServerAddress(),
				title,
				description,
				userId);

	    url = url.replaceAll(" ", "%20");
		asyncDataRetrieval  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
			
			/*
			 * Override for the 
			 */
			@Override
			public void onFinished(String result) {
				
			//Trail Created
				trailId = result;
			}
		});
		
		asyncDataRetrieval.execute();	

	}
	
	private String getDiscription() {
	/*	String description;
		EditText descText = (EditText) rootView.findViewById(R.id.crumbTitle);
		description = descText.getText().toString();
		return description;*/
		return "BALLS";
	}
	
	private String getTrailId() {
		/*String id = "";
		Spinner spinner = (Spinner) rootView.findViewById(R.id.trailSpinner);
		String titleName = spinner.getSelectedItem().toString();
		id = map.get(titleName);
		return id;*/
		return "";
	}
	
	public void GetDataForTrailSpinner() {
		
		ArrayList<String> array = new ArrayList<String>();
		/*Spinner s = (Spinner) rootView.findViewById(R.id.trailSpinner);
		JSONObject ourData = MasterProxy.GetProxyInstance().GetCachedData();	
		getNamesFromJSON(ourData);
		array.addAll(map.keySet());

		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this.context, android.R.layout.simple_spinner_item, array); //selected item will look like a spinner set from XML
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(spinnerArrayAdapter);*/
	}
	
	private void getNamesFromJSON(JSONObject json) {
		ArrayList<String> names = new ArrayList<String>();
		try {
   	 		int i = 0;
   	 		while (i < json.length()) {
        		System.out.println(json.length());
    			//Iterate through the list
				JSONObject singleNode = new JSONObject(json.get("Node" + i).toString());
				System.out.println("singleNode: " + singleNode);
				
				//Get name
				String tit = singleNode.getString("TrailName");
				//We need to know this to get crumbs/data about the trail.
				//We cannot pull down all the crumbs for every trail on load.
				String id = singleNode.getString("trailId");
				map.put(tit, id);
				i+= 1;
   	 		}
		}catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
