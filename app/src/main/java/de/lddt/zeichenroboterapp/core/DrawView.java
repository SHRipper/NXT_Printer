package de.lddt.zeichenroboterapp.core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

import de.lddt.zeichenroboterapp.R;
import de.lddt.zeichenroboterapp.math.Path;
import de.lddt.zeichenroboterapp.math.Vector2D;

import static de.lddt.zeichenroboterapp.util.VectorConverter.applyBounds;

/**
 * The Canvas for the drawing.
 */
public class DrawView extends SurfaceView {
    private List<Path> paths;
    private Paint paint;
    private LineMode lineMode;
    private boolean drawing, lineIsLinked;
    private int canvasLength;

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

    /**
     * Initialize a new DrawView instance.
     */
    private void init() {
        this.paths = new ArrayList<>();
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        lineMode = LineMode.FREE;
        drawing = false;
        lineIsLinked = false;
        canvasLength = -1;
    }

    /**
     * Calculate the length (width and height) of the canvas.
     */
    public int getCanvasLength() {
        if (canvasLength == -1) {
            //The canvas size is a square.
            canvasLength = Math.min(getMeasuredHeight(), getMeasuredWidth());
        }
        return canvasLength;
    }

    /**
     * Clear all recorded paths and vectors.
     */
    public void clear() {
        drawing = false;
        paths.clear();
        invalidate();
    }

    /**
     * Remove only the last drawn path.
     */
    public void undo() {
        drawing = false;
        if (paths.size() > 0) {
            paths.remove(paths.size() - 1);
        }
        invalidate();
    }

    /**
     * Change the Drawing Mode. This effects how the touch input is processed.
     *
     * @param lineMode the new drawing mode.
     */
    public void setLineMode(LineMode lineMode) {
        this.lineMode = lineMode;
    }

    /**
     * Redraw the recorded paths.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //set the stroke width
        paint.setStrokeWidth(getStrokeWidth());

        for (int i = 0; i < paths.size(); i++) {
            Path curPath = paths.get(i);
            if (curPath.length() >= 2) {
                if (!drawing || i < paths.size() - 1) {
                    //The default color for drawing the paths on the canvas.
                    paint.setColor(getResources().getColor(R.color.final_draw_color));
                } else {
                    //While the user is drawing, set another color for the current path.
                    paint.setColor(getResources().getColor(R.color.hint_draw_color));
                }
                //draw the path on the canvas
                canvas.drawLines(curPath.getPointsOfLine(), paint);
            }
        }
    }

    /**
     * Handle touch events and create paths.
     *
     * @param event the motion event.
     * @return true if the event was handled
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Vector2D linkedLineStart = getLinkedLineStart();
                //If the drawing mode is linked line.
                //A new line starts where the previous path ended.
                if (linkedLineStart != null && lineMode == LineMode.LINKED_LINE) {
                    startNewPath(linkedLineStart.x, linkedLineStart.y);
                    addToPath(x, y);
                    invalidate();
                } else {
                    //Start a new path.
                    startNewPath(x, y);
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                //In draw mode add the new position to the path.
                //If the drawing mode is line or linked line also change the path.
                if (drawing || lineMode == LineMode.LINE || lineMode == LineMode.LINKED_LINE) {
                    addToPath(x, y);
                    invalidate();//Redraw the canvas.
                    //Set bool drawing depending on whether the touch position is inside the canvas.
                    drawing = isInBounds(x, y);
                } else {
                    //Start a new path when the touch position is now inside the canvas
                    //and has previously been not.
                    drawing = isInBounds(x, y);
                    if (drawing) {
                        startNewPath(x, y);
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
                drawing = false; //finished drawing
                invalidate(); //redraw the canvas
                return true;
        }
        return false;
    }

    /**
     * Create a new path with x and y as start position and add it to the list of paths.
     */
    private void startNewPath(float x, float y) {
        drawing = true;//Started drawing on the canvas.
        //Set bool lineIsLinked to true if the current drawing mode is linked line.
        lineIsLinked = (lineMode == LineMode.LINKED_LINE);
        //Create a new path and set x and y as the start position.
        Path newPath = new Path(createVector(x, y));
        //Add the path to List
        paths.add(newPath);
    }

    /**
     * Add a new position to the current path.
     */
    private void addToPath(float x, float y) {
        if (paths.size() > 0) {
            Path currentPath = paths.get(paths.size() - 1);
            //If the drawing mode is line or linked line, the last vector of the line is removed.
            if ((lineMode == LineMode.LINE || lineMode == LineMode.LINKED_LINE)
                    && currentPath.length() > 1) {
                currentPath.rewind();
            }
            //Extend the path by the new position
            currentPath.addNewPos(createVector(x, y));
        }
    }

    /**
     * Get the start for a new linked line.
     * The new linked line starts where the last one ended.
     *
     * @return the Vector of the start position.
     */
    private Vector2D getLinkedLineStart() {
        if (lineIsLinked && paths.size() > 0) {
            Path currentPath = paths.get(paths.size() - 1);
            return currentPath.last();
        }
        return null;
    }

    /**
     * Create a new Vector2D instance. The Vector represents a position on the canvas.
     *
     * @return the new vector.
     */
    private Vector2D createVector(float x, float y) {
        Vector2D v = new Vector2D(x, y);
        applyBounds(v, canvasLength);
        return v;
    }

    /**
     * @return true if a position is inside the canvas area.
     */
    private boolean isInBounds(float x, float y) {
        return x >= 0 && x <= canvasLength
                && y >= 0 && y <= canvasLength;
    }

    /**
     * Create a List with all vectors of the recorded paths.
     * Different paths are separate with a special vector.
     * The x and y value of this special vector are set to Short.Max_Value.
     *
     * @return list with all vectors.
     */
    public List<Vector2D> getPosVList() {
        List<Vector2D> completeList = new ArrayList<>();
        Vector2D lastOfPath = null;
        for (int i = 0; i < paths.size(); i++) {
            Path currentPath = paths.get(i);

            if (i > 0) {
                //If the last vector of the previous path and the first vector of the current path are equal,
                // remove one of these equal vectors
                if (lastOfPath != null && lastOfPath.equals(currentPath.first())) {
                    completeList.remove(completeList.size() - 1);
                } else {
                    //The vector (x = Short.MAX_VALUE, y = Short.MAX_VALUE) indicates a new path.
                    completeList.add(new Vector2D(Short.MAX_VALUE, Short.MAX_VALUE));
                }
            }
            lastOfPath = currentPath.last();
            completeList.addAll(currentPath.getVectors());
        }
        return completeList;
    }

    /**
     * Calculates the stroke width in pixels for the preview drawing on the screen.
     * The width depends on the resolution of the canvas.
     *
     * @return stroke width in pixels.
     */
    private float getStrokeWidth() {
        return ((float) this.canvasLength) / 150;
    }

    public boolean isDrawing() {
        return drawing;
    }
}
