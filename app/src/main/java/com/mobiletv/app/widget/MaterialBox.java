package com.mobiletv.app.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.material.checkbox.MaterialCheckBox;

public class MaterialBox extends MaterialCheckBox {

    private CheckedChangeListener mListener;

    public MaterialBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaterialBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MaterialBox(Context context) {
        super(context);
    }

    public void addListener(CheckedChangeListener changeListener) {
        mListener = changeListener;
        setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked && mListener != null) {
                mListener.onConfirmCheckEnabled();
            }
        });
    }

    public interface CheckedChangeListener {
        void onConfirmCheckEnabled();
    }
}
