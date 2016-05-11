package de.lddt.zeichenroboterapp.bluetooth;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

import de.lddt.zeichenroboterapp.MyBrick;
import de.lddt.zeichenroboterapp.util.ToByteConverter;
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
            //brickConn.connectTo("NummerS04", "00:16:53:1C:46:C7", NXTCommFactory.BLUETOOTH, NXTComm.PACKET);
            brickConn.connectTo(myBrick.getName(), myBrick.getMacAddress(), NXTCommFactory.BLUETOOTH, NXTComm.PACKET);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if(brickConn.getNXTInfos() != null) {
            return true;
        }
        return false;
    }

    public static boolean send(byte [] data) {
        byte sendBytes [] = new byte[data.length + 4];

        int i = 0;
        for(byte b : ToByteConverter.intToByte(data.length)) {
            sendBytes[i++] = b;
        }
        for (byte b : data) {
            sendBytes[i++] = b;
        }

        OutputStream outputStream = brickConn.getOutputStream();
        try {
            outputStream.write(sendBytes);
            outputStream.flush();
            outputStream.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void close() {
        try {
            brickConn.getNXTComm().close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
