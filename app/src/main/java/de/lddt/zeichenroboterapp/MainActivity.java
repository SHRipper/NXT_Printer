package de.lddt.zeichenroboterapp;

import android.app.Activity;
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
        if (!BluetoothConn.isBluetoothEnabled()) {
            Toast.makeText(MainActivity.this, getString(R.string.bluetooth_disabled), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean succesfullyConnected = BluetoothConn.connectTo(
                new MyBrick(getString(R.string.brick_name), getString(R.string.brick_mac_address))
        );
        if (!succesfullyConnected) {
            Toast.makeText(MainActivity.this, getString(R.string.connection_failed), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean succesfullySend = sendData(positionVToDirectionV(drawView.getPositionVectorList()));
        BluetoothConn.close();

        if (succesfullySend) {
            Toast.makeText(MainActivity.this, "Daten gesendet!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.data_transfer_failed), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean sendData(List<Vector2D> vectorList) {
        byte[] data = ToByteConverter.vectorListToByte(vectorList);
        boolean succesfullySend = BluetoothConn.send(data);
        return succesfullySend;
    }
}
