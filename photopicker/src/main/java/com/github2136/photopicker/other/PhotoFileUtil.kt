package com.github2136.photopicker.other

import android.annotation.TargetApi
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object PhotoFileUtil {
    /**
     * 外部存储根目录
     */
    @JvmStatic
    fun getExternalStorageRootPath(): String {
        return Environment.getExternalStorageDirectory().absoluteFile.toString()
    }

    /**
     * 外部存储私有图片目录
     */
    @JvmStatic
    fun getExternalStoragePrivatePicPath(context: Context): String {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.absolutePath
    }
    ///////////////////////////////////////////////////////////////////////////
    // 创建文件名称、创建文件
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 创建文件名称
     *
     * @param suffix 后缀 .jpg .png
     */
    @JvmStatic
    fun createFileName(suffix: String): String {
        return createFileName(null, suffix)
    }

    /**
     * 创建文件名称
     *
     * @param prefix 前缀
     * @param suffix 后缀 .jpg .png
     */
    @JvmStatic
    fun createFileName(prefix: String?, suffix: String): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(Date())
        return (if (prefix == null) "" else prefix + "_") + timeStamp + suffix
    }

    ///////////////////////////////////////////////////////////////////////////
    // 获得文件绝对路径
    ///////////////////////////////////////////////////////////////////////////
    /**
     * 根据Uri获取文件绝对路径，解决Android4.4以上版本Uri转换
     */
    @TargetApi(19)
    @JvmStatic
    fun getFileAbsolutePath(context: Context, imageUri: Uri): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract
                        .isDocumentUri(context, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                val docId = DocumentsContract.getDocumentId(imageUri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + File.separator + split[1]
                }
            } else if (isDownloadsDocument(imageUri)) {
                val id = DocumentsContract.getDocumentId(imageUri)
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long
                        .valueOf(id))
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(imageUri)) {
                val docId = DocumentsContract.getDocumentId(imageUri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = MediaStore.Images.Media._ID + "=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(imageUri.scheme, ignoreCase = true)) {
            return if (isGooglePhotosUri(imageUri)) imageUri.lastPathSegment else getDataColumn(context, imageUri, null, null)
        } else if ("file".equals(imageUri.scheme, ignoreCase = true)) {
            return imageUri.path
        }
        return null
    }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = MediaStore.Images.Media.DATA
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
    ///////////////////////////////////////////////////////////////////////////
    // 文件操作
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 从本地或网络地址获取文件后缀如 jpg  txt<br></br>
     * MimeTypeMap.getFileExtensionFromUrl(urlStr);
     *
     * @param str
     * @return
     */
    @JvmStatic
    fun getSuffix(str: String): String {
        var str = str
        val fragment = str.lastIndexOf('#')
        if (fragment > 0) {
            str = str.substring(0, fragment)
        }
        val query = str.lastIndexOf('?')
        if (query > 0) {
            str = str.substring(0, query)
        }
        val filenamePos = str.lastIndexOf(File.separatorChar)
        val filename = if (0 <= filenamePos) str.substring(filenamePos + 1) else str
        if (!filename.isEmpty()) {
            val dotPos = filename.lastIndexOf('.')
            if (0 <= dotPos) {
                return filename.substring(dotPos + 1)
            }
        }
        return ""
    }

}