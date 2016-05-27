package de.lddt.zeichenroboterapp.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import de.lddt.zeichenroboterapp.entity.MyBrick;
import de.lddt.zeichenroboterapp.math.Vector2D;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;

/**
 * This Class handles everything related to Bluetooth.
 * It establishes bluetooth connections, transfers Vectors and closes bluetooth connections.
 */
public class BluetoothConn {
    private static final String TAG = "BluetoothConn";
    //The via bluetooth connected nxt brick
    private static NXTConnector brickConn;

    /**
     * Check if Bluetooth is enabled
     *
     * @return true if bluetooth in the device system settings is enabled
     */
    public static boolean isBluetoothEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    /**
     * Connect to a NXT brick via bluetooth
     *
     * @param myBrick the desired brick
     * @return true if connection is established
     */
    public static boolean connectTo(MyBrick myBrick) {
        //see http://www.lejos.org/nxt/nxj/tutorial/Android/Android.htm
        //TODO: try catch unnötig??
        Log.v(TAG, "Trying to connecto to " + myBrick.getName() + "; " + myBrick.getMacAddress());
        try {
            brickConn = new NXTConnector();
            brickConn.setDebug(true);
            brickConn.addLogListener(new NXTCommLogListener() {
                public void logEvent(String arg0) {
                    Log.e(TAG + " NXJ log:", arg0);
                }

                public void logEvent(Throwable arg0) {
                    Log.e(TAG + " NXJ log:", arg0.getMessage(), arg0);
                }
            });
            brickConn.connectTo(myBrick.getName(), myBrick.getMacAddress(),
                    NXTCommFactory.BLUETOOTH, NXTComm.PACKET);
        } catch (Exception e) {
            e.printStackTrace();
            brickConn = null;
            return false;
        }

        //return true if connection is established
        return brickConn != null && brickConn.getOutputStream() != null;
    }

    /**
     * Transfer vectors to the connected NXT brick.
     * In Packet mode only 254 Bytes can be transferred at once.
     *
     * @param vectorList List of vectors;
     * @param pId        the id of the package. Last package has id 1, first package has id n.
     * @return true if sending was successful.
     */
    public static boolean send(List<Vector2D> vectorList, short pId) {
        DataOutputStream dataOutStream = brickConn.getDataOut();
        try {
            //write the number of packages to come including this one
            dataOutStream.writeShort(pId);
            //write the vectors
            for (int i = 0; i < vectorList.size(); i++) {
                Vector2D v = vectorList.get(i);
                dataOutStream.writeShort((short) v.x);
                dataOutStream.writeShort((short) v.y);
            }
            dataOutStream.flush();
            Log.d(TAG, "Transferred" + vectorList.size() + " vectors." +
                    "\nPackage: (" + pId + ")" +
                    "\nReceiver: " + brickConn.getNXTInfo().name);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Wait for a response from the NXT brick.
     *
     * @return true if the Break sends a boolean with value true
     */
    public static boolean waitForResponse() {
        Log.v(TAG, "Waiting for response from " + brickConn.getNXTInfo().name);
        DataInputStream dataInStream = brickConn.getDataIn();
        boolean success = false;
        while (!success) {
            try {
                success = dataInStream.readBoolean();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                return false;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Close the outputStream and the bluetooth connection.
     */
    public static void close() {
        if (brickConn != null) {
            try {
                brickConn.getDataIn().close();
                brickConn.getDataOut().close();
                brickConn.getNXTComm().close();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}
