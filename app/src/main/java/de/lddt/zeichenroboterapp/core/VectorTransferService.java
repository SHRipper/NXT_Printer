package de.lddt.zeichenroboterapp.core;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import de.lddt.zeichenroboterapp.bluetooth.BluetoothConn;
import de.lddt.zeichenroboterapp.entity.MyBrick;
import de.lddt.zeichenroboterapp.listener.TransferListener;
import de.lddt.zeichenroboterapp.math.vector.Vector2D;

/**
 * This class transfers vectors to a nxt brick.
 * Perform all Bluetooth operations in a background thread to keep the main thread responsive.
 *
 * Class extends AsyncTask, because the AsyncTask provides an easy way to execute code in a background thread
 * while still being able update the main ui thread.
 */
public class VectorTransferService extends AsyncTask<List<Vector2D>, Integer, Boolean> {
    private static final String TAG = "VectorTransferService";

    //Listener to communicate progress updates and actions.
    private TransferListener listener;
    // the brick to connect to and to send vectors to.
    private MyBrick brick;
    //the maximim amounts of vectors per package
    private final int maxVectors = 63;

    /**
     * @param brick the brick this service will try to connect and will try to send vectors to.
     */
    public VectorTransferService(MyBrick brick) {
        this.brick = brick;
    }

    /**
     * Execute this in a background thread. Connect to the brick and transfer vectors.
     *
     * @param params List of Vectors to be transferred.
     * @return true if successful.
     */
    @Override
    protected Boolean doInBackground(List<Vector2D>... params) {
        //try to connect
        boolean success = BluetoothConn.connectTo(brick);
        if (!success) {
            return false;}

        List<Vector2D> vectorList = params[0];
        Log.v(TAG, "About to transfer " + vectorList.size() + " vectors to " + brick.getName());

        //number of total packages
        int packagesTotal = (int) Math.ceil((float) vectorList.size() / maxVectors);

        publishProgress(0, packagesTotal);
        //Send multiple packages to brick, because of the 254 Byte limit.
        for (int i = 0; i < packagesTotal; i++) {
            //send a sublist of vectors to the brick
            int startIndex = i * maxVectors;
            int endIndex = Math.min(vectorList.size(), (i + 1) * maxVectors);
            List<Vector2D> vPackage = vectorList.subList(startIndex, endIndex);

            short packageId = (short) (packagesTotal - i);
            success = BluetoothConn.send(vPackage, packageId);
            if (!success) {
                return false;
            }

            //Update the progress.
            publishProgress(i + 1, packagesTotal);

            //Wait till the brick is ready for the next package.
            success = BluetoothConn.waitForResponse();
            if(!success) {return false;}
        }

        //Sleep some time before finishing and dismissing the 100% progress indicator
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {}
        return true;
    }

    /**
     * The service is about to try to connect to the brick.
     * A proper indicator can be displayed on the screen.
     */
    @Override
    protected void onPreExecute() {
        listener.onConnect();
    }

    /**
     * Update the progress indicator.
     * @param values the amount of packages already transferred and the total amount of packages.
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        listener.onProgressUpdate(values[0], values[1]);
    }

    /**
     * When finished close the bluetooth connection.
     * Call listener to update the ui.
     * @param success true if the vectors have been transferred successfully.
     */
    @Override
    protected void onPostExecute(Boolean success) {
        BluetoothConn.close();
        if(success) {
            listener.onFinished();
        } else {
            listener.error();
        }
    }

    /**
     * Set a listener to communicate progress updates and actions.
     * @param listener the listener to be called at different actions.
     */
    public void registerListener(TransferListener listener) {
        this.listener = listener;
    }
}