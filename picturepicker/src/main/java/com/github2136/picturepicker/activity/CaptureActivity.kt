package com.github2136.picturepicker.activity

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
import com.github2136.picturepicker.R
import com.github2136.picturepicker.other.PictureFileUtil
import com.github2136.picturepicker.other.PictureSPUtil
import java.io.File

/**
 * 图片拍摄<br></br>
 * 默认存储只外部私有图片目录下，或在application中添加name为picture_picker_path的&lt;meta&#62;，私有目录下的图片不能添加到媒体库中，选择图片时将会无法查看到<br></br>
 * ARG_FILE_PATH图片保存路径目录，不包括文件名，优先级比picture_picker_path高，可不填<br></br>
 * ARG_RESULT返回的图片路径
 */
class CaptureActivity : AppCompatActivity() {
    private var mSpUtil: PictureSPUtil? = null

    private val photoPath: String?
        get() {
            var mPhotoPath: String? = null
            try {
                val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                val metaData = applicationInfo.metaData
                if (metaData != null) {
                    mPhotoPath = metaData.getString("picture_picker_path")
                    if (!TextUtils.isEmpty(mPhotoPath)) {
                        mPhotoPath = PictureFileUtil.getExternalStorageRootPath() + File.separator + mPhotoPath
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            if (TextUtils.isEmpty(mPhotoPath)) {
                mPhotoPath = PictureFileUtil.getExternalStoragePrivatePicPath(this)
            }
            return mPhotoPath
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
        mSpUtil = PictureSPUtil.getInstance(this)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val filePath: String
        val file: File
        if (getIntent().hasExtra(ARG_FILE_PATH)) {
            filePath = getIntent().getStringExtra(ARG_FILE_PATH)
            file = File(PictureFileUtil.getExternalStorageRootPath() + File.separator + filePath, PictureFileUtil.createFileName(".jpg"))
        } else {
            file = File(photoPath, PictureFileUtil.createFileName(".jpg"))
        }
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        val mShootUri = getUri(file)
        mSpUtil!!.edit().putValue(KEY_FILE_NAME, file.path).apply()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mShootUri)
        startActivityForResult(intent, REQUEST_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var data = data
        if (resultCode == Activity.RESULT_OK) {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val fileName = mSpUtil!!.getString(KEY_FILE_NAME)
            val f = File(fileName)
            val contentUri = getUri(f)
            mediaScanIntent.data = contentUri
            this.sendBroadcast(mediaScanIntent)
            data = Intent()
            data.putExtra(ARG_RESULT, fileName)
            setResult(Activity.RESULT_OK, data)
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
        val ARG_FILE_PATH = "FILE_PATH"//图片保存路径

        private val REQUEST_CAPTURE = 706
        private val KEY_FILE_NAME = "CAPTURE_FILE_NAME"
    }
}
