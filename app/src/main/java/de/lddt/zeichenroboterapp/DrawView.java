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
 * The Canvas for the drawing.
 */
public class DrawView extends SurfaceView {
    private List<List<Vector2D>> posVPaths;
    private List<Path> liveDrawPaths;
    private Vector2D startVector;
    private Paint paint;
    private boolean drawing;
    private LineMode lineMode;

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
        lineMode = LineMode.FREE;
        liveDrawPaths = new ArrayList<>();
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
    }

    /**
     * Redraw the recorded paths.
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //set the strokeWidth relative to the canvas resolution
        paint.setStrokeWidth(getStrokeWidth());

        for (int i = 0; i < liveDrawPaths.size(); i++) {
            if (drawing && i == liveDrawPaths.size() - 1) {
                //While the user is drawing, set another color for the current path.
                paint.setColor(getResources().getColor(R.color.hint_draw_color));
            } else {
                //Default color
                paint.setColor(getResources().getColor(R.color.final_draw_color));
            }
            //draw the path on the canvas
            canvas.drawPath(liveDrawPaths.get(i), paint);
        }
    }

    /**
     * Clear all recorded paths and vectors.
     */
    public void clear() {
        posVPaths.clear();
        liveDrawPaths.clear();
        invalidate();
    }

    /**
     * Remove only the last drawn path.
     */
    public void undo() {
        if (posVPaths.size() > 0 && liveDrawPaths.size() > 0) {
            posVPaths.remove(posVPaths.size() - 1);
            liveDrawPaths.remove(liveDrawPaths.size() - 1);
        }
        invalidate();
    }

    /**
     * Handle touch events.
     * @param event
     * @return true if the event was handled
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        Vector2D newVector;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //started drawing on the canvas
                drawing = true;
                if (lineMode == LineMode.LINE) {
                    startVector = new Vector2D(event.getX(), event.getY());
                }

                //add a new path to the liveDrawPathsList and set this position as the start position
                liveDrawPaths.add(new Path());
                liveDrawPaths.get(liveDrawPaths.size() - 1).moveTo(event.getX(), event.getY());

                //also save this position (projected on the grid for future transfer to the nxt brick)
                newVector = createVector(event.getX(), event.getY());
                posVPaths.add(new ArrayList<Vector2D>());
                posVPaths.get(posVPaths.size() - 1).add(newVector);
                return true;

            case MotionEvent.ACTION_MOVE:
                if (drawing) {
                    /*If drawing in line mode, reset the last path.
                    Set the previously stored start position as start position for the path.*/
                    if (lineMode == LineMode.LINE) {
                        liveDrawPaths.get(liveDrawPaths.size() - 1).rewind();
                        liveDrawPaths.get(liveDrawPaths.size() - 1).moveTo(startVector.x, startVector.y);
                    }
                    //Extend the path by the new position
                    liveDrawPaths.get(liveDrawPaths.size() - 1).lineTo(event.getX(), event.getY());

                    /*If in free mode save this position.
                    (projected on the grid for future transfer to the nxt brick)*/
                    if (lineMode == LineMode.FREE) {
                        newVector = createVector(event.getX(), event.getY());
                        List<Vector2D> currentList = posVPaths.get(posVPaths.size() - 1);
                        if (!currentList.get(currentList.size() - 1).equals(newVector)) {
                            currentList.add(newVector);
                        }
                    }

                    //Redraw the canvas.
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
                /* Save the position where the user lifts his finger .
                This is the position to which a line will be drawn*/
                if (drawing && lineMode == LineMode.LINE) {
                    newVector = createVector(event.getX(), event.getY());
                    posVPaths.get(posVPaths.size() - 1).add(newVector);
                }
                //finished drawing
                drawing = false;

                //redraw the canvas
                invalidate();
                return true;
        }
        return false;
    }

    /**
     * Create a new Vector2D instance.
     *
     * @param x value of the new vector.
     * @param y value of the new vector.
     * @return the new vector. The x and y value are projected on the grid the nxt robot can process.
     */
    private Vector2D createVector(float x, float y) {
        Vector2D v = new Vector2D(x, y);
        int gridLength = getResources().getInteger(R.integer.grid_length);
        applyGrid(v, this.getMeasuredWidth(), this.getMeasuredHeight(), gridLength);
        applyBounds(v, gridLength);
        return v;
    }

    /**
     * Create a List with all vectors of the recorded lines.
     * Different lines are separate with a special vector.
     * The x and y value of this vector are set to Short.Max_Value.
     *
     * @return list with all vectors.
     */
    public List<Vector2D> getPosVList() {
        List<Vector2D> completeList = new ArrayList<>();
        for (List<Vector2D> vectorList : posVPaths) {
            if (completeList.size() > 0) {
                completeList.add(new Vector2D(Short.MAX_VALUE, Short.MAX_VALUE));
            }
            completeList.addAll(vectorList);
        }
        return completeList;
    }

    /**
     * Calculates the stroke width in pixels. The width depends on the size of the draw view canvas.
     *
     * @return stroke width
     */
    private float getStrokeWidth() {
        return ((float) this.getMeasuredHeight()) / 150;
    }

    public void setLineMode(LineMode lineMode) {

        this.lineMode = lineMode;
    }

    public boolean isDrawing() {
        return drawing;
    }
}
