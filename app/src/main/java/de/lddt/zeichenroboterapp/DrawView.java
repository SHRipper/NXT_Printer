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

import de.lddt.zeichenroboterapp.math.vector.Vector2D;

import static de.lddt.zeichenroboterapp.util.VectorConverter.applyBounds;
import static de.lddt.zeichenroboterapp.util.VectorConverter.applyGrid;

/**
 * Created by Tim on 27.04.2016.
 */
public class DrawView extends SurfaceView {
    private List<List<Vector2D>> positionVPaths;
    private List<Path> liveDrawPaths;
    private Vector2D startVector;
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
        this.positionVPaths = new ArrayList<>();
        drawing = false;
        lineMode = false;
        liveDrawPaths = new ArrayList<>();
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
    }

    public void reset() {
        positionVPaths.clear();
        liveDrawPaths.clear();
        invalidate();
    }

    public void revert(){
        if (positionVPaths.size() > 0 && liveDrawPaths.size() > 0) {
            positionVPaths.remove(positionVPaths.size() - 1);
            liveDrawPaths.remove(liveDrawPaths.size() - 1);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setStrokeWidth(getStrokeWidth());
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
                if(lineMode) {
                    startVector = new Vector2D(event.getX(), event.getY());
                }

                liveDrawPaths.add(new Path());
                liveDrawPaths.get(liveDrawPaths.size()-1).moveTo(event.getX(), event.getY());

                newVector = createVector((short) event.getX(),(short) event.getY());
                positionVPaths.add(new ArrayList<Vector2D>());
                positionVPaths.get(positionVPaths.size() -1).add(newVector);

                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if(drawing) {
                    if(lineMode) {
                        liveDrawPaths.get(liveDrawPaths.size() - 1).rewind();
                        liveDrawPaths.get(liveDrawPaths.size() - 1).moveTo(startVector.x, startVector.y);
                    }
                    liveDrawPaths.get(liveDrawPaths.size() - 1).lineTo(event.getX(), event.getY());

                    if(!lineMode) {
                        newVector = createVector((short) event.getX(), (short) event.getY());
                        List<Vector2D> currentList = positionVPaths.get(positionVPaths.size() - 1);
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
                        positionVPaths.get(positionVPaths.size() - 1).add(newVector);
                    }
                }
                return true;
        }
        return false;
    }

    private Vector2D createVector(short x, short y) {
        Vector2D v = new Vector2D(x,y);
        applyGrid(v, getResources().getInteger(R.integer.grid_width), this.getMeasuredWidth(),
                getResources().getInteger(R.integer.grid_height), this.getMeasuredHeight());
        applyBounds(v, getResources().getInteger(R.integer.grid_width), getResources().getInteger(R.integer.grid_width));
        return v;
    }

    public List<Vector2D> getPositionVectorList() {
        List<Vector2D> completeList = new ArrayList<>();
        for(List<Vector2D> vectorList : positionVPaths) {
            if(completeList.size() > 0) {
                completeList.add(new Vector2D(Short.MAX_VALUE, Short.MAX_VALUE));
            }
            completeList.addAll(vectorList);
        }
        return completeList;
    }

    /**
     * Calculates the stroke width in pixels. The width depends on the size of the draw view canvas.
     * @return stroke width
     */
    private float getStrokeWidth() {
        return ((float)this.getMeasuredHeight()) / 150;
    }

    public void setLineMode(boolean lineMode) {
        this.lineMode = lineMode;
    }

    public boolean isDrawing() {
        return drawing;
    }
}
