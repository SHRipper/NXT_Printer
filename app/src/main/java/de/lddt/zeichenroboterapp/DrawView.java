package de.lddt.zeichenroboterapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tim on 27.04.2016.
 */
public class DrawView extends SurfaceView {
    InputListenerInterface inputListener;
    List<Vector2D> positionVectorList;

    public DrawView(Context context) {
        super(context);
        this.positionVectorList = new ArrayList<>();

    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.positionVectorList = new ArrayList<>();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.positionVectorList = new ArrayList<>();
    }

    public void registerListener(InputListenerInterface listener) {
        inputListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                System.out.println("touch down: " + event.getX() + "; " + event.getY());
                positionVectorList.add(inputListener.onTouchDown(event.getX(), event.getY()));
                return true;

            case MotionEvent.ACTION_MOVE:
                System.out.println("touch move: " + event.getX() + "; " + event.getY());
                positionVectorList.add(inputListener.onTouchDown(event.getX(), event.getY()));
                return true;

            case MotionEvent.ACTION_UP:
                System.out.println("touch up: " + event.getX() + "; " + event.getY());
                positionVectorList.add(inputListener.onTouchDown(event.getX(), event.getY()));
                return true;

            case MotionEvent.ACTION_CANCEL:
                break;

            case MotionEvent.ACTION_OUTSIDE:
                break;
        }
        return false;
    }
}
