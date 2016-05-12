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
    private List<List<Vector2D>> positionVectorPaths;
    private List<Path> liveDrawPaths;
    private float startX, startY;
    private Paint paint;
    private boolean drawing, lineMode;

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
        this.positionVectorPaths = new ArrayList<>();
        drawing = false;
        lineMode = false;
        liveDrawPaths = new ArrayList<>();
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6);
    }

    public void reset() {
        positionVectorPaths.clear();
        liveDrawPaths.clear();
        invalidate();
    }

    public void revert(){
        if (positionVectorPaths.size() > 0 && liveDrawPaths.size() > 0) {
            positionVectorPaths.remove(positionVectorPaths.size() - 1);
            liveDrawPaths.remove(liveDrawPaths.size() - 1);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for(int i = 0; i < liveDrawPaths.size(); i++) {
            if(drawing && i == liveDrawPaths.size()-1) {
                paint.setColor(getResources().getColor(R.color.hint_draw_color));
            } else {
                paint.setColor(getResources().getColor(R.color.final_draw_color));
            }
            canvas.drawPath(liveDrawPaths.get(i), paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        Vector2D newVector;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                drawing = true;
                startX = event.getX();
                startY = event.getY();

                liveDrawPaths.add(new Path());
                liveDrawPaths.get(liveDrawPaths.size()-1).moveTo(event.getX(), event.getY());

                newVector = createVector((short) event.getX(),(short) event.getY());
                positionVectorPaths.add(new ArrayList<Vector2D>());
                positionVectorPaths.get(positionVectorPaths.size() -1).add(newVector);

                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if(drawing) {
                    if(lineMode) {
                        liveDrawPaths.get(liveDrawPaths.size() - 1).rewind();
                        liveDrawPaths.get(liveDrawPaths.size() - 1).moveTo(startX, startY);
                    }
                    liveDrawPaths.get(liveDrawPaths.size() - 1).lineTo(event.getX(), event.getY());

                    if(!lineMode) {
                        newVector = createVector((short) event.getX(), (short) event.getY());
                        List<Vector2D> currentList = positionVectorPaths.get(positionVectorPaths.size() - 1);
                        if (!currentList.get(currentList.size() - 1).equals(newVector)) {
                            currentList.add(newVector);
                        }
                    }

                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
                if(drawing) {
                    drawing = false;

                    if(lineMode) {
                        newVector = createVector((short) event.getX(), (short) event.getY());
                        positionVectorPaths.get(positionVectorPaths.size() - 1).add(newVector);
                    }
                }
                return true;
        }
        return false;
    }

    private Vector2D createVector(short x, short y) {
        PositionVector2D vector2D = new PositionVector2D(x,y);

        vector2D.applyGridWidth(getResources().getInteger(R.integer.grid_width), this.getMeasuredWidth());
        vector2D.applyGridHeight(getResources().getInteger(R.integer.grid_height), this.getMeasuredHeight());

        vector2D.applyWidthBound(getResources().getInteger(R.integer.grid_width));
        vector2D.applyHeightBound(getResources().getInteger(R.integer.grid_width));
        return vector2D;
    }

    public List<Vector2D> getPositionVectorList() {
        List<Vector2D> completeList = new ArrayList<>();
        for(List<Vector2D> vectorList : positionVectorPaths) {
            if(completeList.size() > 0) {
                completeList.add(new Vector2D(Short.MAX_VALUE, Short.MAX_VALUE));
            }
            completeList.addAll(vectorList);
        }
        return completeList;
    }

    public void setLineMode(boolean lineMode) {
        this.lineMode = lineMode;
    }
}
