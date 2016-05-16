package com.teamunemployment.breadcrumbs.client.StoryBoard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.teamunemployment.breadcrumbs.Dialogs.IDialogCallback;
import com.teamunemployment.breadcrumbs.Dialogs.SimpleMaterialDesignDialog;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.Cards.CrumbCardDataObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by jek40 on 26/04/2016.
 */
public class StoryBoardActivity extends Activity {
    private ArrayList<CrumbCardDataObject> mCrumbObjects;
    private CrumbCardDataObject mLastObject;
    private StoryBoardController storyBoardController;
    private CrumbCardDataObject mFirstObject;
    private Context context;

    private int index = 0;
    private int timer = 0;

    private boolean userOwnsTrail = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.story_board_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Fade fade = new Fade();
            fade.setDuration(1000);
            getWindow().setEnterTransition(fade);
        }
        Intent intent = getIntent();
        mCrumbObjects = intent.getParcelableArrayListExtra("CrumbArray");
        mFirstObject = intent.getParcelableExtra("StartingObject");
        index = intent.getIntExtra("Index", -1);
        timer = intent.getIntExtra("Timer", 0);
        userOwnsTrail = intent.getBooleanExtra("UserOwnsTrail", false);
        if (savedInstanceState !=  null) {
            Log.d("STORYBOARD_ACTIVITY", "Found saved state from ");
            restoreInstanceState(savedInstanceState);
        }
        initialiseStoryBoardAdapter();
        setUpDeleteButton();
        storyBoardController.SetUpRestoredTimer(timer);
        storyBoardController.Start();
    }

    private void restoreInstanceState(Bundle instanceState) {
        // Not sure what we are going to do here.
        index = instanceState.getInt("Index");
        timer = instanceState.getInt("TimerPosition");
    }

    private void initialiseStoryBoardAdapter() {
        ImageView image01 = (ImageView) findViewById(R.id.image_01);
        ImageView image02 = (ImageView) findViewById(R.id.image_02);
        ImageView image03 = (ImageView) findViewById(R.id.image_03);
        ImageView image04 = (ImageView) findViewById(R.id.image_04);
        ImageView image05 = (ImageView) findViewById(R.id.image_05);

        ArrayList<ImageView> imageViews = new ArrayList<>();
        imageViews.add(0, image01);
        imageViews.add(1, image02);
        imageViews.add(2, image03);
        imageViews.add(3, image04);
        imageViews.add(4, image05);

        if (index == -1) {
            index = fetchIndex();
        }

        storyBoardController = new StoryBoardController(mCrumbObjects, imageViews, this, index);
        FrameLayout frameLayoutOverlay = (FrameLayout) findViewById(R.id.storyboard_overlay);
        storyBoardController.SetForewardButton(frameLayoutOverlay);

        // Listener for when we return to te
        setMapButtonClickListener();
    }

    private void setUpDeleteButton() {
        FloatingActionButton deleteFab = (FloatingActionButton) findViewById(R.id.delete_crumb);
        if (userOwnsTrail) {
            deleteFab.setVisibility(View.VISIBLE);
            // Show popup
            deleteFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SimpleMaterialDesignDialog.Build(context)
                            .SetTitle("Really?")
                            .SetTextBody("Are you sure you want to delete this photo? \n This action is not reversible.")
                            .SetActionWording("DELETE")
                            .SetCallBack(GetDeleteCallback())
                            .Show();
                }
            });


        } else {
            deleteFab.setVisibility(View.GONE);
        }
    }

    /*
        Fetch a callback for the delete dialog.
     */
    private IDialogCallback GetDeleteCallback() {
        return new IDialogCallback() {
            @Override
            public void DoCallback() {
                storyBoardController.DeleteCurrentItem();

            }
        };
    }

    private int fetchIndex() {
        Iterator<CrumbCardDataObject> crumbCardDataObjectIterator = mCrumbObjects.iterator();
        while (crumbCardDataObjectIterator.hasNext()) {
            CrumbCardDataObject tempObject = crumbCardDataObjectIterator.next();
            if (mFirstObject.GetCrumbId().equals(tempObject.GetCrumbId())) {
                return mCrumbObjects.indexOf(tempObject);
            }
        }

        // Didnt find it, so we dont really have an option but to start from the start.
        return 0;
    }

    private void setMapButtonClickListener() {
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.back_to_map);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do finish shit()
                doFinishShit();
            }
        });
    }

    // Pass back variables like last object, index and timer position so that we can pass them back if we hit the play button again
    private void doFinishShit() {
        Intent intent = new Intent();
        intent.putExtra("LastObject", storyBoardController.GetCurrentObject());
        intent.putExtra("Index", storyBoardController.GetCurrentIndex());
        intent.putExtra("TimerPosition", storyBoardController.GetTimerPosition());

        storyBoardController.Stop();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        outState.putInt("Index", storyBoardController.GetCurrentIndex());
//        outState.putInt("TimerPosition", storyBoardController.GetTimerPosition()); // This will be used for both video and image
//        outState.putParcelable("LastObect", storyBoardController.GetCurrentObject());
        super.onSaveInstanceState(outState);

    }

}
