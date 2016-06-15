package de.lddt.zeichenroboterapp.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.lddt.zeichenroboterapp.R;
import de.lddt.zeichenroboterapp.core.DrawMode;
import de.lddt.zeichenroboterapp.math.Path;
import de.lddt.zeichenroboterapp.math.Vector2D;

/**
 * This class handles the loading of Samples from android resource xml files.
 */
public class Sample {

    /**
     * Load a sample.
     *
     * @param context      context reference to access the resource xml file
     * @param sampleId     the id if the integer array containing the coordinates of the sample
     * @param canvasLength the length of the current drawing canvas
     * @return a list of Paths
     */
    public static List<Path> loadSample(Context context, int sampleId, int canvasLength) {
        int[] sample = context.getResources().getIntArray(sampleId);
        int gridLength = context.getResources().getInteger(R.integer.grid_length);

        List<Path> sampleDrawing = new ArrayList<>();

        Path currentPath = new Path(DrawMode.LINKED_LINE);
        sampleDrawing.add(currentPath);
        for (int i = 0; i < sample.length; i += 2) {
            //the value '-1' means a new path is starting now
            if (sample[i] == -1 && sample[i + 1] == -1) {
                currentPath = new Path(DrawMode.LINKED_LINE);
                sampleDrawing.add(currentPath);
            } else {
                Vector2D v = new Vector2D(sample[i], gridLength - sample[i + 1]);
                VectorConverter.applyGrid(v, gridLength, canvasLength);
                currentPath.addNewPos(v);
            }
        }

        return sampleDrawing;
    }

}
