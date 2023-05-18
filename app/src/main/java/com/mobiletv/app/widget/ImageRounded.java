package com.mobiletv.app.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class ImageRounded extends AppCompatImageView {

    private static final float DEFAULT_CORNER_RADIUS = 15f;
    private float cornerRadius;

    public ImageRounded(Context context) {
        super(context);
        init(context, null);
    }

    public ImageRounded(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ImageRounded(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        cornerRadius = DEFAULT_CORNER_RADIUS;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            super.onDraw(canvas);
            return;
        }

        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable) {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas tempCanvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, tempCanvas.getWidth(), tempCanvas.getHeight());
            drawable.draw(tempCanvas);
        }

        if (bitmap == null) {
            super.onDraw(canvas);
            return;
        }

        // Calcular as dimensões da imagem centralizada dentro do ImageView
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        float scale;
        float dx = 0;
        float dy = 0;

        if (bitmapWidth * viewHeight > viewWidth * bitmapHeight) {
            scale = (float) viewHeight / (float) bitmapHeight;
            dx = (viewWidth - bitmapWidth * scale) * 0.5f;
        } else {
            scale = (float) viewWidth / (float) bitmapWidth;
            dy = (viewHeight - bitmapHeight * scale) * 0.5f;
        }

        // Aplicar transformações de escala e posicionamento à matriz de desenho da imagem
        canvas.save();
        canvas.scale(scale, scale);
        canvas.translate(dx / scale, dy / scale);

        // Criar uma máscara com cantos arredondados
        Path clipPath = new Path();
        RectF rect = new RectF(0, 0, bitmapWidth, bitmapHeight);
        float[] radii = {cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius};
        clipPath.addRoundRect(rect, radii, Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
        Paint borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(5f); // Define a largura da borda
        borderPaint.setColor(Color.WHITE); // Define a cor da borda
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint);

        canvas.restore();
    }

    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        invalidate();
    }
}
