package de.lddt.zeichenroboterapp;

import de.lddt.zeichenroboterapp.math.vector.PositionVector2D;
import de.lddt.zeichenroboterapp.math.vector.Vector2D;
import de.lddt.zeichenroboterapp.math.vector.VectorType;

/**
 * Created by Tim on 27.04.2016.
 */
public class Inputlistener implements InputListenerInterface{

    private int maxWidth, maxHeight;

    public Inputlistener(int width, int height) {
        this.maxWidth = width;
        this.maxHeight = height;
    }

    @Override
    public Vector2D onTouchDown(float x, float y) {
        return createVector(x,y, VectorType.START);
    }

    @Override
    public Vector2D onTouchDrag(float x, float y) {
        return createVector(x, y, VectorType.BODY);
    }

        @Override
    public Vector2D onTouchUp(float x, float y) {
        return createVector(x,y, VectorType.END);
    }

    private Vector2D createVector(float x, float y, VectorType type) {
        PositionVector2D positionVector = new PositionVector2D(x,y, type);
        positionVector.applyWidthBound(maxWidth);
        positionVector.applyHeightBound(maxHeight);
        return positionVector;
    }
}
