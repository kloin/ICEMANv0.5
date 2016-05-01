package com.teamunemployment.breadcrumbs.client.StoryBoard;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.Cards.CrumbCardDataObject;

import java.util.ArrayList;

/**
 * Created by jek40 on 26/04/2016.
 */
public class StoryBoardController {
    private static ArrayList<ImageView> images = new ArrayList<>();
    private final ArrayList<ProgressBar> mProgressBars = new ArrayList<ProgressBar>();
    private final ArrayList<SurfaceView> videoHolders = new ArrayList<>();
    private ArrayList<StoryBoardModel> storyBoardModels = new ArrayList<>();
    private int mViewIndex = 0;
    private int mFrontIndex = 0;
    private ArrayList<CrumbCardDataObject> mCrumbs;
    private Context mContext;
    private final String TAG = "StoryBoardController";
    private StoryBoardModel mLastObject;
    private ProgressBar mProgressBar;
    private Activity act;

    public StoryBoardController(ArrayList<CrumbCardDataObject> crumbs, ArrayList<ImageView> imageViews, Context context, int index) {
        mCrumbs = crumbs;
        images = imageViews;
        mContext = context;
        mFrontIndex = index;
        act = (Activity) mContext;
        buildObjects();
        init();
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

        SurfaceView video1 = (SurfaceView) act.findViewById(R.id.video_01);
        SurfaceView video2 = (SurfaceView) act.findViewById(R.id.video_02);
        SurfaceView video3 = (SurfaceView) act.findViewById(R.id.video_03);
        SurfaceView video4 = (SurfaceView) act.findViewById(R.id.video_04);
        SurfaceView video5 = (SurfaceView) act.findViewById(R.id.video_05);

        videoHolders.add(video1);
        videoHolders.add(video2);
        videoHolders.add(video3);
        videoHolders.add(video4);
        videoHolders.add(video5);
    }

    private void init() {
        int count = 2;
        // First two objects in the list are just blank objects that will be recycled as we go through.
        StoryBoardModel model1 = new StoryBoardModel(0, null, images.get(0), mProgressBars.get(0));
        StoryBoardModel model2 = new StoryBoardModel(1, null, images.get(1), mProgressBars.get(1));

        //Add our two blank objects to the from of the list for later use.
        storyBoardModels.add(0,model1);
        storyBoardModels.add(1, model2);

        // Initialise the remaining objects with actual data and
        while (count < 5) {
            if (mCrumbs.size() > mFrontIndex) {
                StoryBoardModel storyBoardModel = new StoryBoardModel(count, mCrumbs.get(mFrontIndex), images.get(count), mProgressBars.get(count));
                mLastObject = storyBoardModel;
                startLoadingImage(storyBoardModel);
                storyBoardModels.add(count, storyBoardModel);
            } else {

                // We have no data to add, so we need to just add nulls that will do nothing.
                StoryBoardModel storyBoardModel = new StoryBoardModel(count, null, images.get(count), mProgressBars.get(count));
                storyBoardModels.add(count, storyBoardModel);
            }

            count += 1;
            mFrontIndex += 1;
        }
    }


    private void moveForward() {
        StoryBoardModel toBeRecycled = storyBoardModels.remove(0);

        // If we have added all the data - then we dont want to instatiate any more real objects, but we stil want to display
        if (mCrumbs.size() > mFrontIndex) {
            StoryBoardModel newModel = recycle(toBeRecycled, mCrumbs.get(mFrontIndex));
            storyBoardModels.add(4, newModel);
            displayNextItem(storyBoardModels.get(1), storyBoardModels.get(2));
             Log.d(TAG, "Moving forward in list. Added crumb with id: " + newModel.CrumbData.GetCrumbId());

            // Start Loading
            if (newModel.CrumbData.GetDataType().equals(".jpg")) {
                startLoadingImage(newModel);
            } else {
                startLoadingVideo(newModel);
            }
            mFrontIndex += 1;
        } else {
            StoryBoardModel blankModel = recycle(toBeRecycled, null);
            storyBoardModels.add(4, blankModel);
            displayNextItem(storyBoardModels.get(1), storyBoardModels.get(2));
            Log.d(TAG, "Moving forward in list. Added nothing as we are out of data.");
            mFrontIndex += 1;
            if (mFrontIndex == mCrumbs.size()+3) {
                act.finish();
            }
            // Need to exit here.
        }
    }

    private void displayNextItem(StoryBoardModel currentModel, StoryBoardModel nextModel) {
        Log.d(TAG, "Changing visibility of imageViews.");
        mLastObject = nextModel;
        currentModel.ImageView.setVisibility(View.GONE);

        nextModel.ImageView.setVisibility(View.VISIBLE);
        if (!nextModel.FinishedLoadingImages) {
            nextModel.ProgressBar.setVisibility(View.VISIBLE);
        }
        //nextModel.ProgressBar.setVisibility(View.VISIBLE);
    }

    private void startLoadingVideo(final StoryBoardModel storyBoardModel) {
        String videoUrl = LoadBalancer.RequestCurrentDataAddress() + "/images/" + storyBoardModel.CrumbData.GetCrumbId() + ".mp4";
        Log.d(TAG, "Attempting to load Video with url: " +videoUrl);
        // Need to load in the background here.
    }
    /*
      Recycle the old view object to create a new object that we
     */
    private StoryBoardModel recycle(StoryBoardModel model, CrumbCardDataObject newData) {
        // We dont want to be showing old images.
        model.ImageView.setImageBitmap(null);
        return new StoryBoardModel(mFrontIndex, newData, model.ImageView, model.ProgressBar);
    }

    public void SetButton(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                moveForward();
            }
        });
    }

    // This begins loading an image
    private void startLoadingImage(final StoryBoardModel storyBoardModel) {
        String imageUrl = LoadBalancer.RequestCurrentDataAddress() + "/images/"+storyBoardModel.CrumbData.GetCrumbId()+".jpg";
        Log.d(TAG, "Attempting to load Image with url: " +imageUrl);

        Picasso.with(mContext).load(imageUrl).into(storyBoardModel.ImageView, new Callback() {
            @Override
            public void onSuccess() {
                // Need to know if it has already loaded in the background.
                storyBoardModel.FinishedLoadingImages = true;
                storyBoardModel.ProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                Toast.makeText(mContext, "An error occured loading the image. This may be a connection issue, or this shitty android cant display images without running out of memory", Toast.LENGTH_LONG).show();
                storyBoardModel.FinishedLoadingImages = true;
                storyBoardModel.ProgressBar.setVisibility(View.GONE);
            }
        });
    }

    public CrumbCardDataObject GetCurrentObject() {
        return mLastObject.CrumbData;
    }
}
