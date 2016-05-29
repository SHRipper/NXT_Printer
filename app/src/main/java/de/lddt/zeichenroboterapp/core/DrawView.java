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
import de.lddt.zeichenroboterapp.util.MetricsConverter;

import static de.lddt.zeichenroboterapp.util.VectorConverter.applyBounds;

/**
 * The Canvas for the drawing.
 */
public class DrawView extends SurfaceView {
    private List<Path> paths;
    private Paint paint;
    private LineMode lineMode;
    private boolean drawing;
    private int canvasLength;
    private Path currentPath;    //Reference to the most recent path

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
        canvasLength = -1;
        currentPath = null;
        float strokeWidth = MetricsConverter.convertToPixels(2.4f, getContext());
        paint.setStrokeWidth(strokeWidth);
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
        currentPath = null;
        paths.clear();
        invalidate();
    }

    /**
     * Remove the last drawn path or the last drawn line (in linked line mode)
     */
    public void undo() {
        drawing = false;
        currentPath = getCurrentPath();
        if (currentPath != null) {
            if (currentPath.getType() == LineMode.FREE || currentPath.getType() == LineMode.LINE) {
                paths.remove(currentPath);
                //Update the currentPath reference
                currentPath = getCurrentPath();
            }

            //In the linked line mode remove only the last line
            if (currentPath.getType() == LineMode.LINKED_LINE) {
                currentPath.rewind();
                //Remove the path if the length of the path is 0
                if (currentPath.length() == 0) {
                    paths.remove(currentPath);
                    //Update the currentPath reference
                    currentPath = getCurrentPath();
                }
            }
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

        for (Path path : paths) {
            if (path.length() >= 2) {
                if (drawing && path == currentPath) {
                    //While the user is drawing, set another color for the current path.
                    paint.setColor(getResources().getColor(R.color.hint_draw_color));
                } else {
                    //The default color for drawing the paths on the canvas.
                    paint.setColor(getResources().getColor(R.color.final_draw_color));
                }
                //draw the path on the canvas
                canvas.drawLines(path.getPointsOfLine(), paint);
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
        currentPath = getCurrentPath();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawing = true;//Started drawing on the canvas.

                //If the drawing mode is linked line and the current path is a linked line path,
                //a new line starts where the previous line ended.
                if (currentPath != null
                        && lineMode == LineMode.LINKED_LINE
                        && currentPath.getType() == LineMode.LINKED_LINE) {
                    addToPath(x, y, currentPath);
                    invalidate();
                } else {
                    //Start a new path.
                    currentPath = startNewPath(x, y);
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (drawing) {
                    drawLine(x, y);
                } else {
                    //Start a new path when the touch position is now inside the canvas
                    //and has previously been not.
                    drawing = isInBounds(x, y);
                    if (drawing) {
                        currentPath = startNewPath(x, y);
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
     * Draw a line. The behaviour depends on the drawing mode of the current path.
     *
     * @param x the x position of the touch input.
     * @param y the y position of the touch input.
     */
    private void drawLine(float x, float y) {
        //If the drawing mode is line or linked line, the last vector of the line is removed.
        if (currentPath.getType() == LineMode.LINE || currentPath.getType() == LineMode.LINKED_LINE
                && currentPath.length() > 1) {
            currentPath.rewind();
        }

        addToPath(x, y, currentPath); //Add a new position to the path.
        invalidate();//Redraw the canvas.

        //Only care about canvas Bounds, when in free drawing mode.
        if (lineMode == LineMode.FREE) {
            //Set bool drawing depending on whether the touch position is inside the canvas.
            drawing = isInBounds(x, y);
        }
    }

    /**
     * Create a new path with x and y as start position and add it to the list of paths.
     * @return the path we just created
     */
    private Path startNewPath(float x, float y) {
        //Create a new path and set x and y as the start position.
        //Also set the type of the path to the current drawing mode.
        Path newPath = new Path(createVector(x, y), lineMode);
        //Add the path to List
        paths.add(newPath);
        return newPath;
    }

    /**
     * @param path Extend the given path by the new position.
     */
    private void addToPath(float x, float y, Path path) {
        path.addNewPos(createVector(x, y));
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
     * @return the most recent path.
     */
    private Path getCurrentPath() {
        if (paths.size() > 0) {
            return paths.get(paths.size() - 1);
        }
        return null;
    }

    /**
     * This Class returns a boolean value that indicates if the user is
     * currently drawing (=true) or not (=false)
     */
    public boolean isDrawing() {
        return drawing;
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
        for (Path path : paths) {
            //a path needs to consist of at least 2 vectors
            if (path.length() < 2) {
                if (completeList.size() > 0) {
                    //The vector (x = Short.MAX_VALUE, y = Short.MAX_VALUE) indicates a new path.
                    Vector2D newPathIndicator = new Vector2D(Short.MAX_VALUE, Short.MAX_VALUE);
                    completeList.add(newPathIndicator);
                }
                completeList.addAll(path.getVectors());
            }
        }
        return completeList;
    }
}
