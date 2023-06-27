package com.mobiletv.app.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

public class CardImageView extends MaterialCardView {
    private ImageView imageView;

    private static final int ORIGINAL_WIDTH = 259;
    private static final int ORIGINAL_HEIGHT = 385;

    public CardImageView(Context context) {
        super(context);
        init(context);
    }

    public CardImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CardImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        imageView = new AppCompatImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(params);
        this.addView(imageView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (width * (float) ORIGINAL_HEIGHT / ORIGINAL_WIDTH);
        int newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
    }

    public void setImageGlide(String imageUrl) {
        Glide.with(getContext()).load(imageUrl).centerCrop().into(imageView);
    }
}
