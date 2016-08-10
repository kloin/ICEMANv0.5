package com.teamunemployment.breadcrumbs.Album;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.teamunemployment.breadcrumbs.App;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author Josiah Kendall
 *
 * The view for the album viewer.
 */
public class  AlbumView extends AppCompatActivity implements AlbumPresenterViewContract{
    public static final String ALBUM_EXTRA_KEY = "AlbumId";
    private Context context;

    @Inject
    AlbumPresenter presenter;

    @Bind(R.id.root) CoordinatorLayout root;
    @Bind(R.id.viewport_holder) RelativeLayout viewPortHolder;
    @Bind(R.id.image_view) ImageView imageView;
    @Bind(R.id.video_view) TextureView videoSurface;
    @Bind(R.id.album_owner_image) CircleImageView profilePicture;
    @Bind(R.id.floating_description) TextView floatingDescription;
    @Bind(R.id.buffering_overlay) View bufferinOverlay;
    @Bind(R.id.buffering_symbol) ProgressBar bufferingSymbol;

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

    @OnClick(R.id.next_storyboard_item) void next() {
        presenter.foreward();
    }

    @OnClick(R.id.reverse_one) void previous() {
        presenter.reverse();
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
    public void setBuffering(final int visibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bufferinOverlay.setVisibility(visibility);
                bufferingSymbol.setVisibility(visibility);
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
                Picasso.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/" + id + ".jpg").fit().centerCrop().noFade().into(imageView);
            }
        });
    }

    @Override
    public void setScreenMessage(final String message, final float posX, final float posY) {

        // Grab our screen size.
        Display dm = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        dm.getSize(size);

        // Build the params that define how the floating description
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) floatingDescription.getLayoutParams();
        layoutParams.leftMargin = (int) (size.x * posX);
        layoutParams.topMargin =  (int) (size.y * posY);
        layoutParams.rightMargin = -250;
        layoutParams.bottomMargin = -250;

        // Set the params
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                floatingDescription.setText(message);
                floatingDescription.setLayoutParams(layoutParams);
            }
        });
    }

    @Override
    public void setProfilePicture(final String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Picasso.with(context).load(url).fit().centerCrop().into(profilePicture);
            }
        });
    }
}
