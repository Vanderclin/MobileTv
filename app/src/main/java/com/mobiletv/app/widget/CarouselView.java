package com.mobiletv.app.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

public class CarouselView extends ViewPager {
    public static final int TRANSFORMER_ZOOM = 0;
    public static final int TRANSFORMER_SCROLL = 1;
    public static final int TRANSFORMER_OVERLAP = 2;

    private int pageTransformerType = TRANSFORMER_SCROLL;

    public CarouselView(Context context) {
        super(context);
        init();
    }

    public CarouselView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setPageTransformer(true, createPageTransformer());
    }

    public void setPageTransformerType(int type) {
        pageTransformerType = type;
        setPageTransformer(true, createPageTransformer());
    }

    private ViewPager.PageTransformer createPageTransformer() {
        switch (pageTransformerType) {
            case TRANSFORMER_SCROLL:
                return new ScrollingTransformer();
            case TRANSFORMER_ZOOM:
                return new ZoomingTransformer();
            case TRANSFORMER_OVERLAP:
                return new OverlappingTransformer();
            default:
                return new ScrollingTransformer();
        }
    }

    private static class ScrollingTransformer implements ViewPager.PageTransformer {
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

    private static class ZoomingTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // For pages off-screen to the left
                view.setAlpha(0f);

            } else if (position <= 1) { // [-1,1]
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
            } else { // For pages off-screen to the right
                view.setAlpha(0f);
            }
        }
    }

    private static class OverlappingTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.8f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View page, float position) {
            int pageWidth = page.getWidth();
            int pageHeight = page.getHeight();

            if (position < -1) { // For pages off-screen to the left
                page.setAlpha(0f);

            } else if (position <= 1) { // [-1,1]
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;

                // Apply overlapping effect by adjusting translationX
                page.setTranslationX(position < 0 ? -horzMargin : horzMargin);

                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);

                // Adjust alpha based on scale (between MIN_ALPHA and 1)
                float alphaFactor = (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE);
                float alpha = MIN_ALPHA + alphaFactor * (1 - MIN_ALPHA);
                page.setAlpha(alpha);

            } else { // For pages off-screen to the right
                page.setAlpha(0f);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int childHeight = child.getMeasuredHeight();
            if (childHeight > height) {
                height = childHeight;
            }
        }
        if (height != 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
