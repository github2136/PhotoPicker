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
 * 图片裁剪，某些机型无法使用<br></br>
 * ARG_CROP_IMG 需要裁剪的图片路径<br></br>
 * ARG_ASPECT_X/ARG_ASPECT_Y裁剪框比例<br></br>
 * ARG_OUTPUT_X/ARG_OUTPUT_Y图片输出尺寸<br></br>
 * 默认存储只外部私有图片目录下，或在application中添加name为photo_picker_path的&lt;meta&#62;，私有目录下的图片不能添加到媒体库中，选择图片时将会无法查看到<br></br>
 * OUTPUT_IMG图片保存路径目录，不包括文件名，优先级比photo_picker_path高，可不填<br></br>
 * ARG_RESULT返回的图片路径
 */
@Deprecated("废弃类，某些机型无法使用")
class CropActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_crop)
        mSpUtil = PhotoSPUtil.getInstance(this)
        if (!(intent.hasExtra(ARG_CROP_IMG) &&
                        intent.hasExtra(ARG_ASPECT_X) &&
                        intent.hasExtra(ARG_ASPECT_Y) &&
                        intent.hasExtra(ARG_OUTPUT_X) &&
                        intent.hasExtra(ARG_OUTPUT_Y))) {
            Toast.makeText(this, "缺少参数", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            val img = intent.getStringExtra(ARG_CROP_IMG)
            val aspX = intent.getIntExtra(ARG_ASPECT_X, 0)
            val aspY = intent.getIntExtra(ARG_ASPECT_Y, 0)
            val outX = intent.getIntExtra(ARG_OUTPUT_X, 0)
            val outY = intent.getIntExtra(ARG_OUTPUT_Y, 0)
            val outImg: File
            if (intent.hasExtra(ARG_OUTPUT_IMG)) {
                val out = intent.getStringExtra(ARG_OUTPUT_IMG)
                outImg = File(PhotoFileUtil.getExternalStorageRootPath() + File.separator + out, PhotoFileUtil.createFileName(".jpg"))
            } else {
                outImg = File(photoPath, PhotoFileUtil.createFileName(".jpg"))
            }
            if (!outImg.parentFile.exists()) {
                outImg.parentFile.mkdirs()
            }
            mSpUtil!!.edit().putValue(KEY_FILE_NAME, outImg.path).apply()
            val intent = Intent("com.android.camera.action.CROP")
            intent.setDataAndType(getUri(File(img)), "image/*")
            intent.putExtra("crop", "true")
            intent.putExtra("aspectX", aspX)
            intent.putExtra("aspectY", aspY)
            intent.putExtra("outputX", outX)
            intent.putExtra("outputY", outY)
            intent.putExtra("scale", true)// 如果选择的图小于裁剪大小则进行放大
            intent.putExtra("scaleUpIfNeeded", true)// 如果选择的图小于裁剪大小则进行放大
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getUri(outImg))
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
        val ARG_CROP_IMG = "CROP_IMG"
        val ARG_ASPECT_X = "ASPECT_X"
        val ARG_ASPECT_Y = "ASPECT_Y"
        val ARG_OUTPUT_X = "OUTPUT_X"
        val ARG_OUTPUT_Y = "OUTPUT_Y"
        val ARG_OUTPUT_IMG = "OUTPUT_IMG"
        private val REQUEST_CROP = 742
        private val KEY_FILE_NAME = "CROP_FILE_NAME"
    }
}
