package com.teamunemployment.breadcrumbs.client.tabs;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.teamunemployment.breadcrumbs.BackgroundServices.SaveCrumbService;
import com.teamunemployment.breadcrumbs.BackgroundServices.UploadTrailService;
import com.teamunemployment.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.teamunemployment.breadcrumbs.Location.PlaceManager;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.NetworkConnectivityManager;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncImageFetch;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.Trails.MyCurrentTrailDisplayManager;
import com.teamunemployment.breadcrumbs.Trails.TrailManagerWorker;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.R;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.teamunemployment.breadcrumbs.caching.Utils;
import com.teamunemployment.breadcrumbs.client.Home.HomeActivity;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    private GlobalContainer globalContainer;
	private Context context;
	private static ViewPager viewPager;
	private Map<String, String> map = new HashMap<String, String>();
	private static Bitmap media;
	private ImageView iv;
	private static String crumbMedia;
	private LruCache<String, Bitmap> mMemoryCache;
	private String trailId;
	private String SavedCrumbId;
	private Spinner s;
    private JSONObject editableTrails;
	private HashMap<String, String> trailAndIdMap;
    private AsyncDataRetrieval asyncDataRetrieval;
	private BreadCrumbsFusedLocationProvider fusedLocationProvider;
	private boolean backCameraOpen;
	private PreferencesAPI mPreferencesApi;
	private Location location;
	private LocationManager locationManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_screen);
		context = this;
		globalContainer = GlobalContainer.GetContainerInstance();
		mPreferencesApi = new PreferencesAPI(this);

		addTapListeners();

		backCameraOpen = getIntent().getBooleanExtra("IsBackCameraOpen", true);
		Double lat = getIntent().getDoubleExtra("Latitude", 0);
		Double lon = getIntent().getDoubleExtra("Longitude", 0);
		if (lat == 0 && lon == 0) {
			// StartFetching Location updates.
			location = new Location("");
			location.setLatitude(lat);
			location.setLongitude(lon);
		}

        SetMedia();

		// Set the height of the camera to be the same as the width so we get a square.
		setBackButtonListener();
		fusedLocationProvider = new BreadCrumbsFusedLocationProvider(this);
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
	}

	private void setBackButtonListener() {
		FloatingActionButton backButton = (FloatingActionButton) findViewById(R.id.backAddScreen);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Close this activity, which will exit the screen

				finish();
			}
		});
	}
	
	private void addTapListeners() {
		final FloatingActionButton done = (FloatingActionButton) findViewById(R.id.done_button);
		done.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				createNewEvent();
				Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
				finish();
			}
		});
	}

	private void createNewEvent() {
		//Currently we will only be creating a crumb. In the future we will be able to create both.
		trailId = Integer.toString(mPreferencesApi.GetLocalTrailId());

		// grab event Id.
		int eventId = mPreferencesApi.GetEventId();

		// Save image to disk.
		String fileName =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/"+eventId;
		Utils.SaveBitmap(fileName, GlobalContainer.GetContainerInstance().GetBitMap());

		// Need to launch intent service to save object.
		Intent saveCrumbService = new Intent(context, SaveCrumbService.class);
		saveCrumbService.putExtra("IsPhoto", true);
		saveCrumbService.putExtra("EventId", eventId);
		context.startService(saveCrumbService);
		mPreferencesApi.SetEventId(eventId+1);

	}

	private String getSuburb(Address address) {
		if (address != null) {
			return address.getSubLocality();
		}

		return " "; // return an empty string which will be sent to the server.
	}

	private String getCity(Address address) {
		if (address != null) {
			return address.getLocality();
		}
		return " "; // return an empty string which will be sent to the server.
	}

	private String getCountry(Address address) {
		if (address != null) {
			return address.getCountryName();
		}
		return " "; // return an empty string which will be sent to the server.
	}

	private void processAndSaveEvent(final Location location) {

		// Fetch Time
		Calendar calendar = Calendar.getInstance();
		final String timeStamp = calendar.getTime().toString();

		// Get the address
		Address address = PlaceManager.GetPlace(context, location.getLatitude(), location.getLongitude());

		// Parameters that we need to save to for a crumb.
		final String suburb = getSuburb(address);
		final String finalCity = getCity(address);
		final String finalCountry = getCountry(address);

		fusedLocationProvider.GetCurrentPlace(new ResultCallback<PlaceLikelihoodBuffer>() {
			@Override
			public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
				String placeId = " ";
				try {
					PlaceLikelihood placeLikelihood = likelyPlaces.get(0);
					if (placeLikelihood != null) {
						Place place = placeLikelihood.getPlace();
						if (place != null) {
							placeId = place.getId();
						}
					}
					likelyPlaces.release();
				} catch (IllegalStateException ex) {
					// This happens when we have no network connection
					ex.printStackTrace();
				}

				// We need to wait
				//createNewCrumb(chat, UserId, TrailId, latitude, longitude,  "icon", ".jpg", placeId, finalSuburb, finalCity, finalCountry, timeStamp);
				DatabaseController dbc = new DatabaseController(context);
				int eventId = PreferenceManager.getDefaultSharedPreferences(context).getInt("EVENTID", -1);
				if (eventId == -1) {
					eventId = 0;
				}

				//eventId += 1;
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
				//preferences.edit().putInt("EVENTID", eventId).commit();
				String userId = preferences.getString("USERID", null);

				// Crash the app here. I am doing this because this should never happen in production - if it does happen I want to catch it as soon as possiible
				if (userId == null) {
					throw new NullPointerException("User id was null.");
				}
				// Need to set our trailId as the local one.
				String localTrailId = Integer.toString(mPreferencesApi.GetLocalTrailId());
				String fileName =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/"+eventId;
				// save our crumb to the db. It will be saved to the server when we publish
				Utils.SaveBitmap(fileName, media);
				dbc.SaveCrumb(localTrailId, " ", userId, eventId, location.getLatitude(), location.getLongitude(), ".jpg", timeStamp, "", placeId, suburb, finalCity, finalCountry, 0, 0, 0);
				TrailManagerWorker trailManagerWorker = new TrailManagerWorker(context);
				trailManagerWorker.CreateEventMetadata(TrailManagerWorker.CRUMB, location);
			}
		});
	}

	public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
		return outputStream.toByteArray();
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

	private void adjustMedia(Bitmap bm) {
		if (backCameraOpen) {

			if (90 != 0 && bm != null) {
				Matrix m = new Matrix();

				m.setRotate(90, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
				try {
					Bitmap b2 = Bitmap.createBitmap(
							bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
					if (bm != b2) {
						bm.recycle();
						bm = b2;
					}
				} catch (OutOfMemoryError ex) {
					ex.printStackTrace();
				}
			}
		}
		// Otherwise its a front cam shot, rotate the other way.
		else {
			if (bm != null) {
				Matrix m = new Matrix();
				m.setRotate(270, (float) bm.getWidth()/2, (float) bm.getHeight() / 2);
				try {
					Bitmap b2 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
					if (bm != b2) {
						bm.recycle();
						bm = b2;
					}

					// Our images keep being flipped?
					Matrix flipHorizontalMatrix = new Matrix();
					flipHorizontalMatrix.setScale(-1,1);
					flipHorizontalMatrix.postTranslate(bm.getWidth(),0);
					bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), flipHorizontalMatrix, true);
				} catch (OutOfMemoryError ex) {
					throw ex;
				}
			}
		}
		iv = (ImageView) findViewById(R.id.media);
		iv.setImageBitmap(bm);
		if (bm != null){
			globalContainer.SetBitMap(bm.copy(Bitmap.Config.ARGB_8888, false));
		}
	}

	public void SetMedia() {
        //Unpack extras
		media = globalContainer.GetBitMap();
		adjustMedia(media);
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
		}, context);
		
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
		}, context);
		
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
