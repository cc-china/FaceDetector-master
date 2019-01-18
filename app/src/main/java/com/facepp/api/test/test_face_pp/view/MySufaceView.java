package com.facepp.api.test.test_face_pp.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.facepp.api.test.R;
import com.facepp.api.test.test_face_pp.utils.SurfaceViewUtils;


/**
 * Created by Administrator on 2019\1\16 0016.
 */

public class MySufaceView extends SurfaceView implements SurfaceHolder.Callback {

    private final Context ctx;
    private SurfaceHolder holder;
    private int width,height;

    public MySufaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySufaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.ctx = context;

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
        initView();
    }

    private void initView() {
        holder = getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSPARENT);
        this.setZOrderOnTop(true);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //为surfaceview  加背景框
        Canvas mCanvas = holder.lockCanvas();
        Bitmap bitmap = SurfaceViewUtils.drawableToBitmap(ctx.getResources().getDrawable(R.drawable.surfaceview_face_line));
        Rect src = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        Rect dst = new Rect(0,0,width,height);
        mCanvas.drawBitmap(bitmap,src, dst,null);
        holder.unlockCanvasAndPost(mCanvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
