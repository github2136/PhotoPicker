package com.github2136.picturepicker.other;

import android.content.Context;
import android.widget.ImageView;

/**
 * 图片加载接口
 * Created by yb on 2018/5/16.
 */

public interface ImageLoader {
    /**
     * 加载小图
     */
    void loadThumbnail(Context context, int resizeX, int resizeY, ImageView imageView, String path);

    /**
     * 加载小图，支持GIF
     */
    void loadAnimatedGifThumbnail(Context context, int resizeX, int resizeY, ImageView imageView, String path);

    /**
     * 加载大图
     */
    void loadImage(Context context, ImageView imageView, String path);

    /**
     * 加载大图，支持GIF
     */
    void loadAnimatedGifImage(Context context, ImageView imageView, String path);

    /**
     * 小图是否支持GIF
     */
    boolean supportAnimatedGifThumbnail();

    /**
     * 大图是否支持GIF
     */
    boolean supportAnimatedGif();
}
