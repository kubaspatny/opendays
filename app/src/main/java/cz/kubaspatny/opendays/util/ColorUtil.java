package cz.kubaspatny.opendays.util;

import android.graphics.Color;

/**
 * Created by Kuba on 15/3/2015.
 */
public class ColorUtil {

    public static int darken(int color, float factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a,
                Math.max((int) (r * (1 - factor)), 0),
                Math.max((int) (g * (1 - factor)), 0),
                Math.max((int) (b * (1 - factor)), 0));
    }

    public static int lighten(int color, float factor) {
        int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
        int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
        int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
        return Color.argb(Color.alpha(color), red, green, blue);
    }

}
