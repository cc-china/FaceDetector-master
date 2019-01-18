package com.facepp.api.test.test_face_pp.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import com.facepp.api.test.R;


public class SFProgressDialog extends Dialog {

    private static SFProgressDialog m_progrssDialog;

    private SFProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public static SFProgressDialog createProgrssDialog(Context context) {
        m_progrssDialog = new SFProgressDialog(context,
                R.style.SF_pressDialogCustom);
        m_progrssDialog.setContentView(R.layout.sf_view_custom_progress_dialog);
        m_progrssDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        return m_progrssDialog;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (null == m_progrssDialog)
            return;
        ImageView loadingImageView = (ImageView) m_progrssDialog
                .findViewById(R.id.sf_iv_progress_dialog_loading);
        AnimationDrawable animationDrawable = (AnimationDrawable) loadingImageView
                .getBackground();
        animationDrawable.start();
    }

    public SFProgressDialog setMessage(String msg) {
         TextView loadingTextView = (TextView) m_progrssDialog
                .findViewById(R.id.sf_tv_progress_dialog_loading);
        if (!TextUtils.isEmpty(msg))
            loadingTextView.setText(msg);
        else
            loadingTextView.setText("正在加载中，请稍后...");
        return m_progrssDialog;
    }

}
