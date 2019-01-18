package com.facepp.api.test.test_face_pp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;


/**
 * Created by Administrator on 2019\1\17 0017.
 */

public class LoadImgService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * 子线程中去上传图片
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final int type = intent.getIntExtra("type", -1);
            final String IMAGE_TIME = intent.getStringExtra("IMAGE_TIME");
            final String path = intent.getStringExtra("path");

//            SavePhotoUtils savePhoto = new SavePhotoUtils(this);
//            //主线程调用
//            if (type != 0) {
//                savePhoto.upLoadImageFile(IMAGE_TIME, path);
//            } else {
//                savePhoto.savePhotoFile(IMAGE_TIME, path);
//            }
        }

        return super.onStartCommand(intent, flags, startId);
    }


}
