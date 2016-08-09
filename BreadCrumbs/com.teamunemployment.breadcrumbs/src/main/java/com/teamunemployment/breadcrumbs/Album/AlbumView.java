package com.teamunemployment.breadcrumbs.Album;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.teamunemployment.breadcrumbs.App;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Josiah Kendall
 *
 */
public class AlbumView extends AppCompatActivity implements AlbumPresenterViewContract{
    public static final String ALBUM_EXTRA_KEY = "AlbumId";
    private Context context;

    @Inject
    AlbumPresenter presenter;

    @Bind(R.id.root) CoordinatorLayout root;
    @Bind(R.id.image_view) ImageView imageView;
    @Bind(R.id.video_view) TextureView videoSurface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_viewer);
        ButterKnife.bind(this);
        context = this;
        ((App) getApplication()).getNetComponent().inject(this);
        Bundle extras = getIntent().getExtras();
        String albumId = extras.getString(ALBUM_EXTRA_KEY);
        if (albumId != null) {
            presenter.SetView(this);
            presenter.Start(albumId);
            presenter.setVideoSurface(videoSurface);
        }
        else {
            showMessage("Error loading album");
        }
    }

    @Override
    public void showMessage(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(root, s, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void setImageVisibility(final int visibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setVisibility(visibility);
            }
        });
    }

    @Override
    public void setVideoVisibility(final int visibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                videoSurface.setVisibility(visibility);
            }
        });
    }

    /**
     * Load an image into our image.
     * @param id the given id of the image that we are loading into our database.
     */
    @Override
    public void setImageUrl(final String id) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Picasso.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/" + id + ".jpg").fit().centerCrop().into(imageView);
            }
        });
    }
}
