package com.teamunemployment.breadcrumbs.client;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnProfileListener;
import com.teamunemployment.breadcrumbs.ActivityRecognition.ActivityHandler;
import com.teamunemployment.breadcrumbs.Facebook.AccountManager;
import com.teamunemployment.breadcrumbs.GCM.RegistrationIntentService;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.Preferences.Preferences;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.client.DialogWindows.DatePickerDialog;
import com.teamunemployment.breadcrumbs.client.DialogWindows.SignUpDialog;
import com.teamunemployment.breadcrumbs.database.DatabaseController;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import com.facebook.FacebookSdk;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * This is the login page. This handles new user sign-ups, saving to the com.teamunemployment.breadcrumbs.database,
 * and logging a user in. A user can skip this page by clicking "Remember Me".
 */
public class Main extends AppCompatActivity {
	private DatabaseController dbc;
    private GlobalContainer gc;
	private SQLiteDatabase dbwrite;
	private Activity context;
	private AsyncDataRetrieval serviceProxy;
	private String userName;
	private String lname;
	private String pin;
	private String TAG = "MAIN";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	private SimpleFacebook mSimpleFacebook;
	private OnLoginListener onLoginListener;
	private OnProfileListener mOnProfileListener;
	private BroadcastReceiver registrationBroadcastReceiver;
	private static final String appId = "668335369892920";

	private Permission[] permissions = new Permission[] {
			Permission.USER_ABOUT_ME
	};

	Profile.Properties properties = new Profile.Properties.Builder()
			.add(Profile.Properties.ID)
			.add(Profile.Properties.FIRST_NAME)
			.add(Profile.Properties.EMAIL)
			.build();

	private GoogleApiClient googleApiClient;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
				.setAppId(appId)
				.setNamespace("com.teamunemployment.breadcrumbs")
				.setPermissions(permissions)
				.setAppSecret("535359abe98d1dbf91e9c1a9227d1cfd")
				.build();
		SimpleFacebook.setConfiguration(configuration);

		// Just call the constructor. The contoller has an oncreate method that should create a
	    // userDb com.teamunemployment.breadcrumbs.database if there is none there (first time install etc..).
		setContentView(R.layout.new_user_page);
		context =  this;
        dbc = new DatabaseController(context);
        gc = GlobalContainer.GetContainerInstance();
        startLogin();
		facebookLoginClickHandler();
		newAccountClickHandler();
		standardAccountLoginHandler();
		forgotDetailsClickHandler();
		setUpSimpleFacebook();
		setUpProfileListener();
	}

	// Listener for fetching facebook profile data
	private void setUpProfileListener() {
		mOnProfileListener = new OnProfileListener() {
			@Override
			public void onComplete(Profile profile) {
				Log.i(TAG, "My name is : " + profile.getFirstName());
				Log.i(TAG, "My Id is:" + profile.getId());
				Log.i(TAG, "My Email is: " + profile.getEmail());
				handleFacebookRegistration(profile.getId(), profile.getFirstName());
				// Check if the user has already created a profile
			}

			@Override
			public void onFail(String reason) {
				Log.e(TAG, "Facebook profile setup failed because: " + reason);
			}

    /*
     * You can override other methods here:
     * onThinking(), onFail(String reason), onException(Throwable throwable)
     */
		};
	}

	/*
	 * Decide what page to display depending on whether the user has an account,
	 * and set up the appropriate handlers.
	 */
	private void startLogin() {
		boolean autoLogin = !PreferenceManager.getDefaultSharedPreferences(context).getString("USERID", "-1").equals("-1"); //To be completed
		if (userHasPreviouslyLoggedIn()) {
            //Check that userId exists, else we need to get it out of the database
    	    if(autoLogin) {
    			Intent myIntent = new Intent();
    			myIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Home.HomeActivity");
    			startActivity(myIntent);
    		} else {
				//handleLogin();
			}
        } else {
			setUpGCM();
		}
	}

	private void setUpSimpleFacebook() {
		onLoginListener = new OnLoginListener() {

			@Override
			public void onLogin(String accessToken, List<Permission> acceptedPermissions, List<Permission> declinedPermissions) {
				// change the state of the button or do whatever you want
				Log.i(TAG, "Logged in");
				mSimpleFacebook.getProfile(properties, mOnProfileListener);
			}

			@Override
			public void onCancel() {
				// user canceled the dialog
			}

			@Override
			public void onFail(String reason) {
				Log.e(TAG, "Failed to log in with simple facebook. Reason: " + reason);
				// failed to login
			}

			@Override
			public void onException(Throwable throwable) {
				throwable.printStackTrace();
				// exception from facebook
			}

		};
	}

	private void standardAccountLoginHandler() {
		TextView login_button = (TextView) findViewById(R.id.login_button);
		login_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doSimpleLogin();
			}
		});
	}

	private void doSimpleLogin() {
		final EditText userName = (EditText) findViewById(R.id.userNameLogin);
		Editable userNameEditable = userName.getText();
		if (userNameEditable.toString().length() < 2 || userNameEditable.toString().contains(" ")) {
			Toast.makeText(context, "Enter a valid username. A user name must be at least 3 characters and cannot contain spaces", Toast.LENGTH_LONG).show();
			return;
		}

		EditText pin = (EditText) findViewById(R.id.password);
		Editable pinEditable = pin.getText();
		if (pinEditable.toString().length() != 4) {
			Toast.makeText(context, "4 digit pin required", Toast.LENGTH_LONG).show();
			return;
		}

		String pinString = pinEditable.toString();
		final String userNameString = userNameEditable.toString();

		String loginUrl = LoadBalancer.RequestServerAddress() + "/rest/login/AttemptToLogInUser/"+userNameString +"/"+pinString;
		AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(loginUrl, new AsyncDataRetrieval.RequestListener() {
			@Override
			public void onFinished(String result) {
				if (result.startsWith("E") || result.startsWith("F") || result.startsWith("P")) {
					Toast.makeText(context, "Failed to login: " + result, Toast.LENGTH_LONG).show();

					return;
				}
				// We should save the user to the database if they are not in there.
				Intent myIntent = new Intent();
				PreferenceManager.getDefaultSharedPreferences(context).edit().putString("USERID", result).commit();
				// Need to get TrailId
				PreferenceManager.getDefaultSharedPreferences(context).edit().putString("USERNAME", userNameString).commit();
				myIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.SplashScreen");
				startActivity(myIntent);
			}
		}, context);
		asyncDataRetrieval.execute();
	}

	private void facebookLoginClickHandler() {
		Button loginButton = (Button) findViewById(R.id.facebook_login_button);
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onFBLogin();
			}
		});
	}

	private void onFBLogin() {
		LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "user_photos", "public_profile"));
		mSimpleFacebook.login(onLoginListener);
		/*LoginManager.getInstance().registerCallback(callbackManager,
				new FacebookCallback<LoginResult>() {
					@Override
					public void onSuccess(LoginResult loginResult) {
						System.out.println("Success");
						GraphRequest.newMeRequest(
								loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
									@Override
									public void onCompleted(JSONObject json, GraphResponse response) {
										//need to record that the event was sent
										if (response.getError() != null) {
											// handle error
											System.out.println("ERROR");
										} else {
											System.out.println("Success");
											try {
												String jsonresult = String.valueOf(json);
												System.out.println("JSON Result" + jsonresult);

												//String str_email = json.getString("email");
												String str_id = json.getString("id");
												if (PreferenceManager.getDefaultSharedPreferences(context).getString("FACEBOOK_REGISTRATION_ID", "-1").equals("-1")) {
													PreferenceManager.getDefaultSharedPreferences(context).edit().putString("FACEBOOK_REGISTRATION_ID", str_id).commit();
													String name = json.getString("name");
													handleFacebookRegistration(str_id, name);
												} else {
													//We have caught the bounce. I am not sure that this matters later
												}

												//String str_firstname = json.getString("first_name");
												//String str_lastname = json.getString("last_name");
											} catch (JSONException e) {
												e.printStackTrace();
											}
										}
									}
								}).executeAsync();
					}

					@Override
					public void onCancel() {
						Log.d("CANCEL", "On cancel");
					}

					@Override
					public void onError(FacebookException error) {
						Log.d("ERROR", error.toString());
					}
				});*/
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mSimpleFacebook.onActivityResult(requestCode, resultCode, data);
	}
	
	//Check if a user already has an account.
	private boolean userHasPreviouslyLoggedIn() {
		//Check for the first id that would be there (needs to be changed)
		try {
			Cursor cursor = dbc.getReadableDatabase().rawQuery("SELECT * FROM users", null);
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
		} catch (SQLiteCantOpenDatabaseException ex) {
			return false;
		}

	}

	private void newAccountClickHandler() {
		TextView signUpTextButton = (TextView) findViewById(R.id.create_an_account);
		signUpTextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Show create account dialog.
				handleNewAccount();
			}
		});
	}

	private void forgotDetailsClickHandler() {
		TextView forgotDetailsTextButton = (TextView) findViewById(R.id.password_forgotten);
		forgotDetailsTextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showForgottenDetailsPopup();
			}
		});
	}

	private void handleNewAccount() {
		//popUp Dialog
		showDialog();
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
		int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (apiAvailability.isUserResolvableError(resultCode)) {
				apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
						.show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(registrationBroadcastReceiver,
				new IntentFilter(Preferences.REGISTRATION_COMPLETE));

		// Logs 'install' and 'app activate' App events
		mSimpleFacebook = SimpleFacebook.getInstance(this);
		//AppEventsLogger.activateApp(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(registrationBroadcastReceiver);
		// Logs 'app deactivate' App Event.
		//AppEventsLogger.deactivateApp(this);
	}

	private void setUpGCM() {

		registrationBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				SharedPreferences sharedPreferences =
						PreferenceManager.getDefaultSharedPreferences(context);
				boolean sentToken = sharedPreferences
						.getBoolean(Preferences.SENT_TOKEN_TO_SERVER, false);
				if (sentToken) {
					Log.d(TAG, "Successfully sent token");
				} else {
					Log.d(TAG, "Failed to send token");
				}
			}
		};

		if (checkPlayServices()) {
			// Start IntentService to register this application with GCM.
			Intent intent = new Intent(this, RegistrationIntentService.class);
			startService(intent);
		}
	}
	
	/*
	 * Process a users sign up form completion.
	 */
	private void processUserSignUp(final String userName, final String pin, final String email) {

		String token =  PreferenceManager.getDefaultSharedPreferences(this).getString("TOKEN", "-1");
		//Construct our url
		String url = MessageFormat.format("{0}/rest/login/CreateNewUser/{1}/{2}/{3}/{4}/{5}/{6}/{7}",
				LoadBalancer.RequestServerAddress(),
				userName,
				pin,
				"0",
				"M",
				token,
				email,
				"-1");
		// Need to add email here once server has been redeployed.

        url = url.replaceAll(" ", "%20");
        serviceProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
			
			/*
			 * Override for the onFinished stuff - needs to be tested
			 */
			@Override
			public void onFinished(String result) {
				if (result!= null && result.length() < 50) { // Error messages are always longer that 50 characters, but if all going well it will return an id, which will be less than 50
					saveUserBaseData(userName, pin, result);
					Intent myIntent = new Intent();
					myIntent.putExtra("UserName", userName);
					PreferenceManager.getDefaultSharedPreferences(context).edit().putString("USERNAME", userName).commit();
					myIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Home.HomeActivity");
					startActivity(myIntent);
				}
			}
		}, context);
		
		serviceProxy.execute();


	}

	/*
	 * Process a users sign up form completion.
	 */
	private void processUserSignUpForFacebookAccount(final String userName, final String facebookId) {

		String token =  PreferenceManager.getDefaultSharedPreferences(this).getString("TOKEN", "-1");
		if (token.equals("-1")) {
			// do I Fail here? 100% need the token otherwise there will never be any notifications for that user.
			Toast.makeText(context, "Login failed due to missing GCM token. If issue persists contact support.", Toast.LENGTH_LONG).show();
			// Shitty message i know but If this happens we are really fucked. It should not happen unless we proceed without a proper internet conenction/or api key is wrong.
			return;
		}
		//Construct our url
		String url = MessageFormat.format("{0}/rest/login/CreateNewUser/{1}/{2}/{3}/{4}/{5}/{6}/{7}",
				LoadBalancer.RequestServerAddress(),
				userName,
				"0",
				"0",
				"M",
				token,
				"0",
				facebookId);

		url = url.replaceAll(" ", "%20");
		Log.d(TAG, "Attempting to create account using facebook: URL - " + url);
		serviceProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {

			/*
			 * Override for the onFinished stuff - needs to be tested
			 */
			@Override
			public void onFinished(String result) {
				//Save to the com.teamunemployment.breadcrumbs.database once we have finshed saving
				/*
				Have put this inside the on finished, because starting the app without a success here is catastrophic
				 */
				Log.d("FBLOGIN", "Create account request using facebook info responded with result: " + result);
				if (result != null && result.length() < 40) {
					saveUserBaseData(userName, " ", result);
					Intent myIntent = new Intent();
					myIntent.putExtra("UserName", userName);
					PreferenceManager.getDefaultSharedPreferences(context).edit().putString("USERNAME", userName).commit();
					myIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Home.HomeActivity");
					startActivity(myIntent);
				}
			}
		}, context);

		serviceProxy.execute();
	}

	private void grabFacebookProfilePicture() {
		AccountManager accountManager = new AccountManager(context);
		accountManager.GetUserProfilePicture(PreferenceManager.getDefaultSharedPreferences(context).getString("FACEBOOK_REGISTRATION_ID", "-1"));
	}
	
	//Handler for when login is clicked.
	private void handleLogin() {
		ImageButton loginButton = (ImageButton) findViewById(R.id.authButton);
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText pword = (EditText) findViewById(R.id.editText1);
				if (pword.getText().toString().equals(getUserPassword())) {
					Toast.makeText(context, "Logging in...", Toast.LENGTH_LONG).show();
					Intent myIntent = new Intent();
					myIntent.putExtra("UserName", userName);
					myIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.BaseViewModel");
					startActivity(myIntent);
				} else {
					Toast.makeText(context, "Incorrect Password!", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	/*
		 Create a user. Give it a flag so that we know that it is a facebook user. When we log in using facebook
		 we want to take the id and find our actual userId with it. If we cannot find it, we create a new user
		 with the userName as the name, pin as blank (which should stop people logging in normally to facebook
		 accounts because you can not log in without 4 digit pin entered.
		 We have no email address.
		 We have gcm id
		 we have everything we need
  */
	private void handleFacebookRegistration(final String facebookRegistrationId, final String name) {
		// First check if we have a facebook account already existing.
		String url = LoadBalancer.RequestServerAddress() + "/rest/User/FindUserByFacebookId/"+facebookRegistrationId;
		AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
			@Override
			public void onFinished(String result) {

				if (result == null) {
					Toast.makeText(context, "Failed to contact server.", Toast.LENGTH_LONG).show();
					return;
				}
				// Need to create an account if there is not one, otherwise log in with the result, which will be the userId.
				if (result.equals("404")) {
					// Not found, create account
					Log.d(TAG, "Could not find user with facebook Id : " + facebookRegistrationId);
					processUserSignUpForFacebookAccount(name, facebookRegistrationId);
					Log.d(TAG, "Grabbing facebook profile picture and setting it as profile");
					grabFacebookProfilePicture();
				} else if (result.equals("500")) {
					// Shits fucked, we need to fix this.
					Log.e(TAG, "Finding facebook user failed critically. Facebook Id : " + facebookRegistrationId);
					Toast.makeText(context, "Shits fucked", Toast.LENGTH_LONG).show();
				} else {
					String userId = result;
					Log.d(TAG, "Found user with facebook Id : " + facebookRegistrationId + ". Logging in now with userId :" + userId);
					//Log in and set userId, TrailId etc
					boolean userIsInDB = checkUserExists(userId);
					if (!userIsInDB) {
						saveUserBaseData(name, " ", userId);
					}
					Intent myIntent = new Intent();
					myIntent.putExtra("UserName", name);
					PreferenceManager.getDefaultSharedPreferences(context).edit().putString("USERID", result).commit();
					// Need to get TrailId
					PreferenceManager.getDefaultSharedPreferences(context).edit().putString("USERNAME", name).commit();
					myIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.SplashScreen");
					startActivity(myIntent);
					// Set our userId and proceed.
				}
			}
		}, context);

		asyncDataRetrieval.execute();
	}

	private boolean checkUserExists(String userId){
		return dbc.CheckUserExists(userId);
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

	private void showDialog() {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.signup_dialog);
		// if button is clicked, close the custom dialog
		final TextView createAccount = (TextView) dialog.findViewById(R.id.signUp_dialog_button);
		createAccount.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				 // Set up our gcm.
				EditText userName = (EditText) dialog.findViewById(R.id.user_name);
				EditText pin = (EditText) dialog.findViewById(R.id.password);
				EditText email = (EditText) dialog.findViewById(R.id.email);
				Editable emailEditable = email.getText();
				Editable pinEditable = pin.getText();
				Editable userNameEditable = userName.getText();
				if (userNameEditable != null && userNameEditable.toString().length() > 2 && !userNameEditable.toString().contains(" ")) {
					if (pinEditable!=null && pinEditable.toString().length() == 4) {
						if (emailEditable != null && emailEditable.toString().contains("@")) {
							processUserSignUp(userNameEditable.toString(), pinEditable.toString(), emailEditable.toString());
						} else {
							Toast.makeText(context, "Please enter a valid email address.",Toast.LENGTH_LONG).show();
						}
					} else {
						Toast.makeText(context, "Pin must be 4 digits", Toast.LENGTH_LONG).show();
					}
				}	else {
					Toast.makeText(context, "Enter a valid username. A user name must be at least 3 characters and cannot contain spaces", Toast.LENGTH_LONG).show();
				}
			}
		});
		dialog.show();
	}



	private void showForgottenDetailsPopup() {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.forgot_details);
		TextView submitTextButton = (TextView) dialog.findViewById(R.id.send_details_button);
		submitTextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Send an email with the details
				// dismiss dialog
				EditText email = (EditText) dialog.findViewById(R.id.forgotten_details_email);
				Editable emailEditable = email.getText();
				if (emailEditable != null && emailEditable.toString().contains("@")) {
					HTTPRequestHandler simpleHttpRequest = new HTTPRequestHandler();
					String emailUrl = LoadBalancer.RequestServerAddress() + "/rest/User/FetchUserDetails/" + emailEditable.toString();
					simpleHttpRequest.SendSimpleHttpRequest(emailUrl, context);
					dialog.dismiss();
					Toast.makeText(context, "Details sent successfully", Toast.LENGTH_LONG).show();
				}
			}
		});
		dialog.show();
	}

	/**
	 *
	 */

}

	

