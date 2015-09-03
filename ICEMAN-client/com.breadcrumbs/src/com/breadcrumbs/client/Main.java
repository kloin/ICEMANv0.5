package com.breadcrumbs.client;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.ServiceProxy.AsyncDataRetrieval;
import com.breadcrumbs.caching.GlobalContainer;
import com.breadcrumbs.database.DatabaseController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;

/*
 * This is the login page. This handles new user sign-ups, saving to the com.breadcrumbs.database,
 * and logging a user in. A user can skip this page by clicking "Remember Me".
 */
public class Main extends AppCompatActivity {
	private DatabaseController dbc;
    private GlobalContainer gc;
	private SQLiteDatabase dbwrite;
	private Context context;
	private AsyncDataRetrieval serviceProxy;
	private String userName;
	private String lname;
	private String pin;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    // Just call the constructor. The contoller has an oncreate method that should create a 
	    // userDb com.breadcrumbs.database if there is none there (first time install etc..).
		setContentView(R.layout.new_user_page);
	    context =  this;
        dbc = new DatabaseController(context);
        gc = GlobalContainer.GetContainerInstance();
        startLogin();
	}

	/*
	 * Decide what page to display depending on whether the user has an account,
	 * and set up the appropriate handlers.
	 */
	private void startLogin() {
		boolean autoLogin = true; //To be completed
		if (userHasPreviouslyLoggedIn()) {
    	    setContentView(R.layout.remember_me_login);
            //Check that userId exists, else we need to get it out of the database

    	    if(autoLogin) {
    			Intent myIntent = new Intent();
    			myIntent.setClassName("com.breadcrumbs.client", "com.breadcrumbs.client.BaseViewModel");
    			startActivity(myIntent);

    		}

            handleLogin();


        } else {       	
        	// Show the new user page, and set up the listeners to handle to process

    	    handleSignUp();
        }
	}
	
	//Check if a user already has an account.
	private boolean userHasPreviouslyLoggedIn() {
		//Check for the first id that would be there (needs to be changed)
		Cursor cursor = dbc.getReadableDatabase().rawQuery("SELECT * FROM users", null);
        Cursor cursor1 = dbc.getReadableDatabase().rawQuery("SELECT * from trailPoints", null);
		//IF present, we have a user.
		 if(cursor.getCount()<1) {

             cursor.close();
             return false;
         }
        cursor.moveToFirst();
        //This is our id we want.
        String id = Integer.toString(cursor.getInt(1));
        //Set the id so we can use it in the app.
        gc.SetUserId(id);
		return true;
	}

	/*
	 * Listener for the sign up button.
	 */
	private void handleSignUp() {
		ImageButton signUpButton = (ImageButton) findViewById(R.id.signUpButton);
		signUpButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				processUserSignUp();
			}
		});
	}
	
	/*
	 * Process a users sign up form completion.
	 */
	private void processUserSignUp() {
		//Add a user using the details
		
		EditText fnameInput = (EditText) findViewById(R.id.firstNameInput);
		userName = fnameInput.getText().toString();
		
		EditText pinInput = (EditText) findViewById(R.id.passCode);
		pin = pinInput.getText().toString();
		
		//Construct our url
		String url = MessageFormat.format("{0}/rest/login/CreateNewUser/{1}/{2}/{3}/{4}",
				LoadBalancer.RequestServerAddress(),
				userName,
				pin,
				"0",
				"M");

        url = url.replaceAll(" ", "%20");
        serviceProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
			
			/*
			 * Override for the onFinished stuff - needs to be tested
			 */
			@Override
			public void onFinished(String result) {
				//Save to the com.breadcrumbs.database once we have finshed saving
				saveUserBaseData(userName, pin, result);
			}
		});
		
		serviceProxy.execute();

        Intent myIntent = new Intent();
        myIntent.setClassName("com.breadcrumbs.client", "com.breadcrumbs.client.BaseViewModel");
        startActivity(myIntent);
	}
	
	//Handler for when login is clicked.
	private void handleLogin() {

		ImageButton loginButton = (ImageButton) findViewById(R.id.authButton);
		loginButton.setOnClickListener( new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		     EditText pword = (EditText) findViewById(R.id.editText1);
		     if (pword.getText().toString().equals(getUserPassword())) {
		    	 Toast.makeText(context, "Logging in...", Toast.LENGTH_LONG).show();
		    	 Intent myIntent = new Intent();
				 myIntent.setClassName("com.breadcrumbs.client", "com.breadcrumbs.client.BaseViewModel");
				 startActivity(myIntent);
		     	} else {
		     		Toast.makeText(context, "Incorrect Password!", Toast.LENGTH_LONG).show();
		     	}
		    }
		});
	}

	/*
	 * Save a new user, based on its base data - names, pin.
	 */
	private void saveUserBaseData(String userName, String pin, String userId) {
        // Save our id for later use. Need to also be able to find this if preferences are cleared.
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("USERID", userId).commit();
        System.out.println("UserId is: " + userId);
        dbwrite = dbc.getWritableDatabase();
		ContentValues newValues = new ContentValues();
        gc.SetUserId(userId);
		newValues.put("userid", userId);
		newValues.put("username", userName);
        newValues.put("pin", pin);

        // Insert the row into your table
        dbwrite.insert("users", null, newValues);
        Toast.makeText(context, "Logging in...", Toast.LENGTH_LONG).show();

	}
	
	/*
	 * Check the password is correct.
	 */
	private String getUserPassword() {
       // fetchCursor();
		Cursor cursor = dbc.getReadableDatabase().rawQuery("SELECT * FROM users", null);//query("users", null, "userid=?", new String[]{"0"}, null, null, null);

        cursor.moveToFirst();
		String pin = cursor.getString(5);
		return pin;
	}

	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
     
}

	

