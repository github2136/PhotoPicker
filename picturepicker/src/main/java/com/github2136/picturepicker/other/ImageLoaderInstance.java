package com.github2136.picturepicker.other;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * Created by yb on 2018/5/16.
 */
public class ImageLoaderInstance {
    private ImageLoader mImageLoader;
    private volatile static ImageLoaderInstance ourInstance;

    public static ImageLoaderInstance getInstance(Context context) {
        if (ourInstance == null) {
            synchronized (ImageLoaderInstance.class) {
                if (ourInstance == null) {
                    ourInstance = new ImageLoaderInstance(context);
                }
            }
        }
        return ourInstance;
    }

    private ImageLoaderInstance(Context context) {
        String className = null;
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle data = appInfo.metaData;
            if (data != null) {
                for (String key : data.keySet()) {
                    if ("picker_image_loader".equals(appInfo.metaData.get(key))) {
                        className=key;
                    }
                }
//                className = data.getString("");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(className)) {
            try {
                mImageLoader = (ImageLoader) Class.forName(className).newInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            throw new NullPointerException("ImageLoader is null");
        }
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
