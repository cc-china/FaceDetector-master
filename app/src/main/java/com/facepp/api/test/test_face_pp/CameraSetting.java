package com.facepp.api.test.test_face_pp;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.view.Surface;

/**
 * Created by Administrator on 2018\12\25 0025.
 */

public class CameraSetting {
    /**
     * 设置相机的显示方向（这里必须这么设置，不然检测不到人脸）
     *
     * @param cameraId 相机ID(0是后置摄像头，1是前置摄像头）
     * @param camera   相机对象
     */
    public static int setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 0;
                break;
            case Surface.ROTATION_90:
                degree = 90;
                break;
            case Surface.ROTATION_180:
                degree = 180;
                break;
            case Surface.ROTATION_270:
                degree = 270;
                break;
        }

        int orientionOfCamera = info.orientation;
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degree) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degree + 360) % 360;
        }
        camera.setDisplayOrientation(result);
        return orientionOfCamera;
    }
}
