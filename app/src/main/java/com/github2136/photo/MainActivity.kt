package com.github2136.photo

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import com.github2136.photopicker.activity.CaptureActivity
import com.github2136.photopicker.activity.CropActivity
import com.github2136.photopicker.activity.PhotoPickerActivity
import com.github2136.photopicker.activity.PhotoViewActivity
import com.github2136.photopicker.other.PhotoFileUtil
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    var selectPaths: ArrayList<String> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        im_select_img.setOnClickListener {
            val intent = Intent(this@MainActivity, PhotoPickerActivity::class.java)
            intent.putExtra(PhotoPickerActivity.ARG_PICKER_COUNT, 1)
            startActivityForResult(intent, 1)
        }

        im_select_imgs.setOnClickListener {
            val intent = Intent(this@MainActivity, PhotoPickerActivity::class.java)
            intent.putExtra(PhotoPickerActivity.ARG_PICKER_COUNT, 5)
            startActivityForResult(intent, 1)
        }

        im_capture.setOnClickListener {
            val intent = Intent(this@MainActivity, CaptureActivity::class.java)
//            intent.putExtra(CaptureActivity.ARG_FILE_PATH, "Photo")
            startActivityForResult(intent, 2)
        }

        im_crop.setOnClickListener {
            val intent = Intent(this@MainActivity, CaptureActivity::class.java)
            intent.putExtra(CaptureActivity.ARG_FILE_PATH, "fffff")
            startActivityForResult(intent, 3)
        }

        im_preview.setOnClickListener {
            val intent = Intent(this@MainActivity, PhotoViewActivity::class.java)
            intent.putStringArrayListExtra(PhotoViewActivity.ARG_PHOTOS, selectPaths)
            intent.putExtra(PhotoViewActivity.ARG_PICKER_COUNT, 3)
            intent.putStringArrayListExtra(PhotoViewActivity.ARG_PICKER_PATHS, selectPaths)
            startActivityForResult(intent, 5)
        }
        im_preview2.setOnClickListener {
            val intent = Intent(this@MainActivity, PhotoViewActivity::class.java)
            intent.putStringArrayListExtra(PhotoViewActivity.ARG_PHOTOS,  arrayListOf("http://pica.zhimg.com/v2-7cb8b1ea5e11779e25b4b35d52b777f2_l.jpg?source=32738c0c","https://pica.zhimg.com/v2-7cb8b1ea5e11779e25b4b35d52b777f2_l.jpg?source=32738c0c"))
            startActivityForResult(intent, 5)
        }
            }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                1 -> {
                    val result = data!!.getStringArrayListExtra(PhotoPickerActivity.ARG_RESULT)
                    selectPaths = result
                    for (i in result.indices) {
                        val s = result[i]
                        Log.e("path", s)
                    }
                    val uri = data.getParcelableArrayListExtra<Uri>(PhotoPickerActivity.ARG_RESULT_URI)
                    for (i in uri.indices) {
                        val s = uri[i]
                        Log.e("path", s.toString())
                    }
                }
                2 -> {
                    val result = data!!.data
                    Log.e("path", PhotoFileUtil.getFileAbsolutePath(this, result!!))
                }
                3 -> {
                    val result = data!!.data
                    val intent = Intent(this@MainActivity, CropActivity::class.java)
                    intent.putExtra(CropActivity.ARG_CROP_IMG, result)
                    intent.putExtra(CropActivity.ARG_ASPECT_X, 1)
                    intent.putExtra(CropActivity.ARG_ASPECT_Y, 1)
                    intent.putExtra(CropActivity.ARG_OUTPUT_X, 200)
                    intent.putExtra(CropActivity.ARG_OUTPUT_Y, 200)
                    intent.putExtra(CropActivity.ARG_FILE_PATH, "fffsdf")
                    startActivityForResult(intent, 4)
                }
                4 -> {
                    val result = data!!.data
                    Log.e("path", PhotoFileUtil.getFileAbsolutePath(this, result!!))
                }
                5 -> {
                    val result = data!!.getStringArrayListExtra(PhotoViewActivity.ARG_PICKER_PATHS)
                    for (i in result.indices) {
                        val s = result[i]
                        Log.e("path", s)
                    }
                }
            }
        }
    }
}