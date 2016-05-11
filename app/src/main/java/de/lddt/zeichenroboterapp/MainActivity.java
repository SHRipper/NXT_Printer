package de.lddt.zeichenroboterapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import de.lddt.zeichenroboterapp.bluetooth.BluetoothConn;
import de.lddt.zeichenroboterapp.math.vector.Vector2D;
import de.lddt.zeichenroboterapp.util.ToByteConverter;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;

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
        int drawViewWidth = drawView.getMeasuredWidth();
        drawView.getLayoutParams().height = (int) Math.round(drawViewWidth / Math.sqrt(2));
        drawView.setLayoutParams(drawView.getLayoutParams());
    }

    public void resetCanvasClick(View v) {
        drawView.reset();
    }

    public void revertPathClick(View v) {
        drawView.revert();
    }

    public void sendClick(View v) {
        boolean succesfullyConnected = BluetoothConn.connectTo(new MyBrick("NummerS04","00:16:53:1C:46:C7"));
        if(succesfullyConnected) {
            List<Vector2D> vectorList = drawView.getPositionVectorList();

            int i = 0;
            Vector2D temp = null;
            for(Vector2D vector : vectorList) {
                if(vector.getX() != Short.MAX_VALUE && vector.getY() != Short.MAX_VALUE) {
                    temp = null;
                    continue;
                }
                if(i >= 1 && temp != null) {
                    vector.sub(temp);
                }
                temp = vector;
                i++;
            }

            byte [] data = ToByteConverter.vectorListToByte(vectorList);
            boolean succesfullySend = BluetoothConn.send(data);
            if(succesfullySend) {
                Toast.makeText(MainActivity.this, "Daten gesendet!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Fehler beim Senden der Daten!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Fehler beim Verbinden! Ist der Brick an?", Toast.LENGTH_SHORT).show();
        }
    }
}
