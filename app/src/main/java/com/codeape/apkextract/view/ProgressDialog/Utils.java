package com.codeape.apkextract.view.ProgressDialog;

import android.content.res.Resources;
import android.util.TypedValue;


/**
 * @author LiuPeiming
 *         Created at 2017/5/14 14:18
 */
public class Utils {


    /**
     * Convert Dp to Pixel
     */
    public static int dpToPx(float dp, Resources resources) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) px;
    }

}
