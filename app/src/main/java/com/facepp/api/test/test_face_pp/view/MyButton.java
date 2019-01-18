package com.facepp.api.test.test_face_pp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.facepp.api.test.R;


/**
 * Created by Administrator on 2019\1\17 0017.
 */

public class MyButton extends View {

    private int circle_x;
    private int circle_y;
    private int circle_1_r;
    private int circle_2_r;
    private int circle_3_r;
    private Paint circle_1_paint,circle_2_paint,circle_3_paint;
    private OnClickListener onClickListener;

    public MyButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 画三层，一个白色圆圈  +  圈外黑色空心圆   +  白色空心圆
     *
     * @param width
     * @param height
     */

    private void initView(int width, int height) {
        circle_1_paint = new Paint();
        circle_1_paint.setStyle(Paint.Style.STROKE);
        circle_1_paint.setColor(Color.WHITE);
        circle_1_paint.setAntiAlias(true);

        circle_2_paint = new Paint();
        circle_2_paint.setStyle(Paint.Style.STROKE);
        circle_2_paint.setColor(Color.BLACK);
        circle_2_paint.setAntiAlias(true);


        circle_3_paint = new Paint();
        circle_3_paint.setStyle(Paint.Style.FILL_AND_STROKE);
        circle_3_paint.setColor(Color.WHITE);
        circle_3_paint.setAntiAlias(true);
        //最外层圆  三个圆的圆心位置都是一样的  半径
        circle_x = width / 2;
        circle_y = height / 2;
        circle_1_r = Math.min(width, height) / 2;
        circle_2_r = circle_1_r - 10;
        circle_3_r = circle_2_r - 6;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initView(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //最外层圆 1
        canvas.drawCircle(circle_x, circle_y, circle_1_r, circle_1_paint);

        //中间的  黑色圆
        canvas.drawCircle(circle_x, circle_y, circle_2_r, circle_2_paint);

        //里边的  白色圆
        canvas.drawCircle(circle_x, circle_y, circle_3_r, circle_3_paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onClickListener.onClick(this,event);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick(View view, MotionEvent event);
    }

    public void setMyButtonBackGroupColor_gray(){

        circle_1_paint.setColor(getResources().getColor(R.color.lightGray));
        circle_3_paint.setColor(getResources().getColor(R.color.lightGray));
        invalidate();
    }

    public void setMyButtonBackGroupColor_while(){

        circle_1_paint.setColor(Color.WHITE);
        circle_3_paint.setColor(Color.WHITE);
        invalidate();
    }
}
