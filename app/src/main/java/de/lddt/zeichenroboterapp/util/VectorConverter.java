package de.lddt.zeichenroboterapp.util;

import java.util.ArrayList;
import java.util.List;

import de.lddt.zeichenroboterapp.math.vector.Vector2D;

/**
 * Created by Tim on 11.05.2016.
 */
public class VectorConverter {
    public static List<Vector2D> positionVToDirectionV(List<Vector2D> posVList, float accuracyDeg) {
        List<Vector2D> directionVectorList = new ArrayList<>();
        Vector2D temp = new Vector2D((short) 0, (short) 0);

        float accuracy = (float)Math.cos(accuracyDeg / 180 * Math.PI);
        List<Vector2D> optimizedposVList = new ArrayList<Vector2D>();
        optimizedposVList.addAll(posVList);

        for(int i = 2; i < optimizedposVList.size(); i++) { //optimize positions
            if(optimizedposVList.get(i-2).getX() != Short.MAX_VALUE && optimizedposVList.get(i-1).getX() != Short.MAX_VALUE && optimizedposVList.get(i).getX() != Short.MAX_VALUE){
                Vector2D v1 = Vector2D.normalize(Vector2D.sub(optimizedposVList.get(i-1), optimizedposVList.get(i-2)));
                Vector2D v2 = Vector2D.normalize(Vector2D.sub(optimizedposVList.get(i  ), optimizedposVList.get(i-1)));
                float dot = Math.abs(Vector2D.dot(v1, v2));
                if(dot > accuracy){
                    optimizedposVList.remove(i-1);
                    i--;
                }
            }
        }

        for(Vector2D vector : optimizedposVList) { //create directions
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
