package com.teamunemployment.breadcrumbs.Album;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.teamunemployment.breadcrumbs.Album.data.MimeDetails;
import com.teamunemployment.breadcrumbs.App;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.Maps.MapViewer;

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
    private static final String TAG = "AlbumView";
    public static final String ALBUM_EXTRA_KEY = "AlbumId";
    private Context context;
    private String albumId;

    @Inject AlbumPresenter presenter;

    @Bind(R.id.root) CoordinatorLayout root;
    @Bind(R.id.viewport_holder) RelativeLayout viewPortHolder;
    @Bind(R.id.image_view) ImageView imageView;
    @Bind(R.id.video_view) TextureView videoSurface;
    @Bind(R.id.album_owner_image) CircleImageView profilePicture;
    @Bind(R.id.album_owner_text) TextView albumOwnerText;
    @Bind(R.id.floating_description) TextView floatingDescription;
    @Bind(R.id.buffering_overlay) View bufferinOverlay;
    @Bind(R.id.buffering_symbol) ProgressBar bufferingSymbol;
    @Bind(R.id.reverse_one) FloatingActionButton reverseButton;
    @Bind(R.id.next_storyboard_item) FloatingActionButton nextButton;
    @Bind(R.id.horizontal_progress) ProgressBar horizonalProgress;
    @Bind(R.id.open_map) FloatingActionButton mapFab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_viewer);
        Log.d(TAG, "launching activity");
        ButterKnife.bind(this);
        context = this;
        ((App) getApplication()).getNetComponent().inject(this);
        Bundle extras = getIntent().getExtras();
        albumId = extras.getString(ALBUM_EXTRA_KEY);
        if (albumId != null) {
            presenter.SetView(this);
            presenter.setProgressBar(horizonalProgress);
            presenter.Start(albumId);
            presenter.setVideoSurface(videoSurface);
        } else {
            showMessage("Error loading album");
        }
        if (savedInstanceState != null) {
            Log.d(TAG, "Found saved instance state - " + savedInstanceState.getInt("position"));
        }

    }

    @OnClick(R.id.open_map) void openMap() {
        Intent TrailViewer = new Intent();
        TrailViewer.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.MapViewer");
        Bundle extras = new Bundle();
        extras.putString("TrailId", albumId);
        TrailViewer.putExtras(extras);
        context.startActivity(TrailViewer);
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
    public void setBufferingVisible() {
        final int visibility = View.VISIBLE;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                bufferinOverlay.setVisibility(visibility);
                bufferingSymbol.setVisibility(visibility);
                nextButton.setEnabled(false);

            }
        });
    }

    @Override
    public void setBufferingInvisible() {
        final int visibility = View.INVISIBLE;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bufferinOverlay.setVisibility(visibility);
                bufferingSymbol.setVisibility(visibility);
                nextButton.setEnabled(true);
            }
        });
    }

    @Override
    public void setUserName(final String userName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                albumOwnerText.setText(userName);
            }
        });
    }

    // TODO - decide if i need these two methods. Currently it is handled by the video timer class, cant decide if i like that solution.
    @Override
    public void setProgressBarState(int position) {
        horizonalProgress.setProgress(position);
    }

    @Override
    public void setProgressBarMax(int max) {
        horizonalProgress.setMax(max);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        Log.d(TAG, "Saving instance state");
        outState.putInt("position", 1);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "Restoring instance state");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "OnRestart called");
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "OnDestroy Called");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "OnResume called");
        presenter.restart();
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "On Pause called");
        //onBackPressed();
        super.onPause();
    }

    @Override
    public void finishUp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onBackPressed();
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
    public void setImageBitmap(final Bitmap bitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setImageBitmap(bitmap);
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
                Picasso.with(context).load(url).fit().centerCrop().placeholder(R.drawable.profileblank).into(profilePicture);
            }
        });
    }

    @Override
    public void onBackPressed() {
        presenter.stop();
        super.onBackPressed();
    }
}
