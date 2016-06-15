package de.lddt.zeichenroboterapp.math;

import java.util.ArrayList;
import java.util.List;

import de.lddt.zeichenroboterapp.core.DrawMode;

/**
 * A Path stores a List of Vectors representing the drawn path.
 */
public class Path {
    private final List<Vector2D> vectors;
    private final DrawMode type;

    public Path(Vector2D start, DrawMode type) {
        vectors = new ArrayList<>();
        vectors.add(start);
        this.type = type;
    }

    public Path(DrawMode type) {
        vectors = new ArrayList<>();
        this.type = type;
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
        //Every vector holds 2 values. Every vector, beside the first and the last, needs to be added twice.
        int arraySize = vectors.size() * 4 - 4;
        float[] array = new float[arraySize];
        int i = 0;
        for (Vector2D v : vectors) {
            array[i] = v.x;
            array[i + 1] = v.y;

            //if this is not the first or last vector, add the values a second time to the array,
            // because a new line starts where the previous line ended
            if (i > 0 && i + 2 < array.length) {
                array[i + 2] = v.x;
                array[i + 3] = v.y;
                i += 4;
            } else {
                i += 2;
            }
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
     * @return the number of vectors
     */
    public int length() {
        return vectors.size();
    }

    /**
     * @return the drawing mode of this path
     */
    public DrawMode getType() {
        return type;
    }
}
