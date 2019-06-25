package com.github2136.photopicker.activity

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.github2136.photopicker.R
import com.github2136.photopicker.other.PhotoFileUtil
import com.github2136.photopicker.other.PhotoSPUtil
import java.io.File

/**
 * 图片拍摄<br></br>
 * 默认存储只外部私有图片目录下，或在application中添加name为photo_picker_path的&lt;meta&#62;，私有目录下的图片不能添加到媒体库中，选择图片时将会无法查看到<br></br>
 * ARG_FILE_PATH图片保存路径目录，不包括文件名，优先级比photo_picker_path高，可不填<br></br>
 * ARG_RESULT返回的图片物理路径
 * ARG_RESULT_URI返回的图片URI路径
 */
class CaptureActivity : AppCompatActivity() {
    private var mSpUtil: PhotoSPUtil? = null

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
        mSpUtil = PhotoSPUtil.getInstance(this)
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
        val mShootUri = getUri(file)
        mSpUtil!!.edit()
            .putValue(KEY_FILE_PATH, file.path)
            .putValue(KEY_FILE_URI, mShootUri.toString())
            .apply()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mShootUri)
        startActivityForResult(intent, REQUEST_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val filePath = mSpUtil!!.getString(KEY_FILE_PATH)
            val fileUri = mSpUtil!!.getString(KEY_FILE_URI)
            val f = File(filePath)
            val contentUri = getUri(f)
            mediaScanIntent.data = contentUri
            this.sendBroadcast(mediaScanIntent)
            val result = Intent()
            result.putExtra(ARG_RESULT, filePath)
            result.putExtra(ARG_RESULT_URI, fileUri)
            setResult(Activity.RESULT_OK, result)
        }
        finish()
    }

    /**
     * 返回不同的图片uri
     *
     * @param file
     * @return
     */
    private fun getUri(file: File): Uri? {
        val uri: Uri?
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(file)
        } else {
            val contentValues = ContentValues(1)
            contentValues.put(MediaStore.Images.Media.DATA, file.absolutePath)
            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        }
        return uri
    }

    companion object {
        val ARG_RESULT = "RESULT"
        val ARG_RESULT_URI = "RESULT_URI"
        val ARG_FILE_PATH = "FILE_PATH"//图片保存路径

        private val REQUEST_CAPTURE = 706
        //文件路径
        private val KEY_FILE_PATH = "CAPTURE_FILE_PATH"
        private val KEY_FILE_URI = "CAPTURE_FILE_URI"
    }
}
