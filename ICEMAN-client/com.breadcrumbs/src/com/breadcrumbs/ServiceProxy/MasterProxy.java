package com.breadcrumbs.ServiceProxy;

import android.os.AsyncTask;

import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.client.FragmentMaster;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;

/*
 * THE BLACK BOX NETWORK CLASS. This was a big brutal beast which is being replaced by
 * smaller chunks that will use my new RESTful API.
 */
public class MasterProxy {
	private static MasterProxy mProxy = null;
	private JSONObject requestedData;
	private String requestCode;
	private String entityId;
	private static FragmentMaster fragment;
	private ArrayList<NameValuePair> value;
	private String title = "title";
	private String description = "description";
	private String userId = "userId";
	private String crumbId = "id";
	private JSONObject cachedData;

	//Singleton to get our proxy instance.
	public static MasterProxy GetProxyInstance() {
		if (mProxy == null) {
			mProxy = new MasterProxy();
		}		
		return mProxy;		
	}
	
	//singleton to get an instance of this bad boy
	public static MasterProxy GetProxyInstance(FragmentMaster fragmentPassed) {
		//Can't use this in a static context so ugly naming applies
		fragment = fragmentPassed;
		return GetProxyInstance();
	}
	
	public JSONObject GetCachedData() {
		return cachedData;
	}
	
	   /*
		 * Here is the list of messages that can be passed by the user, 
		 * and their required action(s)
		 * =====================================================================
		 * 
		 * User Events (POST)
		 * -----------------------------------------------------------
		 * (000) - Create user 
		 * (001) - Log user in
		 * (301) - Add user as friend
		 * -----------------------------------------------------------
		 * Add data (trail/crumb) events (POST)
		 * -----------------------------------------------------------
		 * (310) - Create Trail
		 * (311) - Create Crumb/Add crumb to trail
		 * (312) - Create Comment
		 * -----------------------------------------------------------
		 * Remove events (POST)
		 * -----------------------------------------------------------
		 * (411) - Remove trail
		 * (421) - Remove crumb from trail
		 * (431) - Remove Comment
		 * -----------------------------------------------------------
		 * Update events (POST)
		 * -----------------------------------------------------------
		 * (511) - Update trail
		 * (521) - Update Crumb
		 * (531) - Update Comment
		 * -----------------------------------------------------------
		 * Retrieving events (GET)
		 * -----------------------------------------------------------
		 * (600) get specific user
	 	 * (610) get specific users friends
		 * ------------------------------------------------------------
		 * (700) get specific trail
		 * (710) get specific crumb
		 * (710) get top 20 crumbs for trail
		 * (711) get 20 - 40 crumbs`for trail
		 * (71X) etc....
		 * (791) Get All trails for this user (id of x)
		 * (792) - Get All crumbs for this user
		 * --------------------------------------
		 * ======================================================================
		 */
	
	//The public variable to get trails with a customizable requestCode
	public JSONObject GetOurData(String requestCode, String entityId) {
		//Get the data
		this.requestCode = requestCode;
		this.entityId = entityId;
		new ClientServiceProxy().execute();
		return requestedData;
	}
	//Get all trails for a user. Needed for readability?
	public void GetAllTrailsForUser(String userId) {
		GetAllTrailsForAnEntity(userId);		
	}
	//Get all trails for any given Entity
	public JSONObject GetAllTrailsForAnEntity(String entityId) {
		
		//An entity can be a userId, trailId, locationId etc.. 
		//THIS IS BAD AND NEEDS TO BE CHANGED AT A LATER DATE
		this.entityId = entityId;
		requestCode = "791";
		new ClientServiceProxy().execute();
		return requestedData;
	}
	
	//Get all crumbs for a specified user
	public JSONObject GetAllCrumbsForUser(String userId) {
		entityId = userId;
		requestCode = "792";
		this.value = new ArrayList<NameValuePair>();
		value.add(new BasicNameValuePair("trailId", userId));
		new ClientServiceProxy().execute();
		return requestedData;
	}
	
	// Get all crumbs for a specified Trail
	public JSONObject GetAllCrumbsForTrail(String trailId) {
		entityId = trailId;		
		this.value = new ArrayList<NameValuePair>();
		value.add(new BasicNameValuePair("trailId", trailId));
		requestCode = "792";
		new ClientServiceProxy().execute();
		return requestedData;
	}
	
	public void AddCrumb(String title, String description, String userId, String media) {
		this.title = title;
		this.description = description;
		this.entityId = userId;
		this.value = new ArrayList<NameValuePair>();

		value.add(new BasicNameValuePair("Title", this.title));
        value.add(new BasicNameValuePair("userId",this.entityId));
        value.add(new BasicNameValuePair("Description",this.description));
        value.add(new BasicNameValuePair("Photo", media));
        
        //DEBUG
		System.out.println("Addding crumb client side");
		requestCode = "320";
		
		//Add shit
		new ClientServiceProxy().execute();
		
	}
	
	
	public void RequestPhotoForCrumb(String id) {
		requestCode = "790";
		this.value = new ArrayList<NameValuePair>();
		this.crumbId = id;
		value.add(new BasicNameValuePair("crumbId", id));
		
		//Add shit
		new ClientServiceProxy().execute();
	}
	
	/*
	 * Add a Trail for a user (based on given Id).
	 * This just sends data to the server, requesting a trail to be made
	 */
	public void AddTrail(String title, String description, String userId) {
		//Create our NameValuePair which we store the values we want to post to the server
		this.value = new ArrayList<NameValuePair>();
		
		value.add(new BasicNameValuePair("TrailName", title));
		value.add(new BasicNameValuePair("Description", description));
		value.add(new BasicNameValuePair("userId", userId));
		
		//Debug
		System.out.println("Adding crumb client side");
		requestCode = "310";
		
		//Add trail by running async server call
		new ClientServiceProxy().execute();	
	}
	
	//Add a crumb to a trail
	public void AddCrumbToTrail(String title, 
							String description, 
							String userId, 
							String trailId, 
							String crumbMedia,
							String latitude,
							String longitude) {
		
		this.value = new ArrayList<NameValuePair>();		
		value.add(new BasicNameValuePair("Latitude", String.valueOf(latitude)));
	    value.add(new BasicNameValuePair("Longitude", String.valueOf(longitude)));
		value.add(new BasicNameValuePair("Title", title));
		value.add(new BasicNameValuePair("Description", description));
		value.add(new BasicNameValuePair("userId", userId));
		value.add(new BasicNameValuePair("trailId", trailId));
		value.add(new BasicNameValuePair("crumbMedia", crumbMedia));
		
		//Debug
		System.out.println("Adding crumb to trail client side");
		requestCode = "320";
		
		//Add trail by running async server call
		new ClientServiceProxy().execute();	
	}
	
	/* Our private async class that we use to get our data. This is a private class inside 
	   * the public class because it needs to manipulate private variables inside the MainPage
	   * class
	   * ====================================================================================
	   * NEEDS A REFACTOR!!!!!!!!!!!!!!!!!!!!
	   * -------------------------------------
	   * want to keep this as one class, but call different methods, generate different urls etc.
	   * This class should not do any processing - just do the retrieval and hand back the data. 
	   * There should be methods in the public class to do the rest
	   */
		private class ClientServiceProxy extends AsyncTask <JSONObject, Void, JSONObject>{

			//The server request is done here
			@Override
			protected JSONObject doInBackground(JSONObject... arg0) { 
				JSONObject jsonObject = new JSONObject();
		    	JSONObject json = null;
		    	if (requestCode.charAt(0) == '3') {
		    		//add data
		    		try {
			    		//Debug
			    		System.out.println("getting info for app");
			    		//We are using http to access our web server
			    	    DefaultHttpClient httpClient = new DefaultHttpClient();
			    	    
			    	    //This is the restful request. Parameters passed in the url
			    	    String urlString = LoadBalancer.RequestServerAddress()+"/breadcrumbs/rest/login/getallTrailsForAUser/0";
			    	    urlString = urlString.replaceAll(" ", "%20");
			    	    URI url = new URI(urlString);
			    	    HttpPost httpPost = new HttpPost(url);
			    	    httpPost.setEntity(new UrlEncodedFormEntity(value));  
			    	    HttpResponse httpResponse = httpClient.execute(httpPost);
			    	    
			    	    //No fucking clue what this magic does
			    	    HttpEntity httpEntity = httpResponse.getEntity();
			    	    String stringResponse = EntityUtils.toString(httpEntity);
			    	    
			    	    //Turn our JSON string into actual JSON
			    	    //json = new JSONObject(stringResponse);
			    	    
			    	    //Just some debug shit
			    	    //System.out.println("Here it is" + json.get("Node0").toString());
			    	}
				    	
			    	catch (Exception ex) {
			    		ex.printStackTrace();
			    	}

		    	}
		    	else {
		    		
				try {
		    		//Debug
		    		System.out.println("getting info for app");

		    		//We are using http to access our web server
		    	    DefaultHttpClient httpClient = new DefaultHttpClient();
		    	    
		    	    //This is the restful request. Parameters passed in the url
		    	    URI url = new URI(LoadBalancer.RequestServerAddress()+"/breadcrumbs/rest/login/getallTrailsForAUser/0");
					HttpGet httpGet = new HttpGet(url);
		    	    HttpResponse httpResponse = httpClient.execute(httpGet);
		    	    
		    	    //No fucking clue what this magic does
		    	    HttpEntity httpEntity = httpResponse.getEntity();
		    	    String stringResponse = EntityUtils.toString(httpEntity);
		    	    
		    	    //Turn our JSON string into actual JSON
		    	    json = new JSONObject(stringResponse);		    	    
		    	}
			    	
		    	catch (Exception ex) {
		    		System.out.println("Getting user data threw threw exception: "+ex);
		    		}
		    	}
		    	System.out.println("returning this: " + json.toString());
		    	return json;
			}
			
			@Override
			protected void onPostExecute(JSONObject result) {
				//give us that shit
				requestedData = result;
				if (requestCode.charAt(0)!= '3') {
					 System.out.println("Retrieved this data: "+ result);
					 //This should use a parent class (not mainPage) and notify every class after its done shit
					 fragment.Notify(result);
					 //cached our data for later use
					 cachedData = new JSONObject(); //Needed?
					 cachedData = result;
				}

				
			}	
		}
	
}
