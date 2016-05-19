package de.lddt.zeichenroboterapp.util;

import java.util.ArrayList;
import java.util.List;

import de.lddt.zeichenroboterapp.math.vector.Vector2D;

/**
 * This Class does operations on different kind of vectors
 */
public class VectorConverter {
    /**
     * Convert position vectors to direction vectors
     * @param posVList a list of position vectors
     * @param accuracyDeg tolerance value needed for optimization.
     * @return a List of direction vectors
     */
    public static List<Vector2D> positionVToDirectionV(List<Vector2D> posVList, float accuracyDeg) {
        //remove unnecessary position Vectors
        posVList = optimizePositionVectors(posVList, accuracyDeg);

        //create a List of direction Vectors which will be drawn by the robot
        List<Vector2D> directionVectorList = new ArrayList<>();
        Vector2D temp = new Vector2D(0, 0);
        for(Vector2D vector : posVList) { //create directions
            if (vector.x == Short.MAX_VALUE && vector.y == Short.MAX_VALUE) {
                directionVectorList.add(new Vector2D(Short.MAX_VALUE, Short.MAX_VALUE));
            } else {
                directionVectorList.add(posVToDirV(vector, temp));
                temp = vector;
            }
        }
        return directionVectorList;
    }

    /**
     * Position Vectors which lay on a line (with a certain tolerance) are excluded.
     * @param posVList the list of all position vectors
     * @param accuracyDeg tolerance value for optimization.
     * @return List of optimized position vectors.
     */
    private static List<Vector2D> optimizePositionVectors(List<Vector2D> posVList, float accuracyDeg) {
        float accuracy = (float) Math.cos(accuracyDeg / 180 * Math.PI);
        for(int i = 2; i < posVList.size(); i++) { //optimize positions
            if(posVList.get(i-2).x != Short.MAX_VALUE && posVList.get(i-1).x != Short.MAX_VALUE && posVList.get(i).x != Short.MAX_VALUE){
                Vector2D v1 = Vector2D.normalize(posVToDirV(posVList.get(i-1), posVList.get(i-2)));
                Vector2D v2 = Vector2D.normalize(posVToDirV(posVList.get(i  ), posVList.get(i-1)));
                float dot = Math.abs(Vector2D.dot(v1, v2));
                if(dot > accuracy){
                    posVList.remove(i-1);
                    i--;
                }
            }
        }
        return posVList;
    }

    /** Creates a Vector2D. This Vector is the direction vector from one position to another position.
     * @param v1 start postion
     * @param v2 end position
     * @return direction vector.
     */
    private static Vector2D posVToDirV(Vector2D v1, Vector2D v2) {
        return Vector2D.sub(v1, v2);
    }

    /**
     * Vectors should never be outside of the grid area
     * @param v the vector to precess
     * @param maxWidth the maximum allowed x value
     * @param maxHeight the maximum allowed y value
     */
    public static void applyBounds(Vector2D v, float maxWidth, float maxHeight) {
        v.x = (short) Math.max(v.x, 0);
        v.x = (short) Math.min(v.x, maxWidth);

        v.y = (short) Math.max(v.y, 0);
        v.y = (short) Math.min(v.y, maxHeight);
    }

    /**
     * projects a vector on the grid
     * @param v the vector to be projected
     * @param gridWidth the width of the grid
     * @param canvasWidth the width of the actual canvas on the screen
     * @param gridHeight the height of the grid
     * @param canvasHeight the height of the actual canvas
     */
    public static void applyGrid(Vector2D v, float gridWidth, float canvasWidth,
                                      float gridHeight, float canvasHeight) {
        v.x *= gridWidth/canvasWidth;
        v.y *= gridHeight/canvasHeight;
    }
}
