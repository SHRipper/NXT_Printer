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
    private List<List<Vector2D>> posVPaths;
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
        this.posVPaths = new ArrayList<>();
        drawing = false;
        lineMode = false;
        liveDrawPaths = new ArrayList<>();
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
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

    public void reset() {
        posVPaths.clear();
        liveDrawPaths.clear();
        invalidate();
    }

    public void revert(){
        if (posVPaths.size() > 0 && liveDrawPaths.size() > 0) {
            posVPaths.remove(posVPaths.size() - 1);
            liveDrawPaths.remove(liveDrawPaths.size() - 1);
        }
        invalidate();
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

                newVector = createVector(event.getX(), event.getY());
                posVPaths.add(new ArrayList<Vector2D>());
                posVPaths.get(posVPaths.size() - 1).add(newVector);

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
                        newVector = createVector(event.getX(), event.getY());
                        List<Vector2D> currentList = posVPaths.get(posVPaths.size() - 1);
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
                        newVector = createVector(event.getX(), event.getY());
                        posVPaths.get(posVPaths.size() - 1).add(newVector);
                    }
                }
                return true;
        }
        return false;
    }

    /**
     * Create a new Vector2D instance.
     * @param x value of the new vector.
     * @param y value of the new vector.
     * @return the new vector. The x and y value are projected on the grid the nxt robot can process.
     */
    private Vector2D createVector(float x, float y) {
        Vector2D v = new Vector2D(x,y);
        int gridLength = getResources().getInteger(R.integer.grid_length);
        applyGrid(v, this.getMeasuredWidth(), this.getMeasuredHeight(), gridLength);
        applyBounds(v, gridLength);
        return v;
    }

    /**
     * Create a List with all vectors of the recorded lines.
     * Different lines are separate with a special vector.
     * The x and y value of this vector are set to Short.Max_Value.
     * @return list with all vectors.
     */
    public List<Vector2D> getPosVList() {
        List<Vector2D> completeList = new ArrayList<>();
        for (List<Vector2D> vectorList : posVPaths) {
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
