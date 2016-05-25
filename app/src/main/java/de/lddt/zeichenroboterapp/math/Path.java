package de.lddt.zeichenroboterapp.math;

import java.util.ArrayList;
import java.util.List;

import de.lddt.zeichenroboterapp.math.vector.Vector2D;

public class Path {
    private List<Vector2D> vectors;

    public Path(Vector2D start) {
        vectors = new ArrayList<>();
        vectors.add(start);
    }

    public void lineTo(Vector2D newVector) {
        vectors.add(newVector);
    }

    public void rewind() {
        if (vectors.size() > 0) {
            vectors.remove(vectors.size() - 1);
        }
    }

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

    public List<Vector2D> getVectors() {
        List<Vector2D> copy = new ArrayList<>();
        for (Vector2D v : vectors) {
            copy.add(new Vector2D(v.x, v.y));
        }
        return copy;
    }

    public Vector2D last() {
        return vectors.get(vectors.size() - 1);
    }

    public int length() {
        return vectors.size();
    }
}
