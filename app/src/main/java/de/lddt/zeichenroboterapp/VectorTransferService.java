package de.lddt.zeichenroboterapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.List;

import de.lddt.zeichenroboterapp.bluetooth.BluetoothConn;
import de.lddt.zeichenroboterapp.entity.MyBrick;
import de.lddt.zeichenroboterapp.math.vector.Vector2D;

/**
 * Created by Tim on 20.05.2016.
 */
public class VectorTransferService extends AsyncTask<List<Vector2D>, Integer, String> {
    private ProgressDialog dialog;
    private Context context;

    public VectorTransferService(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(List<Vector2D>... params) {
        boolean succesfullyConnected = BluetoothConn.connectTo(
                new MyBrick(context.getString(R.string.brick_name), context.getString(R.string.brick_mac_address)));

        if (!succesfullyConnected) {
            return context.getString(R.string.connection_failed);
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
            return context.getString(R.string.data_transfer_success);
        }
        return context.getString(R.string.data_transfer_failed);
    }


    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(context, result, Toast.LENGTH_LONG).show();
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
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(context.getString(message));
        dialog.setCancelable(false);
        return dialog;
    }
}