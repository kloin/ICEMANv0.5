package com.teamunemployment.breadcrumbs.Album.LocalAlbumSummary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.widget.EditText;
import android.widget.ImageView;

import com.teamunemployment.breadcrumbs.App;
import com.teamunemployment.breadcrumbs.DependencyInjection.DaggerAppComponent;
import com.teamunemployment.breadcrumbs.R;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Josiah Kendall
 */
public class LocalAlbumView extends AppCompatActivity implements LocalAlbumSummaryPresenterViewContract {

    @Inject
    public LocalAlbumSummaryPresenter presenter;

    @Bind(R.id.album_title) EditText editTextTitle;
    @Bind(R.id.public_switch) SwitchCompat publicSwitch;
    @Bind(R.id.headerPicture) ImageView headerPicture;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_album_summary_view);
        ((App) getApplication()).getNetComponent().injectView(this);
        ButterKnife.bind(this);
        presenter.setViewContract(this);
        presenter.loadInfo();
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
}
