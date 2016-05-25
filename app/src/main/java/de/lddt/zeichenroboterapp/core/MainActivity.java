package de.lddt.zeichenroboterapp.core;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.List;

import de.lddt.zeichenroboterapp.R;
import de.lddt.zeichenroboterapp.bluetooth.BluetoothConn;
import de.lddt.zeichenroboterapp.entity.MyBrick;
import de.lddt.zeichenroboterapp.listener.TransferListener;
import de.lddt.zeichenroboterapp.math.vector.Vector2D;

import static de.lddt.zeichenroboterapp.util.VectorConverter.posVToDirVList;

/**
 * The main activity for the "Zeichenroboter" project. The Canvas is part of this activity.
 */
public class MainActivity extends Activity {
    private DrawView drawView;
    private ImageButton buttonFreeMode, buttonLineMode, buttonLineModeChooser;
    private LineMode lineMode;
    private int animationDurationFade;
    private boolean menuIsHidden;

    private int clickCounter;

    private ProgressDialog dialog;
    private Toast toast;
    //Service to perform bluetooth operations in a second thread.
    private Listener transferListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        transferListener = new Listener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Get a reference to important views.
        drawView = (DrawView) findViewById(R.id.main_draw_view);
        buttonFreeMode = (ImageButton) findViewById(R.id.button_free_mode);
        buttonLineMode = (ImageButton) findViewById(R.id.button_line_mode);
        buttonLineModeChooser = (ImageButton) findViewById(R.id.button_line_mode_chooser);

        clickCounter = 0;
        menuIsHidden = true;
        animationDurationFade = getResources().getInteger(R.integer.animation_alpha_fade_duration_ms);
    }

    /**
     * Modify the width and height, so the draw view is a square.
     *
     * @param hasFocus not used
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //the drawView is a square. Set the width and height to the Minimum of width and height
        int length = Math.min(drawView.getMeasuredHeight(), drawView.getMeasuredWidth());
        ViewGroup.LayoutParams drawViewParams = drawView.getLayoutParams();
        drawViewParams.width = length;
        drawViewParams.height = length;
        drawView.setLayoutParams(drawViewParams);
    }

    /**
     * Called when the "CLEAR" button is clicked.
     * The DrawView changes its color to the color of the brush
     * and then to its default again.
     * <p>
     * The animation takes 300 milliseconds
     * <p>
     * After this process the border of the DrawView is reset.
     *
     * @param v not used.
     */
    public void clearCanvasClick(View v) {

        final int colorWhite = getResources().getColor(R.color.canvas_background_color);
        final int colorBlack = getResources().getColor(R.color.final_draw_color);
        int duration = getResources().getInteger(R.integer.animation_color_fade_duration_ms);

        final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorWhite, colorBlack);
        colorAnimation.setDuration(duration);
        colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
        colorAnimation.setRepeatCount(1);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                drawView.setBackgroundColor((int) colorAnimation.getAnimatedValue());
            }
        });
        colorAnimation.start();

        // set border of the DrawView as soon as the animation finished
        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                drawView.setBackgroundResource(R.drawable.draw_view_background);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                drawView.clear();
            }
        });

    }

    /**
     * Called when the "UNDO" button is clicked.
     * Tells the drawView to remove the last drawn path.
     *
     * @param v not used.
     */
    public void undoClick(View v) {
        drawView.undo();
    }

    /**
     * Called when the one of the line mode buttons is clicked,
     * e.g. the line mode should change.
     * <p>
     * Change the drawing mode only if the user currently does not draw on the canvas.
     * TODO: das hört sich scheiße an, stimmt das?
     *
     * @param v is the view of the clicked button.
     */
    public void changeDrawingModeClick(View v) {

        int buttonID = v.getId();

        if (!drawView.isDrawing() && !menuIsHidden) {
            if (buttonID == R.id.button_free_mode) {
                lineMode = LineMode.FREE;
                buttonFreeMode.setBackgroundResource(R.drawable.linemode_child_button_shape_selected);
                buttonLineMode.setBackgroundResource(R.drawable.linemode_child_button_shape_unselected);
            } else if (buttonID == R.id.button_line_mode) {
                lineMode = lineMode.LINE;
                buttonFreeMode.setBackgroundResource(R.drawable.linemode_child_button_shape_unselected);
                buttonLineMode.setBackgroundResource(R.drawable.linemode_child_button_shape_selected);
            }
            hideLineModeMenu();
            drawView.setLineMode(lineMode);
        }
    }

    public void lineModeChooserClick(View v) {
        clickCounter++;

        if (menuIsHidden) {
            // fade out chooser button
            // fade in line and free mode buttons

            showLineModeMenu();
        } else if (!menuIsHidden) {
            clickCounter = 0;

            // fade in chooser button
            // fade out line an free mode buttons
            hideLineModeMenu();
        }
    }

    private void hideLineModeMenu() {

        // Chooser button fades in and moves up
        buttonLineModeChooser.animate().translationY(0).setDuration(animationDurationFade).start();
        Animation animFadeIn = AnimationUtils.loadAnimation(this, R.anim.button_chooser_fade_in);
        buttonLineModeChooser.startAnimation(animFadeIn);

        // line and free mode button fade out
        Animation animFadeOut = AnimationUtils.loadAnimation(this, R.anim.button_mode_fade_out);
        buttonFreeMode.startAnimation(animFadeOut);
        buttonFreeMode.animate().setDuration(animationDurationFade).translationY(0).start();

        buttonLineMode.startAnimation(animFadeOut);
        buttonLineMode.animate().setDuration(animationDurationFade).translationY(0).start();

        menuIsHidden = true;
    }

    private void showLineModeMenu() {

        // Chooser button fades out and moves down
        buttonLineModeChooser.animate().translationY(240).setDuration(animationDurationFade).start();
        Animation animFadeOut = AnimationUtils.loadAnimation(this, R.anim.button_chooser_fade_out);
        buttonLineModeChooser.startAnimation(animFadeOut);

        // line and free mode button fade in
        Animation animFadeIn = AnimationUtils.loadAnimation(this, R.anim.button_mode_fade_in);
        buttonFreeMode.startAnimation(animFadeIn);
        buttonFreeMode.animate().setDuration(animationDurationFade).translationY(60).start();

        buttonLineMode.startAnimation(animFadeIn);
        buttonLineMode.animate().setDuration(animationDurationFade).translationY(-90).start();

        menuIsHidden = false;
    }

    /**
     * Called when the "SEND" button is clicked.
     * Check if something is drawn and bluetooth is enabled.
     * Try to transfer the vectors to the brick.
     *
     * @param v not used.
     */
    public void sendClick(View v) {
        float accuracyDeg = getResources().getInteger(R.integer.optimization_accuracy_degs);
        List<Vector2D> directionVectorList =
                posVToDirVList(drawView.getPosVList(), accuracyDeg);

        //Check if nothing is drawn, show error Toast and cancel operation.
        if (directionVectorList.size() == 0) {
            showToast(getString(R.string.nothing_drawn));
            return;
        }

        //Check if bluetooth is enabled. If not open bluetooth system settings and show toast.
        if (!BluetoothConn.isBluetoothEnabled()) {
            showToast(getString(R.string.bluetooth_disabled));
            openSystemBluetoothSettings();
            return;
        }

        //For Debug display how many vectors are excluded because of the optimization algorithm.
        showToast("Vector optimization kicked out " + (drawView.getPosVList().size() - directionVectorList.size()) + "/" + drawView.getPosVList().size() + " vectors.");

        //Create a Service instance which performs bluetooth operations in a second thread.
        VectorTransferService service = new VectorTransferService(getDefaultBrick());
        //Register a Listener to update the UI while sending data to the nxt brick.
        service.registerListener(transferListener);
        //start to transfer the vectors to the brick in a second thread.
        service.execute(directionVectorList);
    }

    /**
     * Listener class to perform updates on the ui and show the progress
     * during the transfer of vectors to the nxt brick.
     */
    private class Listener implements TransferListener {
        /**
         * Show loading dialog while connection is established
         */
        @Override
        public void onConnect() {
            dialog = createDialog(getString(R.string.connect_dialog_title),
                    getString(R.string.connect_dialog_message));
            dialog.show();
        }

        /**
         * Show progress dialog.
         *
         * @param progress     the number of packages successfully sent.
         * @param packageCount the total number of packages.
         */
        @Override
        public void onProgressUpdate(int progress, int packageCount) {
            if (dialog.getMax() != packageCount) {
                dialog.cancel();
                dialog = createDialog(getString(R.string.send_dialog_title),
                        getString(R.string.send_dialog_message));
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setMax(packageCount);
                dialog.show();
            }
            dialog.setProgress(progress);
        }

        /**
         * Show toast if all vectors have been successfully sent.
         */
        @Override
        public void onFinished() {
            dialog.cancel();
            showToast(getString(R.string.data_transfer_success));
        }

        /**
         * Show error message if any error occurred.
         */
        @Override
        public void error() {
            dialog.cancel();
            showToast(getString(R.string.data_transfer_failed));
        }
    }

    /**
     * Show toast on the screen. Cancel currently displayed toasts.
     *
     * @param message the message to be displayed.
     */
    private void showToast(String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Create a ProgressDialog instance. The Dialog is not cancelable.
     *
     * @param title   the title of tje dialog.
     * @param message the message of the dialog.
     * @return the created dialog.
     */
    private ProgressDialog createDialog(String title, String message) {
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        return dialog;
    }

    /**
     * Open the system bluetooth settings.
     */
    private void openSystemBluetoothSettings() {
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intent);
    }

    /**
     * Creates a MyBrick instance with name and mac address specified in the resource file
     *
     * @return the created instance
     */
    private MyBrick getDefaultBrick() {
        return new MyBrick(getString(R.string.brick_name),
                getString(R.string.brick_mac_address));
    }
}
