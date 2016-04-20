package com.teamunemployment.breadcrumbs.RandomUsefulShit;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;

import com.teamunemployment.breadcrumbs.R;

/**
 * Created by jek40 on 18/04/2016.
 */
public class FABShrinker {

    private static final int FAB_ANIM_DURATION = 200;
    private FloatingActionButton mFab;
    private Context mContext;
    private Activity activity;

    public FABShrinker(FloatingActionButton fab, Context context) {
        mContext = context;
        mFab = fab;
    }


    public void hide() {
        // Only use scale animation if FAB is visible
        if (mFab.getVisibility() == View.VISIBLE) {
            // Pivots indicate where the animation begins from
            float pivotX = mFab.getPivotX() + mFab.getTranslationX();
            float pivotY = mFab.getPivotY() + mFab.getTranslationY();

            // Animate FAB shrinking
            ScaleAnimation anim = new ScaleAnimation(1, 0, 1, 0, pivotX, pivotY);
            anim.setDuration(FAB_ANIM_DURATION);
            anim.setInterpolator(getInterpolator());
            mFab.startAnimation(anim);
        }
        mFab.setVisibility(View.INVISIBLE);
    }


    public void show() {
        show(0, 0);
    }

    public void show(float translationX, float translationY) {

        // Set FAB's translation
        setTranslation(translationX, translationY);

        // Only use scale animation if FAB is hidden
        if (mFab.getVisibility() != View.VISIBLE) {
            // Pivots indicate where the animation begins from
            float pivotX = mFab.getPivotX() + translationX;
            float pivotY = mFab.getPivotY() + translationY;

            ScaleAnimation anim;
            // If pivots are 0, that means the FAB hasn't been drawn yet so just use the
            // center of the FAB
            if (pivotX == 0 || pivotY == 0) {
                anim = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
            } else {
                anim = new ScaleAnimation(0, 1, 0, 1, pivotX, pivotY);
            }

            // Animate FAB expanding
            anim.setDuration(FAB_ANIM_DURATION);
            anim.setInterpolator(getInterpolator());
            mFab.startAnimation(anim);
        }
        mFab.setVisibility(View.VISIBLE);
    }

    private void setTranslation(float translationX, float translationY) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            mFab.animate().setInterpolator(getInterpolator()).setDuration(FAB_ANIM_DURATION)
                    .translationX(translationX).translationY(translationY);
        }
    }

    private Interpolator getInterpolator() {
        return null;// AnimationUtils.loadInterpolator(mContext,);
    }
}
