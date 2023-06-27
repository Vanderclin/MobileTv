package com.mobiletv.app.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mobiletv.app.R;

public class MaterialEditText extends TextInputLayout {

    private TextInputEditText textInputEditText;

    public MaterialEditText(Context context) {
        super(context);
        init();
    }

    public MaterialEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        applyAttributes(attrs);
    }

    public MaterialEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        applyAttributes(attrs);
    }

    private void init() {
        setBoxBackgroundMode(BOX_BACKGROUND_OUTLINE);
        setHintEnabled(true);
        textInputEditText = new TextInputEditText(getContext());
        addView(textInputEditText);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // Realize inicializações adicionais aqui, se necessário
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Realize a limpeza de recursos aqui, se necessário
    }

    private void applyAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MaterialEditText);
        boolean isDescription = a.getBoolean(R.styleable.MaterialEditText_isDescription, false);
        int inputType = a.getInt(R.styleable.MaterialEditText_inputType, EditorInfo.TYPE_NULL);
        a.recycle();

        setIsDescription(isDescription);
        setInputType(inputType);
        adjustMaxHeight(isDescription);
    }

    public void setIsDescription(boolean isDescription) {
        adjustMaxHeight(isDescription);
    }

    private void adjustMaxHeight(boolean isDescription) {
        int maxHeight = isDescription ? dpToPx(120) : Integer.MAX_VALUE;
        textInputEditText.setMaxHeight(maxHeight);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public void setInputType(int inputType) {
        textInputEditText.setInputType(inputType);
    }

    public TextInputEditText getEditText() {
        return textInputEditText;
    }

    public String getText() {
        return String.valueOf(textInputEditText.getText());
    }

    public void clear() {
        Editable text = textInputEditText.getText();
        if (text != null) {
            text.clear();
        }
    }
}
