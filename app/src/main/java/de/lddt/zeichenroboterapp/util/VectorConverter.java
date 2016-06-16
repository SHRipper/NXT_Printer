package de.lddt.zeichenroboterapp.util;

import java.util.ArrayList;
import java.util.List;

import de.lddt.zeichenroboterapp.math.Vector2D;

/**
 * This Class does operations on different kinds of vectors.
 */
public class VectorConverter {

    /**
     * Convert position vectors to direction vectors
     *
     * @param posVList    a list of position vectors
     * @param accuracyDeg tolerance value needed for optimization.
     * @return a List of direction vectors
     */
    public static List<Vector2D> posVToDirV(List<Vector2D> posVList, float accuracyDeg) {
        //leave recorded Vectors untouched
        List<Vector2D> optimizedVList = new ArrayList<>(posVList);
        //remove unnecessary position Vectors
        optimizedVList = optimizePosVectors(optimizedVList, accuracyDeg);

        //create a List of direction Vectors which will be drawn by the robot
        List<Vector2D> dirVList = new ArrayList<>();
        Vector2D temp = new Vector2D(0, 0);
        for (Vector2D vector : optimizedVList) { //create directions
            if (vector.x == Short.MAX_VALUE && vector.y == Short.MAX_VALUE) {
                dirVList.add(new Vector2D(Short.MAX_VALUE, Short.MAX_VALUE));
            } else {
                dirVList.add(posVToDirV(vector, temp));
                temp = vector;
            }
        }
        return dirVList;
    }

    /**
     * Creates a Vector2D. This Vector is the direction vector from one position to another position.
     *
     * @param v1 start position
     * @param v2 end position
     * @return direction vector.
     */
    private static Vector2D posVToDirV(Vector2D v1, Vector2D v2) {
        return Vector2D.sub(v1, v2);
    }

    /**
     * Project the vectors on the grid so the nxt brick can process the vectors.
     *
     * @param vectorList   the list of vectors.
     * @param currentGridLen the current grid length
     * @param currentGridLen the target grid length
     * @return the on the grid projected vectors.
     */
    public static List<Vector2D> applyGrid(List<Vector2D> vectorList, int currentGridLen, int targetGridLen) {
        List<Vector2D> appliedList = new ArrayList<>();
        Vector2D temp = null;
        for (Vector2D v : vectorList) {
            if (v.x != Short.MAX_VALUE && v.y != Short.MAX_VALUE) {
                applyGrid(v, currentGridLen, targetGridLen);
            }
            if (!v.equals(temp)) {
                appliedList.add(v);
                temp = v;
            }
        }
        return appliedList;
    }

    /**
     * Projects a vector on the grid.
     *
     * @param v            the vector to be projected
     * @param targetGridLen   the width and height of the grid
     * @param currentGridLen the width/the height of the actual canvas on the screen
     */
    public static void applyGrid(Vector2D v, float currentGridLen, float targetGridLen) {
        v.x *= targetGridLen / currentGridLen;
        v.y *= targetGridLen / currentGridLen;
        v.round();
    }

    /**
     * Vectors should never be outside of the grid area
     *
     * @param v         the vector to precess
     * @param maxLength the maximum allowed x and y value
     */
    public static void applyBounds(Vector2D v, float maxLength) {
        v.x = Math.max(v.x, 0);
        v.x = Math.min(v.x, maxLength);
        v.y = Math.max(v.y, 0);
        v.y = Math.min(v.y, maxLength);
    }

    /**
     * Position Vectors which lay on a line (with a certain tolerance) are excluded.
     *
     * @param posVList    the list of all position vectors
     * @param accuracyDeg tolerance value for optimization.
     * @return List of optimized position vectors.
     */
    private static List<Vector2D> optimizePosVectors(List<Vector2D> posVList, float accuracyDeg) {
        float accuracy = (float) Math.cos(accuracyDeg / 180 * Math.PI);
        for (int i = 2; i < posVList.size(); i++) { //optimize positions
            if (posVList.get(i - 2).x != Short.MAX_VALUE && posVList.get(i - 1).x != Short.MAX_VALUE && posVList.get(i).x != Short.MAX_VALUE) {
                Vector2D v1 = Vector2D.normalize(posVToDirV(posVList.get(i - 1), posVList.get(i - 2)));
                Vector2D v2 = Vector2D.normalize(posVToDirV(posVList.get(i), posVList.get(i - 1)));
                float dot = Math.abs(Vector2D.dot(v1, v2));
                if (dot > accuracy) {
                    posVList.remove(i - 1);
                    i--;
                }
            }
        }
        return posVList;
    }
}
