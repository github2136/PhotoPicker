package com.github2136.photopicker.activity

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github2136.photopicker.R
import com.github2136.photopicker.other.PhotoFileUtil
import com.github2136.photopicker.other.PhotoSPUtil
import java.io.File

/**
 *      图片拍摄
 *      默认存储只外部私有图片目录下，或在application中添加name为photo_picker_path的&lt;meta&#62;，私有目录下的图片不能添加到媒体库中，选择图片时将会无法查看到
 *      ARG_FILE_PATH图片保存路径目录，不包括文件名，优先级比photo_picker_path高，可不填
 *      intent.data返回图片URI，可使用PhotoFileUtil.getFileAbsolutePath(this, intent.data)将URI转换为物理路径
 */
class CaptureActivity : AppCompatActivity() {
    private val mSpUtil by lazy { PhotoSPUtil.getInstance(this) }
    val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val photoPath: String?
        get() {
            var mPhotoPath: String? = null
            try {
                val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                val metaData = applicationInfo.metaData
                if (metaData != null) {
                    mPhotoPath = metaData.getString("photo_picker_path")
                    if (!TextUtils.isEmpty(mPhotoPath)) {
                        mPhotoPath = PhotoFileUtil.getExternalStorageRootPath() + File.separator + mPhotoPath
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            if (TextUtils.isEmpty(mPhotoPath)) {
                mPhotoPath = PhotoFileUtil.getExternalStoragePrivatePicPath(this)
            }
            return mPhotoPath
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkPermissionDenied(permissions)) {
            requestPermissions(permissions, 1)
        } else {
            initCapture()
        }
    }

    fun initCapture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val filePath: String
        val file: File
        if (getIntent().hasExtra(ARG_FILE_PATH)) {
            filePath = getIntent().getStringExtra(ARG_FILE_PATH)
            file = File(PhotoFileUtil.getExternalStorageRootPath() + File.separator + filePath, PhotoFileUtil.createFileName(".jpg"))
        } else {
            file = File(photoPath, PhotoFileUtil.createFileName(".jpg"))
        }
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        val mShootUri = insert(file)
        mSpUtil.edit()
            .putValue(KEY_FILE_URI, mShootUri.toString())
            .apply()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mShootUri)
        startActivityForResult(intent, REQUEST_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.parse(mSpUtil.getString(KEY_FILE_URI))
            mediaScanIntent.data = contentUri
            this.sendBroadcast(mediaScanIntent)
            val result = Intent()
            result.data = contentUri
            setResult(Activity.RESULT_OK, result)
        }
        finish()
    }

    /**
     * 返回图片uri
     */
    private fun insert(file: File): Uri? {
        val contentValues = ContentValues(1)
        contentValues.put(MediaStore.Images.Media.DATA, file.absolutePath)
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var allow = true
        var denied = mutableListOf<String>()
        for ((index, permission) in permissions.withIndex()) {
            //  拒绝的权限
            if (grantResults[index] == PackageManager.PERMISSION_DENIED) {
                allow = false
                denied.add(permission)
                //判断是否点击不再提示
                val showRationale = shouldShowRequestPermissionRationale(permission);
                if (!showRationale) {
                    // 用户点击不再提醒，打开设置页让用户开启权限
                    AlertDialog.Builder(this)
                        .setTitle("警告")
                        .setMessage("缺少 ${getString(packageManager.getPermissionInfo(permission, 0).labelRes)} 权限，是否打开设置修改权限？")
                        .setPositiveButton("打开设置") { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            val uri = Uri.fromParts("package", packageName, null);
                            intent.data = uri;
                            startActivity(intent);
                            finish()
                        }
                        .setNegativeButton("取消") { _, _ ->
                            finish()
                        }
                        .show()
                    break
                }
            }

            if (!allow) {
                // 用户点击了取消...
                AlertDialog.Builder(this)
                    .setTitle("警告")
                    .setMessage("缺少 ${denied.joinToString { getString(packageManager.getPermissionInfo(it, 0).labelRes) }} 权限，继续使用请重新请求权限")
                    .setPositiveButton("请求权限") { _, _ ->
                        requestPermissions(permissions, 1)
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        }
        if (allow) {
            initCapture()
        }
    }

    /**
     * 判断权限拒绝
     */
    fun checkPermissionDenied(permissions: Array<String>): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissions.firstOrNull { checkSelfPermission(it) == PackageManager.PERMISSION_DENIED } != null
        } else {
            false
        }
    }

    companion object {
        val ARG_FILE_PATH = "FILE_PATH" //图片保存目录
        private val REQUEST_CAPTURE = 706
        //文件Uri
        private val KEY_FILE_URI = "CAPTURE_FILE_URI"
    }
}
