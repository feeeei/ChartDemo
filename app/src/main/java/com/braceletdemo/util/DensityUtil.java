/**
 *
 */
package com.braceletdemo.util;

import android.content.Context;

/**
 * 单位转化工具类,dp,px,sp之间转化
 */
public class DensityUtil {

    /** 这里需要改成float返回,绘图的时候更精确 */
    public static float dip2px(Context paramContext, float paramFloat) {
        float f = paramContext.getResources().getDisplayMetrics().density;
        return paramFloat * f;
    }

    public static int px2dip(Context paramContext, float paramFloat) {
        float f = paramContext.getResources().getDisplayMetrics().density;
        return (int) (paramFloat / f + 0.5F);
    }

    public static int sp2px(Context paramContext, float paramFloat) {
        float f = paramContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (paramFloat * f + 0.5F);
    }

    public static int px2sp(Context paramContext, float paramFloat) {
        float f = paramContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (paramFloat / f + 0.5F);
    }
}