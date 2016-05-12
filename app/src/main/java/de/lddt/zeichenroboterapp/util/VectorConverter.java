package de.lddt.zeichenroboterapp.util;

import java.util.ArrayList;
import java.util.List;

import de.lddt.zeichenroboterapp.math.vector.Vector2D;

/**
 * Created by Tim on 11.05.2016.
 */
public class VectorConverter {
    public static List<Vector2D> positionVToDirectionV(List<Vector2D> vectorList) {
        List<Vector2D> directionVectorList = new ArrayList<>();
        Vector2D temp = new Vector2D((short) 0, (short) 0);

        for(Vector2D vector : vectorList) {
            if (vector.getX() == Short.MAX_VALUE || vector.getY() == Short.MAX_VALUE) {
                directionVectorList.add(new Vector2D(Short.MAX_VALUE, Short.MAX_VALUE));
            } else {
                short newX = (short) (vector.getX() - temp.getX());
                short newY = (short) (vector.getY() - temp.getY());
                directionVectorList.add(new Vector2D(newX, newY));
                temp = vector;
            }
        }
        return directionVectorList;
    }
}
