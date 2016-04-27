package de.lddt.zeichenroboterapp;


import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;

public class MainActivity extends Activity {
    private InputListenerInterface inputListener;
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

        inputListener = new Inputlistener(drawView.getMeasuredWidth(), drawView.getMeasuredHeight());
        drawView.registerListener(inputListener);
    }
}
