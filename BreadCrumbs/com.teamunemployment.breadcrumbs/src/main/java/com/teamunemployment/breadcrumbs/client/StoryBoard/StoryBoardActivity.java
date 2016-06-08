package com.teamunemployment.breadcrumbs.client.StoryBoard;

import android.app.Activity;
import android.content.Context;

import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs._HAX.ExceptionHandler;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.Cards.CrumbCardDataObject;

import java.util.ArrayList;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.teamunemployment.breadcrumbs.Dialogs.IDialogCallback;
import com.teamunemployment.breadcrumbs.Dialogs.SimpleMaterialDesignDialog;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.client.Animations.GUIUtils;
import com.teamunemployment.breadcrumbs.client.Animations.OnRevealAnimationListener;
import com.teamunemployment.breadcrumbs.client.Cards.CrumbCardDataObject;

import java.util.ArrayList;
import java.util.Iterator;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by jek40 on 26/04/2016.
 */
public class StoryBoardActivity extends Activity {
    RelativeLayout mRlContainer;
    RelativeLayout mLlContainer;

    FloatingActionButton mFab;
    @Bind(R.id.back_to_map) FloatingActionButton backFab;
    private ArrayList<CrumbCardDataObject> mCrumbObjects;
    private CrumbCardDataObject mLastObject;
    private StoryBoardController storyBoardController;
    private CrumbCardDataObject mFirstObject;
    private Context context;

    private int index = 0;
    private int timer = 0;

    private boolean paused = false;

    private boolean userOwnsTrail = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.story_board_view);
        mRlContainer = (RelativeLayout) findViewById(R.id.root);
        mLlContainer = (RelativeLayout) findViewById(R.id.activity_storyboard_ll_container);
        mFab = (FloatingActionButton) findViewById(R.id.activity_contact_fab);
        setUpPauseButton();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setupEnterAnimation();
            setupExitAnimation();
        } else {
            initViews();
        }

        Intent intent = getIntent();
        Log.d("TIME", "Loading parcelable array List start at: " + SystemClock.currentThreadTimeMillis());
        mCrumbObjects = intent.getParcelableArrayListExtra("CrumbArray");
        Log.d("TIME", "Loading parcelable array List finish at: " + SystemClock.currentThreadTimeMillis());
        mFirstObject = intent.getParcelableExtra("StartingObject");
        Log.d("TIME", "Loading parcelable array starting object finish at: " + SystemClock.currentThreadTimeMillis());

        index = intent.getIntExtra("Index", -1);
        timer = intent.getIntExtra("Timer", 0);
        userOwnsTrail = intent.getBooleanExtra("UserOwnsTrail", false);
        if (savedInstanceState !=  null) {
            Log.d("STORYBOARD_ACTIVITY", "Found saved state from ");
            restoreInstanceState(savedInstanceState);
        }
        Log.d("TIME", "Starting set up of storyboard adapter: " + SystemClock.currentThreadTimeMillis());
        initialiseStoryBoardAdapter();
        Log.d("TIME", "Finished set up of storyboard adapter: " + SystemClock.currentThreadTimeMillis());
        setUpDeleteButton();
        setUpNextClickListener();
        setUpBackButton();
        Log.d("TIME", "Buttons set up complete: " + SystemClock.currentThreadTimeMillis());
    }

    /**
     * Anumation setup.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupEnterAnimation() {
        Transition transition = TransitionInflater.from(this)
                .inflateTransition(R.transition.changebounds_with_arcmotion);
        getWindow().setSharedElementEnterTransition(transition);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                transition.removeListener(this);
                animateRevealShow(mRlContainer);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
    }

    private void animateRevealShow(final View viewRoot) {
        int cx = (viewRoot.getLeft() + viewRoot.getRight()) / 2;
        int cy = (viewRoot.getTop() + viewRoot.getBottom()) / 2;
        GUIUtils.animateRevealShow(this, mRlContainer, mFab.getWidth() / 2, R.color.accent,
                cx, cy, new OnRevealAnimationListener() {
                    @Override
                    public void onRevealHide() {

                    }

                    @Override
                    public void onRevealShow() {
                        initViews();
                    }
                });
    }

    private void initViews() {
        new Handler(Looper.getMainLooper()).post(task);
    }

    private Runnable task = new Runnable() {
        @Override
        public void run() {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
           animation.setDuration(300);
            storyBoardController.SetUpRestoredTimer(timer);
            storyBoardController.Start();
            mLlContainer.startAnimation(animation);
            mLlContainer.setVisibility(View.VISIBLE);
        }
    };

    /**
     * Restore where we are in the in the video.
     * @param instanceState
     */
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

        storyBoardController = new StoryBoardController(mCrumbObjects, imageViews, this, index, getFinshedListener());

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
                    storyBoardController.Pause();
                    SimpleMaterialDesignDialog.Build(context)
                            .SetTitle("Really?")
                            .SetTextBody("Are you sure you want to delete this photo? \n This action is not reversible.")
                            .SetActionWording("DELETE")
                            .SetCallBack(GetDeleteCallback())
                            .SetCancelCallback(GetCancelDialogCallback())
                            .Show();
                }
            });
        } else {
            deleteFab.setVisibility(View.GONE);
        }
    }

    private IDialogCallback GetCancelDialogCallback() {
        return new IDialogCallback() {
            @Override
            public void DoCallback() {
                storyBoardController.Unpause();

            }
        };
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
        backFab = (FloatingActionButton) findViewById(R.id.back_to_map);
        backFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do finish shit()
                doFinishShit();
            }
        });
    }

    private void setUpNextClickListener() {
        FloatingActionButton nextButton = (FloatingActionButton) findViewById(R.id.next_storyboard_item);
        storyBoardController.SetForewardButton(nextButton);
    }

    // Pass back variables like last object, index and timer position so that we can pass them back if we hit the play button again
    private void doFinishShit() {
        Intent intent = new Intent();
        intent.putExtra("LastObject", storyBoardController.GetCurrentObject());
        intent.putExtra("Index", storyBoardController.GetCurrentIndex());
        intent.putExtra("TimerPosition", storyBoardController.GetTimerPosition());
        intent.putParcelableArrayListExtra("DeletedCrumbs", storyBoardController.GetDeletedCrumbs());
        setResult(RESULT_OK, intent);
       // finish();
    }

    @Override
    public void onBackPressed() {
        backPressedHandler();
    }

    // Set the listener for the pause button
    private void setUpBackButton() {
        FloatingActionButton pauseButton = (FloatingActionButton) findViewById(R.id.back_to_map);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backPressedHandler();
            }
        });
    }

    // We have a back pressed handler as we have two entry points to this bit of code.
    private void backPressedHandler() {
        storyBoardController.Stop();
        // finish();
        GUIUtils.animateRevealHide(context, mRlContainer, R.color.white, mFab.getWidth() / 2,
                new OnRevealAnimationListener() {
                    @Override
                    public void onRevealHide() {
                        mLlContainer.setVisibility(View.GONE);
                        Intent intent = new Intent();
                        intent.putExtra("LastObject", storyBoardController.GetCurrentObject());
                        intent.putExtra("Index", storyBoardController.GetCurrentIndex());
                        intent.putExtra("TimerPosition", storyBoardController.GetTimerPosition());
                        setResult(RESULT_OK, intent);
                        //finish();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            backPressed();
                        } else {
                            finish();
                        }
                        //backPressed();
                    }

                    @Override
                    public void onRevealShow() {
                        Toast.makeText(context, "Showing", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void backPressed() {
        super.onBackPressed();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupExitAnimation() {
        Fade fade = new Fade(1);
        getWindow().setExitTransition(fade);
        getWindow().setReturnTransition(fade);
        fade.setDuration(300);
    }

    @OnClick(R.id.back_to_map)
    public void onIvCloseClicked() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            onBackPressed();
        } else {
            backPressed();
        }
    }

    private void pause() {
        storyBoardController.Pause();
    }

    private void setUpPauseButton() {
        final Drawable playDrawable = context.getResources().getDrawable(R.drawable.exomedia_ic_play_arrow_black);
        final Drawable pauseDrawable = context.getResources().getDrawable(R.drawable.exomedia_ic_pause_black);
        final FloatingActionButton pauseFAB =  (FloatingActionButton) findViewById(R.id.pause_story_board);
        pauseFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paused) {
                    pauseFAB.setImageDrawable(pauseDrawable);
                    storyBoardController.Unpause();
                    paused = false;
                    return;
                }
                paused = true;
                pauseFAB.setImageDrawable(playDrawable);
                storyBoardController.Pause();
            }
        });
    }

    private StoryBoardController.FinishedListener getFinshedListener() {
        return new StoryBoardController.FinishedListener() {
            @Override
            public void onStoryboardFinished() {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    backPressedHandler();
                } else {
                    backPressed();
                }
            }
        };
    }
}
