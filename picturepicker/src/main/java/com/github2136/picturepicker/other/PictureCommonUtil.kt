package com.github2136.picturepicker.other

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/**
 * 通用工具类
 */
object PictureCommonUtil {
    /**
     * 该Intent是否可执行
     *
     * @param context
     * @param intent
     * @return
     */
    fun isIntentExisting(context: Context, intent: Intent): Boolean {
        val packageManager = context.packageManager
        val resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo.size > 0
    }
}