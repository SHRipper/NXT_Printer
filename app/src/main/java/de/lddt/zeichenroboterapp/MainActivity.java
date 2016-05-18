package de.lddt.zeichenroboterapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;
import java.util.Vector;

import de.lddt.zeichenroboterapp.bluetooth.BluetoothConn;
import de.lddt.zeichenroboterapp.math.vector.Vector2D;

import static de.lddt.zeichenroboterapp.util.VectorConverter.positionVToDirectionV;

public class MainActivity extends Activity {
    private DrawView drawView;
    private Button buttonFreeMode, buttonLineMode;
    private Drawable defaultButtonBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        drawView = (DrawView) findViewById(R.id.main_draw_view);

        drawView.getLayoutParams().width = drawView.getMeasuredHeight();
        drawView.setLayoutParams(drawView.getLayoutParams());

        buttonFreeMode = (Button) findViewById(R.id.button_free_mode);
        buttonLineMode = (Button) findViewById(R.id.button_line_mode);

        defaultButtonBackground = buttonLineMode.getBackground();
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

    public void sendClick(View v) {
        List<Vector2D> directionVectorList = positionVToDirectionV(drawView.getPositionVectorList(), getResources().getInteger(R.integer.optimization_accuracy));

        Toast.makeText(MainActivity.this, "Vector optimization kicked out " + (drawView.getPositionVectorList().size() - directionVectorList.size()) + "/" + drawView.getPositionVectorList().size()+ " vectors.", Toast.LENGTH_LONG).show();

        if(!(directionVectorList.size() > 0)) {
            Toast.makeText(MainActivity.this, getString(R.string.nothing_drawn), Toast.LENGTH_LONG).show();
            return;
        }

        if (!BluetoothConn.isBluetoothEnabled()) {
            Toast.makeText(MainActivity.this, getString(R.string.bluetooth_disabled), Toast.LENGTH_LONG).show();
            openSystemBluetoothSettings();
            return;
        }

        new TransferVectorsToBrick().execute(directionVectorList);
    }

    private void openSystemBluetoothSettings() {
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intent);
    }

    private class TransferVectorsToBrick extends AsyncTask<List<Vector2D>, Integer, String> {
        private ProgressDialog dialog;

        @Override
        protected String doInBackground(List<Vector2D>... params) {
            boolean succesfullyConnected = BluetoothConn.connectTo(
                    new MyBrick(getString(R.string.brick_name), getString(R.string.brick_mac_address)));

            if (!succesfullyConnected) {
                return getString(R.string.connection_failed);
            }

            int maxVectors = 63;
            List<Vector2D> vectorList = params[0];
            int packages = (int) Math.ceil((float) vectorList.size() / maxVectors);

            publishProgress(0, packages, R.string.send_dialog_title, R.string.send_dialog_message);

            boolean successfullySend = true;
            for(int i = 0; i < packages; i++) {
                List<Vector2D> vPackage = vectorList.subList(i*maxVectors, Math.min(vectorList.size(), (i+1) * maxVectors));
                successfullySend = sendData(vPackage, (short) (packages - i));
                publishProgress(i + 1);
                successfullySend = waitForBrick();
            }

            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {}

            if (successfullySend) {
                return getString(R.string.data_transfer_success);
            }
            return getString(R.string.data_transfer_failed);
        }


        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
            dialog.cancel();
            BluetoothConn.close();
        }

        @Override
        protected void onPreExecute() {
            dialog = createDialog(R.string.connect_dialog_title, R.string.connect_dialog_message);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if(values.length > 1) {
                dialog.cancel();
                dialog = createDialog(values[2], values[3]);
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setMax(values[1]);
                dialog.show();
            }
            dialog.setProgress(values[0]);
        }

        private boolean sendData(List<Vector2D> vectorList, short packageId) {
            return BluetoothConn.send(vectorList, packageId);
        }

        private boolean waitForBrick() {
            return BluetoothConn.waitForResponse();
        }

        private ProgressDialog createDialog(int title, int message) {
            ProgressDialog dialog = new ProgressDialog(MainActivity.this);
            dialog.setTitle(title);
            dialog.setMessage(getString(message));
            dialog.setCancelable(false);
            return dialog;
        }
    }
}
