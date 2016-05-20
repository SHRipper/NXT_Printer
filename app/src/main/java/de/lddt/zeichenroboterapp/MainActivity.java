package de.lddt.zeichenroboterapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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

        //the drawView is a square. Set the width and height to the Minimum of width and height
        int length = Math.min(drawView.getMeasuredHeight(), drawView.getMeasuredWidth());
        ViewGroup.LayoutParams drawViewParams = drawView.getLayoutParams();
        drawViewParams.width = length;
        drawViewParams.height = length;
        drawView.setLayoutParams(drawViewParams);

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
        if(!drawView.isDrawing()) {
            buttonFreeMode.setBackgroundColor(Color.argb(255, 0, 255, 0));
            buttonLineMode.setBackground(defaultButtonBackground);
            drawView.setLineMode(false);
        }
    }

    public void lineDrawingModeClick(View v) {
        if(!drawView.isDrawing()) {
            buttonFreeMode.setBackground(defaultButtonBackground);
            buttonLineMode.setBackgroundColor(Color.argb(255, 0, 255, 0));
            drawView.setLineMode(true);
        }
    }

    public void sendClick(View v) {
        List<Vector2D> directionVectorList = positionVToDirectionV(drawView.getPositionVectorList(), getResources().getInteger(R.integer.optimization_accuracy));

        Toast.makeText(MainActivity.this, "Vector optimization kicked out " + ((drawView.getPositionVectorList().size() -1) - directionVectorList.size()) + "/" + drawView.getPositionVectorList().size()+ " vectors.", Toast.LENGTH_LONG).show();

        if(!(directionVectorList.size() > 0)) {
            Toast.makeText(MainActivity.this, getString(R.string.nothing_drawn), Toast.LENGTH_LONG).show();
            return;
        }

        if (!BluetoothConn.isBluetoothEnabled()) {
            Toast.makeText(MainActivity.this, getString(R.string.bluetooth_disabled), Toast.LENGTH_LONG).show();
            openSystemBluetoothSettings();
            return;
        }

        new VectorTransferService(this).execute(directionVectorList);
    }

    private void openSystemBluetoothSettings() {
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intent);
    }


}
