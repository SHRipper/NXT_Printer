package de.lddt.zeichenroboterapp.util;

import java.nio.ByteBuffer;
import java.util.List;

import de.lddt.zeichenroboterapp.math.vector.Vector2D;

/**
 * Created by Tim on 11.05.2016.
 */
public class ToByteConverter {
    public static byte [] intToByte(int i) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(i);
        return buffer.array();
    }

    public static byte [] vectorListToByte(List<Vector2D> vectorList) {
        byte bytes [] = new byte[vectorList.size() *4];

        int i = 0;
        for (Vector2D vector2D : vectorList) {
            byte [] temp = shortToByte(vector2D.getX());
            bytes [i++] = temp [0];
            bytes [i++] = temp [1];

            temp = shortToByte(vector2D.getY());
            bytes [i++] = temp [0];
            bytes [i++] = temp [1];
        }

        return bytes;
    }

    public static byte [] shortToByte(short s) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(s);
        return buffer.array();
    }
}
