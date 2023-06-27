package com.mobiletv.app.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.mobiletv.app.R;

@SuppressLint("DrawAllocation")
public class Cover extends AppCompatImageView {
    private int width;
    private int height;
    // private float cornerRadius;

    private int originalWidth = 259;
    private int originalHeight = 385;

    public Cover(Context context) {
        super(context);
        init();
    }

    public Cover(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Cover(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        height = originalHeight;
        setAdjustViewBounds(true);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    public void setDimension(int width, int height) {
        originalWidth = width;
        originalHeight = height;
        this.width = (int) (originalWidth * 1.0);
        this.height = (int) (originalHeight * 1.0);
        requestLayout();
    }

    public void setImageGlide(Context context, String url) {
        Glide.with(context).load(url).placeholder(R.drawable.ic_launcher_background).into(this);
    }
}
