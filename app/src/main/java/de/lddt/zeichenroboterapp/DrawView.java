package de.lddt.zeichenroboterapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

import de.lddt.zeichenroboterapp.math.vector.PositionVector2D;
import de.lddt.zeichenroboterapp.math.vector.Vector2D;

/**
 * Created by Tim on 27.04.2016.
 */
public class DrawView extends SurfaceView {
    private List<Vector2D> positionVectorList;
    private List<Path> paths;
    private Paint paint;
    private boolean drawing;

    public DrawView(Context context) {
        super(context);
        init();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.positionVectorList = new ArrayList<>();
        drawing = false;
        paths = new ArrayList<>();
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6);
    }

    public void reset() {
        positionVectorList.clear();
        paths.clear();
        invalidate();
    }

    public void revert(){
        if (positionVectorList.size() > 0 && paths.size() > 0) {
            positionVectorList.remove(positionVectorList.size() - 1);
            paths.remove(paths.size() - 1);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for(int i = 0; i < paths.size(); i++) {
            if(drawing && i == paths.size()-1) {
                paint.setColor(getResources().getColor(R.color.hint_draw_color));
            } else {
                paint.setColor(getResources().getColor(R.color.final_draw_color));
            }

            canvas.drawPath(paths.get(i), paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        Vector2D newVector;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                drawing = true;
                System.out.println("touch down: " + event.getX() + "; " + event.getY());

                newVector = createVector((short) event.getX(),(short) event.getY());
                if(positionVectorList.size() > 0 && !positionVectorList.get(positionVectorList.size() -1).equals(newVector)) {
                    positionVectorList.add(newVector);
                }

                paths.add(new Path());
                paths.get(paths.size()-1).moveTo(event.getX(), event.getY());

                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                System.out.println("touch move: " + event.getX() + "; " + event.getY());

                newVector = createVector((short) event.getX(),(short) event.getY());
                if(positionVectorList.size() > 0 && !positionVectorList.get(positionVectorList.size() -1).equals(newVector)) {
                    positionVectorList.add(newVector);
                }
                paths.get(paths.size()-1).lineTo(event.getX(), event.getY());

                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                drawing = false;
                System.out.println("touch up: " + event.getX() + "; " + event.getY());

                newVector = createVector((short) event.getX(),(short) event.getY());
                if(positionVectorList.size() > 0 && !positionVectorList.get(positionVectorList.size() -1).equals(newVector)) {
                    positionVectorList.add(newVector);
                }
                newVector = createVector(Short.MAX_VALUE, Short.MAX_VALUE);
                if(positionVectorList.size() > 0 && !positionVectorList.get(positionVectorList.size() -1).equals(newVector)) {
                    positionVectorList.add(newVector);
                }

                paths.get(paths.size()-1).lineTo(event.getX(), event.getY());

                invalidate();
                return true;

            case MotionEvent.ACTION_CANCEL:
                break;

            case MotionEvent.ACTION_OUTSIDE:
                break;
        }
        return false;
    }

    private Vector2D createVector(short x, short y) {
        PositionVector2D vector2D = new PositionVector2D(x,y);
        vector2D.applyWidthBound(this.getMeasuredWidth());
        vector2D.applyHeightBound(this.getMeasuredHeight());

        int gridWidth = 200;
        vector2D.applyGridWidth(gridWidth, this.getMeasuredWidth());
        vector2D.applyGridHeight((short) (gridWidth * Math.sqrt(2)), this.getMeasuredWidth());

        return vector2D;
    }

    public List<Vector2D> getPositionVectorList() {
        return positionVectorList;
    }
}
