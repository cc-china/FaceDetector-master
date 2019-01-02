package com.facepp.api.test.test_face_pp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.facepp.api.test.R;
import com.megvii.facepp.api.FacePPApi;
import com.megvii.facepp.api.IFacePPCallBack;
import com.megvii.facepp.api.bean.CompareResponse;
import com.megvii.facepp.api.bean.DetectResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018\12\21 0021.
 */

public class FaceApiUtils {

    private Result result;
    private final FacePPApi faceppApi;
    private DetectResult detectResult;

    public FaceApiUtils(Context ctx) {

//        faceppApi = new FacePPApi(ctx.getResources().getString(R.string.faceOkKey),
//                ctx.getResources().getString(R.string.faceOkSecret));
        faceppApi = new FacePPApi(ctx.getResources().getString(R.string.faceTestKey),
                ctx.getResources().getString(R.string.faceTestSecret));
    }

    public void FaceContrastTwoImager(String path1, byte[] data2) {
        byte[] data1 = BitmapUtil.File2byte(path1);
        Map<String, String> params = new HashMap<>();
        faceppApi.compare(params, data1, data2, new IFacePPCallBack<CompareResponse>() {
            @Override
            public void onSuccess(CompareResponse compareResponse) {
                result.CallBackResult(compareResponse.getConfidence() + "");
            }

            @Override
            public void onFailed(String error) {
                result.CallBackResult(error);
            }
        });

    }

    public void detectApi(byte[] data) {
        Map<String, String> params = new HashMap<>();
        params.put("return_attributes", "facequality");
        Log.e("datadetect", data.toString());
        Log.e("params", params.toString());
        faceppApi.detect(params, data, new IFacePPCallBack<DetectResponse>() {
            @Override
            public void onSuccess(DetectResponse detectResponse) {
//                float threshold = detectResponse.getFaces().get(0).getAttributes().getFacequality().getThreshold();
                detectResult.detectResult(detectResponse);
            }

            @Override
            public void onFailed(String error) {
                DetectResponse response = new DetectResponse();
                response.setError_message(error);
                detectResult.detectResult(response);
            }
        });
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public void detectResult(DetectResult detectResult) {
        this.detectResult = detectResult;
    }

    public interface Result {
        void CallBackResult(String value);
    }

    public interface DetectResult {
        void detectResult(DetectResponse value);
    }




}

