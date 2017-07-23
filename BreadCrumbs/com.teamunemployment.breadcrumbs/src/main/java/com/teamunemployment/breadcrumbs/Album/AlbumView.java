package com.teamunemployment.breadcrumbs.Album;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.Display;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.teamunemployment.breadcrumbs.App;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
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
    private static final String TAG = "AlbumView";
    public static final String ALBUM_EXTRA_KEY = "AlbumId";
    private Context context;
    private String albumId;
    private BottomSheetBehavior bottomSheetBehavior;

    @Inject
    public PreferencesAPI preferencesAPI;

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
    @Bind(R.id.comments_bottom_sheet) View commentsBottomSheet;
    @Bind(R.id.comment_input) EditText commentInput;
    @Bind(R.id.comments_count) TextView commentCount;
    @Bind(R.id.comments_recycler) RecyclerView recyclerView;
    @Bind(R.id.no_content_placeholder) RelativeLayout noContentPlaceholder;
    @Bind(R.id.play_count) TextView playCount;
    @Bind(R.id.settings) FloatingActionButton setttingsFab;
    @Bind(R.id.views_image) ImageView playCountIcon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_viewer);
        ButterKnife.bind(this);
        context = this;
        ((App) getApplication()).getNetComponent().inject(this);
        Bundle extras = getIntent().getExtras();
        albumId = extras.getString(ALBUM_EXTRA_KEY);
        setUpBottomSheet();
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

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        presenter.BindRecyclerView(recyclerView);
    }

    @OnClick(R.id.open_map) void openMap() {
        //presenter.Pause();
        if (albumId.endsWith("L")) {
            Intent TrailViewer = new Intent();
            TrailViewer.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.LocalMap");
            Bundle extras = new Bundle();
            extras.putString("TrailId", albumId);
            TrailViewer.putExtras(extras);
            context.startActivity(TrailViewer);
        } else {
            Intent TrailViewer = new Intent();
            TrailViewer.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.MapViewer");
            Bundle extras = new Bundle();
            extras.putString("TrailId", albumId);
            TrailViewer.putExtras(extras);
            Activity contextActivity = (Activity) context;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(contextActivity, mapFab, mapFab.getTransitionName());

                contextActivity.startActivity(TrailViewer, options.toBundle());
            } else {
                contextActivity.startActivityForResult(TrailViewer, 1);
            }
        }
    }

    @OnClick(R.id.settings) void openSettings() {
        Intent newIntent = new Intent();
        int localTrail = preferencesAPI.GetLocalTrailId();
        String localTrailString = Integer.toString(localTrail) + "L";
        newIntent.putExtra("AlbumId", localTrailString);
        newIntent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.Album.LocalAlbumSummary.LocalAlbumView");
        startActivity(newIntent);
    }

    @OnClick(R.id.next_storyboard_item) void next() {
        presenter.foreward();
    }

    @OnClick(R.id.reverse_one) void previous() {
        presenter.reverse();
    }

    @OnClick(R.id.image_view) void pauseImage() {
        presenter.togglePauseState();
    }

    @OnClick(R.id.video_view) void pauseVideo() {
        presenter.togglePauseState();
    }

    @OnClick(R.id.send_comment_button) void saveComment() {

        Editable editable = commentInput.getText();
        if (editable == null || editable.toString().isEmpty()) {
            return;
        }

        String textToSave = editable.toString();
        presenter.saveComment(textToSave);

        commentInput.setText("");
        commentInput.clearFocus();
    }

    /**
     * Show a snackbar with the given message
     * @param s The message to show.
     */
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
        presenter.Resume();
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
    public void showCommentsBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mapFab.setEnabled(false);
        nextButton.setEnabled(false);
        reverseButton.setEnabled(false);
    }

    @Override
    public void showDimScreenOverlay() {
        bufferinOverlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideCommentsBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        nextButton.setVisibility(View.VISIBLE);
        reverseButton.setVisibility(View.VISIBLE);
        mapFab.setEnabled(true);
        nextButton.setEnabled(true);
        reverseButton.setEnabled(true);
    }

    @Override
    public void hideDimScreenOverlay() {
        bufferinOverlay.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setCommentsCount(final int size) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                commentCount.setText(size + " Comments");
            }
        });
    }

    @Override
    public void setRecyclerViewAdapter(final CommentAdapter commentAdapter) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerView.setAdapter(commentAdapter);
            }
        });
    }

    @Override
    public void SetImageViewWithImage(final String url, final CircleImageView profileImage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Picasso.with(context).load(url).fit().centerCrop().into(profileImage);
            }
        });
    }

    @Override
    public void showNoContentMessage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                nextButton.setVisibility(View.GONE);
                reverseButton.setVisibility(View.GONE);
                noContentPlaceholder.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void setImageViewCount(final String countString) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playCount.setText(countString);
            }
        });
    }

    @Override
    public void setSettingsButton(int visible) {
        setttingsFab.setVisibility(visible);
    }

    @Override
    public void setPlayCountVisibility(int visibility) {
        playCount.setVisibility(visibility);
        playCountIcon.setVisibility(visibility);
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
                Picasso.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/" + id + ".jpg").placeholder(R.drawable.profileblank).fit().centerCrop().noFade().into(imageView);
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
        finish();
    }



    /**
     * Set up our comments bottom sheet.
     */
    private void setUpBottomSheet() {
        //test
        bottomSheetBehavior = BottomSheetBehavior.from(commentsBottomSheet);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // React to state change
                presenter.onBottomSheetChanged(bottomSheet, newState);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                presenter.onBottomSheetSlide(bottomSheet, slideOffset);
            }
        });
    }
}
