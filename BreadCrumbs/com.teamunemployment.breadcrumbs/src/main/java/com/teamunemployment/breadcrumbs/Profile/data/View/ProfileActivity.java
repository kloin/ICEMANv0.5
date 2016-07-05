package com.teamunemployment.breadcrumbs.Profile.data.View;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.Preferences.Preferences;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.Profile.data.Presenter.Presenter;
import com.teamunemployment.breadcrumbs.Profile.data.Presenter.ProfileContract;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.caching.TextCaching;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.Image.ImageLoadingManager;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import java.io.IOException;
import java.util.ArrayList;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by jek40 on 3/07/2016.
 */
public class ProfileActivity extends Fragment implements ProfileContract.ViewContract {

    static final int PICK_PROFILE_REQUEST = 1;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST = 5;

    @Bind(R.id.user_about) TextView userAboutTextView;
    @Bind(R.id.user_web) TextView userWebsiteTextView;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.collapsable_toolbar_holder) CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.headerPicture) ImageView headerImageView;
    @Bind(R.id.profile_edit_fab) FloatingActionButton profileEditFab;
    @Bind(R.id.profile_select_prompt) TextView selectProfilePhotoPrompt;
    @Bind(R.id.app_bar_layout_profile) AppBarLayout appBarLayout;
    @Bind(R.id.profile_prompt_base_layer) View baseLayer;
    @Bind(R.id.user_about_edit) EditText userAboutEdit;
    @Bind(R.id.user_web_edit) EditText websiteEdit;
    @Bind(R.id.trips_list_view) LinearLayout listView;

    private LayoutInflater inflater;
    private Presenter presenter;
    private AppCompatActivity appCompatActivity;
    private long userId;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        View rootView = inflater.inflate(R.layout.profile_page_fragment, container, false);
        ButterKnife.setDebug(true);
        ButterKnife.bind(this, rootView);
        appCompatActivity = (AppCompatActivity) getActivity();

        PreferencesAPI preferencesAPI = new PreferencesAPI(appCompatActivity);
        userId = Long.parseLong(preferencesAPI.GetUserId());

        DatabaseController controller = new DatabaseController(appCompatActivity);
        presenter = new Presenter(this, controller, appCompatActivity, userId);
        presenter.Start();

        initialiseCollapsableToolbar();
        initialiseTextWatchers();
        return rootView;
    }


    private void initialiseCollapsableToolbar() {
        appCompatActivity.setSupportActionBar(toolbar);
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
        setToolbarMaxHeight();
    }

    // Set up listeners for changes in the about me/website sections.
    private void initialiseTextWatchers() {
        userAboutEdit.addTextChangedListener(getAboutTextWatcher());
        websiteEdit.addTextChangedListener(getWebsiteTextWatcher());
    }

    // Set the height to be the same as the screen width, so that at full expansion it will be a square.
    private void setToolbarMaxHeight() {
        ViewGroup.LayoutParams layoutParams = appBarLayout.getLayoutParams();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        appCompatActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int widthDouble = displaymetrics.widthPixels;
        layoutParams.height = widthDouble;
        appBarLayout.setLayoutParams(layoutParams);
    }

    @Override
    public void setUserName(final String userName) {
        appCompatActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                collapsingToolbarLayout.setTitle(userName);
            }
        });
    }

    @Override
    public void setUserAbout(final String about) {
        appCompatActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userAboutTextView.setText(about);
            }
        });
    }

    @Override
    public void setUserWeb(final String website) {
        appCompatActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userWebsiteTextView.setText(website);
            }
        });
    }

    @Override
    public void setUserTripsAdapter(final ArrayList<Trip> trips) {
        if (trips!= null && trips.size() > 0) {
            appCompatActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Trip[] tripsArray = new Trip[trips.size()];
                    trips.toArray(tripsArray);
                    final TripListAdapter tripArrayAdapter = new TripListAdapter(appCompatActivity,tripsArray);
                    for(int index = 0; index < tripArrayAdapter.getCount(); index += 1) {
                        View item = tripArrayAdapter.getView(index, null, null);
                        listView.addView(item);
                        View view = inflater.inflate(R.layout.list_divider, listView);
                        final int finalIndex = index;
                        item.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent TrailViewer = new Intent();
                                TrailViewer.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.MapViewer");
                                Bundle extras = new Bundle();
                                extras.putString("TrailId", trips.get(finalIndex).getId());
                                TrailViewer.putExtras(extras);
                                appCompatActivity.startActivity(TrailViewer);
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void setProfilePicture(final String url) {
        appCompatActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Picasso.with(appCompatActivity).load(url).into(headerImageView);
            }
        });
    }

    @Override
    public void setProfileBitmap(final Bitmap bitmap) {
        appCompatActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                headerImageView.setImageBitmap(bitmap);
            }
        });
    }

    @Override
    public void setFabAsGreen() {
        appCompatActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                profileEditFab.setBackgroundTintList(ColorStateList.valueOf((getContext().getResources().getColor(R.color.good_to_go))));
            }
        });
    }

    @Override
    public void setFabAsWhite() {
        appCompatActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                profileEditFab.setBackgroundTintList(ColorStateList.valueOf((getContext().getResources().getColor(R.color.white))));
            }
        });
    }

    @Override
    public void setFabIconAsEdit() {
        appCompatActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                profileEditFab.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_action_edit));
            }
        });
    }

    @Override
    public void setFabIconAsTick() {
        appCompatActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                profileEditFab.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_action_accept));
            }
        });
    }

    @Override
    public void setProfileClickPromptAsVisible() {
        appCompatActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SimpleAnimations.FadeInView(selectProfilePhotoPrompt);
                SimpleAnimations.FadeInView(baseLayer);
            }
        });
    }



    @Override
    public void setProfileClickPromptAsGone() {
        appCompatActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SimpleAnimations.FadeOutView(selectProfilePhotoPrompt);
                SimpleAnimations.FadeOutView(baseLayer);
            }
        });
    }

    //Handle profile prompt text click
    @OnClick(R.id.profile_select_prompt) void initiateProfilePicSelect() {
        int permissionCheckWriteToExternalStorage = ContextCompat.checkSelfPermission(appCompatActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheckWriteToExternalStorage == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent();
            intent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.ImageChooser.ImageChooserTabWrapper");
            startActivityForResult(intent, PICK_PROFILE_REQUEST);
        } else {
            ActivityCompat.requestPermissions(appCompatActivity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST);
        }
    }

    // Handle toggle between edit mode and standard mode.
    @OnClick(R.id.profile_edit_fab) void doEditToggle() {

        presenter.HandleEditToggle();
    }

    @Override
    public void setUserAboutAsReadOnly() {
        userAboutTextView.setVisibility(View.VISIBLE);
        userAboutEdit.setVisibility(View.INVISIBLE);
        userAboutEdit.setEnabled(false);
    }

    @Override
    public void setUserWebsiteAsReadOnly() {

        userWebsiteTextView.setVisibility(View.VISIBLE);
        websiteEdit.setVisibility(View.INVISIBLE);
        websiteEdit.setEnabled(false);
    }

    @Override
    public void setUserWebsiteAsEditable() {
        userWebsiteTextView.setVisibility(View.INVISIBLE);
        websiteEdit.setVisibility(View.VISIBLE);
        websiteEdit.setText("");
        websiteEdit.requestFocus();
        websiteEdit.setEnabled(true);
    }

    @Override
    public void setUserAboutAsEditable() {
        userAboutTextView.setVisibility(View.INVISIBLE);
        userAboutEdit.setVisibility(View.VISIBLE);
        userAboutEdit.setText("");
        userAboutEdit.requestFocus();
        userAboutEdit.setEnabled(true);
    }

    @Override
    public void setUserTripsCount() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.ImageChooser.ImageChooserTabWrapper");
                startActivityForResult(intent, PICK_PROFILE_REQUEST);
            } else {
                Toast.makeText(appCompatActivity, "Cannot select a profile image as app does not have sufficient permissions.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private TextWatcher getAboutTextWatcher () {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                presenter.UpdateAboutText(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    private TextWatcher getWebsiteTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                presenter.UpdateWebsiteText(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

}
