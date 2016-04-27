package de.lddt.zeichenroboterapp;

/**
 * Created by Tim on 27.04.2016.
 */
public interface InputListenerInterface {
    Vector2D onTouchDown(float x, float y);
    Vector2D onTouchUp(float x, float y);
    Vector2D onTouchDrag(float x, float y);
}
