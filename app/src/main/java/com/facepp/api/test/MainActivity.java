package com.facepp.api.test;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.facepp.api.test.test_face_pp.utils.BitmapUtil;
import com.megvii.facepp.api.FacePPApi;
import com.megvii.facepp.api.IFacePPCallBack;
import com.megvii.facepp.api.bean.CompareResponse;
import com.megvii.facepp.api.bean.HumanSegmentResponse;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.response)
    TextView txtResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

//        FacePPApi faceppApi = new FacePPApi("syAe75QXfQHDt9YcmC8BJAJD0mX5nwqJ", "Q23rhNN6TsA8A6TcTOHkBsu-a7hBOUEB");
        // params.put("return_attribute", "age,gender");
        FacePPApi faceppApi = new FacePPApi("oTaH7pgtZIBn1F2w9jmOe_LUxI2FCZEf",
                "dfEJD7_eQf-P613oqS78X_zbElita4Ru");
        Map<String, String> params = new HashMap<>();

        byte[] data1 = BitmapUtil.File2byte(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/test/base.jpg");
        byte[] data2 = BitmapUtil.File2byte(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/test/1.jpg");
        faceppApi.compare(params, data1,data2, new IFacePPCallBack<CompareResponse>() {
            @Override
            public void onSuccess(CompareResponse compareResponse) {
                refreshView(compareResponse.toString());
            }

            @Override
            public void onFailed(String error) {
                refreshView(error);
            }
        });
    }

    private void refreshView(final String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtResponse.setText(response);
            }
        });
    }
}
