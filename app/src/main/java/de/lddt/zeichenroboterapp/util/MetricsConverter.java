package de.lddt.zeichenroboterapp.util;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Different Android devices have different screen sizes and different screen resolution.
 * This class converts a relative metrics and pixels.
 */
public class MetricsConverter {
    /**
     * Convert relative metric (dp) value to pixels
     *
     * @param dp      the given value
     * @param context the context to access DisplayMetrics and density information
     * @return pixels
     */
    public static float convertToPixels(float dp, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
