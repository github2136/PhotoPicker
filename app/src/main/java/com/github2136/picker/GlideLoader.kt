package com.github2136.picker

import android.content.Context
import android.widget.ImageView

import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github2136.picturepicker.other.ImageLoader

/**
 * Created by yb on 2018/5/16.
 */
class GlideLoader : ImageLoader {

    override fun loadThumbnail(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, path: String) {
        GlideApp.with(context)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(resizeX, resizeY)
                .placeholder(R.drawable.img_picker_place)
                .error(R.drawable.img_picker_fail)
                .centerCrop()
                .dontAnimate()
                .into(imageView)
    }

    override fun loadAnimatedGifThumbnail(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, path: String) {
        GlideApp.with(context)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(resizeX, resizeY)
                .placeholder(R.drawable.img_picker_place)
                .error(R.drawable.img_picker_fail)
                .centerCrop()
                .into(imageView)
    }

    override fun loadImage(context: Context, imageView: ImageView, path: String) {
        GlideApp.with(context)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.img_picker_place)
                .error(R.drawable.img_picker_fail)
                .dontAnimate()
                .into(imageView)
    }

    override fun loadAnimatedGifImage(context: Context, imageView: ImageView, path: String) {
        GlideApp.with(context)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.img_picker_place)
                .error(R.drawable.img_picker_fail)
                .into(imageView)
    }

    override fun supportAnimatedGifThumbnail(): Boolean {
        return true
    }

    override fun supportAnimatedGif(): Boolean {
        return true
    }
}
