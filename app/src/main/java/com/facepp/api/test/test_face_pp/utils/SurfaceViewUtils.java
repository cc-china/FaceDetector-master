package com.facepp.api.test.test_face_pp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.SurfaceHolder;

import com.facepp.api.test.R;


/**
 * Created by Administrator on 2019\1\18 0018.
 */

public class SurfaceViewUtils {
    public static void setBackGroupBitmap(SurfaceHolder holder, Context ctx, int width, int height) {
        Canvas canvas = holder.lockCanvas();
        Bitmap bitmap = drawableToBitmap(ctx.getResources().getDrawable(R.drawable.surfaceview_face_line));
        Rect src = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        Rect dst = new Rect(0,0,width,height);
        canvas.drawBitmap(bitmap,src, dst,null);
        holder.unlockCanvasAndPost(canvas);
    }

    // drawable 转换成bitmap
    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();// 取drawable的长宽
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;// 取drawable的颜色格式
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);// 建立对应bitmap
        Canvas canvas = new Canvas(bitmap);// 建立对应bitmap的画布
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);// 把drawable内容画到画布中
        return bitmap;
    }
}
