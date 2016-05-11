package de.lddt.zeichenroboterapp.math.vector;

/**
 * Created by Tim on 27.04.2016.
 */
public class PositionVector2D extends Vector2D {

    public PositionVector2D(short x, short y) {
        super(x, y);
    }

    public void applyWidthBound(float maxWidth) {
        this.x = (short) Math.max(this.x, 0);
        this.x = (short) Math.min(this.x, maxWidth);
    }

    public void applyHeightBound(float maxHeight) {
        this.y = (short) Math.max(this.y, 0);
        this.y = (short) Math.min(this.y, maxHeight);
    }

    public void applyGridWidth(float gridWidth, float canvasWidth) {
        x *= gridWidth/canvasWidth;
    }

    public void applyGridHeight(float gridHeight, float canvasHeight) {
        y *= gridHeight/canvasHeight;
    }
}
