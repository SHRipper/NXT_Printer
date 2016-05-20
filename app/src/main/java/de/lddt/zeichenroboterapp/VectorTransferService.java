package de.lddt.zeichenroboterapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.List;

import de.lddt.zeichenroboterapp.bluetooth.BluetoothConn;
import de.lddt.zeichenroboterapp.entity.MyBrick;
import de.lddt.zeichenroboterapp.listener.TransferListener;
import de.lddt.zeichenroboterapp.math.vector.Vector2D;

/**
 * Created by Tim on 20.05.2016.
 */
public class VectorTransferService extends AsyncTask<List<Vector2D>, Integer, Boolean> {
    private TransferListener listener;
    private Context context;
    private final int maxVectors;

    public VectorTransferService(Context context) {
        this.context = context;
        this.maxVectors = 63;
    }

    @Override
    protected Boolean doInBackground(List<Vector2D>... params) {
        boolean success = BluetoothConn.connectTo(getDefaultBrick());
        if (!success) {return false;}

        List<Vector2D> vectorList = params[0];
        int packages = (int) Math.ceil((float) vectorList.size() / maxVectors);

        publishProgress(0, packages);

        for(int i = 0; i < packages; i++) {
            List<Vector2D> vPackage = vectorList.subList(i*maxVectors, Math.min(vectorList.size(), (i+1) * maxVectors));

            success = sendData(vPackage, (short) (packages - i));
            if(!success){return false;}
            publishProgress(i + 1, packages);

            success = waitForBrick();
            if(!success) {return false;}
        }

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {}
        return true;
    }

    @Override
    protected void onPreExecute() {
        listener.onConnect();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        listener.onProgressUpdate(values[0], values[1]);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        BluetoothConn.close();
        if(success) {
            listener.onFinished();
        } else {
            listener.error();
        }
    }

    private boolean sendData(List<Vector2D> vectorList, short packageId) {
        return BluetoothConn.send(vectorList, packageId);
    }

    private boolean waitForBrick() {
        return BluetoothConn.waitForResponse();
    }

    private MyBrick getDefaultBrick() {
        return new MyBrick(context.getString(R.string.brick_name),
                context.getString(R.string.brick_mac_address));
    }

    public void registerListener(TransferListener listener) {
        this.listener = listener;
    }
}