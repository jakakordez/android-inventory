package org.partkeepr.inventory;

import android.content.Context;
import android.content.res.Configuration;

import androidx.core.content.ContextCompat;

public class ColorHelper {
    public static int GetBackgroundColor(Context ctx)
    {
        int nightModeFlags = ctx.getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;

        boolean isDark = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
        int color = isDark ? R.color.black : R.color.white;
        return ContextCompat.getColor(ctx, color);
    }
}
