package de.lddt.zeichenroboterapp.math.vector;

/**
 * Created by Tim on 27.04.2016.
 */
public class Vector2D {
    protected float x,y;
    protected VectorType type;

    public Vector2D(float x, float y, VectorType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
}
