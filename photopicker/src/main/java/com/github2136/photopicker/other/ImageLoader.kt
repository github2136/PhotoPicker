package com.github2136.photopicker.other

import android.content.Context
import android.widget.ImageView

/**
 * 图片加载接口
 * Created by yb on 2018/5/16.
 */

interface ImageLoader {
    /**
     * 加载小图
     */
    fun loadThumbnail(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, path: String)

    /**
     * 加载小图，支持GIF
     */
    fun loadAnimatedGifThumbnail(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, path: String)

    /**
     * 加载大图
     */
    fun loadImage(context: Context, imageView: ImageView, path: String)

    /**
     * 加载大图，支持GIF
     */
    fun loadAnimatedGifImage(context: Context, imageView: ImageView, path: String)

    /**
     * 小图是否支持GIF
     */
    fun supportAnimatedGifThumbnail(): Boolean

    /**
     * 大图是否支持GIF
     */
    fun supportAnimatedGif(): Boolean
}
