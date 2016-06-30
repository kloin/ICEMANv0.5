package com.teamunemployment.breadcrumbs.client.Animations;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;

import com.teamunemployment.breadcrumbs.R;

/**
 * Created by jek40 on 21/02/2016.
 */
public class SimpleAnimations {

    // Fade out a given view
    public static void FadeOutView(View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        //fadeOut.setStartOffset(1000);
        fadeOut.setDuration(500);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeOut);
        view.setAnimation(animation);
        view.setVisibility(View.GONE);
    }

    public static void AlphaFadeInView(View view) {

    }
    // Fade in a given view.
    public static void FadeInView(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(1000);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        view.setAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }

    /**
     * Shrink a {@link FloatingActionButton} into a state of invisibility, over a given duration
     * @param fab The fab to hide.
     * @param duration The duration of the animation in milliseconds.
     */
    public static void ShrinkFab(final FloatingActionButton fab, int duration) {
        fab.clearAnimation();
        // Scale down animation
        ScaleAnimation shrink =  new ScaleAnimation(1f, 0.2f, 1f, 0.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(duration);     // animation duration in milliseconds
        shrink.setInterpolator(new DecelerateInterpolator());
        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Change FAB color and icon
                // fab.setBackgroundTintList(context.getResources().getColorStateList(colorIntArray[position]));
                // fab.setImageDrawable(context.getResources().getDrawable(iconIntArray[position], null));
                fab.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fab.startAnimation(shrink);
    }

    /**
     * Change a fab from being invisible to visible with an expand animation
     * @param fab The fab to expand.
     * @param duration The duration of the
     */
    public static void ExpandFab(FloatingActionButton fab, int duration) {
        fab.setVisibility(View.VISIBLE);
        // Scale up animation
        ScaleAnimation expand = new ScaleAnimation(0.2f, 1f, 0.2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        expand.setDuration(duration);     // animation duration in milliseconds
        expand.setInterpolator(new AccelerateInterpolator());
        fab.startAnimation(expand);
    }

    public static void FadeInViewWithSetDuration(View view, int duration) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(duration);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        view.setAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }


    public static void ShrinkToggleAFab(final FloatingActionButton floatingActionButton, final String colorToChangeTo, final Drawable newDrawableReference) {
        floatingActionButton.clearAnimation();
        ScaleAnimation shrink =  new ScaleAnimation(1f, 0.2f, 1f, 0.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(150);     // animation duration in milliseconds
        shrink.setInterpolator(new DecelerateInterpolator());
        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colorToChangeTo)));
                floatingActionButton.setImageDrawable(newDrawableReference);
                ScaleAnimation expand = new ScaleAnimation(0.2f, 1f, 0.2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                expand.setDuration(100);     // animation duration in milliseconds
                expand.setInterpolator(new AccelerateInterpolator());
                floatingActionButton.startAnimation(expand);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        floatingActionButton.startAnimation(shrink);
    }

    public static void ShrinkUnshrinkStandardFab(final FloatingActionButton floatingActionButton) {

        // do first half of shink
        floatingActionButton.clearAnimation();
        ScaleAnimation shrink =  new ScaleAnimation(1f, 0.2f, 1f, 0.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(150);     // animation duration in milliseconds
        shrink.setInterpolator(new DecelerateInterpolator());
        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                // Change FAB color and icon
                // Scale up animation
                // expand back to full size
                ScaleAnimation expand = new ScaleAnimation(0.2f, 1f, 0.2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                expand.setDuration(100);     // animation duration in milliseconds
                expand.setInterpolator(new AccelerateInterpolator());
                floatingActionButton.startAnimation(expand);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        floatingActionButton.startAnimation(shrink);

    }

}


