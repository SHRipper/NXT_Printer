package de.lddt.zeichenroboterapp.math.vector;

/**
 * Created by Tim on 27.04.2016.
 */
public class Vector2D {
    protected float x;
    protected float y;

    public Vector2D(){}
    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        return getClass().isInstance(o) && x == ((Vector2D) o).getX() && y == ((Vector2D) o).getY();
    }

    public static Vector2D sub(Vector2D v1, Vector2D v2){
        return new Vector2D((v1.x - v2.x), (v1.y - v2.y));
    }
    public static Vector2D sum(Vector2D... v){
        Vector2D val = new Vector2D();
        for(int i = 0; i < v.length; i++){
            val.x += v[i].x;
            val.y += v[i].y;
        }
        return val;
    }

    public static float dot(Vector2D v1, Vector2D v2){
        return (v1.x * v2.x + v1.y * v2.y);
    }

    public float length(){
        return (float)Math.sqrt(Math.pow(x, 2) + Math.pow(y,2));
    }

    public void normalize(){
        float len = length();
        x /= len;
        y /= len;
    }
    public static Vector2D normalize(Vector2D v){
        float len = v.length();
        return new Vector2D(v.x / len, v.y / len);
    }
}
