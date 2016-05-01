package com.teamunemployment.breadcrumbs.client.StoryBoard;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.transition.Fade;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

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
    private StoryBoardController mStoryBoardController;
    private CrumbCardDataObject mFirstObject;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.story_board_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Fade fade = new Fade();
            fade.setDuration(1000);
            getWindow().setEnterTransition(fade);
        }
        mCrumbObjects = getIntent().getParcelableArrayListExtra("CrumbArray");
        mFirstObject = getIntent().getParcelableExtra("StartingObject");
        initialiseStoryBoardAdapter();
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


        int index = fetchIndex();

        mStoryBoardController = new StoryBoardController(mCrumbObjects, imageViews, this, index);
        FrameLayout frameLayoutOverlay = (FrameLayout) findViewById(R.id.storyboard_overlay);
        mStoryBoardController.SetButton(frameLayoutOverlay);
        setMapButtonClickListener();
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
                Intent intent = new Intent();
                intent.putExtra("LastObject",mStoryBoardController.GetCurrentObject());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }


}
