package com.mobiletv.app.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.viewpager.widget.PagerAdapter;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.database.IgnoreExtraProperties;
import com.mobiletv.app.R;
import com.mobiletv.app.pojo.Carousel;
import com.mobiletv.app.widget.CarouselView;

import java.util.List;

@Keep
@IgnoreExtraProperties
public class AdapterCarousel extends PagerAdapter {
    protected Context mContext;
    protected List<Carousel> carouselList;
    protected LayoutInflater inflater;

    public AdapterCarousel() {
        // Need Here
    }

    public AdapterCarousel(Context mContext, List<Carousel> carouselList) {
        this.mContext = mContext;
        this.carouselList = carouselList;
        this.inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return carouselList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view.equals(o);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ((CarouselView) container).removeView((View) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = inflater.inflate(R.layout.item_carousel, container, false);
        AppCompatImageView carouselImage = view.findViewById(R.id.card_pager_image);
        AppCompatImageView carouselCover = view.findViewById(R.id.card_pager_cover);
        AppCompatTextView carouselTitle = view.findViewById(R.id.card_pager_title);
        AppCompatTextView carouselDescription = view.findViewById(R.id.card_pager_desc);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(carouselImage, "scaleX", 1.0f, 1.5f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(carouselImage, "scaleY", 1.0f, 1.5f);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.setRepeatMode(ObjectAnimator.REVERSE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatMode(ObjectAnimator.REVERSE);
        AnimatorSet scaleAnim = new AnimatorSet();
        scaleAnim.setDuration(8000);
        scaleAnim.setStartDelay(1000);
        scaleAnim.play(scaleY).with(scaleX);
        scaleAnim.start();
        String address = carouselList.get(position).getAddress();
        String description = carouselList.get(position).getDescription();
        String image = carouselList.get(position).getImage();
        String key = carouselList.get(position).getKey();
        String title = carouselList.get(position).getTitle();
        Glide.with(mContext).asBitmap().load(image).placeholder(R.drawable.icon_placeholder_carousel).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                Bitmap blurredBitmap = applyBlur(resource, 15);
                carouselImage.setImageBitmap(blurredBitmap);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                // Empty Implementation
            }
        });

        Glide.with(mContext).load(image).placeholder(R.drawable.icon_placeholder_carousel).into(carouselCover);
        carouselTitle.setText(title);
        carouselTitle.setSelected(true);
        carouselDescription.setText(description);
        carouselDescription.setSelected(true);
        container.addView(view);
        return view;
    }

    private Bitmap applyBlur(Bitmap image, int radius) {
        Bitmap blurredBitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        RenderScript rs = RenderScript.create(mContext);
        Allocation input = Allocation.createFromBitmap(rs, image);
        Allocation output = Allocation.createFromBitmap(rs, blurredBitmap);
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(radius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(blurredBitmap);
        rs.destroy();
        return blurredBitmap;
    }

}