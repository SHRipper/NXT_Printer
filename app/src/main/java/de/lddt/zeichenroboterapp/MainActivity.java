package de.lddt.zeichenroboterapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import de.lddt.zeichenroboterapp.bluetooth.BluetoothConn;
import de.lddt.zeichenroboterapp.math.vector.Vector2D;
import de.lddt.zeichenroboterapp.util.ToByteConverter;

import static de.lddt.zeichenroboterapp.util.VectorConverter.positionVToDirectionV;

public class MainActivity extends Activity {
    private DrawView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        drawView = (DrawView) findViewById(R.id.main_draw_view);

        drawView.getLayoutParams().width = drawView.getMeasuredHeight();
        drawView.setLayoutParams(drawView.getLayoutParams());
    }

    public void resetCanvasClick(View v) {
        drawView.reset();
    }

    public void revertPathClick(View v) {
        drawView.revert();
    }

    public void sendClick(View v) {
        List<Vector2D> directionVectorList = positionVToDirectionV(drawView.getPositionVectorList());

        if(!(directionVectorList.size() > 0)) {
            Toast.makeText(MainActivity.this, getString(R.string.nothing_drawn), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!BluetoothConn.isBluetoothEnabled()) {
            Toast.makeText(MainActivity.this, getString(R.string.bluetooth_disabled), Toast.LENGTH_SHORT).show();
            return;
        }

        new TransferVectorsToBrick().execute(directionVectorList);

        BluetoothConn.close();
    }

    private class TransferVectorsToBrick extends AsyncTask<List<Vector2D>, String, String> {
        private final AlertDialog dialog;

        public TransferVectorsToBrick() {
            this.dialog = createDialog(getString(R.string.connect_dialog_title), getString(R.string.connect_dialog_message));
            this.dialog.setCancelable(false);
        }

        @Override
        protected String doInBackground(List<Vector2D>... params) {
            boolean succesfullyConnected = BluetoothConn.connectTo(
                    new MyBrick(getString(R.string.brick_name), getString(R.string.brick_mac_address)));

            if (!succesfullyConnected) {
                return getString(R.string.connection_failed);
            }

            publishProgress(getString(R.string.send_dialog_title), getString(R.string.send_dialog_message));
            boolean succesfullySend = sendData(params[0]);

            if (succesfullySend) {
                return getString(R.string.data_transfer_success);
            }
            return getString(R.string.data_transfer_failed);
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            dialog.cancel();
        }

        @Override
        protected void onPreExecute() {
            dialog.show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            dialog.setTitle(values[0]);
            dialog.setMessage(values[1]);
        }

        private boolean sendData(List<Vector2D> vectorList) {
            byte[] data = ToByteConverter.vectorListToByte(vectorList);
            return BluetoothConn.send(data);
        }

        private AlertDialog createDialog(String title, String message) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder .setTitle(title)
                    .setMessage(message);
            return builder.create();
        }
    }
}
