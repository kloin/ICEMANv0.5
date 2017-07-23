package com.teamunemployment.breadcrumbs.Album.LocalAlbumSummary;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.NotificationCompat;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.teamunemployment.breadcrumbs.BackgroundServices.UploadTrailService;
import com.teamunemployment.breadcrumbs.DependencyInjection.DaggerAppComponent;
import com.teamunemployment.breadcrumbs.Dialogs.IDialogCallback;
import com.teamunemployment.breadcrumbs.Dialogs.SimpleMaterialDesignDialog;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.caching.Utils;
import com.teamunemployment.breadcrumbs.database.DatabaseController;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.MessageFormat;

import javax.inject.Inject;

import retrofit2.http.Path;

/**
 * @author Josiah Kendall
 */
public class LocalAlbumSummaryPresenter {

    public LocalAlbumModel model;
    public static final int REQUEST_PICTURE = 100;
    public PreferencesAPI preferencesAPI;
    public Context context;
    public DatabaseController databaseController;

    @Inject
    public LocalAlbumSummaryPresenter(LocalAlbumModel model, PreferencesAPI preferencesAPI, Context context,
                                      DatabaseController databaseController) {
        this.model = model;
        this.context = context;
        this.preferencesAPI = preferencesAPI;
        this.databaseController = databaseController;
    }

    public LocalAlbumSummaryPresenterViewContract viewContract;

    public void setViewContract(LocalAlbumSummaryPresenterViewContract viewContract) {
        this.viewContract = viewContract;
    }

    /**
     * Load our info, such as the album title, cover photo etc.
     */
    public void loadInfo() {
        String title = model.LoadTitle();
        viewContract.setAlbumTitle(title);
        Bitmap bitmap = model.LoadCoverBitmap();
        viewContract.setBitmapCoverPhoto(bitmap);
        boolean isPublic = model.LoadPublicity();
        viewContract.setAlbumPublic(isPublic);
    }

    /**
     * Save our public or private setting for the users album
     * @param isPublic true or false setting.
     */
    public void SavePublicity(boolean isPublic) {
        model.SavePublicity(isPublic);
    }

    /**
     * Save a cover photo. NOt sure how this will work yet.
     */
    public void SaveCoverPhoto(Bitmap bitmap){
        String fileName =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/coverphoto";
        com.teamunemployment.breadcrumbs.caching.Utils.SaveBitmap(fileName, bitmap);
    }

    /**
     * Save the albums title.
     * @param title The title of our album.
     */
    public void SaveTitle(String title) {
        if (title != null) {
            model.SaveTitle(title);
        }
    }

    public void onPhotoSelectedResult(int requestCode, int resultCode, Intent data) {
        //
        if (requestCode == 155 && resultCode == Activity.RESULT_OK) {
            String photoIdRaw = data.getStringExtra("Id");
            String photoId = photoIdRaw.substring(0, photoIdRaw.length()-1);
            viewContract.launchPhotoEditor(photoId + ".jpg");
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {

                Uri resultUri = result.getUri();
                viewContract.setImageBitmapFromUri(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    public void publish() {
        viewContract.showPublishConfirmationDialog();
    }

    /**
     * Handle the use case where we have not saved our trail yet. This involves creating the trail
     * and saving the Id that gets returned.
     */
    private boolean handleFirstTimePublishing() {
        String trailName = viewContract.getAlbumTitle();

        // Check if we have
        final String localTrailId = Integer.toString(preferencesAPI.GetLocalTrailId());
        JSONObject crumbs = databaseController.fetchMetadataFromDB(localTrailId, false);
        if (crumbs.length() == 0) {
            // No data, dont allow saving.
            Toast.makeText(context, "Cannot save - album has no content", Toast.LENGTH_LONG).show();
            return false;
        }
        // This is the use case where the trail title has no data. This is an issue an we cannot save
        if (trailName.isEmpty()) {
            return false;
        }

        viewContract.startPublishingNotification();
        String userId = preferencesAPI.GetUserId();

        model.publishAlbum(trailName, userId);
        return true;
    }

    /**
     * Do the heavy lifting of actually publishing the album.
     */
    public void sendAlbumToServer() {
        String userId = preferencesAPI.GetUserId();
        int serverTrailId = preferencesAPI.GetServerTrailId();
        if (serverTrailId == -1) {
            boolean result = handleFirstTimePublishing();
        } else {
            model.publishAlbum(Integer.toString(serverTrailId), userId);
        }
    }
}
