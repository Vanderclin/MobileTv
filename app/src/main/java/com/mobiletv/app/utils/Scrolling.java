package com.mobiletv.app.utils;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

public class Scrolling implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.75f;

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();

        if (position < -1) { // For pages off-screen to the left
            view.setAlpha(0f);

        } else if (position <= 0) { // For pages entering the screen from the left
            view.setAlpha(1f);
            view.setTranslationX(0f);
            view.setScaleX(1f);
            view.setScaleY(1f);

        } else if (position <= 1) { // For pages leaving the screen to the right
            view.setAlpha(1f - position);
            view.setTranslationX(pageWidth * -position);
            float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        } else { // For pages off-screen to the right
            view.setAlpha(0f);
        }
    }
}
