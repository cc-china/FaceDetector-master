package com.facepp.api.test.test_face_pp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facepp.api.test.R;
import com.facepp.api.test.test_face_pp.utils.BitmapUtil;
import com.facepp.api.test.test_face_pp.utils.CameraSetting;
import com.facepp.api.test.test_face_pp.utils.FaceApiUtils;
import com.facepp.api.test.test_face_pp.utils.Utils;
import com.facepp.api.test.test_face_pp.view.MyButton;
import com.megvii.facepp.api.bean.DetectResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2018\12\21 0021.
 */

public class SufaceViewActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private Camera camera;
    private ImageView iv_bitmap;
    private TextView tv_info;
    private Bitmap bitmap;
    private File pathFile;
    private boolean isContrastFlag;
    private FaceApiUtils faceUtils;
    private Bitmap detectorBitmap;
    private int orientionOfCamera;
    private Dialog dialog;
    private MyButton my_btn_camera;
    private SurfaceView sfv_camera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sufaceview);
        init();
    }

    private void init() {
        dialog = Utils.showProgressDialog(SufaceViewActivity.this, "正在上传", true);
        Utils.setLight(SufaceViewActivity.this, 140);
        faceUtils = new FaceApiUtils(SufaceViewActivity.this);
        //在使用 BitmapUtil.path ---test文件夹之前   你需要判断它是否存在，否的话创建文件夹
        judgeFolderIsExists();
        pathFile = new File(BitmapUtil.path);
        bindID();
        permission();
    }

    private void judgeFolderIsExists() {
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/test/");
        if (!file.exists()) {
            file.mkdir();
        }
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
        sfv_camera = findViewById(R.id.sfv_camera);
        iv_bitmap = findViewById(R.id.iv_bitmap);
        tv_info = findViewById(R.id.tv_info);
        my_btn_camera = findViewById(R.id.My_btn_camera);

        if (pathFile.exists()) {
            isContrastFlag = true;
        } else {
            tv_info.setText("自拍上传,人脸头像");
            isContrastFlag = false;

        }
        SurfaceHolder surfaceHolder = sfv_camera.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 設置顯示器類型，setType必须设置


        my_btn_camera.setOnClickListener(new MyButton.OnClickListener() {
            @Override
            public void onClick(View view, MotionEvent event) {
                try {
                    //点击后切换Mybutton背景
                    my_btn_camera.setMyButtonBackGroupColor_gray();
                    mLock.lock();
                    if (camera != null) {
                        camera.setPreviewCallback(SufaceViewActivity.this);
                    }
                } catch (Exception e) {
                    e.getStackTrace();
                } finally {
                    mLock.unlock();
                }

            }
        });

    }

    ReentrantLock mLock = new ReentrantLock();

    /**
     * 开启相机预览
     */
    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        new Thread() {
            @Override
            public void run() {
                try {
                    if (camera == null) {
                        camera = Camera.open(1);
                    }
                    camera.setPreviewDisplay(holder);
                    orientionOfCamera = CameraSetting.setCameraDisplayOrientation(
                            SufaceViewActivity.this,
                            1, camera);

                    Camera.Parameters parameters = camera.getParameters();
                    //设置相机预览照片帧数
                    parameters.setPreviewFpsRange(7000, 10000);
                    //设置图片的质量..
                    parameters.set("jpeg-quality", 90);
//                    camera.setParameters(parameters);//相机中的这个属性设置很苛刻，参数不对会报错，请谨慎使用
                    camera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

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
    private volatile boolean isGetObtainImager = true;
    FaceDetector.Face[] faces = new FaceDetector.Face[10];

    private void autoCheckFace(final ByteArrayOutputStream stream, Bitmap bmp) {
        Message message = Message.obtain();
        message.obj = "正在获取人脸";
        mHandler.sendMessage(message);

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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    detectMyBitmap(stream.toByteArray());
                }
            });
        } else if (faceNumber == 0) {
            compareCount++;
            Message message1 = Message.obtain();
            message1.obj = "未检测到人脸，请重新识别";
            mHandler.sendMessage(message1);

        }
        bitmap1.recycle();
    }


    /**
     * data ：0--------基础人脸照片存储成功
     * data ：1--------两张照片比对成功
     * data ：2--------两次比对人脸照片都失败，跳过人脸判定，直接打卡成功
     */
    public void detectMyBitmap(final byte[] data) { //*****旋转一下
        Message message3 = Message.obtain();
        message3.obj = "正在检测人脸";
        mHandler.sendMessage(message3);
        if (dialog != null) {
            dialog.show();
        }
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();//停掉摄像头的预览
        }
        faceUtils.detectResult(new FaceApiUtils.DetectResult() {
            @Override
            public void detectResult(DetectResponse detectResponse) {
                if (detectResponse == null
                        || detectResponse.getFaces() == null
                        || detectResponse.getFaces().size() == 0) {
                    Message message4 = Message.obtain();
                    message4.obj = "未检测到人脸，请重新识别";
                    mHandler.sendMessage(message4);
                    dialog.dismiss();
                    isGetObtainImager = true;
                    compareCount++;
//                    if (isContrastFlag) {
                    //开启摄像头的预览
                    Message message2 = Message.obtain();
                    message2.what = 2;
                    mHandler.sendMessage(message2);
//                    }
                    return;
                }
                float threshold = detectResponse.getFaces().get(0).getAttributes().getFacequality().getThreshold();

                if (threshold > 50) {
                    Message message5 = Message.obtain();
                    message5.obj = "检测到人脸";
                    mHandler.sendMessage(message5);
                    if (isContrastFlag) {
                        //有人脸图片，人脸比对
                        saveIamgerFile2location(data);
                    } else {
                        //无人脸图片，存储人脸图片,先上传到服务器，后存储到本地
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                saveFacePhoto(data, 0);
                            }
                        });
                    }
                } else {
                    Message message6 = Message.obtain();
                    message6.obj = "未检测到人脸，请重新识别";
                    mHandler.sendMessage(message6);
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    isGetObtainImager = true;
                    compareCount++;
//                    if (isContrastFlag) {
                    //开启摄像头的预览
                    Message message2 = Message.obtain();
                    message2.what = 2;
                    mHandler.sendMessage(message2);
//                    }
                    return;
                }
            }
        });

        faceUtils.detectApi(data);
    }

    /**
     * 1、当人脸比对成功，返回上个Activity成功打卡，考勤打卡图片启用后台Service去上传
     * <p>
     * 存储图片,先上传到服务器，后存储到本地
     */
    private void saveFacePhoto(final byte[] data, final int type) {

        final String IMAGE_TIME = System.currentTimeMillis() + ".jpg";
        final String path = BitmapUtil.testPath + IMAGE_TIME;
        //临时存放照片
//        BitmapUtil.byte2File(data, path);
        if (type == 0) {
            //正式保存照片
            BitmapUtil.byte2File(data, BitmapUtil.path);
        }
        /**
         * 启用服务上传照片  IMAGE_TIME, path
         * */
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

//                Intent intent = new Intent(SufaceViewActivity.this, LoadImgService.class);
//                intent.putExtra("type", type);
//                intent.putExtra("IMAGE_TIME", IMAGE_TIME);
//                intent.putExtra("path", path);
//                startService(intent);

                Intent backIntent = new Intent();
                //代表存储基础人脸照片
                backIntent.putExtra("type", type);
                backIntent.putExtra("imgPath", path);
                backIntent.putExtra("imgName", IMAGE_TIME);
                SufaceViewActivity.this.setResult(RESULT_OK, backIntent);
                SufaceViewActivity.this.finish();
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        //返回上一级
        recycleCamera();
        if (type == 0) {
            BitmapUtil.byte2File(data, path);
            Message message10 = Message.obtain();
            message10.what = 10;
            message10.obj = "人脸信息采集完毕,请开始打卡";
            mHandler.sendMessage(message10);
        }
    }

    volatile int compareCount = 0;

    private void saveIamgerFile2location(final byte[] path1) {
        //已有人脸图片，两张图片进行比对
        Message message7 = Message.obtain();
        message7.obj = "正在比对人脸信息";
        mHandler.sendMessage(message7);
        if (dialog != null) {
            dialog.show();
        }
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
                            Message message8 = Message.obtain();
                            message8.obj = "人脸识别完成，即将退出";
                            mHandler.sendMessage(message8);
                            saveFacePhoto(path1, 1);
                        } else {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            if (camera != null) {
                                camera.setPreviewCallback(SufaceViewActivity.this);
                                camera.startPreview();//开启摄像头的预览
                            }
                            isGetObtainImager = true;
                            compareCount++;
                            Message message9 = Message.obtain();
                            message9.obj = "打卡失败" + compareCount + "次";
                            mHandler.sendMessage(message9);
                        }
                    }
                });
            }
        });
    }

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
                if (compareCount >= 2 && isContrastFlag) {
                    Message message11 = Message.obtain();
                    message11.what = 11;
                    message11.obj = "自拍打卡,人事审核";
                    mHandler.sendMessage(message11);
                    my_btn_camera.setOnClickListener(new MyButton.OnClickListener() {
                        @Override
                        public void onClick(View view, MotionEvent event) {
                            saveFacePhoto(stream.toByteArray(), 2);
                        }
                    });
                } else {
                    //android源码中检测人脸的类
                    autoCheckFace(stream, bmp);
                }
                stream.close();
            }
        } catch (Exception ex) {
            Log.e("Sys", "Error:" + ex.getMessage());
        }
    }

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String text = (String) msg.obj;
            if (msg.what == 10) {
                Toast.makeText(SufaceViewActivity.this, text, Toast.LENGTH_SHORT).show();
            } else if (msg.what == 2) {
                if (camera != null) {
                    camera.setPreviewCallback(SufaceViewActivity.this);
                    camera.startPreview();//开启摄像头的预览
                }
            } else if (msg.what == 11) {
                tv_info.setText(text + "");
                tv_info.setTextColor(Color.RED);
                my_btn_camera.setMyButtonBackGroupColor_while();
            } else {
                tv_info.setText(text + "");
            }
            return false;
        }
    });
}
