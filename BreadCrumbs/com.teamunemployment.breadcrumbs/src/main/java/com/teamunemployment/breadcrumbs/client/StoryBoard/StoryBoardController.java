package com.teamunemployment.breadcrumbs.client.StoryBoard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.upstream.Loader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.teamunemployment.breadcrumbs.BreadcrumbsExoPlayer.BreadcrumbsExoPlayer;
import com.teamunemployment.breadcrumbs.BreadcrumbsExoPlayer.BreadcrumbsExoPlayerWrapper;
import com.teamunemployment.breadcrumbs.BreadcrumbsVideoProgressTimer;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UpdateViewElementWithProperty;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.Cards.CrumbCardDataObject;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by jek40 on 26/04/2016.
 */
public class StoryBoardController {
    private ArrayList<CrumbCardDataObject> deletedCrumbs = new ArrayList<>();
    private ArrayList<ImageView> images = new ArrayList<>();
    private final ArrayList<ProgressBar> mProgressBars = new ArrayList<ProgressBar>();
    private final ArrayList<TextureView> videoHolders = new ArrayList<>();
    private ArrayList<StoryBoardModel> storyBoardModels = new ArrayList<>();
    private int viewingIndex = 0;
    private int loadingIndex = 0;
    private int DISPLAY_INDEX = 0;
    private ProgressBar itemDisplayDurationProgressBar;
    private BreadcrumbsVideoProgressTimer videoProgressTimer;
    // This is a record of what object we are using of the mCrumbs list. Each time I CREATE a storyboard this gets incremented.
    private int DATA_INDEX = 0;

    // the index of what we are currently viewing
    private int VIEWING_INDEX = 0;

    // Record of how many of the storyboard objects we have loaded. start at 3 (middle of array)
    private int LOADED_INDEX = 2;
    private int displayObjectIndex = 0;
    private ArrayList<CrumbCardDataObject> mCrumbs;
    private Context mContext;
    private final String TAG = "StoryBoardController";
    private StoryBoardModel mLastObject;
    private ProgressBar mProgressBar;
    private Activity act;
    private StoryBoardModel displayModel;
    private boolean amLoading = false;
    private TextView crumbCount;
    private boolean awaitingPhotoLoad = false;
    private boolean isPlaying = true;
    private TextView placeName;
    // Flag for when we have been buffering. This means that we have to restart the timer.
    private boolean beenBuffering = false;

    private int seekingTo = 0;

    private FinishedListener finishedListener;

    public interface FinishedListener {
        void onStoryboardFinished();
    }

    public StoryBoardController(ArrayList<CrumbCardDataObject> crumbs, ArrayList<ImageView> imageViews, Context context, int index, FinishedListener listener) {
        this.finishedListener = listener;
        mCrumbs = crumbs;
        images = imageViews;
        mContext = context;
        DATA_INDEX = index;
        VIEWING_INDEX = index;
        act = (Activity) mContext;
        buildObjects();
        itemDisplayDurationProgressBar = (ProgressBar) act.findViewById(R.id.horizontal_progress);
        videoProgressTimer = new BreadcrumbsVideoProgressTimer(itemDisplayDurationProgressBar);
        setCrumbCount();
    }

    /*
        Set up the timer back to the position it was when we last exited this activity.
        @Param time - the time of the timer in milliseconds.
     */
    public void SetUpRestoredTimer(int time) {
        if (time <= 200) {
            videoProgressTimer.SetDisplayTimer(0);
            return;
        }
        videoProgressTimer.SetDisplayTimer(time-200);
        seekingTo = time-200;
    }

    public void Start() {
        init();
    }

    private BreadcrumbsVideoProgressTimer.ITimer GetTimerCallback() {
        return new BreadcrumbsVideoProgressTimer.ITimer() {
            @Override
            public void OnFinished() {
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        videoProgressTimer.StopTimer();
                        videoProgressTimer.ResetTimer();
                        moveForward();
                    }
                });
            }
        };
    }

    private void buildObjects() {
        // Progress bars
        ProgressBar progressBar01 = (ProgressBar) act.findViewById(R.id.progress_01);
        ProgressBar progressBar02 = (ProgressBar) act.findViewById(R.id.progress_02);
        ProgressBar progressBar03 = (ProgressBar) act.findViewById(R.id.progress_03);
        ProgressBar progressBar04 = (ProgressBar) act.findViewById(R.id.progress_04);
        ProgressBar progressBar05 = (ProgressBar) act.findViewById(R.id.progress_05);
        mProgressBars.add(progressBar01);
        mProgressBars.add(progressBar02);
        mProgressBars.add(progressBar03);
        mProgressBars.add(progressBar04);
        mProgressBars.add(progressBar05);

        TextureView video1 = (TextureView) act.findViewById(R.id.video_01);
        TextureView video2 = (TextureView) act.findViewById(R.id.video_02);
        TextureView video3 = (TextureView) act.findViewById(R.id.video_03);
        TextureView video4 = (TextureView) act.findViewById(R.id.video_04);
        TextureView video5 = (TextureView) act.findViewById(R.id.video_05);

        // Surfaces that we are going to use.
        videoHolders.add(video1);
        videoHolders.add(video2);
        videoHolders.add(video3);
        videoHolders.add(video4);
        videoHolders.add(video5);
    }

    private void init() {
        // First two objects in the list are just blank objects that will be recycled as we go through. I use the data index to identify our Exoplayer
        StoryBoardModel model1 = new StoryBoardModel(0, null, images.get(0), mProgressBars.get(0), new BreadcrumbsExoPlayerWrapper(videoHolders.get(0), mContext, DATA_INDEX));
        StoryBoardModel model2 = new StoryBoardModel(1, null, images.get(1), mProgressBars.get(1), new BreadcrumbsExoPlayerWrapper(videoHolders.get(1), mContext, DATA_INDEX));

        //Add our two blank objects to the from of the list for later use.
        storyBoardModels.add(0,model1);
        storyBoardModels.add(1, model2);

        int count = 2;
        // Initialise the remaining objects with actual data and

        while (count < 5) {
            // Check we havent run out of data.
            if (mCrumbs.size() > DATA_INDEX) {

                // Create an object to model the info we want to display.
                StoryBoardModel storyBoardModel = new StoryBoardModel(DATA_INDEX, mCrumbs.get(DATA_INDEX), images.get(count),
                        mProgressBars.get(count), new BreadcrumbsExoPlayerWrapper(videoHolders.get(count), mContext, DATA_INDEX));

                // Last object added to the list.
                mLastObject = storyBoardModel;

                // Start loading image or video.
                storyBoardModels.add(count, storyBoardModel);
                DATA_INDEX += 1;
            } else {
                // We have no data to add, so we need to just add nulls that will do nothing.
                StoryBoardModel storyBoardModel = new StoryBoardModel(count, null, images.get(count), mProgressBars.get(count), null);
                storyBoardModels.add(count, storyBoardModel);
                mLastObject = storyBoardModel;
            }
            count += 1;
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                StartLoadingWorkerProcess();
                setPlaceName();
                setDescription();
            }
        }, 1000);
    }

    // This method triggers the loading to start. Loading continues until we are 2(3?) ahead. This  method only displays the first object, click handlers takke care of the rest.
    private void StartLoadingWorkerProcess() {

        // If this is null, its first time use case. Thist triggers the load of the first crumb, and
        // then the rest will just follow by the callbacks after each previous item has loaded.
        constructAndLoadObject(2); // 2 is the viewing index.

        // Display the first object
        StoryBoardModel currentObject = storyBoardModels.get(2);
        displayModel = currentObject;
        // Hard coding this here because we are loading a single object.
        if (currentObject.CrumbData == null) {
            // This is some dodgey 11pm coding here. This happened to me because I deleted a bunch of images,
            // quit before the end and then tried to play again so it was like 29/23. This caused a crash
            // Should reset to 0 and finish
            Stop();
            act.finish();
        }
        if (currentObject.CrumbData.GetDataType().equals(".jpg")) {
            currentObject.ImageView.setVisibility(View.VISIBLE);
            if (!currentObject.FinishedLoadingImages) {
                currentObject.ProgressBar.setVisibility(View.VISIBLE);
                awaitingPhotoLoad = true;
            }
            else {
                    if (videoProgressTimer == null) {
                        videoProgressTimer = new BreadcrumbsVideoProgressTimer(itemDisplayDurationProgressBar);
                    }
                    videoProgressTimer.SetTimerDuration(5000);
                    videoProgressTimer.startTimerWithCallback(GetTimerCallback());
            }
        } else {
            currentObject.PlayerWrapper.VideoSurface.setVisibility(View.VISIBLE);
            currentObject.PlayerWrapper.BuildPlayerAndSeek(true, seekingTo); // This means that it will start playing when built
        }
    }

    // A simpler, more readable version of loadObject()
    private void constructAndLoadObject(int index) {
        StoryBoardModel model = buildStoryBoard(index);
        loadObject(model);
    }

    // Begin to load story board object with the data thats on it.
    private void loadObject(StoryBoardModel model) {
       // storyBoardModels.add(model);
        if (model.CrumbData != null) {
            if (model.CrumbData.GetDataType().equals(".mp4")) {
                model.PlayerWrapper.setInfoListenerforLoading(infoListener);
                model.PlayerWrapper.setWrapperInterface(buildMeWrapperInterfaceListener());

                // Means we are local.
                if (model.CrumbData.GetIsLocal() == 0) {
                    // Load from a local datasource
                   model.PlayerWrapper.BuildLocalDatasource(Utils.FetchLocalPathToVideoFile(model.CrumbData.GetCrumbId()));
                } else {
                    // Load from external datasource
                    model.PlayerWrapper.BeginLoading(LoadBalancer.RequestCurrentDataAddress() + "/images/"+model.CrumbData.GetCrumbId()+".mp4");
                }

                // Carry on.
                model.PlayerWrapper.buildPlayer(false);
                amLoading = true;
            } else {
                startLoadingImage(model);
            }
        }
        else {
            boolean isNull = model.CrumbData == null;
            Log.d(TAG, "Models crumb data is null: " + isNull);
        }
    }

    private BreadcrumbsExoPlayer.InfoListener infoListener = new BreadcrumbsExoPlayer.InfoListener() {

        @Override
        public void onLoadCompleted(Loader.Loadable loadable) {
            doLoadCompletedCallback();
        }
    };

    private BreadcrumbsExoPlayerWrapper.WrapperInterface buildMeWrapperInterfaceListener() {
        return new BreadcrumbsExoPlayerWrapper.WrapperInterface() {
            @Override
            public void stateChangedListener(boolean playWhenReady, int state, final int id) {
                if(displayModel.PlayerWrapper.GetId() != id) {
                    return;
                }

                // Show loading symbol now that we
                if (state == ExoPlayer.STATE_BUFFERING) {
                    Log.d("BCExoplayer", "Buffering");
                    if (displayModel != null) {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                displayModel.ProgressBar.setVisibility(View.VISIBLE);
                                videoProgressTimer.StopTimer();
                            }
                        });
                    }
                }
                // Stop showing loading symbol
                if (state == ExoPlayer.STATE_READY) {
                    Log.d("BCExoplayer", "Ready to play");
                    if (displayModel != null) {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (id == displayModel.PlayerWrapper.GetId()) {
                                    Log.d("StoryBoardController", "Exoplayer ready to play video");
                                    displayModel.ProgressBar.setVisibility(View.GONE);
                                    displayModel.PlayerWrapper.VideoSurface.setVisibility(View.VISIBLE);
                                    displayModel.PlayerWrapper.VideoSurface.setAlpha(1);
                                    displayModel.PlayerWrapper.VideoSurface.setBackgroundColor(Color.TRANSPARENT);

                                    // Set up the progress timer
                                    long duration = displayModel.PlayerWrapper.player.getDuration();
                                    if (duration > 0) {
                                       videoProgressTimer.StopTimer();
                                        videoProgressTimer.SetTimerDuration((int) duration);
                                        videoProgressTimer.startTimer();
                                    }
                                }
                            }
                        });
                    }
                }
                // Loop back to start
                if (state == ExoPlayer.STATE_ENDED) {
                    Log.d("BCExoplayer", "Finshed playing mp4, looping back to start");
                    if (displayModel != null && isPlaying) {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                videoProgressTimer.StopTimer();
                                videoProgressTimer.ResetTimer();
                                moveForward();
                            }
                        });
                    }
                }
            }
        };
    }

    private void doLoadCompletedCallback() {
        amLoading = false;
        LOADED_INDEX += 1;
        if (LOADED_INDEX >= 5) {
            LOADED_INDEX = 5;
            // Just do nothing - we have loaded all we should load
            return;
        }
        // We are calling the
        constructAndLoadObject(LOADED_INDEX);
    }

    // Create a story board object
    private StoryBoardModel buildStoryBoard(int index) {
        return storyBoardModels.get(index);
    }

    private void moveForward() {
        Log.d(TAG, "Moving foreward in the list.");
        StoryBoardModel toBeRecycled = storyBoardModels.remove(0);
        // If we have added all the data - then we dont want to instatiate any more real objects, but we stil want to display
        if (mCrumbs.size() > DATA_INDEX) {
            // Move the carousel along one.
            StoryBoardModel newModel = recycle(toBeRecycled, mCrumbs.get(DATA_INDEX));
            DATA_INDEX += 1;
            storyBoardModels.add(4, newModel);
            displayNextItem(storyBoardModels.get(1), storyBoardModels.get(2));

            Log.d(TAG, "Moving forward in list. Added crumb with id: " + newModel.CrumbData.GetCrumbId());
            if (LOADED_INDEX > 2) {
                LOADED_INDEX -= 1;
            }
            // trigger the load of a new object if we are already 5 ahead.
            if (!amLoading && LOADED_INDEX < storyBoardModels.size()) {
                amLoading = true;
                Log.d(TAG, "Triggering load with index: " + LOADED_INDEX + " for storyBoard item with Id: " + storyBoardModels.get(LOADED_INDEX).CrumbData.GetCrumbId());
                constructAndLoadObject(LOADED_INDEX);
            }
        } else {
            if (VIEWING_INDEX >= mCrumbs.size()) {

                return;
            }
            StoryBoardModel blankModel = recycle(toBeRecycled, null);
            storyBoardModels.add(4, blankModel);
            displayNextItem(storyBoardModels.get(1), storyBoardModels.get(2));
            Log.d(TAG, "Moving forward in list. Added nothing as we are out of data.");
        }
    }

    // Change the visibility of the views . ie toggle one invisible and te other visible
    private void displayNextItem(StoryBoardModel currentModel, StoryBoardModel nextModel) {
        if (nextModel.CrumbData == null) {
            // Nothing we can do with nothing.
            finishedListener.onStoryboardFinished();
            return;
        }
        Log.d(TAG, "Changing visibility of imageViews.");
        if(currentModel != null) {
            Log.d(TAG, "Hiding object with ");
        }
        VIEWING_INDEX += 1;
        if (LOADED_INDEX == 2) {
            // If we skip foreward while the current item is still loading, it doesnt load the next. This here triggers the load of the next item.
            constructAndLoadObject(LOADED_INDEX);
        }
        // Updates the view.
        setCrumbCount();

        mLastObject = nextModel;
        currentModel.ImageView.setVisibility(View.GONE);
        currentModel.ProgressBar.setVisibility(View.GONE);

        if (currentModel.PlayerWrapper != null) {
            currentModel.PlayerWrapper.VideoSurface.setAlpha(0);
            //currentModel.PlayerWrapper.StopPlaying();
        }

        // Check to see if we have a wrapper and its player. If we do, release the player.
        // This means we will have to release the current models player. We should probably only stop it.
        if (currentModel.PlayerWrapper != null && currentModel.PlayerWrapper.player != null) {
            currentModel.PlayerWrapper.player.release();
            Log.d(TAG, "Releasing video player because we are no longer displaying it.");
        }

        // Set the new to be the display model for the entire class scope. Also delete the crumb data
        // Nulling the crumb data is not vital, but it stops accifental use down the line.
        displayModel = nextModel;


        // Decide if its photo.
        if (nextModel.CrumbData.GetDataType().equals(".jpg")) {
            Log.d(TAG, "Setting Visibility for item ");
            nextModel.ImageView.setVisibility(View.VISIBLE);
            if (!nextModel.FinishedLoadingImages) {
                awaitingPhotoLoad = true;
                nextModel.ProgressBar.setVisibility(View.VISIBLE);
                videoProgressTimer.StopTimer();
            } else {
                if (videoProgressTimer != null) {
                    videoProgressTimer.StopTimer();
                    videoProgressTimer.ResetTimer();
                    videoProgressTimer.SetTimerDuration(5000);
                    videoProgressTimer.startTimerWithCallback(GetTimerCallback());
                }
            }
        }
               // Decide if its video
        if (nextModel.CrumbData.GetDataType().equals(".mp4")) {
            if(nextModel.PlayerWrapper.player == null) {
                nextModel.PlayerWrapper.BeginLoading(LoadBalancer.RequestCurrentDataAddress() + "/images/" + nextModel.CrumbData.GetCrumbId() + ".mp4");
                nextModel.PlayerWrapper.buildPlayer(false); // This means that it will start playing when built
            }
            displayModel.PlayerWrapper.VideoSurface.setAlpha(1);
            nextModel.ProgressBar.setVisibility(View.VISIBLE);
            nextModel.PlayerWrapper.player.setPlayWhenReady(true); // This means that it will start playing when built
            if (videoProgressTimer != null) {
                videoProgressTimer.StopTimer();
                videoProgressTimer.ResetTimer();
                videoProgressTimer.SetTimerDuration(5000);
            }
        }

        setPlaceName();
        setDescription();
    }

    /*
      Recycle the old view object to create a new object that we can use. We recyvle a view by taking the earliest
      postion (i.e get(0) or get(-1) so that we can set it to the  opposite side, so that we can make up
      for the scrolling.
     */
    private StoryBoardModel recycle(StoryBoardModel model, CrumbCardDataObject newData) {
        // We dont want to be showing old images.
        if (model.CrumbData != null && newData != null) {
            Log.d(TAG, "Recyclingold object with Id of :" +model.CrumbData.GetCrumbId() + " to make way for new object with id: " + newData.GetCrumbId());
        }
        model.ImageView.setImageBitmap(null);
        if (model.PlayerWrapper != null) {
            if (model.PlayerWrapper.player != null) {
                model.PlayerWrapper.player.release();
                model.PlayerWrapper.player = null;
                Log.d(TAG, "Released player and set it to null");
            }
        }

        Log.d(TAG, "Finished recycling");
        return new StoryBoardModel(loadingIndex, newData, model.ImageView, model.ProgressBar,
                new BreadcrumbsExoPlayerWrapper(model.PlayerWrapper.VideoSurface, mContext, DATA_INDEX));
    }

    // Set the move forward button. This needs to change
    public void SetForewardButton(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 moveForward();
            }
        });
    }

    private void loadLocalImageFile(final StoryBoardModel storyBoardModel) {
        final String imageUrl = Utils.FetchLocalPathToImageFile(storyBoardModel.CrumbData.GetCrumbId());
        final File imageFile = new File(imageUrl);
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Picasso.with(mContext).load(imageFile).into(storyBoardModel.ImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Need to know if it has already loaded in the background.
                        storyBoardModel.FinishedLoadingImages = true;
                        storyBoardModel.ProgressBar.setVisibility(View.GONE);
                        Log.d(TAG, "Successfully loaded image with Id: " + storyBoardModel.CrumbData.GetCrumbId());

                        // Check if the user is waiting for the image to load.
                        checkForWaiting();

                        doLoadCompletedCallback();
                        // do loading callback
                    }

                    @Override
                    public void onError() {
                        storyBoardModel.FinishedLoadingImages = true;
                        storyBoardModel.ProgressBar.setVisibility(View.GONE);
                        Log.d(TAG, "Failed to load Image with Id: " + storyBoardModel.CrumbData.GetCrumbId());
                        doLoadCompletedCallback();
                    }
                });
            }
        });
    }

    // This begins loading an image
    private void startLoadingImage(final StoryBoardModel storyBoardModel) {
        if (storyBoardModel.CrumbData.GetIsLocal() == 0) {
            // Load local file
            loadLocalImageFile(storyBoardModel);
            return;
        }
        final String imageUrl = LoadBalancer.RequestCurrentDataAddress() + "/images/"+storyBoardModel.CrumbData.GetCrumbId()+".jpg";
        Log.d(TAG, "Attempting to load Image with url: " + imageUrl);

        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Picasso.with(mContext).load(imageUrl).into(storyBoardModel.ImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Need to know if it has already loaded in the background.
                        storyBoardModel.FinishedLoadingImages = true;
                        storyBoardModel.ProgressBar.setVisibility(View.GONE);
                        Log.d(TAG, "Successfully loaded image with Id: " + storyBoardModel.CrumbData.GetCrumbId());

                        // Check if the user is waiting for the image to load.
                        checkForWaiting();

                        doLoadCompletedCallback();
                        // do loading callback
                    }

                    @Override
                    public void onError() {
                        storyBoardModel.FinishedLoadingImages = true;
                        storyBoardModel.ProgressBar.setVisibility(View.GONE);
                        Log.d(TAG, "Failed to load Image with Id: " + storyBoardModel.CrumbData.GetCrumbId());
                        // Not sure I should
                        doLoadCompletedCallback();
                    }
                });
            }
        });
    }

    // Clean up the image and video loading.
    private void cleanUp() {
        Picasso.with(displayModel.ImageView.getContext())
                .cancelRequest(displayModel.ImageView);
    }

    public int GetCurrentIndex() {
        if (VIEWING_INDEX +1 < mCrumbs.size()) {
            return VIEWING_INDEX;
        }
        return 0;
    }

    private void checkForWaiting() {
        // If we are waiting, we need to set up the duration timer
        if (awaitingPhotoLoad) {
            videoProgressTimer.ResetTimer();
            videoProgressTimer.SetTimerDuration(5000);
            videoProgressTimer.startTimerWithCallback(GetTimerCallback());
            awaitingPhotoLoad = false;
        }
    }

    //
    private void setPlaceName() {
        if (placeName == null) {
            placeName = (TextView) act.findViewById(R.id.place_name);
        }

        if(mLastObject.CrumbData != null) {
            placeName.setText(mLastObject.CrumbData.GetPlaceName());
        }
    }

    private void setDescription() {
        TextView caption = (TextView) act.findViewById(R.id.caption);
        if (mLastObject.CrumbData != null) {
            RelativeLayout relativeLayout = (RelativeLayout) act.findViewById(R.id.root);
            TextView description = (TextView) act.findViewById(R.id.floating_description);

            description.setText(mLastObject.CrumbData.GetDescripton());
            Display dm = act.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            dm.getSize(size);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) description.getLayoutParams();
            layoutParams.leftMargin = (int) (size.x * mLastObject.CrumbData.GetDescriptionXPosition());
            layoutParams.topMargin =  (int) (size.y * mLastObject.CrumbData.GetDescriptionYPosition());
            layoutParams.rightMargin = -250;
            layoutParams.bottomMargin = -250;
            description.setLayoutParams(layoutParams);

        }
    }

    private void setCrumbCount() {
//        if (crumbCount == null) {
//            crumbCount = (TextView) act.findViewById(R.id.crumb_count);
//        }
//
//        crumbCount.setText(Integer.toString(VIEWING_INDEX + 1) + "/" + Integer.toString(mCrumbs.size()));
    }

    public CrumbCardDataObject GetCurrentObject() {
        return mLastObject.CrumbData;
    }

    public int GetTimerPosition() {
        return videoProgressTimer.GetDisplayTimer();
    }

    public int GetDisplayObjectType() {
        if (mLastObject != null) {
            if (mLastObject.CrumbData.GetDataType().equals(".jpg")) {
                return 0;
            } else {
                return 1; // For video
            }
        }
        return -1; // This is an error
    }

    /*
        Deletes the item that we are currently looking at in our storyboard.
    */
    public void DeleteCurrentItem() {
        // Local use case
        if (displayModel.CrumbData != null && displayModel.CrumbData.GetIsLocal() == 0) {
            doLocalDelete(displayModel);
            deletedCrumbs.add(displayModel.CrumbData);
            moveForward();
        } else if (displayModel.CrumbData != null && displayModel.CrumbData.GetIsLocal() == 1) {
            doServerDelete(displayModel);
        }
    }

    private void doLocalDelete(StoryBoardModel itemToDelete) {
        // Delete from db
        if (itemToDelete!=null && itemToDelete.CrumbData != null) {
            DatabaseController databaseController = new DatabaseController(mContext);
            databaseController.DeleteCrumb(itemToDelete.CrumbData.GetCrumbId());
            // Set the counter back to 1, and make the previous one null.
            Log.d("StoryBoardController", "Deleting crumb with id : " + itemToDelete.CrumbData.GetCrumbId());
            itemToDelete = null;
        }
    }

    private void doServerDelete(StoryBoardModel itemDoDelete) {
        // Send request to server to delete our crumb. This should be done by crumb Id as well
    }

    public ArrayList<CrumbCardDataObject> GetDeletedCrumbs() {
        return deletedCrumbs;
    }

    /*
        Used to stop the timer, and stop the playing through of videos/photos etc.
     */
    public void Stop() {
        videoProgressTimer.GetDisplayTimer();
        videoProgressTimer.StopTimer();
        isPlaying = false;
        if (displayModel == null) {
            return;
        }
        BreadcrumbsExoPlayerWrapper wrapper = displayModel.PlayerWrapper;
        if (wrapper != null && wrapper.player != null) {
            wrapper.player.Stop();
            wrapper.player.setPlayWhenReady(false);
            wrapper.player.release(); // Player must not be used after calling this.
        }
        cleanUp();
    }

    // Pause playback if the storyboard.
    public void Pause() {
        videoProgressTimer.setTimerPauseState(true);
        if (displayModel.PlayerWrapper.player != null) {
           // displayModel.PlayerWrapper.player.Stop();
            displayModel.PlayerWrapper.player.setPlayWhenReady(false);
        }
    }

    public void Unpause() {
        videoProgressTimer.setTimerPauseState(false);

        if (displayModel.PlayerWrapper != null && displayModel.PlayerWrapper.player != null) {
            displayModel.PlayerWrapper.player.setPlayWhenReady(true);
        }
    }

    public void Play() {
        if (displayModel.PlayerWrapper.player != null) {
            displayModel.PlayerWrapper.player.setPlayWhenReady(true);
        }
    }
}
