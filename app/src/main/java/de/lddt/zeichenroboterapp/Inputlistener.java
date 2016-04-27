package de.lddt.zeichenroboterapp;

/**
 * Created by Tim on 27.04.2016.
 */
public class Inputlistener implements InputListenerInterface{

    public Inputlistener() {}

    @Override
    public Vector2D onTouchDown(float x, float y) {
        return createVector(x,y, VectorType.START);
    }

    @Override
    public Vector2D onTouchDrag(float x, float y) {
        return createVector(x,y, VectorType.BODY);
    }

    @Override
    public Vector2D onTouchUp(float x, float y) {
        return createVector(x,y, VectorType.END);
    }

    private Vector2D createVector(float x, float y, VectorType type) {
        return new Vector2D(x,y, type);
    }

}
