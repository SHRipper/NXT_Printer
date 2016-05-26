package de.lddt.zeichenroboterapp.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.view.animation.Animation;

import de.lddt.zeichenroboterapp.R;
import de.lddt.zeichenroboterapp.core.DrawView;

/**
 * This class is a wrapper for the ValueAnimator we use to animate the clearing of the drawView
 */
public class ColorAnimator extends Animation {
    DrawView drawView;
    ValueAnimator colorAnimation;

    public ColorAnimator(DrawView drawView, int colorFrom, int colorTo, int repeatMode, int repeatCount) {
        // get an instance of ValueAnimator
        this.colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),colorFrom, colorTo);
        colorAnimation.setRepeatMode(repeatMode);
        colorAnimation.setRepeatCount(repeatCount);

        this.drawView = drawView;
    }

    public void start(int duration) {
        // set the listeners for the animation
        colorAnimation.addUpdateListener(updateListener);
        colorAnimation.addListener(animatorListenerAdapter);

        // set a duration and start
        colorAnimation.setDuration(duration);
        colorAnimation.start();
    }

    private ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            // if the animated value, e.g. the color, changes, the drawView should
            // set the curren color as background color
            drawView.setBackgroundColor((int) animation.getAnimatedValue());
        }
    };

    private AnimatorListenerAdapter animatorListenerAdapter = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            // since the border of the drawView gets lost in the animating process
            // set it again after the process finished
            drawView.setBackgroundResource(R.drawable.draw_view_background);
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            super.onAnimationRepeat(animation);
            // this method is called when the animation value has reached
            // "colorTo" and is now changing again to "colorFrom"
            drawView.clear();
        }
    };
}
