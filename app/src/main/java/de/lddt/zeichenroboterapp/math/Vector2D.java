package de.lddt.zeichenroboterapp.math;

/**
 * This class is used to manage positions and directions.
 */
public class Vector2D {
    public float x;
    public float y;

    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Subtraction of 2 vectors.
     *
     * @param v1 minuend
     * @param v2 subtrahend
     * @return new vector instance.
     */
    public static Vector2D sub(Vector2D v1, Vector2D v2) {
        return new Vector2D((v1.x - v2.x), (v1.y - v2.y));
    }

    /**
     * Calculate the dot product of two vectors.
     *
     * @return the dot product
     */
    public static float dot(Vector2D v1, Vector2D v2) {
        return (v1.x * v2.x + v1.y * v2.y);
    }

    /**
     * The length of the vector.
     * @return the calculated length.
     */
    public float length() {
        //noinspection SuspiciousNameCombination
        return (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    /**
     * Return a new normalized vector instance.
     * @param v the vector on which the new vector is based on.
     * @return a new vector instance.
     */
    public static Vector2D normalize(Vector2D v) {
        float len = v.length();
        return new Vector2D(v.x / len, v.y / len);
    }

    /**
     * Round the vector.
     */
    public void round() {
        this.x = Math.round(x);
        this.y = Math.round(y);
    }

    @Override
    public boolean equals(Object o) {
        return getClass().isInstance(o) && x == ((Vector2D) o).x && y == ((Vector2D) o).y;
    }
}
