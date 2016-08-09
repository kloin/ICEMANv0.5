package com.teamunemployment.breadcrumbs.client.NavMenu.Profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer.C;
import com.squareup.picasso.Picasso;
import com.teamunemployment.breadcrumbs.CustomElements.FancyFollow;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UpdateViewElementWithProperty;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.caching.TextCaching;
import com.teamunemployment.breadcrumbs.client.DialogWindows.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by Josiah Kendall on 4/21/2015.
 */
public class ProfilePageViewer extends AppCompatActivity implements DatePickerDialog.DatePickerDialogListener {
    private String userId;
    private Activity myContext;
    private boolean isDirty = false;
    private TextView ageEditText;
    static final int PICK_PROFILE_REQUEST = 1;
    private boolean isOwnProfile = false;
    private TextCaching textCaching;
    private String followKey;
    private String TAG = "PROFILE";
    private String name;
    private boolean refreshed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myContext = this;
        setContentView(R.layout.profile_screen);
        textCaching = new TextCaching(this);
        Bundle extras = getIntent().getExtras();
        // Set up User
        setUpUser(extras);
        // Set up user interaction - click handlers etc depending on the userId.
        setUpFollowing();
        setUpCollapsableToolbar(name);
        addMyProfileName(name);
        loadDetails();
        setHeaderPic();
        loadTrailsIntoCard();
        setEditButtonVisibility();
        setButtonListeners();
    }

    // Toolbar button listeners - save and back
    private void setButtonListeners() {
        // Save button listener.

        // back button listener
        ImageButton button = (ImageButton) findViewById(R.id.profile_back_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Quit this screen
            }
        });
    }


    // Set up all the user details, like fetching Name and Id
    private void setUpUser(Bundle extras) {
        Log.d(TAG, "Begin setting up user details");
        userId = extras.getString("userId");
        if (userId != null && userId.equals(PreferenceManager.getDefaultSharedPreferences(this).getString("USERID", "-1"))) {
            isOwnProfile = true;
        }

        // Users name
        name = extras.getString("name");
        Log.d(TAG, "Found Name: " + name);
        if (userId == null) {
            Log.d(TAG, "UserId is null, we are fucked");
        } else if (name == null) {
            // Re fetch name.
            Log.d(TAG, "Name was null - re fetching from server using userId");
            String url = LoadBalancer.RequestServerAddress() + "/rest/login/GetPropertyFromNode/" +
                    userId + "/Username";
            AsyncDataRetrieval fetchDescription = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
                @Override
                public void onFinished(String result) {
                    if (result != null && !result.isEmpty()) {
                        setUpCollapsableToolbar(result);
                        addMyProfileName(result);
                        Log.d(TAG, "Setting users name: " + result);
                    }
                }
            }, myContext);
            fetchDescription.execute();
        }
    }

    private void addMyProfileName(String name) {
        TextView myProfile = (TextView) findViewById(R.id.my_profile_name);
        myProfile.setText(name);


    }

    private void setEditButtonVisibility() {
        Log.d(TAG, "Setting up edit button visiblity");
        if (!isOwnProfile) {
            Log.d(TAG, "Not our local user - need to hide the edit/save buttons");
            // Hide the edsit button, and the Save button.
            TextView editButton = (TextView) findViewById(R.id.toggle_edit_profile);
            editButton.setVisibility(View.GONE);
        }
    }

    // Set the height and name field for our toolbar heading thingy that slides up.
    private void setUpCollapsableToolbar(String name) {
        Log.d(TAG, "Setting up the collapsable toolbar");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsable_toolbar_holder);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.Gone);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setTitle(name);

        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout_profile);
        ViewGroup.LayoutParams layoutParams = appBarLayout.getLayoutParams();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int widthDouble = displaymetrics.widthPixels;
        layoutParams.height = widthDouble;
        appBarLayout.setLayoutParams(layoutParams);
        setUpToggleEditModeListener();
    }

    // Load the user bio details
    private void loadDetails() {
        //load up bio and date etc
        EditText bio = (EditText) findViewById(R.id.bio_edit_text);
        TextView ageDisplay = (TextView) findViewById(R.id.age);
        TextView ageButton = (TextView) findViewById(R.id.date_picker_launcher);

        if (!userId.equals(PreferenceManager.getDefaultSharedPreferences(myContext).getString("USERID", "-1"))) {
            bio.setEnabled(false);
            ageButton.setVisibility(View.GONE);
        }

        TextView aboutTextView = (TextView) findViewById(R.id.about_uneditable);
        TextView websiteTextView = (TextView) findViewById(R.id.about_uneditable_web);
        EditText webEdit = (EditText) findViewById(R.id.web_edit_text);
        UpdateViewElementWithProperty updater = new UpdateViewElementWithProperty();
        updater.UpdateEditTextElement(bio, userId, "About", myContext);
        updater.UpdateEditTextElement(webEdit, userId, "Web", myContext);
        updater.UpdateTextViewElement(ageDisplay, userId, "Age", myContext);
        updater.UpdateTextViewElement(aboutTextView, userId, "About", myContext);
        updater.UpdateTextViewElement(websiteTextView, userId, "Web", myContext);

    }

    private void setUpFollowing() {
        FancyButton followButton = (FancyButton) findViewById(R.id.follow_button);
        final String currentUser = PreferenceManager.getDefaultSharedPreferences(myContext).getString("USERID", "-1");

        FancyFollow customFollowButton = new FancyFollow(currentUser, userId, followButton, myContext);
        customFollowButton.init();
    }

    private void followUserOnClickHandler(final FancyButton followButton, final String currentUserId) {
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send request to follow user
                Log.d(TAG, "Sending follow request to server");

                if (!currentUserId.equals("-1")) {
                    //followButton.setTextColor(getResources().getColor(R.color.accent));
                    followButton.setText("Following");
                    followButton.setTextColor(Color.WHITE);
                    followButton.setBackgroundColor(getResources().getColor(R.color.ColorPrimary));
                    Log.d(TAG, "User with Id: " + currentUserId + " is following user with ID: " + userId);
                    String followUserUrl = LoadBalancer.RequestServerAddress() + "/rest/User/PinUserForUser/" + currentUserId + "/" + userId;
                    AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(followUserUrl, new AsyncDataRetrieval.RequestListener() {
                        @Override
                        public void onFinished(String result) {
                            // need to check its legit here though
                            Log.d(TAG, "Follow request responded: " + result);
                            textCaching.CacheText(followKey, "Y");
                        }
                    }, myContext);
                    asyncDataRetrieval.execute();
                    unfollowUserOnClickHandler(followButton, currentUserId);
                }
            }
        });
    }

    private void unfollowUserOnClickHandler(final FancyButton followButton, final String currentUserId) {
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentUserId.equals("-1")) {
                    followButton.setText("Follow");
                    followButton.setTextColor(getResources().getColor(R.color.ColorPrimary));
                    followButton.setBackgroundColor(Color.WHITE);
                    String followUserUrl = LoadBalancer.RequestServerAddress() + "/rest/User/UnPinUserForUser/" + currentUserId + "/" + userId;
                    AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(followUserUrl, new AsyncDataRetrieval.RequestListener() {
                        @Override
                        public void onFinished(String result) {
                            // need to check its legit here though
                            textCaching.CacheText(followKey, "N");
                        }
                    }, myContext);
                    asyncDataRetrieval.execute();
                    followUserOnClickHandler(followButton, currentUserId);
                }
            }
        });
    }

    // Save our changes age, gender and Nationality
    private void save() {
        // Just going to save all values, cos fuck it.

        EditText bio = (EditText) findViewById(R.id.bio_edit_text);
        String about = bio.getText().toString();
        //String genderInfo = gender.getText().toString();

        String aboutInfoUrl = LoadBalancer.RequestServerAddress() + "/rest/login/SaveStringPropertyToNode/" + userId + "/About/" + about;
        //String genderInfoUrl = LoadBalancer.RequestServerAddress()+ "/rest/login/SaveStringPropertyToNode/"+userId+"/Sex/"+genderInfo;

        // Do the saving
        HTTPRequestHandler simpleSaver = new HTTPRequestHandler();
        simpleSaver.SendSimpleHttpRequestAndReturnString(aboutInfoUrl, myContext);
    }

    @Override
    public void onBackPressed() {
        // If dirty, save file
        if (isDirty) {
            //save();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        }
        try {
            finish();
        } catch (NullPointerException ex) {
            //Failed due to circular image. COuld be a memory leak here.
            Log.e("PROFILE", "Failed to close profile page safely. Possible Mem leak");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = null;
        if (data != null) {
            bitmap = data.getParcelableExtra("bitmap");
            if (bitmap == null) {
                bitmap = GlobalContainer.GetContainerInstance().GetBitMap();
            }
        }
        // This is the check for when we return with no data. Usually when the user hits the back button
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                refreshed = true;
                ImageView header = (ImageView) findViewById(R.id.headerPicture);
                if (bitmap != null) {
                    header.setImageBitmap(bitmap);
                } else {
                    String id = PreferenceManager.getDefaultSharedPreferences(myContext).getString("COVERPHOTOID", "-1");
                    Picasso.with(myContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/" + id + ".jpg").centerCrop().into(header);
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    // Try to set the profile picture for a user
    private void setHeaderPic() {

        final ImageView header = (ImageView) findViewById(R.id.headerPicture);
        ViewGroup.LayoutParams layoutParams = header.getLayoutParams();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        layoutParams.height = width;
        header.setLayoutParams(layoutParams);
        String coverPhotoId = PreferenceManager.getDefaultSharedPreferences(myContext).getString("COVERPHOTOID", "-1");

        if (!userId.equals(PreferenceManager.getDefaultSharedPreferences(myContext).getString("USERID", "-1"))) {
            TextView textView = (TextView) myContext.findViewById(R.id.profile_select_prompt);
            textView.setVisibility(View.GONE);
            String imageIdUrl = LoadBalancer.RequestServerAddress() + "/rest/login/GetPropertyFromNode/" + userId + "/CoverPhotoId";
            AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(imageIdUrl, new AsyncDataRetrieval.RequestListener() {
                @Override
                public void onFinished(String result) {
                    if (result != null || !result.isEmpty()) {
                        try {
                            Picasso.with(myContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/" + result + ".jpg").centerCrop().into(header);
                        } catch (IllegalArgumentException ex) {
                            Log.d("PROF", "caught glide exception in profile page. Probably due to activity being destroyed before load was finished");
                        }
                    }
                }
            }, myContext);
            asyncDataRetrieval.execute();
        } else {
            TextView textView = (TextView) myContext.findViewById(R.id.profile_select_prompt);
            if (!coverPhotoId.equals("-1")) {
                textView.setVisibility(View.GONE);
            }
            Picasso.with(myContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/" + coverPhotoId + ".jpg").centerCrop().into(header);
            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.ImageChooser.ImageChooserTabWrapper");
                    startActivityForResult(intent, PICK_PROFILE_REQUEST);
                }
            });
        }
    }

    // Used when the user wants to toggle edit mode for their profile - this makes the text views dissapear and the editTexts appear.
    private void setUpToggleEditModeListener() {
        final TextView editTextView = (TextView) findViewById(R.id.toggle_edit_profile);
        editTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is where we toggle
                toggleEditMode(editTextView);
            }
        });
    }

    private void toggleEditMode(TextView editButton) {
        final String EDITMODE = "0";
        final String READONLY = "1";
        // IF tag = 0, it is in edit mode, if it is 1 it is in
        String tag = editButton.getTag().toString();
        if (tag.equals(EDITMODE)) {
            editButton.setText("      SAVE      ");
            editButton.setTag(READONLY);

            TextView descriptionTextView = (TextView) findViewById(R.id.about_uneditable);
            TextView websiteTextView = (TextView) findViewById(R.id.about_uneditable_web);

            descriptionTextView.setVisibility(View.GONE);
            websiteTextView.setVisibility(View.GONE);


            // Set the edit texts visible
            EditText aboutEdit = (EditText) findViewById(R.id.bio_edit_text);
            EditText webEdit = (EditText) findViewById(R.id.web_edit_text);
            aboutEdit.setVisibility(View.VISIBLE);
            webEdit.setVisibility(View.VISIBLE);
            aboutEdit.requestFocus();
            aboutEdit.setEnabled(true);
            webEdit.setEnabled(true);
        } else {
            editButton.setText("EDIT YOUR PROFILE");
            editButton.setTag(EDITMODE);
            TextView descriptionTextView = (TextView) findViewById(R.id.about_uneditable);
            TextView websiteTextView = (TextView) findViewById(R.id.about_uneditable_web);
            descriptionTextView.setVisibility(View.VISIBLE);
            websiteTextView.setVisibility(View.VISIBLE);

            // Set the edit texts visible
            EditText aboutEdit = (EditText) findViewById(R.id.bio_edit_text);
            EditText webEdit = (EditText) findViewById(R.id.web_edit_text);
            descriptionTextView.setText(aboutEdit.getText().toString());
            websiteTextView.setText(webEdit.getText().toString());
            aboutEdit.setVisibility(View.GONE);
            webEdit.setVisibility(View.GONE);
            aboutEdit.setEnabled(false);
            webEdit.setEnabled(false);
            //SAVE HERE
            Editable aboutEditable = aboutEdit.getText();
            Editable webEditable = webEdit.getText();
            String userId = PreferenceManager.getDefaultSharedPreferences(myContext).getString("USERID", "-1");
            HTTPRequestHandler requestHandler = new HTTPRequestHandler();
            if (aboutEditable != null) {
                requestHandler.SaveNodeProperty(userId, "About", aboutEditable.toString(), myContext);
                requestHandler.SaveNodeProperty(userId, "Web", webEditable.toString(), myContext);
            }
        }
    }

    @Override
    public void onDateClick(int day, int month, int year) {
        ageEditText = (TextView) findViewById(R.id.age);
        ageEditText.setText(day + "/" + month + "/" + year);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // Load the trail data to add chips to the
    private void loadTrailsIntoCard() {
        // Load all trail Ids
        String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetAllTrailsForAUser/" + userId;
        url = url.replaceAll(" ", "%20");
        AsyncDataRetrieval clientRequestProxy = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                try {
                    // Get our arrayList for the card adapter
                    JSONObject jsonResult = new JSONObject(result);
                    if (jsonResult != null) {
                        hideProgressBar();
                        beginProcessingTrailsIntoChips(jsonResult);
                    }
                    // Create the adapter, and set it to the recyclerView so that it displays
                    //mAdapter = new HomeCardAdapter(ids, context);
                    //mRecyclerView.setAdapter(mAdapter);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.e("Cards", "Failed to convert String to json");
                }
            }
        }, myContext);
        clientRequestProxy.execute();
    }

    private void hideProgressBar() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.profile_progress_bar);
        progressBar.setVisibility(View.GONE);
    }

    private void beginProcessingTrailsIntoChips(JSONObject jsonResult) throws JSONException {
        Iterator<String> iterator = jsonResult.keys();
        int count = 0;
        while (count < 3 && iterator.hasNext()) {
            count += 1;
            //Get base Details
            JSONObject trail = new JSONObject(jsonResult.get(iterator.next()).toString());
            final int finalCount = count;
            createTrailChip(trail, finalCount);
        }
    }

    // Create the trail chip and add it to the card
    private void createTrailChip(JSONObject resultJSON, int count) throws JSONException {
        String desc = resultJSON.get("Description").toString();
        String title = resultJSON.get("TrailName").toString();
        String coverId = resultJSON.get("CoverPhotoId").toString();
        final String id = resultJSON.get("Id").toString();
        try {
            RelativeLayout parent = null;
            if (count == 1) {
                parent = (RelativeLayout) findViewById(R.id.chip_sub_wrapper0);

                parent.setVisibility(View.VISIBLE);
                TextView header = (TextView) parent.findViewById(R.id.trail_chip_main_title1);
                header.setText(title);
                TextView description = (TextView) parent.findViewById(R.id.description0);
                description.setText(desc);
                // Devider
                View view = findViewById(R.id.devider1);
                view.setVisibility(View.VISIBLE);
                ImageView imageView = (ImageView) parent.findViewById(R.id.trail_image1);
                Picasso.with(myContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/" + coverId + ".jpg").centerCrop().placeholder(R.drawable.ic_landscape_black_24dp).into(imageView);
                parent.setOnTouchListener(fetchOpenTrailClickListener(id));
            } else if (count == 2) {
                parent = (RelativeLayout) findViewById(R.id.chip_sub_wrapper);
                parent.setVisibility(View.VISIBLE);
                // Devider
                View view = findViewById(R.id.devider2);
                view.setVisibility(View.VISIBLE);

                TextView header = (TextView) parent.findViewById(R.id.trail_chip_main_title);
                header.setText(title);
                TextView description = (TextView) parent.findViewById(R.id.trail_chip_secondary_title);
                description.setText(desc);
                ImageView imageView = (ImageView) parent.findViewById(R.id.trail_image);
                Picasso.with(myContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/" + coverId + ".jpg").centerCrop().placeholder(R.drawable.ic_landscape_black_24dp).into(imageView);
                parent.setOnTouchListener(fetchOpenTrailClickListener(id));
            } else {
                parent = (RelativeLayout) findViewById(R.id.chip_sub_wrapper2);
                parent.setVisibility(View.VISIBLE);
                TextView header = (TextView) parent.findViewById(R.id.trail_chip_main_title2);
                header.setText(title);
                // Devider
                View view = findViewById(R.id.devider3);
                view.setVisibility(View.VISIBLE);
                TextView description = (TextView) parent.findViewById(R.id.trail_chip_secondary_title2);
                description.setText(desc);
                ImageView imageView = (ImageView) parent.findViewById(R.id.trail_image2);
                Picasso.with(myContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/" + coverId + ".jpg").centerCrop().placeholder(R.drawable.ic_landscape_black_24dp).into(imageView);
                parent.setOnTouchListener(new View.OnTouchListener() {
                                              private float startX;
                                              private float startY;

                                              @Override
                                              public boolean onTouch(View v, MotionEvent event) {
                                                  switch (event.getAction()) {
                                                      case MotionEvent.ACTION_DOWN:
                                                          startX = event.getX();
                                                          startY = event.getY();
                                                          break;
                                                      case MotionEvent.ACTION_UP: {
                                                          float endX = event.getX();
                                                          float endY = event.getY();
                                                          if (isAClick(startX, endX, startY, endY)) {
                                                              Intent TrailViewer = new Intent();
                                                              TrailViewer.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.MapViewer");
                                                              Bundle extras = new Bundle();
                                                              extras.putString("TrailId", id);
                                                              TrailViewer.putExtras(extras);
                                                              myContext.startActivity(TrailViewer);
                                                          }
                                                          break;
                                                      }
                                                  }
                                                  v.getParent().requestDisallowInterceptTouchEvent(true);
                                                  return false;
                                              }

                                              private boolean isAClick(float startX, float endX, float startY, float endY) {
                                                  float differenceX = Math.abs(startX - endX);
                                                  float differenceY = Math.abs(startY - endY);
                                                  if (differenceX > 5/* =5 */ || differenceY > 5) {
                                                      return false;
                                                  }
                                                  return true;
                                              }
                                          }

                );

            }
        } catch (IllegalArgumentException ex) {
            // Generally happens if we press back too fast. Need to catch this
            Log.e("PROFILE", "IllegalArguementException thrown and caught. User probably pressed back before glide had loaded images.");
        }
    }

    // A method that returns a click listener to load a trail when it is clicked on.
    private View.OnTouchListener fetchOpenTrailClickListener(final String id) {
        return new View.OnTouchListener() {
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP: {
                        float endX = event.getX();
                        float endY = event.getY();
                        if (isAClick(startX, endX, startY, endY)) {
                            Intent TrailViewer = new Intent();
                            TrailViewer.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.MapViewer");
                            Bundle extras = new Bundle();
                            extras.putString("TrailId", id);
                            TrailViewer.putExtras(extras);
                            myContext.startActivity(TrailViewer);
                        }
                        break;
                    }
                }
                v.getParent().requestDisallowInterceptTouchEvent(true); //specific to my project
                return false; //specific to my project
            }

            private boolean isAClick(float startX, float endX, float startY, float endY) {
                float differenceX = Math.abs(startX - endX);
                float differenceY = Math.abs(startY - endY);
                if (differenceX > 5/* =5 */ || differenceY > 5) {
                    return false;
                }
                return true;
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}