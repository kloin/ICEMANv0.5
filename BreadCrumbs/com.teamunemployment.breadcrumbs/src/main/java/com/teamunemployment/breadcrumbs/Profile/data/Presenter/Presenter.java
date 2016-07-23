package com.teamunemployment.breadcrumbs.Profile.data.Presenter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.NetworkConnectivityManager;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.Profile.data.model.ProfileModel;
import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.client.Image.ImageLoadingManager;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Josiah Kendall
 */
public class Presenter implements ProfileContract.PresenterContract {

    private static final int PICK_PROFILE_REQUEST = 1;

    private final static String TAG = "ProfilePresenter";
    private ProfileModel model;
    private ProfileContract.ViewContract view;
    private long userId;
    private Context context;

    public Presenter(ProfileContract.ViewContract view, DatabaseController databaseController, Context context, long userId) {
        this.view = view;
        this.model = new ProfileModel(this,context,databaseController, userId);
        this.userId = userId;
        this.context = context;
    }

    /**
     * Start loading the data to be shown.
     */
    public void Start() {
        String deviceUserId = new PreferencesAPI(context).GetUserId();
        model.setUserState(Long.parseLong(deviceUserId));
        model.LoadUserAbout(userId);
        model.LoadUserName(userId);
        model.LoadUserProfileId(userId);
        model.LoadUserWebsite(userId);
        model.LoadTripIds(userId);
        model.LoadUserFollowing();
        if (!NetworkConnectivityManager.IsNetworkAvailable(context)) {
            view.showMessage("No Network Connection available.");
        }
    }

    @Override
    public void setUserName(String userName) {
        view.setUserName(userName);
    }

    @Override
    public void setUserAbout(String about) {
        view.setUserAbout(about);
    }

    @Override
    public void setUserWeb(String website) {
        view.setUserWeb(website);
    }

    @Override
    public void setUserTrips(ArrayList<Trip> trips) {
        view.setUserTripsAdapter(trips);
    }

    @Override
    public void setProfilePicture(String id) {
        if (id == null || id.equals("0")) {
            view.setMissingProfileBackground();
            return;
        }
        String url = LoadBalancer.RequestCurrentDataAddress() + "/images/"+id+".jpg";
        view.setProfilePicture(url);
    }

    @Override
    public void setUserTripsCount(int count) {
        view.setUserTripsCount("("+count+")");
    }

    @Override
    public void setUserReadOnly() {
        view.setUserEditButtonVisible(false);
        view.setUserFollowButtonVisibile(true);
    }

    @Override
    public void setUserEditable() {
        view.setUserEditButtonVisible(true);
        view.setUserFollowButtonVisibile(false);
    }

    @Override
    public void setUserFollowingState(boolean isFollowing) {
        view.setIAmFollowingThisUser(isFollowing);
    }

    public void HandleEditToggle() {
        boolean nowIneditMode = model.ToggleEditReadOnlyButton();
        if (nowIneditMode) {
            view.setFabAsGreen();
            view.setFabIconAsTick();
            view.setUserAboutAsEditable();
            view.setUserWebsiteAsEditable();
            view.setProfileClickPromptAsVisible();
        } else {
            view.setFabAsWhite();
            view.setFabIconAsEdit();
            view.setUserAboutAsReadOnly();
            view.setUserWebsiteAsReadOnly();
            view.setProfileClickPromptAsGone();
        }
    }

    public void HandleProfileFollow() {
        model.FollowUser();
        view.setIAmFollowingThisUser(true);
    }

    public void HandleProfileUnfollow() {
        model.UnFollowUser();
        view.setIAmFollowingThisUser(false);
    }

    public void SaveNewProfilePicId(long userId, String coverPhotoPic) {
        model.SaveNewProfilePicId(userId, coverPhotoPic);
    }

    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == PICK_PROFILE_REQUEST && resultCode == 1) {
            String picId = data.getStringExtra("ProfilePicId");
            SaveNewProfilePicId(userId, picId);
            String url = LoadBalancer.RequestCurrentDataAddress() + "/images/" +picId + ".jpg";
            view.setProfilePicture(url);
        } else if (requestCode == PICK_PROFILE_REQUEST && resultCode == 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (data == null) {
                        return;
                    }

                    String id = data.getStringExtra("ProfileId");
                    if (id == null) {
                        return;
                    }
                    Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                    final Uri uri = Uri.parse(images + "/" + id);
                    // Fetch the bitmap and set it to our header pic
                    ImageLoadingManager imageLoadingManager = new ImageLoadingManager(context);
                    Bitmap bitmap;
                    try {
                        bitmap = imageLoadingManager.GetFull720Bitmap(uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Fuck all we can do here. Maybe show some sort of error.
                        return;
                    }
                    view.setProfileBitmap(bitmap);
                }
            }).start();

        }
    }

    public void UpdateAboutText(String aboutText) {
        model.SetUserAboutText(aboutText);
    }

    public void UpdateWebsiteText(String websiteText) {
        model.SetUserWebText(websiteText);
    }
}
