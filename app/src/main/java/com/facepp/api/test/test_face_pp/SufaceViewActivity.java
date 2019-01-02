package com.facepp.api.test.test_face_pp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facepp.api.test.R;
import com.megvii.facepp.api.FacePPApi;
import com.megvii.facepp.api.IFacePPCallBack;
import com.megvii.facepp.api.bean.DetectResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2018\12\21 0021.
 */

public class SufaceViewActivity extends Activity implements SurfaceHolder.Callback {
    private Camera camera;
    private ImageView iv_bitmap;
    private TextView tv_timer;
    private Bitmap bitmap;
    private Button btn_camera;
    private File pathFile;
    private boolean isContrastFlag;
    private FaceApiUtils faceUtils;
    private Bitmap detectorBitmap;
    private int orientionOfCamera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sufaceview);
        faceUtils = new FaceApiUtils(SufaceViewActivity.this);
        pathFile = new File(BitmapUtil.path);
        bindID();
        permission();
    }

    private void permission() {
        if (Build.VERSION.SDK_INT > 22) {
            if (ContextCompat.checkSelfPermission(SufaceViewActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //先判断有没有权限 ，没有就在这里进行权限的申请
                ActivityCompat.requestPermissions(SufaceViewActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
            } else if (ContextCompat.checkSelfPermission(SufaceViewActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SufaceViewActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        }
    }

    private void bindID() {
        SurfaceView sfv_camera = findViewById(R.id.sfv_camera);
        iv_bitmap = findViewById(R.id.iv_bitmap);
        tv_timer = findViewById(R.id.tv_timer);
        btn_camera = findViewById(R.id.btn_camera);

        if (pathFile.exists()) {
            btn_camera.setVisibility(View.GONE);
            isContrastFlag = true;
        } else {
            btn_camera.setVisibility(View.VISIBLE);
            isContrastFlag = false;
            btn_camera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                mLock.lock();
                                getPreViewImage();
                            } catch (Exception e) {
                            } finally {
                                mLock.unlock();
                            }
                        }
                    }.start();
                }
            });
        }
        SurfaceHolder surfaceHolder = sfv_camera.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 設置顯示器類型，setType必须设置

    }

    ReentrantLock mLock = new ReentrantLock();

    /**
     * 开启相机预览
     */
    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        try {
            if (camera == null) {
                camera = Camera.open(1);
            }
            camera.setPreviewDisplay(holder);
            orientionOfCamera = CameraSetting.setCameraDisplayOrientation(
                    SufaceViewActivity.this,
                    1, camera);
            camera.startPreview();

            Camera.Parameters parameters = camera.getParameters();

            //设置相机预览照片帧数
            parameters.setPreviewFpsRange(1, 2);
            //设置图片的质量..
            parameters.set("jpeg-quality", 90);
            if (isContrastFlag) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            mLock.lock();
                            getPreViewImage();
                        } catch (Exception e) {
                        } finally {
                            mLock.unlock();
                        }
                    }
                }.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //主要在这里实现Camera的释放
        recycleCamera();
    }

    private void recycleCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();//停掉摄像头的预览
            camera.release();
            camera = null;
        }
    }

    /**
     * 获取预览图片
     */
    private boolean isGetObtainImager = true;
    FaceDetector.Face[] faces = new FaceDetector.Face[10];

    private void getPreViewImage() {
        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                Camera.Size size = camera.getParameters().getPreviewSize();
                try {
                    YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                    if (image != null) {
                        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        image.compressToJpeg(new Rect(0, 0, size.width, size.height),
                                100, stream);
                        final Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                        //android源码中检测人脸的类
                        if (compareCount >= 2) {
                            btn_camera.setVisibility(View.VISIBLE);
                            btn_camera.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //上传图片，无条件验证成功
                                }
                            });
                        } else {
                            autoCheckFace(stream, bmp);
                        }
                        stream.close();
                    }
                } catch (Exception ex) {
                    Log.e("Sys", "Error:" + ex.getMessage());
                }
            }
        });
    }

    private void autoCheckFace(ByteArrayOutputStream stream, Bitmap bmp) {
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
        int faceNumber = detector.findFaces(bitmap2, faces);
        Log.e("faceNumber", faceNumber + "");
        if (faceNumber != 0 && isGetObtainImager) {
            isGetObtainImager = false;
            detectMyBitmap(bitmap2, stream.toByteArray());
        }
        bitmap1.recycle();
    }

    public void detectMyBitmap(Bitmap bmp, final byte[] data) { //*****旋转一下
        //*******显示一下
        if (bmp != null) {
            iv_bitmap.setImageBitmap(bmp);
//            bmp.recycle();
        }
        faceUtils.detectApi(data);
        faceUtils.detectResult(new FaceApiUtils.DetectResult() {
            @Override
            public void detectResult(DetectResponse detectResponse) {
                if (detectResponse.getFaces().size() == 0) {
                    isGetObtainImager = true;
                    return;
                }
                float threshold = detectResponse.getFaces().get(0).getAttributes().getFacequality().getThreshold();
                if (threshold > 50) {
                    if (isContrastFlag) {
                        //有人脸图片，人脸比对
                        saveIamgerFile2location(data);
                    } else {
                        //无人脸图片，存储人脸图片
                        BitmapUtil.byte2File(data, BitmapUtil.path);
                        if (pathFile.exists()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    recycleCamera();
                                    Toast.makeText(SufaceViewActivity.this, "存储成功,退出界面", Toast.LENGTH_SHORT).show();
                                    SufaceViewActivity.this.finish();
                                }
                            });

                        }
                    }
                }
            }
        });
    }

    volatile int compareCount = 0;

    private void saveIamgerFile2location(final byte[] path1) {
        //已有人脸图片，两张图片进行比对
        faceUtils.FaceContrastTwoImager(BitmapUtil.path, path1);
        faceUtils.setResult(new FaceApiUtils.Result() {
            @Override
            public void CallBackResult(final String value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (value.matches("-?[0-9]+\\.?[0-9]*") &&
                                Float.parseFloat(value) > 50) {
                            compareCount = 0;
                            Toast.makeText(SufaceViewActivity.this,
                                    "验证通过", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            isGetObtainImager = true;
                            compareCount++;
                            Toast.makeText(SufaceViewActivity.this,
                                    "打卡失败" + compareCount + "次", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
