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
        buttonFreeMode.setBackgroundColor(Color.argb(255,0,255,0));
        buttonLineMode.setBackground(defaultButtonBackground);
        drawView.setLineMode(false);
    }

    public void lineDrawingModeClick(View v) {
        buttonFreeMode.setBackground(defaultButtonBackground);
        buttonLineMode.setBackgroundColor(Color.argb(255,0,255,0));
        drawView.setLineMode(true);
    }

    public void sendClick(View v) {
        List<Vector2D> directionVectorList = positionVToDirectionV(drawView.getPositionVectorList());

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

            publishProgress(R.string.send_dialog_title, R.string.send_dialog_message, 1);
            boolean successfullySend = sendData(params[0]);
            if (successfullySend) {
                publishProgress(R.string.send_dialog_title, R.string.send_dialog_message, 2);
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {}
                return getString(R.string.data_transfer_success);
            }
            return getString(R.string.data_transfer_failed);
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
            dialog.dismiss();
            BluetoothConn.close();
        }

        @Override
        protected void onPreExecute() {
            dialog = createDialog(R.string.connect_dialog_title, R.string.connect_dialog_message);
            dialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            dialog.setTitle(values[0]);
            dialog.setMessage(getString(values[1]));
            dialog.setProgress(values[2]);
        }

        private boolean sendData(List<Vector2D> vectorList) {
            return BluetoothConn.send(vectorList);
        }

        private ProgressDialog createDialog(int title, int message) {
            ProgressDialog dialog = new ProgressDialog(MainActivity.this);
            dialog.setTitle(title);
            dialog.setMessage(getString(message));
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgress(0);
            dialog.setMax(2);
            dialog.setCancelable(false);
            return dialog;
        }
    }
}
