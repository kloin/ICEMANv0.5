package com.breadcrumbs.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.breadcrumbs.ServiceProxy.MasterProxy;

import org.json.JSONObject;
/*
 * This is the model class for the trail page (trail_activity).
 * I have used a FragmentActivity rather than an Activity because it needs
 * to pass an instance of the page to the GetProxyInstance() call
 * 
 * NOTE: 13/02/2015
 * This class is not used, however I am going to leave it in here as it is somewhat along the lines 
 * of what I want/need to do for the trail class.
 */
public class TrailActivity extends FragmentMaster {
	private MasterProxy clientRequestProxy;
	private LinearLayout storyBoard;
	//public final static String EXTRA_MESSAGE = "com.breadcrumbs.client.MESSAGE";
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		//Start getting the data at the very start.
        clientRequestProxy = MasterProxy.GetProxyInstance(this);
        setContentView(R.layout.trail_activity);
        //Set up bundle to retrieve extras from, and trail to set
        Bundle extras;
        String trailId;
        extras = getIntent().getExtras();
        
        //Check if extras found anything. If not, let the console/user know
        if(extras == null) {
        	System.out.println("Cant find the id for a trail");
            trailId = null;
        } else {
            trailId = extras.getString("message");
        }
        
        //Get our data
        System.out.println(trailId);
        clientRequestProxy.GetAllCrumbsForTrail(trailId);
        //Add our data to view        
    }
	
	@Override
	public void Notify(JSONObject jsonResponse) {
    	System.out.println("App has been given this data: ");
    	//Try loading our data
		storyBoard = (LinearLayout)findViewById(R.id.base);
		LoadCrumbs(jsonResponse);
	}
	
	//Create an item (e.g a crumb or a trail) Called sbo for ease of typing
    private void createStoryObject(int index, String storyTitle, String storyDescription, String trailId) {
    	//Infate that shit
    	LayoutInflater inflater = getLayoutInflater();
    	//Create a base object, which we will attatch to the storyboard, and later manipulate
    	View sbo = inflater.inflate(R.layout.single_crumb_template, null);
    	TextView title = (TextView) sbo.findViewById(R.id.Title);
    	//TextView description = (TextView) sbo.findViewById(R.id.Description);
    	title.setText(storyTitle);
    	//description.setText(storyDescription);
    	//description.setTag(trailId);
    	storyBoard.addView(sbo);
    	
    	//set click listener to load map page. Needs to change so that it relates to the correct id.
    	title.setOnClickListener(new View.OnClickListener() {
    	    @Override
    	    public void onClick(View v) {
    	    	//do shit
    	    }
    	});
    }
   	
	//Load up all the crumbs
	public void LoadCrumbs(JSONObject result) {
		//lets do it
		try {
   	 		int i = 0;
   	 		while (i < result.length()) {
        		System.out.println(result.length());
    			//Iterate through the list
				JSONObject singleNode = new JSONObject(result.get("Node" + i).toString());
				System.out.println("singleNode: " + singleNode);
				
				//Get details.
				String desc = singleNode.getString("Description"); //Is description just going to be a dateTime?
				String tit = singleNode.getString("Title");
				//We need to know this to get crumbs/data about the trail.
				//We cannot pull down all the crumbs for every trail on load.
				String id = singleNode.getString("crumbId");
            	createStoryObject(i, tit, desc, id);
        		i += 1;
        	}
		} catch(Exception ex) {
			//exception occured creating data
			System.out.println("Exception occured creating data: ");
			ex.printStackTrace();
		}		
	}

}
