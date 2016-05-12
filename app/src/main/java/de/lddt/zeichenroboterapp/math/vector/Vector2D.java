package de.lddt.zeichenroboterapp.math.vector;

/**
 * Created by Tim on 27.04.2016.
 */
public class Vector2D {
    protected short x,y;

    public Vector2D(short x, short y) {
        this.x = x;
        this.y = y;
    }

    public short getX() {
        return x;
    }

    public short getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        return getClass().isInstance(o) && x == ((Vector2D) o).getX() && y == ((Vector2D) o).getY();
    }
}
