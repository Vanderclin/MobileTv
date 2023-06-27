package com.mobiletv.app.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class WhiteNoiseView extends View {

    private Random random;
    private Paint paint;
    private Bitmap staticBitmap;
    private int[] noisePixels;
    private boolean isAnimating = false;

    public WhiteNoiseView(Context context) {
        super(context);
        init();
    }

    public WhiteNoiseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WhiteNoiseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        random = new Random();
        paint = new Paint();
        startAnimation();
    }

    public void startAnimation() {
        if (!isAnimating) {
            isAnimating = true;
            invalidate();
        }
    }

    public void stopAnimation() {
        isAnimating = false;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = 620;
        setMeasuredDimension(width, height);
        if (staticBitmap == null || staticBitmap.getWidth() != width || staticBitmap.getHeight() != height) {
            staticBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            noisePixels = new int[width * height];
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isAnimating) {
            int width = getWidth();
            int height = getHeight();

            for (int i = 0; i < noisePixels.length; i++) {
                int noise = random.nextInt(256);
                int color = Color.rgb(noise, noise, noise);
                noisePixels[i] = color;
            }

            staticBitmap.setPixels(noisePixels, 0, width, 0, 0, width, height);
            canvas.drawBitmap(staticBitmap, 0, 0, paint);
            postInvalidateDelayed(8);
        }
    }
}
