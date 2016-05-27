package de.lddt.zeichenroboterapp.math;

import java.util.ArrayList;
import java.util.List;

/**
 * A Path stores a List of Vectors representing the drawn path.
 */
public class Path {
    private List<Vector2D> vectors;

    public Path(Vector2D start) {
        vectors = new ArrayList<>();
        vectors.add(start);
    }

    /**
     * Add a vector to the list. This extends the path by one vector.
     *
     * @param newVector the new vector to add.
     */
    public void addNewPos(Vector2D newVector) {
        vectors.add(newVector);
    }

    /**
     * Remove the last vector of the list.
     * This rewinds the path by one element.
     */
    public void rewind() {
        if (vectors.size() > 0) {
            vectors.remove(vectors.size() - 1);
        }
    }

    /**
     * @return a float array containing the positions of each line from the path.
     */
    public float[] getPointsOfLine() {
        float[] array = new float[vectors.size() * 4 - 4];
        int i = 0;
        for (Vector2D v : vectors) {
            array[i] = v.x;
            array[i + 1] = v.y;

            if (i > 0 && i + 2 < array.length) {
                i += 2;
                array[i] = v.x;
                array[i + 1] = v.y;
            }
            i += 2;
        }
        return array;
    }

    /**
     * @return a copy of the vectorList.
     */
    public List<Vector2D> getVectors() {
        List<Vector2D> clone = new ArrayList<>();
        for (Vector2D v : vectors) {
            clone.add(new Vector2D(v.x, v.y));
        }
        return clone;
    }

    /**
     * @return the last vector of the list.
     */
    public Vector2D last() {
        return vectors.get(vectors.size() - 1);
    }

    /**
     * @return the number of vectors
     */
    public int length() {
        return vectors.size();
    }
}
