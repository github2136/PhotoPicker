package com.github2136.photo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.github2136.photopicker.activity.CaptureActivity
import com.github2136.photopicker.activity.CropActivity
import com.github2136.photopicker.activity.PhotoPickerActivity
import com.github2136.photopicker.activity.PhotoViewActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    internal var selectPaths: ArrayList<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnImg = findViewById(R.id.im_select_img) as Button
        btnImg.setOnClickListener {
            val intent = Intent(this@MainActivity, PhotoPickerActivity::class.java)
            intent.putExtra(PhotoPickerActivity.ARG_PICKER_COUNT, 1)
            startActivityForResult(intent, 1)
        }
        val btnImgs = findViewById(R.id.im_select_imgs) as Button
        btnImgs.setOnClickListener {
            val intent = Intent(this@MainActivity, PhotoPickerActivity::class.java)
            intent.putExtra(PhotoPickerActivity.ARG_PICKER_COUNT, 5)
            startActivityForResult(intent, 1)
        }
        val btnCapture = findViewById(R.id.im_capture) as Button
        btnCapture.setOnClickListener {
            val intent = Intent(this@MainActivity, CaptureActivity::class.java)
            intent.putExtra(CaptureActivity.ARG_FILE_PATH, "fffff")
            startActivityForResult(intent, 2)
        }
        val btnPreview = findViewById(R.id.im_preview) as Button
        btnPreview.setOnClickListener {
            val intent = Intent(this@MainActivity, PhotoViewActivity::class.java)
            val path = ArrayList<String>()


            startActivityForResult(intent, 2)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                1 -> {
                    val result = data!!.getStringArrayListExtra(PhotoPickerActivity.ARG_RESULT)
                    for (i in result.indices) {
                        val s = result[i]
                        Log.e("path", s)
                    }
                }
                2 -> {
                    val result = data!!.getStringExtra(CaptureActivity.ARG_RESULT)
                    val intent = Intent(this@MainActivity, CropActivity::class.java)
                    intent.putExtra(CropActivity.ARG_CROP_IMG, result)
                    intent.putExtra(CropActivity.ARG_ASPECT_X, 1)
                    intent.putExtra(CropActivity.ARG_ASPECT_Y, 1)
                    intent.putExtra(CropActivity.ARG_OUTPUT_X, 200)
                    intent.putExtra(CropActivity.ARG_OUTPUT_Y, 200)
                    intent.putExtra(CropActivity.ARG_OUTPUT_IMG, "fffsdf")
                    startActivityForResult(intent, 3)
                }
            }

        }
    }
}
