package com.facepp.api.test.test_face_pp.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by Administrator on 2019\1\9 0009.
 */

public class Utils {
    /**
     * 生成对话框
     *
     * @param context
     * @return
     */
    public static Dialog showProgressDialog(Context context, String message,
                                            boolean cancelableOnTouch) {
//        final ProgressDialog dialog = new ProgressDialog(context);
//        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条的形式为圆形转动的进度条
        SFProgressDialog dialog = SFProgressDialog.createProgrssDialog(context);
        dialog.setCancelable(cancelableOnTouch);// 设置是否可以通过点击Back键取消
        dialog.setCanceledOnTouchOutside(cancelableOnTouch);// 设置在点击Dialog外是否取消Dialog进度条
        dialog.setMessage(message);
        return dialog;
    }

    /**
     * 调整当前Activity屏幕亮度
     */
    public static void setLight(Activity context, int brightness) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
        context.getWindow().setAttributes(lp);
    }

    public static void getDisplayMetrics(Activity ctx){
        Display display = ctx.getWindowManager().getDefaultDisplay();
        int heigth = display.getWidth();
        int width = display.getHeight();
    }

    public static float dp2px(Context ctx,float dp){
        DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
        float value = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dp, metrics);
        return value;
    }
}
