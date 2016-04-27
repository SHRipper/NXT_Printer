package de.lddt.zeichenroboterapp.math.vector;

/**
 * Created by Tim on 27.04.2016.
 */
public class PositionVector2D extends Vector2D {

    public PositionVector2D(float x, float y, VectorType type) {
        super(x, y, type);
    }

    public void applyWidthBound(float maxWidth) {
        this.x = Math.max(this.x, 0);
        this.x = Math.min(this.x, maxWidth);
    }

    public void applyHeightBound(float maxHeight) {
        this.y = Math.max(this.y, 0);
        this.y = Math.min(this.y, maxHeight);
    }
}
