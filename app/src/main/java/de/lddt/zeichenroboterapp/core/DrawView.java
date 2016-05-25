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
import de.lddt.zeichenroboterapp.math.vector.Vector2D;

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

    private void init() {
        this.paths = new ArrayList<>();
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        lineMode = LineMode.FREE;
        drawing = false;
        lineIsLinked = false;
        canvasLength = -1;
    }

    public int getCanvasLength() {
        if (canvasLength == -1) {
            canvasLength = Math.min(getMeasuredHeight(), getMeasuredWidth());
        }
        return canvasLength;
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

        for (int i = 0; i < paths.size(); i++) {
            Path curPath = paths.get(i);
            if (curPath.length() >= 2) {
                if (drawing && i == paths.size() - 1) {
                    //While the user is drawing, set another color for the current path.
                    paint.setColor(getResources().getColor(R.color.hint_draw_color));
                } else {
                    paint.setColor(getResources().getColor(R.color.final_draw_color));
                }
                //draw the path on the canvas
                canvas.drawLines(curPath.getPointsOfLine(), paint);
            }
        }
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
     * Handle touch events.
     *
     * @param event
     * @return true if the event was handled
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Vector2D linkedLineStart = getLinkedLineStart();
                if (linkedLineStart != null && lineMode == LineMode.LINKED_LINE) {
                    startNewPath(linkedLineStart.x, linkedLineStart.y);
                    addToPath(x, y);
                    invalidate();
                } else {
                    startNewPath(x, y);
                }

                return true;

            case MotionEvent.ACTION_MOVE:
                if (drawing || lineMode == LineMode.LINE || lineMode == LineMode.LINKED_LINE) {
                    addToPath(x, y);
                    invalidate();//Redraw the canvas.
                    drawing = isInBounds(x, y);
                } else {
                    drawing = isInBounds(x, y);
                    if (drawing) {
                        startNewPath(x, y);
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
                //finished drawing
                drawing = false;

                //redraw the canvas
                invalidate();
                return true;
        }
        return false;
    }

    private void startNewPath(float x, float y) {
        drawing = true;//started drawing on the canvas
        lineIsLinked = (lineMode == LineMode.LINKED_LINE);
        //also save this position
        Vector2D start = createVector(x, y);
        paths.add(new Path(start));
    }

    private void addToPath(float x, float y) {
        if (paths.size() > 0) {
            Path currentPath = paths.get(paths.size() - 1);
            if ((lineMode == LineMode.LINE || lineMode == LineMode.LINKED_LINE)
                    && currentPath.length() > 1) {
                currentPath.rewind();
            }
            //Extend the path by the new position
            currentPath.lineTo(createVector(x, y));
        }
    }

    private Vector2D getLinkedLineStart() {
        if (lineIsLinked && paths.size() > 0) {
            Path currentPath = paths.get(paths.size() - 1);
            return currentPath.last();
        }
        return null;
    }

    /**
     * Create a new Vector2D instance.
     *
     * @param x value of the new vector.
     * @param y value of the new vector.
     * @return the new vector.
     */
    private Vector2D createVector(float x, float y) {
        Vector2D v = new Vector2D(x, y);
        applyBounds(v, canvasLength);
        return v;
    }

    private boolean isInBounds(float x, float y) {
        return x >= 0 && x <= canvasLength
                && y >= 0 && y <= canvasLength;
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
        for (Path path : paths) {
            completeList.addAll(path.getVectors());
            completeList.add(new Vector2D(Short.MAX_VALUE, Short.MAX_VALUE));
        }
        return completeList;
    }

    /**
     * Calculates the stroke width in pixels. The width depends on the size of the draw view canvas.
     *
     * @return stroke width
     */
    private float getStrokeWidth() {
        return ((float) this.canvasLength) / 150;
    }

    public void setLineMode(LineMode lineMode) {
        this.lineMode = lineMode;
    }

    public boolean isDrawing() {
        return drawing;
    }
}
