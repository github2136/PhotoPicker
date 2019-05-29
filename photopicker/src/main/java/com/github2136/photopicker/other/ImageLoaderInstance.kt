package com.github2136.photopicker.other

import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils

/**
 * Created by yb on 2018/5/16.
 */
class ImageLoaderInstance private constructor(context: Context) {
    var imageLoader: ImageLoader? = null
        private set

    init {
        var className: String? = null
        try {
            val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val data = appInfo.metaData
            if (data != null) {
                for (key in data.keySet()) {
                    if ("picker_image_loader" == appInfo.metaData.get(key)) {
                        className = key
                    }
                }
                //                className = data.getString("");
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        if (!TextUtils.isEmpty(className)) {
            try {
                imageLoader = Class.forName(className!!).newInstance() as ImageLoader
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: InstantiationException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }

        } else {
            throw NullPointerException("ImageLoader is null")
        }
    }

    companion object {
        @Volatile
        private var ourInstance: ImageLoaderInstance? = null

        fun getInstance(context: Context): ImageLoaderInstance? {
            if (ourInstance == null) {
                synchronized(ImageLoaderInstance::class.java) {
                    if (ourInstance == null) {
                        ourInstance = ImageLoaderInstance(context)
                    }
                }
            }
            return ourInstance
        }
    }
}
