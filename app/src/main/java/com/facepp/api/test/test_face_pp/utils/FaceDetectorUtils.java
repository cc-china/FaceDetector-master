package com.facepp.api.test.test_face_pp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.FaceDetector;

import java.io.ByteArrayOutputStream;

/**
 * Created by Administrator on 2019\1\15 0015.
 */

public class FaceDetectorUtils {
    private static FaceDetector.Face[] faces = new FaceDetector.Face[10];

    public static int autoCheckFace(final ByteArrayOutputStream stream, int orientionOfCamera) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap1 = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.toByteArray().length,
                options);
        int width = bitmap1.getWidth();
        int height = bitmap1.getHeight();
        FaceDetector detector = null;
        detector = new FaceDetector(height, width, 1);
        Matrix matrix = new Matrix();
        Bitmap bitmap2 = null;
        switch (orientionOfCamera) {
            case 0:
                detector = new FaceDetector(width, height, 10);
                matrix.postRotate(0.0f, width / 2, height / 2);
                // 以指定的宽度和高度创建一张可变的bitmap（图片格式必须是RGB_565，不然检测不到人脸）
                bitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                break;
            case 90:
                detector = new FaceDetector(height, width, 1);
                matrix.postRotate(-270.0f, height / 2, width / 2);
                bitmap2 = Bitmap.createBitmap(height, width, Bitmap.Config.RGB_565);
                break;
            case 180:
                detector = new FaceDetector(width, height, 1);
                matrix.postRotate(-180.0f, width / 2, height / 2);
                bitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                break;
            case 270:
                detector = new FaceDetector(height, width, 1);
                matrix.postRotate(-90.0f, height / 2, width / 2);
                bitmap2 = Bitmap.createBitmap(height, width, Bitmap.Config.RGB_565);
                break;
        }
        Paint paint = new Paint();
        paint.setDither(true);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap2);
        canvas.setMatrix(matrix);
        // 将bitmap1画到bitmap2上（这里的偏移参数根据实际情况可能要修改）
        canvas.drawBitmap(bitmap1, 0, 0, paint);
        bitmap1.recycle();
        int faceNumber = detector.findFaces(bitmap2, faces);
        return faceNumber;
    }
}
