package com.breadcrumbs.caching;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.breadcrumbs.Location.BreadCrumbsFusedLocationProvider;
import com.breadcrumbs.client.CameraController;
import com.breadcrumbs.client.Maps.DisplayCrumb;
import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.Cluster;

import org.json.JSONObject;

import java.util.ArrayList;

/*
    TODO : Check the efficiency of this class. Check for mem leaks.
    I Feel like this class is bad. I would love an alternative, but this is just so fucken easy
    that I am going to use it (sparingly). I also have concerns about security - this may be very
    exploitable - I dont know.

    ONLY USE THIS CLASS FOR VARIABLES THAT ARE CALLED VERY REGULARLY. (i.e UserId).
    I have kinda broken this rule....

    The problem with static variables is that they can cause leaks. These globals will also cause a lot
    of confusion if used regularily. At the moment I am using these when:

        - I HAVE to access a variable from another class and i am in the wrong context. (bitmap, camerabutton)
        - I use the variable over many different classes/contexts, and need to have the consistent value.
        - Caching for efficiency reasons - E.g trails.
 */
public class GlobalContainer {
	private static GlobalContainer singletonContainer = null;
	private Double latitude = null;
	private Double longitude = null;
    private GoogleMap mapInstance = null;
    private Context context = null;
    private String userId = null;
    private Bitmap bm;
    private BreadCrumbsFusedLocationProvider breadCrumbsFusedLocationProvider;
    private ImageButton cameraButton;
    private Long lastSavedTime;
    private Activity cameraContext;
    private Cluster<DisplayCrumb> cluster;
    private String trails;
    private ArrayList<String> currentTrailIds;
	//Private constructor - the only way an instance of this class can be created.
	private GlobalContainer() {
	}
	/*
	Singleton constructor. only constructs once, and i want it to constantly persits so I am hoping
	that it wont cause memory leaks.
	 */
	public static GlobalContainer GetContainerInstance() {
		if (singletonContainer == null) {
			singletonContainer = new GlobalContainer();
		}
		
		return singletonContainer;
	}

    /*
    ===============================================================================================
    THis is the caching for the trails. We save this because we do quite a bit of working and reworking
    on the trails.
     */
    public void SetTrailsJSON(String trailsJson) {
        this.trails = trailsJson;
    }

    public String GetTrailsJSON() {
        return trails;
    }

    public void SetBreadCrumbsFusedLocationProvider(BreadCrumbsFusedLocationProvider breadCrumbsFusedLocationProvider) {
        this.breadCrumbsFusedLocationProvider = breadCrumbsFusedLocationProvider;
    }

    public BreadCrumbsFusedLocationProvider GetBreadCrumbsFusedLocationProvider() {
        return breadCrumbsFusedLocationProvider;
    }

    public void SetCluster(Cluster<DisplayCrumb> cluster) {
        this.cluster = cluster;
    }

    public Cluster<DisplayCrumb> GetCluster() {
        return cluster;
    }

    /*
      @Reason : using this because I dont want to save this to the users phone, and I dont want to
                pass it in the bundle (or i dont know how - it wont work...).
      @Usage  : used to pass the picture captured from CameraController class to the saveEventFragment.
                it is also handy to have it saved so i can use it for undo etc...
     */
    public void SetBitMap(Bitmap bm) {
        this.bm = bm;
    }
    // Getter
    public Bitmap GetBitMap() {
        return this.bm;
    }

    /*
        @Reason : Cannot access the button from the camera context. Cannot access camera from buttons context. whatya do bruh
        @Usage  : Button listener in cameraController, saved in addFragment.
     */
    public void SetCaptureButton(ImageButton button) {
        this.cameraButton = button;
    }

    // Getter
    public ImageButton GetCameraButton() {
        return this.cameraButton;
    }

    //public void SetAddFragmentRootView()
	
	public void CacheLatestPoint(Double longitude, Double latitude) {
		this.longitude = longitude;
		this.latitude= latitude;
	}
    /*
      @Reason: need to know the time between saves in the service intent, but that cannot be saved
               in there because it restarts every time it gets called.
      @usage:  used to determine whether or not it is time to empty db of saved trail points
               then save the beasts.
     */
    public void setLastSavedTime(long lastSavedTime) {
        this.lastSavedTime = lastSavedTime;
    }

    public Long getLastSavedTime() {
        return this.lastSavedTime;
    }

	/* Not sure i need this	 */
	public Double GetUsersCurrentLongitude() {
		return this.longitude;
	}
	
	public Double GetUsersCurrentLatitude() {
		return this.latitude;
	}

    //Get our map instance
    public GoogleMap GetGoogleMapInstance() {
        return mapInstance;
    }

    //Set our map instance to be accessed by other backgroud threads.
    public void SetMapInstance(GoogleMap map){
        mapInstance = map;
    }
    // this is fucken dumb
    public void SetContext(Context context) {
        this.context = context;
    }

    public Context GetContext() {
        return context;
    }

    // Our users global Id
    public String GetUserId() {
        return userId;
    }

    public void SetUserId(String userId) {
        this.userId = userId;
    }

    public void SetTrailIdsCurrentlyDisplayed(ArrayList<String> ids) {
        this.currentTrailIds = ids;
    }

    public ArrayList<String> GetTrailIdsCurrentlyDisplayed() {
        return this.currentTrailIds;
    }
}
