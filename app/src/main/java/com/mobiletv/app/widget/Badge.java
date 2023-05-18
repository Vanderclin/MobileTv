package com.mobiletv.app.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.mobiletv.app.R;

public class Badge extends LinearLayoutCompat {

    private LinearLayoutCompat BadgeLayout;
    private AppCompatTextView BadgeText;
    private static final int BADGE_DEFAULT_BACKGROUND_COLOR = 0x50000000;
    private static final int BADGE_DEFAULT_STROKE_COLOR = 0xFFF8F9FA;
    private static final int BADGE_DEFAULT_STROKE_WIDTH = 2;
    private static final int BADGE_DEFAULT_CORNER_RADIUS = 50;
//    private static final int BADGE_DEFAULT_LAYOUT_MARGIN_BOTTOM = 0;
//    private static final int BADGE_DEFAULT_LAYOUT_MARGIN_RIGHT = 10;
//    private static final int BADGE_DEFAULT_LAYOUT_MARGIN_LEFT = 10;
//    private static final int BADGE_DEFAULT_LAYOUT_MARGIN_TOP = 0;

    private static final int BADGE_DEFAULT_TEXT_COLOR = 0xFFF8F9FA;

    public Badge(Context context) {
        super(context);
        init(context, null);
    }

    public Badge(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Badge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.badge, this);
        BadgeLayout = findViewById(R.id.badge_layout);
        BadgeText = findViewById(R.id.badge_text);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Badge);
            GradientDrawable background = new GradientDrawable();

            int card_background = a.getColor(R.styleable.Badge_badge_background, BADGE_DEFAULT_BACKGROUND_COLOR);
            int card_radius = a.getDimensionPixelSize(R.styleable.Badge_badge_corner_radius, BADGE_DEFAULT_CORNER_RADIUS);
            int card_stroke_color = a.getColor(R.styleable.Badge_badge_stroke_color, BADGE_DEFAULT_STROKE_COLOR);
            int card_stroke_width = a.getDimensionPixelSize(R.styleable.Badge_badge_stroke_width, BADGE_DEFAULT_STROKE_WIDTH);

            background.setColor(card_background);
            background.setCornerRadius(card_radius);
            background.setStroke(card_stroke_width, card_stroke_color);
            BadgeLayout.setBackground(background);
            // AppCompatTextView Attribute Text
            String badge_text = a.getString(R.styleable.Badge_badge_text);
            if (badge_text != null) {
                BadgeText.setText(badge_text);
            }
            // AppCompatTextView Attribute Text Color
            int badge_text_color = a.getColor(R.styleable.Badge_badge_text_color, 0);
            if (badge_text_color != 0) {
                BadgeText.setTextColor(badge_text_color);
            } else {
                BadgeText.setTextColor(BADGE_DEFAULT_TEXT_COLOR);
            }
            // AppCompatTextView Attribute Text Color Hint
            int badge_text_color_hint = a.getColor(R.styleable.Badge_badge_text_color_hint, 0);
            if (badge_text_color_hint != 0) {
                BadgeText.setHintTextColor(badge_text_color_hint);
            } else {
                BadgeText.setHintTextColor(0);
            }

            // AppCompatTextView Attribute Text Type Face
            boolean badge_text_bold = a.getBoolean(R.styleable.Badge_badge_text_bold, false);
            if (badge_text_bold) {
                BadgeText.setTypeface(BadgeText.getTypeface(), Typeface.BOLD);
            } else {
                BadgeText.setTypeface(BadgeText.getTypeface(), Typeface.NORMAL);
            }

            a.recycle();
        }
    }

    public void setText(String text) {
        if (text != null) {
            BadgeText.setText(text);
        }
    }

}


