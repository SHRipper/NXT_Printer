package de.lddt.zeichenroboterapp.util;

import java.util.List;

import de.lddt.zeichenroboterapp.math.vector.Vector2D;

/**
 * Created by Tim on 11.05.2016.
 */
public class VectorConverter {
    public static List<Vector2D> positionVToDirectionV(List<Vector2D> vectorList) {
        Vector2D temp = null;
        for(Vector2D vector : vectorList) {
            if(vector.getX() == Short.MAX_VALUE && vector.getY() == Short.MAX_VALUE) {
                continue;
            }
            if(temp != null) {
                vector.sub(temp);
            }
            temp = vector;
        }
        return vectorList;
    }
}
