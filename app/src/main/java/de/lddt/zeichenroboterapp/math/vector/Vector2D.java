package de.lddt.zeichenroboterapp.math.vector;

/**
 * This class is used to manage positions and directions.
 */
public class Vector2D {
    public float x;
    public float y;

    public Vector2D() {
        this.x = 0;
        this.y = 0;
    }

    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Subtraction of 2 vectors
     *
     * @param v1 minuend
     * @param v2 subtrahend
     * @return new vector. The is the difference of the values of the 2 given vectors.
     */
    public static Vector2D sub(Vector2D v1, Vector2D v2) {
        return new Vector2D((v1.x - v2.x), (v1.y - v2.y));
    }

    public static Vector2D sum(Vector2D... v) {
        Vector2D val = new Vector2D();
        for (Vector2D aV : v) {
            val.x += aV.x;
            val.y += aV.y;
        }
        return val;
    }

    public static float dot(Vector2D v1, Vector2D v2) {
        return (v1.x * v2.x + v1.y * v2.y);
    }

    public float length() {
        return (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    public void normalize() {
        float len = length();
        x /= len;
        y /= len;
    }

    public static Vector2D normalize(Vector2D v) {
        float len = v.length();
        return new Vector2D(v.x / len, v.y / len);
    }

    public void round() {
        this.x = Math.round(x);
        this.y = Math.round(y);
    }

    @Override
    public boolean equals(Object o) {
        return getClass().isInstance(o) && x == ((Vector2D) o).x && y == ((Vector2D) o).y;
    }
}
