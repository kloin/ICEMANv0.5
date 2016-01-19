package com.breadcrumbs.client;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.breadcrumbs.Framework.JsonHandler;
import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.R;
import com.breadcrumbs.ServiceProxy.AsyncDataRetrieval;
import com.breadcrumbs.ServiceProxy.AsyncImageFetch;
import com.breadcrumbs.ServiceProxy.AsyncRetrieveImage;
import com.breadcrumbs.ServiceProxy.HTTPRequestHandler;
import com.breadcrumbs.ServiceProxy.UpdateViewElementWithProperty;
import com.breadcrumbs.caching.GlobalContainer;
import com.breadcrumbs.client.Adapters.TrailChipAdapter;
import com.breadcrumbs.client.Cards.HomeCardAdapter;
import com.breadcrumbs.client.Cards.ImageChooserGridViewAdapter;
import com.breadcrumbs.client.DialogWindows.DatePickerDialog;
import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Josiah Kendall on 4/21/2015.
 */
public class ProfilePageViewer extends AppCompatActivity  implements DatePickerDialog.DatePickerDialogListener{
    private GlobalContainer gc;
    private String userId;
    private View rootView;
    private Activity myContext;
    private boolean isDirty = false;
    private TextView ageEditText;
    private EditText countriesTravelled;
    private EditText gender;
    static final int PICK_PROFILE_REQUEST = 1;
    private boolean isOwnProfile = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myContext = this;
        setContentView(R.layout.profile_screen);

        //Get user Id and name from bundle
        Bundle extras = getIntent().getExtras();
        userId = extras.getString("userId");
        if (userId.equals(PreferenceManager.getDefaultSharedPreferences(this).getString("USERID", "-1"))) {
            isOwnProfile = false;
        }
        String name = extras.getString("name");
        if (userId == null ) {
            Toast.makeText(this, "Serious issues", Toast.LENGTH_SHORT).show();
        } else if(name == null) {
            // Re fetch name.
        }

        // Set up user interaction - click handlers etc depending on the userId.
        GlobalContainer gc = GlobalContainer.GetContainerInstance();
        if (userId.equals(PreferenceManager.getDefaultSharedPreferences(this).getString("USERID", gc.GetUserId()))) {
            // This means that we are loading our current users profile. Everything should be editable.
            SetUpClickHandlers();
        }

        setUpCollapsableToolbar(name);
        setIsDirtyListener();
        loadDetails();
        setProfilePic();
        setButtonListeners();
        setHeaderPic();
        loadTrailsIntoCard();
    }

    private void setUpCollapsableToolbar(String name) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       // mRecyclerView = (RecyclerView) findViewById(R.id.crumb_recycler);
      //  mRecyclerView.setHasFixedSize(true);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsable_toolbar_holder);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.HeaderFont);
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

        UpdateViewElementWithProperty updater = new UpdateViewElementWithProperty();
        updater.UpdateEditTextElement(bio, userId, "About");
        updater.UpdateTextViewElement(ageDisplay, userId, "Age");

        // BAd but IDGAF
        updater.UpdateTextViewElement(aboutTextView, userId, "About");
    }

    // Toolbar button listeners - save and back
    private void setButtonListeners() {
        // Save button listener.
        TextView saveButton = (TextView) findViewById(R.id.profile_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
                Toast.makeText(myContext, "Saved", Toast.LENGTH_SHORT).show();
            }
        });

        // back button listener
        ImageButton button = (ImageButton) findViewById(R.id.profile_back_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Quit this screen
            }
        });

        TextView dobEditLauncher = (TextView) findViewById(R.id.date_picker_launcher);
        dobEditLauncher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });
    }

    private void openDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog();
        datePickerDialog.show(getSupportFragmentManager(), "datepicker");
    }

    // Save our changes age, gender and Nationality
    private void save() {
        // Just going to save all values, cos fuck it.
        ageEditText = (TextView) findViewById(R.id.age);
        String age = ageEditText.getText().toString();
        EditText bio = (EditText) findViewById(R.id.bio_edit_text);
        String about = bio.getText().toString();
        //String genderInfo = gender.getText().toString();

        String ageUrl = LoadBalancer.RequestServerAddress()+ "/rest/login/SaveStringPropertyToNode/"+userId+"/Age/"+age;
        String aboutInfoUrl = LoadBalancer.RequestServerAddress()+ "/rest/login/SaveStringPropertyToNode/"+userId+"/About/"+about;
        //String genderInfoUrl = LoadBalancer.RequestServerAddress()+ "/rest/login/SaveStringPropertyToNode/"+userId+"/Sex/"+genderInfo;

        // Do the saving
        HTTPRequestHandler simpleSaver = new HTTPRequestHandler();
    //    simpleSaver.SendSimpleHttpRequestAndReturnString(saveNameUrl);
        simpleSaver.SendSimpleHttpRequestAndReturnString(ageUrl);
        simpleSaver.SendSimpleHttpRequestAndReturnString(aboutInfoUrl);

     //   simpleSaver.SendSimpleHttpRequestAndReturnString(genderInfoUrl);

    }

    private void setIsDirtyListener() {
       // EditText text = (EditText) findViewById(R.id.about_me);
        //text.addTextChangedListener(textWatcher);
    }

    // For isDirty. Not using ATM
    private TextWatcher textWatcher = new TextWatcher() {
        // Required
        public void afterTextChanged(Editable s) {
        }

        // Required
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        // what we will work with
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            isDirty = true;
        }
    };

    private void SetUpClickHandlers() {
        // Set Image click handlers for profile and header photo.

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // This is the check for when we return with no data. Usually when the user hits the back button
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                // Load our new image
                Toast.makeText(myContext, "Update profile", Toast.LENGTH_SHORT).show();
                final ImageView header = (ImageView) findViewById(R.id.headerPicture);
                String id = PreferenceManager.getDefaultSharedPreferences(myContext).getString("COVERPHOTOID", "-1");
                Glide.with(myContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+id + ".jpg").centerCrop().crossFade().into(header);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    // Try to set the profile picture for a user
    private void setProfilePic() {
        //Try load
        gc = GlobalContainer.GetContainerInstance();
        //ImageView profile = (ImageView) findViewById(R.id.profilePicture);
       // Glide.with(this).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+userId + "P.jpg").centerCrop().crossFade().into(profile);
        //if fail, use normal
    }

    // Try to set the profile picture for a user
    private void setHeaderPic() {
        // Set hieght
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
            String imageIdUrl = LoadBalancer.RequestServerAddress() + "/rest/login/GetPropertyFromNode/"+userId+"/CoverPhotoId";
            AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(imageIdUrl, new AsyncDataRetrieval.RequestListener() {
                @Override
                public void onFinished(String result) {
                    if (result != null || !result.isEmpty()) {
                        Glide.with(myContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+result + ".jpg").centerCrop().crossFade().into(header);
                    }
                }
            });
            asyncDataRetrieval.execute();

        } else {
            TextView textView = (TextView) myContext.findViewById(R.id.profile_select_prompt);
            textView.setVisibility(View.GONE);
            Glide.with(myContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+coverPhotoId + ".jpg").centerCrop().crossFade().into(header);
            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClassName("com.breadcrumbs", "com.breadcrumbs.client.ImageChooser.GridImageSelector");
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
        editButton.setVisibility(View.GONE);
        TextView descriptionTextView = (TextView) findViewById(R.id.about_uneditable);
        descriptionTextView.setVisibility(View.GONE);

        // Set the edit texts visible
        EditText aboutEdit = (EditText) findViewById(R.id.bio_edit_text);
        aboutEdit.setVisibility(View.VISIBLE);
        aboutEdit.setEnabled(true);
    }

    @Override
    public void onDateClick(int day, int month, int year) {
        ageEditText = (TextView) findViewById(R.id.age);
        ageEditText.setText(day + "/" + month + "/" + year);
    }

    // Load the trail data to add chips to the
    private void loadTrailsIntoCard() {
        // Load all trail Ids
        String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/GetAllTrailsForAUser/"+userId;
        url = url.replaceAll(" ", "%20");
       AsyncDataRetrieval clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                try {
                    // Get our arrayList for the card adapter
                    JSONObject jsonResult = new JSONObject(result);
                    if (jsonResult != null) {
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
        });

        clientRequestProxy.execute();
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
        try {
            // While we have not added three things:
            // For option 0, add it to the first
            // For round 1, add it to the secondr
            // for round 2 same as ...
            // No reound three
            RelativeLayout parent = null;
            if (count == 1) {
                parent = (RelativeLayout) findViewById(R.id.chip_sub_wrapper0);
                parent.setVisibility(View.VISIBLE);
                TextView header = (TextView) parent.findViewById(R.id.trail_chip_main_title1);
                header.setText(title);
                TextView description = (TextView) parent.findViewById(R.id.description0);
                description.setText(desc);

                ImageView imageView = (ImageView) parent.findViewById(R.id.trail_image1);
                Glide.with(myContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+coverId+".jpg").centerCrop().crossFade().into(imageView);

            } else if (count == 2) {
                parent = (RelativeLayout) findViewById(R.id.chip_sub_wrapper);
                parent.setVisibility(View.VISIBLE);
                TextView header = (TextView) parent.findViewById(R.id.trail_chip_main_title);
                header.setText(title);
                TextView description = (TextView) parent.findViewById(R.id.trail_chip_secondary_title);
                description.setText(desc);
                ImageView imageView = (ImageView) parent.findViewById(R.id.trail_image);
                Glide.with(myContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+coverId+".jpg").centerCrop().crossFade().into(imageView);
            } else {
                parent = (RelativeLayout) findViewById(R.id.chip_sub_wrapper2);
                parent.setVisibility(View.VISIBLE);
                TextView header = (TextView) parent.findViewById(R.id.trail_chip_main_title2);
                header.setText(title);
                TextView description = (TextView) parent.findViewById(R.id.trail_chip_secondary_title2);
                description.setText(desc);

                ImageView imageView = (ImageView) parent.findViewById(R.id.trail_image2);
                Glide.with(myContext).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+coverId+".jpg").centerCrop().crossFade().into(imageView);


            }
        } catch (IllegalArgumentException ex) {
            // Generally happens if we press back too fast. Need to catch this
            Log.e("PROFILE", "IllegalArguementException thrown and caught. User probably pressed back before glide had loaded images.");
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
