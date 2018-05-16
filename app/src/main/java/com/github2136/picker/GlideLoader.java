package com.github2136.picker;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github2136.picturepicker.other.ImageLoader;

/**
 * Created by yb on 2018/5/16.
 */

public class GlideLoader implements ImageLoader {

    @Override
    public void loadThumbnail(Context context, int resizeX, int resizeY, ImageView imageView, String path) {
        GlideApp.with(context)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(resizeX, resizeY)
                .placeholder(R.drawable.img_picker_place)
                .error(R.drawable.img_picker_fail)
                .centerCrop()
                .dontAnimate()
                .into(imageView);
    }

    @Override
    public void loadAnimatedGifThumbnail(Context context, int resizeX, int resizeY, ImageView imageView, String path) {
        GlideApp.with(context)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(resizeX, resizeY)
                .placeholder(R.drawable.img_picker_place)
                .error(R.drawable.img_picker_fail)
                .centerCrop()
                .into(imageView);
    }

    @Override
    public void loadImage(Context context, ImageView imageView, String path) {
        GlideApp.with(context)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.ALL.ALL)
                .placeholder(R.drawable.img_picker_place)
                .error(R.drawable.img_picker_fail)
                .dontAnimate()
                .into(imageView);
    }

    @Override
    public void loadAnimatedGifImage(Context context, ImageView imageView, String path) {
        GlideApp.with(context)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.ALL.ALL)
                .placeholder(R.drawable.img_picker_place)
                .error(R.drawable.img_picker_fail)
                .into(imageView);
    }

    @Override
    public boolean supportAnimatedGifThumbnail() {
        return true;
    }

    @Override
    public boolean supportAnimatedGif() {
        return true;
    }
}
