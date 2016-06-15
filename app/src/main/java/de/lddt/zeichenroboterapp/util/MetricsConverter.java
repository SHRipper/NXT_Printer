package de.lddt.zeichenroboterapp.util;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by Lukas on 27.05.2016.
 */
public class MetricsConverter {
    public static float convertToPixels(float dp, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
