package de.lddt.zeichenroboterapp.util;

import android.content.Context;
import android.content.res.Resources;

/**
 * Created by Lukas on 27.05.2016.
 */
public class MetricsConverter {


    public static float convertToPixels(float dp, Context context){
        Resources resources = context.getResources();
        android.util.DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / android.util.DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
}
