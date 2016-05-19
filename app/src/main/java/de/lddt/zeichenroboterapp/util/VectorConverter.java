package de.lddt.zeichenroboterapp.util;

import java.util.ArrayList;
import java.util.List;

import de.lddt.zeichenroboterapp.math.vector.Vector2D;

/**
 * This Class converts different kind of vectors
 */
public class VectorConverter {
    /**
     * Convert position vectors to direction vectors
     * Position Vectors which lay on a line (with a certain tolerance) are excluded.
     * @param posVList a list of position vectors
     * @param accuracyDeg tolerance value for optimization.
     * @return a List of direction vectors
     */
    public static List<Vector2D> positionVToDirectionV(List<Vector2D> posVList, float accuracyDeg) {
        //remove unnecessary position Vectors
        float accuracy = (float) Math.cos(accuracyDeg / 180 * Math.PI);
        for(int i = 2; i < posVList.size(); i++) { //optimize positions
            if(posVList.get(i-2).getX() != Short.MAX_VALUE && posVList.get(i-1).getX() != Short.MAX_VALUE && posVList.get(i).getX() != Short.MAX_VALUE){
                Vector2D v1 = Vector2D.normalize(posVToDirV(posVList.get(i-1), posVList.get(i-2)));
                Vector2D v2 = Vector2D.normalize(posVToDirV(posVList.get(i  ), posVList.get(i-1)));
                float dot = Math.abs(Vector2D.dot(v1, v2));
                if(dot > accuracy){
                    posVList.remove(i-1);
                    i--;
                }
            }
        }

        //create a List of direction Vectors which will be drawn by the robot
        List<Vector2D> directionVectorList = new ArrayList<>();
        Vector2D temp = new Vector2D(0, 0);
        for(Vector2D vector : posVList) { //create directions
            if (vector.getX() == Short.MAX_VALUE && vector.getY() == Short.MAX_VALUE) {
                directionVectorList.add(new Vector2D(Short.MAX_VALUE, Short.MAX_VALUE));
            } else {
                directionVectorList.add(posVToDirV(vector, temp));
                temp = vector;
            }
        }
        return directionVectorList;
    }


    /** Creates a Vector2D. This Vector is the direction vector from one position to another position.
     * @param v1 start postion
     * @param v2 end position
     * @return direction vector.
     */
    public static Vector2D posVToDirV(Vector2D v1, Vector2D v2) {
        return Vector2D.sub(v1, v2);
    }
}
