package com.halohoop.usoppbubble.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by Pooholah on 2017/5/25.
 */

public class Utils {
    public static final boolean DEBUG = true;
    /**
     * 得到传入的view的bitmap
     * @param view
     * @return
     */
    @Deprecated
    public static Bitmap createBitmapFromView(View view) {
        Bitmap bitmap;
//        Rect rect = new Rect();
//        view.getGlobalVisibleRect(rect);

        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap src = view.getDrawingCache();
        bitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight());
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);
        src.recycle();
        src = null;
        return bitmap;
    }

    /**
     * 弧度转角度
     * @param radian
     * @return
     */
    public static float radian2Degree(double radian){
        return (float) (180 / Math.PI * radian);
    }

    /**
     * 获取状态栏高度
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 得到过两点直线的 斜率 和 偏移，y=kx+h，那么最终得到k 和 h的值。
     * float[0] --> k
     * float[1] --> h
     * @param pointF0 点1
     * @param pointF1 点2
     * @return
     */
    public static float[] getTwoPointLine(PointF pointF0, PointF pointF1) {
        float[] k_h = new float[2];
        k_h[0] = (pointF0.y - pointF1.y) / (pointF0.x - pointF1.x);
        k_h[1] = pointF0.y - k_h[0] * pointF0.x;
        return k_h;
    }

    /**
     * 直线y=kx+h 当为x时，得到y
     * @param k
     * @param h
     * @param x
     * @return
     */
    public static float getYFromLine(float k,float h, float x){
        return k * x + h;
    }

    /**
     * 直线y=kx+h 当为y时，得到x
     * @param k
     * @param h
     * @param y
     * @return
     */
    public static float getXFromLine(float k,float h, float y){
        return (y - h) / k;
    }

    /**
     * sqrt((x0 - x1)^2 + (y0 - y1)^2)
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @return
     */
    public static float getTwoPointsDistance(float x0,float y0,float x1,float y1){
        return (float) Math.hypot(x0 - x1, y0 - y1);
    }

    public static float getTwoPointsDistance(PointF pointF0,PointF pointF1){
        return getTwoPointsDistance(pointF0.x, pointF0.y, pointF1.x, pointF1.y);
    }

    /**
     * 获取屏幕宽高
     * @param context
     * @param contentOffset
     * @return
     */
    public static Point getContentSize(Activity context, float contentOffset) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point out = new Point();
        if (Build.VERSION.SDK_INT >= 13) {
            display.getSize(out);
        } else {
            int width = display.getWidth();
            int height = display.getHeight();
            out.set(width, height);
        }
        if (contentOffset > 0) {
            out.y -= contentOffset;
        }
        return out;
    }

    private static final String TAG = "UsoppBubble--";

    public static void l(String str) {
        if (DEBUG) {
            Log.i(TAG, str);
        }
    }

    public static Activity scanForActivity(Context cont) {
        if (cont == null)
            return null;
        else if (cont instanceof Activity)
            return (Activity) cont;
        else if (cont instanceof ContextWrapper)
            return scanForActivity(((ContextWrapper) cont).getBaseContext());

        return null;
    }


}
