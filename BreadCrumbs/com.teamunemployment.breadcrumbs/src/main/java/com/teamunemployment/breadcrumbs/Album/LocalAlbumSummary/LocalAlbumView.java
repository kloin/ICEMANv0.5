package com.teamunemployment.breadcrumbs.Album.LocalAlbumSummary;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.teamunemployment.breadcrumbs.App;
import com.teamunemployment.breadcrumbs.DependencyInjection.DaggerAppComponent;
import com.teamunemployment.breadcrumbs.Dialogs.IDialogCallback;
import com.teamunemployment.breadcrumbs.Dialogs.SimpleMaterialDesignDialog;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.ImageChooser.TrailCoverImageSelector;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Josiah Kendall
 */
public class LocalAlbumView extends AppCompatActivity implements LocalAlbumSummaryPresenterViewContract {

    private final int COVER_PHOTO_SELECT_CODE = 155;

    @Inject
    public LocalAlbumSummaryPresenter presenter;

    @Bind(R.id.album_title) EditText editTextTitle;
    @Bind(R.id.public_switch) SwitchCompat publicSwitch;
    @Bind(R.id.headerPicture) ImageView headerPicture;
    @Bind(R.id.app_bar_layout_profile) AppBarLayout appBarLayout;
    @Bind(R.id.root) CoordinatorLayout root;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_album_summary_view);
        ((App) getApplication()).getNetComponent().injectView(this);
        ButterKnife.bind(this);
        ViewGroup.LayoutParams params = appBarLayout.getLayoutParams();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        params.height = size.x;
        appBarLayout.setLayoutParams(params);
        presenter.setViewContract(this);
        presenter.loadInfo();
    }

    @Override
    public String getAlbumTitle() {

        Editable currentTitleEditable = editTextTitle.getText();
        if (currentTitleEditable == null || currentTitleEditable.toString().isEmpty()) {
            editTextTitle.setError("Trip title must not be blank.");
            Snackbar.make(root, "Trip title must not be blank.", Snackbar.LENGTH_LONG);
            return null;
        }

        return currentTitleEditable.toString();
    }

    @Override
    public void startPublishingNotification() {
        int id = 8;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle("Upload Trip").setContentText("Upload in progress").setSmallIcon(R.drawable.ic_backup_white_24dp);
        notificationBuilder.setProgress(0,0, true);
        notificationManager.notify(id, notificationBuilder.build());
    }

    @Override
    public void showPublishConfirmationDialog() {
        final SimpleMaterialDesignDialog materialDesignDialog = SimpleMaterialDesignDialog.Build(this);

        // Callback for the action button on the dialog.
        IDialogCallback onAccept = new IDialogCallback() {
            @Override
            public void DoCallback() {
                 presenter.sendAlbumToServer();
            }
        };

        // Make the dialog look and work the way we want
        materialDesignDialog.SetTitle("Publish Trip")
                .SetTextBody("This will publish your trip and make it visible to the world.")
                .SetActionWording("Publish")
                .SetCallBack(onAccept);

        // Show the dialog.
        materialDesignDialog.Show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.onPhotoSelectedResult(requestCode, resultCode, data);
    }

    @Override
    public void setBitmapCoverPhoto(final Bitmap bitmapCoverPhoto) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                headerPicture.setImageBitmap(bitmapCoverPhoto);
            }
        });
    }

    @Override
    public void setAlbumTitle(final String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editTextTitle.setText(title);
            }
        });
    }

    @Override
    public void setAlbumPublic(final boolean isPublic) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                publicSwitch.setChecked(isPublic);
            }
        });
    }

    @Override
    public void launchPhotoEditor(String fileId) {
        File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = new File(sd, fileId);
        CropImage.ActivityBuilder builder = CropImage.activity(Uri.fromFile(image));
        builder.setGuidelines(CropImageView.Guidelines.ON).start(this);
    }

    @Override
    public void setImageBitmapFromUri(Uri resultUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
            setBitmapCoverPhoto(bitmap);
            presenter.SaveCoverPhoto(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.album_edit_fab) void handleSelectPhotoClick() {
        Intent selectCoverPhotoIntent = new Intent(this, TrailCoverImageSelector.class);
        startActivityForResult(selectCoverPhotoIntent, COVER_PHOTO_SELECT_CODE);
    }

    @OnClick(R.id.publish_button) void handlePublishClick() {
        presenter.publish();
    }

    @Override
    public void onBackPressed() {
        Editable titleEditable = editTextTitle.getText();
        presenter.SaveTitle(titleEditable.toString());

        boolean isPublic = publicSwitch.isChecked();
        presenter.SavePublicity(isPublic);
        super.onBackPressed();
    }


}
