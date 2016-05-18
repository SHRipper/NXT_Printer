package de.lddt.zeichenroboterapp.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import de.lddt.zeichenroboterapp.MyBrick;
import de.lddt.zeichenroboterapp.math.vector.Vector2D;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;

/**
 * Created by Tim on 11.05.2016.
 */
public class BluetoothConn{
    private static final String TAG = "BluetoothConn";
    private static NXTConnector brickConn;

    public static boolean isBluetoothEnabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled();
    }

    public static boolean connectTo(MyBrick myBrick) {
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
            brickConn.connectTo(myBrick.getName(), myBrick.getMacAddress(), NXTCommFactory.BLUETOOTH, NXTComm.PACKET);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        //return true if connection is established
        return brickConn.getOutputStream() != null;
    }

    public static boolean send(List<Vector2D> vectorList) {
        DataOutputStream outputStream = brickConn.getDataOut();
        try {
            for(int i = 1; i <= vectorList.size(); i++) {
                outputStream.writeShort((short)vectorList.get(i-1).getX());
                outputStream.writeShort((short)vectorList.get(i-1).getY());
            }
            outputStream.flush();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void close() {
        try {
            brickConn.getDataOut().close();
            brickConn.getNXTComm().close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
