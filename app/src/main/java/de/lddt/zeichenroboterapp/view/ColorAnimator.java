package de.lddt.zeichenroboterapp.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.view.animation.Animation;

import de.lddt.zeichenroboterapp.R;

/**
 * This class is a wrapper for the ValueAnimator we use to animate the clearing of the drawView
 */
class ColorAnimator extends Animation {
    private final DrawView drawView;
    private final ValueAnimator colorAnimation;

    /**
     * Instantiates the ColorAnimator with the given inputs.
     *
     * @param drawView is the affected view
     * @param colorFrom is the current color the background
     * @param colorTo is the color the background should get to
     * @param repeatMode describes how the animation should be repeated
     * @param repeatCount is the repetition count of the animation
     */
    public ColorAnimator(DrawView drawView, int colorFrom, int colorTo, int repeatMode, int repeatCount) {
        // get an instance of ValueAnimator
        this.colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),colorFrom, colorTo);
        colorAnimation.setRepeatMode(repeatMode);
        colorAnimation.setRepeatCount(repeatCount);

        this.drawView = drawView;
    }

    /**
     * Starts the animation with the given duration
     *
     * @param duration is the duration of the animation
     */
    public void start(int duration) {
        // set the listeners for the animation
        colorAnimation.addUpdateListener(updateListener);
        colorAnimation.addListener(animatorListenerAdapter);

        // set a duration and start
        colorAnimation.setDuration(duration);
        colorAnimation.start();
    }

    /**
     * Listener which is called every time the color value of the animated color changes
     *
     *
     */
    private final ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
        /**
         * <p>Notifies the occurrence of another frame of the animation.</p>
         *
         * @param animation is the animation that the listener is linked to
         */
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            // if the animated value, e.g. the color, changes, the drawView should
            // set the current color as background color
            drawView.setBackgroundColor((int) animation.getAnimatedValue());
        }
    };

    /**
     * Listener Adapter that is called every time the animation progress reaches certain stages
     */
    private final AnimatorListenerAdapter animatorListenerAdapter = new AnimatorListenerAdapter() {

        /**
         * Called if the animation has ended.
         *
         * @param animation is the animation that the listener is linked to
         */
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            // since the border of the drawView gets lost in the animating process
            // set it again after the process finished
            drawView.setBackgroundResource(R.drawable.draw_view_background);
        }

        /**
         * Called every time the animation progress repeats
         *
         * @param animation is the animation that the listener is linked to
         */
        @Override
        public void onAnimationRepeat(Animator animation) {
            super.onAnimationRepeat(animation);
            // clear the draw view, if the animation repeats,
            // e.g. the animated value reached "colorTo"
            drawView.clear();
        }
    };
}
