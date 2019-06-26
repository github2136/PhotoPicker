package com.github2136.photopicker.activity

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github2136.photopicker.R
import com.github2136.photopicker.other.PhotoCommonUtil
import com.github2136.photopicker.other.PhotoFileUtil
import com.github2136.photopicker.other.PhotoSPUtil
import java.io.File

/**
 *      图片裁剪
 *      ARG_CROP_IMG 需要裁剪的图片URI
 *      ARG_ASPECT_X/ARG_ASPECT_Y裁剪框比例
 *      ARG_OUTPUT_X/ARG_OUTPUT_Y图片输出尺寸
 *      默认存储只外部私有图片目录下，或在application中添加name为photo_picker_path的&lt;meta&#62;，私有目录下的图片不能添加到媒体库中，选择图片时将会无法查看到
 *      OUTPUT_IMG图片保存路径目录，不包括文件名，优先级比photo_picker_path高，可不填
 *      intent.data返回图片URI，可使用PhotoFileUtil.getFileAbsolutePath(this, intent.data)将URI转换为物理路径
 */
class CropActivity : AppCompatActivity() {
    private val mSpUtil: PhotoSPUtil by lazy { PhotoSPUtil.getInstance(this) }

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
        setContentView(R.layout.activity_crop)
        if (!(intent.hasExtra(ARG_CROP_IMG) &&
                intent.hasExtra(ARG_ASPECT_X) &&
                intent.hasExtra(ARG_ASPECT_Y) &&
                intent.hasExtra(ARG_OUTPUT_X) &&
                intent.hasExtra(ARG_OUTPUT_Y))) {
            Toast.makeText(this, "缺少参数", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            val img = intent.getParcelableExtra<Uri>(ARG_CROP_IMG)
            val aspX = intent.getIntExtra(ARG_ASPECT_X, 0)
            val aspY = intent.getIntExtra(ARG_ASPECT_Y, 0)
            val outX = intent.getIntExtra(ARG_OUTPUT_X, 0)
            val outY = intent.getIntExtra(ARG_OUTPUT_Y, 0)
            val outImg: File
            if (intent.hasExtra(ARG_FILE_PATH)) {
                val out = intent.getStringExtra(ARG_FILE_PATH)
                outImg = File(PhotoFileUtil.getExternalStorageRootPath() + File.separator + out, PhotoFileUtil.createFileName(".jpg"))
            } else {
                outImg = File(photoPath, PhotoFileUtil.createFileName(".jpg"))
            }
            if (!outImg.parentFile.exists()) {
                outImg.parentFile.mkdirs()
            }
            val mOutUri = insert(outImg)

            mSpUtil.edit()
                .putValue(KEY_FILE_URI, mOutUri.toString())
                .apply()
            val intent = Intent("com.android.camera.action.CROP")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            intent.setDataAndType(img, "image/*")
            intent.putExtra("crop", "true")
            intent.putExtra("aspectX", aspX)
            intent.putExtra("aspectY", aspY)
            intent.putExtra("outputX", outX)
            intent.putExtra("outputY", outY)
            intent.putExtra("scale", true)// 如果选择的图小于裁剪大小则进行放大
            intent.putExtra("scaleUpIfNeeded", true)// 如果选择的图小于裁剪大小则进行放大
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutUri)
            intent.putExtra("return-data", false)
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
            intent.putExtra("noFaceDetection", true) // no face detection
            if (PhotoCommonUtil.isIntentExisting(this, intent)) {
                startActivityForResult(intent, REQUEST_CROP)
            } else {
                Toast.makeText(this, "该机器无法裁剪", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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

    companion object {
        val ARG_CROP_IMG = "CROP_IMG"//需要裁剪地图片URI
        val ARG_ASPECT_X = "ASPECT_X"
        val ARG_ASPECT_Y = "ASPECT_Y"
        val ARG_OUTPUT_X = "OUTPUT_X"
        val ARG_OUTPUT_Y = "OUTPUT_Y"
        val ARG_FILE_PATH = "FILE_PATH"//图片保存目录
        private val REQUEST_CROP = 742
        private val KEY_FILE_URI = "CROP_FILE_URI"
    }
}
