package de.lddt.zeichenroboterapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

import de.lddt.zeichenroboterapp.bluetooth.BluetoothConn;
import de.lddt.zeichenroboterapp.entity.MyBrick;
import de.lddt.zeichenroboterapp.listener.TransferListener;
import de.lddt.zeichenroboterapp.math.vector.Vector2D;

import static de.lddt.zeichenroboterapp.util.VectorConverter.posVToDirVList;

public class MainActivity extends Activity {
    private DrawView drawView;
    private Button buttonFreeMode, buttonLineMode;
    private Drawable defaultButtonBackground;

    private ProgressDialog dialog;
    private Toast toast;

    private VectorTransferService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        service = new VectorTransferService(getDefaultBrick());
        service.registerListener(new Listener());
    }

    @Override
    protected void onStart() {
        super.onStart();
        drawView = (DrawView) findViewById(R.id.main_draw_view);
        buttonFreeMode = (Button) findViewById(R.id.button_free_mode);
        buttonLineMode = (Button) findViewById(R.id.button_line_mode);
        defaultButtonBackground = buttonLineMode.getBackground();
    }

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

    public void resetCanvasClick(View v) {
        drawView.reset();
    }

    public void revertPathClick(View v) {
        drawView.revert();
    }

    public void freeDrawingModeClick(View v) {
        if(!drawView.isDrawing()) {
            buttonFreeMode.setBackgroundColor(Color.argb(255, 0, 255, 0));
            buttonLineMode.setBackground(defaultButtonBackground);
            drawView.setLineMode(false);
        }
    }

    public void lineDrawingModeClick(View v) {
        if(!drawView.isDrawing()) {
            buttonFreeMode.setBackground(defaultButtonBackground);
            buttonLineMode.setBackgroundColor(Color.argb(255, 0, 255, 0));
            drawView.setLineMode(true);
        }
    }

    /**
     * Gets called when the "SEND" button is clicked.
     * Check if something is drawn and bluetooth is enabled.
     * Try to transfer the vectors to the brick.
     * @param v the button, not used.
     */
    public void sendClick(View v) {
        float accuracyDeg = getResources().getInteger(R.integer.optimization_accuracy);
        List<Vector2D> directionVectorList =
                posVToDirVList(drawView.getPosVList(), accuracyDeg);

        //Check if nothing is drawn, show error Toast and cancel operation.
        if(directionVectorList.size() == 0) {
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
        showToast("Vector optimization kicked out " + ((drawView.getPosVList().size() -1) - directionVectorList.size()) + "/" + drawView.getPosVList().size()+ " vectors.");

        //start to transfer the vectors to the brick in a secont thread.
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
         * @param progress the number of packages successfully sent.
         * @param packageCount the total number of packages.
         */
        @Override
        public void onProgressUpdate(int progress, int packageCount) {
            if(dialog.getMax() != packageCount) {
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
     * @param message the message to be displayed.
     */
    private void showToast(String message) {
        if(toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Create a ProgressDialog instance. The Dialog is not cancelable.
     * @param title the title of tje dialog.
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
     * @return the created instance
     */
    private MyBrick getDefaultBrick() {
        return new MyBrick(getString(R.string.brick_name),
                getString(R.string.brick_mac_address));
    }
}
