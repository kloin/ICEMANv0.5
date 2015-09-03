package com.breadcrumbs.client;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.breadcrumbs.ServiceProxy.AsyncImageFetch;
import com.breadcrumbs.ServiceProxy.AsyncRetrieveImage;
import com.breadcrumbs.caching.GlobalContainer;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.w3c.dom.Text;

import java.io.IOException;

/**
 * Created by Josiah Kendall on 4/21/2015.
 */
public class ProfilePageViewer extends AppCompatActivity {
    private GlobalContainer gc;
    private String userId;
    private View rootView;
    private Activity myContext;
    private boolean isDirty = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This is our base view - the subscription manager with the bar and the container frame.
        GlobalContainer gc = GlobalContainer.GetContainerInstance();
        Bundle extras = getIntent().getExtras();
        userId = extras.getString("userId");
        myContext = this;
        String name = getIntent().getExtras().getString("name");
        if (!userId.equals("-1")) {
            setContentView(R.layout.profile_screen);
        }
      //  ActionBar actionBar = getActionBar();
       // actionBar.hide();
        if (userId == null ) {
            Toast.makeText(this, "Serious issues", Toast.LENGTH_SHORT).show();
            // Get our local user Id, because we are viewing our own profile
           // userId = PreferenceManager.getDefaultSharedPreferences(myContext).getString("USERID", "-1");
        } else if(name == null) {
            // Re fetch name.
        }
        // Set up user interaction - click handlers etc depending on the userId.
        if (userId.equals(PreferenceManager.getDefaultSharedPreferences(this).getString("USERID", gc.GetUserId()))) {
            // This means that we are loading our current users profile. Everything should be editable.
            SetUpClickHandlers();
        } else {
            EditText description = (EditText) findViewById(R.id.about_me);
            description.setEnabled(false);
            FloatingActionsMenu fam = (FloatingActionsMenu) findViewById(R.id.set_photo_options);
            fam.setVisibility(View.GONE);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_friend);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(myContext, "Following not yet available", Toast.LENGTH_SHORT).show();
                }
            });
            // This means that the current user is
        }

        setIsDirtyListener();
        SetUpNameLabel(name);
        setProfilePic();
        setButtonListeners();
        setHeaderPic();
    }

    private void setButtonListeners() {
        ImageButton button = (ImageButton) findViewById(R.id.profile_back_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDirty) {
                    //save();
                    Toast.makeText(myContext, "Saved", Toast.LENGTH_SHORT).show();
                }
                finish(); // Quit this screen
            }
        });
    }

    private void setIsDirtyListener() {
        EditText text = (EditText) findViewById(R.id.about_me);
        text.addTextChangedListener(textWatcher);
    }

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
        FloatingActionButton headerFab = (FloatingActionButton) findViewById(R.id.set_header_photo);
        headerFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(i, "Select Picture"), 1);
            }
        });

        FloatingActionButton profileFab = (FloatingActionButton) findViewById(R.id.set_profile_photo);
        profileFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(i, "Select Picture"), 0);
            }
        });


        // Set click handler for saving? or just save on exit?

        // Set add friend button as invisible.
    }

    @Override
    public void onBackPressed() {
        // If dirty, save file
        if (isDirty) {
            //save();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    // Set the profile name to be the name that was passed in to this intent.
    private void SetUpNameLabel(String name) {
        TextView userName = (TextView) findViewById(R.id.user_name);
        userName.setText(name);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // This is the check for when we return with no data. Usually when the user hits the back button
        if (data == null || resultCode == 0) {
            return;
        }

        Uri uri = Uri.parse(data.getData().toString());
        try {
            //Set our profile picture, then save it

            Bitmap media = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            ImageView profilePic = (ImageView) findViewById(R.id.profilePicture);
            ImageView headerPic = (ImageView) findViewById(R.id.headerPicture);
            String userId = GlobalContainer.GetContainerInstance().GetUserId();
            if (requestCode == 0) {
                userId = userId + "P";
                profilePic.setImageBitmap(media);
            } else {
                userId = userId + "H"; // Header photo
                headerPic.setImageBitmap(media);
            }
            // Save our profile picture
            AsyncImageFetch imagesave = new AsyncImageFetch(media, userId, new AsyncImageFetch.RequestListener() {

                /*
                 * Override for the onFinished.
                 */
                @Override
                public void onFinished(String result) {


                }
            });

            imagesave.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Try to set the profile picture for a user
    private void setProfilePic() {
        //Try load
        gc = GlobalContainer.GetContainerInstance();
        final ImageView profile = (ImageView) findViewById(R.id.profilePicture);
        AsyncRetrieveImage asyncFetch = new AsyncRetrieveImage(userId + "P", new AsyncRetrieveImage.RequestListener() {

            @Override
            public void onFinished(Bitmap result) {
                if (result != null) {
                    profile.setImageBitmap(result);
                }
            }
        });
        asyncFetch.execute();
        //if fail, use normal
    }

    // Try to set the profile picture for a user
    private void setHeaderPic() {
        //Try load
        gc = GlobalContainer.GetContainerInstance();
        final ImageView header = (ImageView) findViewById(R.id.headerPicture);
        AsyncRetrieveImage asyncFetch = new AsyncRetrieveImage(userId + "H", new AsyncRetrieveImage.RequestListener() {

            @Override
            public void onFinished(Bitmap result) {
                if (result != null) {
                    header.setImageBitmap(result);
                }
            }
        });
        asyncFetch.execute();
    }


    /*
        begin construction in here
     */
//    private void load() {
//        gc = GlobalContainer.GetContainerInstance();
//        this.setProfilePic();
//        this.setUpButtonListeners();
//    }
/*
    // Set up image widths/heights etc..
    private void SetImageWidths() {
        FrameLayout pinnedHolder = (FrameLayout) findViewById(R.id.pinnedHolder);
        int height = pinnedHolder.getHeight();
        Display display = getWindowManager().getDefaultDisplay();
        ImageView centre = (ImageView) findViewById(R.id.centrePinned);
        ImageView leftCentre = (ImageView) findViewById(R.id.leftCentrePinned);
        ImageView rightCentre = (ImageView) findViewById(R.id.rightCentrePinned);
        ImageView leftMiddleTop = (ImageView) findViewById(R.id.leftMiddleTopPinned);
        ImageView leftTop = (ImageView) findViewById(R.id.leftTopPinned);
        ImageView rightTop = (ImageView) findViewById(R.id.rightTopPinned);
        ImageView topCentre = (ImageView) findViewById(R.id.centreTopPinned);
        ImageView rightMiddleTop = (ImageView) findViewById(R.id.topMiddleRightPinned);
        ImageView bottomRight = (ImageView) findViewById(R.id.rightBottomPinned);
        ImageView bottomLeft = (ImageView) findViewById(R.id.leftBottomPinned);
        ImageView bottomMiddleLeft = (ImageView) findViewById(R.id.centreBottomPinnedLeft);
        ImageView bottomMiddleRight = (ImageView) findViewById(R.id.centreBottomPinnedRight);

        int screenWidth = display.getWidth();
        centre.setMinimumHeight(screenWidth/4);
        centre.setMinimumWidth(screenWidth/3);
        leftCentre.setMinimumHeight(screenWidth/4);
        leftCentre.setMinimumWidth(screenWidth/3);
        rightCentre.setMinimumHeight(screenWidth/4);
        rightCentre.setMinimumWidth(screenWidth/3);

        // Top layer
        leftMiddleTop.setMinimumHeight(screenWidth/5);
        leftMiddleTop.setMinimumWidth(screenWidth/5);
        leftMiddleTop.offsetLeftAndRight(-screenWidth/5);
        leftTop.setMinimumHeight(screenWidth/5);
        leftTop.setMinimumWidth(screenWidth/5);
        rightTop.setMinimumHeight(screenWidth/5);
        rightTop.setMinimumWidth(screenWidth/5);
        topCentre.setMinimumHeight(screenWidth/5);
        topCentre.setMinimumWidth(screenWidth/5);
        rightMiddleTop.setMinimumHeight(screenWidth/5);
        rightMiddleTop.setMinimumWidth(screenWidth/5);

        // Bottom layer
        bottomRight.setMinimumHeight(screenWidth/4);
        bottomRight.setMinimumWidth(screenWidth/4);
        bottomLeft.setMinimumHeight(screenWidth/4);
        bottomLeft.setMinimumWidth(screenWidth/4);
        bottomMiddleLeft.setMinimumHeight(screenWidth/4);
        bottomMiddleLeft.setMinimumWidth(screenWidth/4);
        bottomMiddleRight.setMinimumHeight(screenWidth/4);
        bottomMiddleRight.setMinimumWidth(screenWidth/4);
    }

    // Try to set the profile picture for a user
    private void setProfilePic() {
        //Try load
        userId = gc.GetUserId();
        final ImageView profile = (ImageView) findViewById(R.id.profilePicture);
        AsyncRetrieveImage asyncFetch = new AsyncRetrieveImage(userId + "P", new AsyncRetrieveImage.RequestListener() {

            @Override
            public void onFinished(Bitmap result) {
                profile.setImageBitmap(result);
            }
        });
        asyncFetch.execute();
        //if fail, use normal
    }

    public void SetNameAndAge() {
        TextView nameTitle = (TextView) findViewById(R.id.nameTitle_profilePage);

    }

    public void setUpButtonListeners() {
        ImageButton backButton = (ImageButton) findViewById(R.id.backUserScreen);
        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        ImageButton addUserButton = (ImageButton) findViewById(R.id.addUserButton);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send request to a user.
            }
        });

        //Click on the profile picture to change your pic
        ImageView profilePic = (ImageView) findViewById(R.id.profilePicture);
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectProfilePictureFromGallery();
            }
        });
    }

    private void selectProfilePictureFromGallery() {
        Intent picChooserIntent = new Intent();
        picChooserIntent.setClassName("com.breadcrumbs.client", "com.breadcrumbs.client.ProfilePicChooser");
        startActivity(picChooserIntent);

    }
*/

}
