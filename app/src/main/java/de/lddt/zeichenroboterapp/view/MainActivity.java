package de.lddt.zeichenroboterapp.view;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import de.lddt.zeichenroboterapp.core.DrawMode;
import de.lddt.zeichenroboterapp.core.VectorTransferService;
import de.lddt.zeichenroboterapp.entity.MyBrick;
import de.lddt.zeichenroboterapp.listener.TransferListener;
import de.lddt.zeichenroboterapp.math.Path;
import de.lddt.zeichenroboterapp.math.Vector2D;
import de.lddt.zeichenroboterapp.util.MetricsConverter;
import de.lddt.zeichenroboterapp.util.Sample;
import de.lddt.zeichenroboterapp.util.VectorConverter;

/**
 * The main activity for the "Zeichenroboter" project. The Canvas is part of this activity.
 */
public class MainActivity extends Activity {
    private DrawView drawView;
    private ImageButton buttonChildFree, buttonChildLine, buttonLinkedLine, buttonDrawModeParent;
    private boolean menuIsHidden;

    //Service to perform bluetooth operations in a second thread.
    private VectorTransferService service;
    private Listener transferListener;

    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        transferListener = new Listener();
        menuIsHidden = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Get a reference to important views.
        drawView = (DrawView) findViewById(R.id.main_draw_view);
        buttonChildFree = (ImageButton) findViewById(R.id.button_child_free_mode);
        buttonChildLine = (ImageButton) findViewById(R.id.button_child_line_mode);
        buttonLinkedLine = (ImageButton) findViewById(R.id.button_child_linked_line_mode);
        buttonDrawModeParent = (ImageButton) findViewById(R.id.button_parent_line_mode);
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
        ViewGroup.LayoutParams drawViewParams = drawView.getLayoutParams();
        drawViewParams.width = drawView.getCanvasLength();
        drawViewParams.height = drawView.getCanvasLength();
        drawView.setLayoutParams(drawViewParams);

        findViewById(R.id.main_left_layout).bringToFront();
        findViewById(R.id.main_right_layout).bringToFront();
    }

    /**
     * Called when the "CLEAR" button is clicked.
     * The DrawView changes its color to the color of the src_brush
     * and then to its default again.
     * <p>
     * The animation takes 300 milliseconds
     * </p/>
     * After this process the border of the DrawView is reset.
     *
     * @param v not used.
     */
    public void clearClick(View v) {

        // Values for the color animator
        int colorWhite = getResources().getColor(R.color.canvas_background);
        int colorBlack = getResources().getColor(R.color.final_draw);
        int duration = getResources().getInteger(R.integer.animation_color_fade_duration_ms);
        int repeatMode = ValueAnimator.REVERSE;

        // Set a colorAnimator for the drawView
        ColorAnimator colorAnimator = new ColorAnimator(drawView, colorWhite, colorBlack, repeatMode, 1);
        colorAnimator.start(duration);
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
     * Called when "LOAD" button is clicked.
     * Loads a sample drawing and gives it to the drawView.
     *
     * @param v not used.
     */
    public void loadSampleClick(View v) {
        List<Path> sampleDrawing =
                Sample.loadSample(this, R.array.sample_castle, drawView.getCanvasLength());

        for (Path samplePath : sampleDrawing) {
            drawView.addPath(samplePath);
        }
    }

    /**
     * Called when the one of the line mode buttons is clicked,
     * e.g. the line mode should change.
     * <p>
     * Change the drawing mode only if the user currently does not draw on the canvas.
     *
     * @param v is the view of the clicked button.
     */
    public void changeDrawingModeClick(View v) {
        Drawable icon;
        DrawMode newDrawMode;
        int animDuration = getResources().getInteger(R.integer.animation_alpha_fade_duration_ms);

        buttonChildFree.setBackgroundResource(R.drawable.button_drawmode_child_background);
        buttonChildLine.setBackgroundResource(R.drawable.button_drawmode_child_background);
        buttonLinkedLine.setBackgroundResource(R.drawable.button_drawmode_child_background);

        if (!drawView.isDrawing() && !menuIsHidden) {
            switch (v.getId()) {
                case R.id.button_child_free_mode:
                    icon = getResources().getDrawable(R.drawable.src_brush);
                    buttonChildFree.setBackgroundResource(R.drawable.button_drawmode_child_background_selected);
                    newDrawMode = DrawMode.FREE;
                    break;
                case R.id.button_child_line_mode:
                    icon = getResources().getDrawable(R.drawable.src_vector_line);
                    buttonChildLine.setBackgroundResource(R.drawable.button_drawmode_child_background_selected);
                    newDrawMode = DrawMode.LINE;
                    break;
                case R.id.button_child_linked_line_mode:
                    icon = getResources().getDrawable(R.drawable.src_vector_polyline);
                    buttonLinkedLine.setBackgroundResource(R.drawable.button_drawmode_child_background_selected);
                    newDrawMode = DrawMode.LINKED_LINE;
                    break;
                default:
                    icon = getResources().getDrawable(R.drawable.src_brush);
                    newDrawMode = DrawMode.FREE;
                    break;
            }
            hideLineModeMenu(animDuration);
            buttonDrawModeParent.setImageDrawable(icon);
            drawView.setDrawMode(newDrawMode);
        }
    }

    public void lineModeChooserClick(View v) {
        int animDuration = getResources().getInteger(R.integer.animation_alpha_fade_duration_ms);

        if (menuIsHidden) {
            // fade out chooser button
            // fade in line and free mode buttons
            showLineModeMenu(animDuration);
        } else {
            // fade in chooser button
            // fade out line an free mode buttons
            hideLineModeMenu(animDuration);
        }
    }

    private void showLineModeMenu(int duration) {

        // set alpha fade in animation
        Animation animFadeIn = AnimationUtils.loadAnimation(this, R.anim.button_drawmode_child_fade_in);
        Animation animFadeOut = AnimationUtils.loadAnimation(this, R.anim.button_drawmode_parent_fade_out);

        // translation values
        float freeModeTranslationY = MetricsConverter.convertToPixels(-60, this);
        float lineModeTranslationY = MetricsConverter.convertToPixels(-50, this);
        float lineModeTranslationX = -lineModeTranslationY;
        float linkedLineModeTranslationX = -freeModeTranslationY;

        buttonDrawModeParent.startAnimation(animFadeOut);

        // free mode button fade in an move up
        buttonChildFree.startAnimation(animFadeIn);
        buttonChildFree.animate().setDuration(duration).translationY(freeModeTranslationY).start();

        // line mode button fade in and move up and right
        buttonChildLine.startAnimation(animFadeIn);
        buttonChildLine.animate().setDuration(duration).translationY(lineModeTranslationY).translationX(lineModeTranslationX).start();

        // linked line mode button fade in and move right
        buttonLinkedLine.startAnimation(animFadeIn);
        buttonLinkedLine.animate().setDuration(duration).translationX(linkedLineModeTranslationX).start();

        menuIsHidden = false;
    }

    private void hideLineModeMenu(int duration) {

        // set alpha fade out animation
        Animation animFadeOut = AnimationUtils.loadAnimation(this, R.anim.button_drawmode_child_fade_out);
        Animation animFadeIn = AnimationUtils.loadAnimation(this, R.anim.button_drawmode_parent_fade_in);

        buttonDrawModeParent.startAnimation(animFadeIn);

        // free mode button fade out und move down
        buttonChildFree.startAnimation(animFadeOut);
        buttonChildFree.animate().setDuration(duration).translationY(0).start();

        // line mode button fade out and move down and left
        buttonChildLine.startAnimation(animFadeOut);
        buttonChildLine.animate().setDuration(duration).translationY(0).translationX(0).start();

        // linked line mode button fade out and move left
        buttonLinkedLine.startAnimation(animFadeOut);
        buttonLinkedLine.animate().setDuration(duration).translationX(0).start();

        menuIsHidden = true;
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
        int gridLength = getResources().getInteger(R.integer.grid_length);

        List<Vector2D> posVList = drawView.getPosVList();
        int canvasLength = drawView.getCanvasLength();
        posVList = VectorConverter.applyGrid(posVList, canvasLength, gridLength);
        List<Vector2D> dirVList = VectorConverter.posVToDirV(posVList, accuracyDeg);

        //Check if nothing is drawn, show error Toast and cancel operation.
        if (dirVList.size() == 0) {
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
        showToast("Vector optimization kicked out " + (posVList.size() - dirVList.size()) + "/" + posVList.size() + " vectors.");

        //Create a Service instance which performs bluetooth operations in a second thread.
        service = new VectorTransferService(MyBrick.getDefaultBrick(this));
        //Register a Listener to update the UI while sending data to the nxt brick.
        service.registerListener(transferListener);
        //start to transfer the vectors to the brick in a second thread.
        service.execute(dirVList);
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
     * Listener class to perform updates on the ui and show the progress
     * during the transfer of vectors to the nxt brick.
     */
    private class Listener implements TransferListener {
        private ProgressDialog dialog;

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
                dialog.dismiss();
                dialog = null;
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
            dialog.dismiss();
            showToast(getString(R.string.data_transfer_success));
        }

        /**
         * Show error message if any error occurred.
         */
        @Override
        public void error() {
            dialog.dismiss();
            showToast(getString(R.string.data_transfer_failed));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (service != null) {
            service.cancel(true);
        }
    }
}
