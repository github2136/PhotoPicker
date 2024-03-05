package com.github2136.photo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github2136.photopicker.activity.CaptureActivity
import com.github2136.photopicker.activity.CropActivity
import com.github2136.photopicker.activity.PhotoPickerActivity
import com.github2136.photopicker.activity.PhotoViewActivity
import com.github2136.photopicker.other.PhotoFileUtil
import kotlinx.android.synthetic.main.activity_main.im_capture
import kotlinx.android.synthetic.main.activity_main.im_crop
import kotlinx.android.synthetic.main.activity_main.im_crop2
import kotlinx.android.synthetic.main.activity_main.im_preview
import kotlinx.android.synthetic.main.activity_main.im_preview2
import kotlinx.android.synthetic.main.activity_main.im_select_img
import kotlinx.android.synthetic.main.activity_main.im_select_imgs

class MainActivity : AppCompatActivity() {
    var selectPaths: ArrayList<String> = ArrayList()
    val llImg by lazy { findViewById<LinearLayout>(R.id.llImg) }
    val tv by lazy { findViewById<TextView>(R.id.tv) }
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
            startActivityForResult(intent, 2)
        }

        im_crop.setOnClickListener {
            val intent = Intent(this@MainActivity, CaptureActivity::class.java)
            startActivityForResult(intent, 3)
        }
        im_crop2.setOnClickListener {
            val intent = Intent(this@MainActivity, PhotoPickerActivity::class.java)
            intent.putExtra(PhotoPickerActivity.ARG_PICKER_COUNT, 1)
            startActivityForResult(intent, 6)
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
            intent.putStringArrayListExtra(
                PhotoViewActivity.ARG_PHOTOS,
                arrayListOf(
                    "http://pica.zhimg.com/v2-7cb8b1ea5e11779e25b4b35d52b777f2_l.jpg?source=32738c0c",
                    "https://pica.zhimg.com/v2-7cb8b1ea5e11779e25b4b35d52b777f2_l.jpg?source=32738c0c"
                )
            )
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                1 -> {
                    tv.text = null
                    llImg.removeAllViews()
                    val result = data!!.getStringArrayListExtra(PhotoPickerActivity.ARG_RESULT)!!
                    for (i in result.indices) {
                        val s = result[i]
                        tv.append("path :$s\n")
                    }
                    val uri = data.getParcelableArrayListExtra<Uri>(PhotoPickerActivity.ARG_RESULT_URI)!!
                    selectPaths = uri.map { it.toString() } as ArrayList<String>
                    for (i in uri.indices) {
                        val s = uri[i]
                        tv.append("path :$s\n")
                        llImg.addView(ImageView(this).apply {
                            layoutParams = LayoutParams(300, 300)
                            setImageURI(s)
                        })
                    }
                }
                2 -> {
                    tv.text = null
                    llImg.removeAllViews()
                    val result = data!!.data
                    tv.append("path :$result\n")
                    tv.append("path :${PhotoFileUtil.getFileAbsolutePath(this, result!!)!!}\n")
                    llImg.addView(ImageView(this).apply {
                        layoutParams = LayoutParams(300, 300)
                        setImageURI(result)
                    })
                }
                3 -> {
                    val result = data!!.data
                    val intent = Intent(this@MainActivity, CropActivity::class.java)
                    intent.putExtra(CropActivity.ARG_IMAGE_URI, result)
                    intent.putExtra(CropActivity.ARG_ASPECT_X, 1)
                    intent.putExtra(CropActivity.ARG_ASPECT_Y, 1)
                    intent.putExtra(CropActivity.ARG_OUTPUT_X, 200)
                    intent.putExtra(CropActivity.ARG_OUTPUT_Y, 200)
                    startActivityForResult(intent, 4)
                }
                4 -> {
                    tv.text = null
                    llImg.removeAllViews()
                    val result = data!!.data
                    tv.append("path :$result\n")
                    tv.append("path :${PhotoFileUtil.getFileAbsolutePath(this, result!!)!!}\n")
                    llImg.addView(ImageView(this).apply {
                        layoutParams = LayoutParams(300, 300)
                        setImageURI(result)
                    })
                }
                5 -> {
                    tv.text = null
                    llImg.removeAllViews()
                    val result = data!!.getStringArrayListExtra(PhotoViewActivity.ARG_PICKER_PATHS)!!
                    for (i in result.indices) {
                        val s = result[i]
                        tv.append("path :${s}\n")
                        llImg.addView(ImageView(this).apply {
                            layoutParams = LayoutParams(300, 300)
                            setImageURI(Uri.parse(s))
                        })
                    }
                }
                6 -> {
                    val uri = data!!.getParcelableArrayListExtra<Uri>(PhotoPickerActivity.ARG_RESULT_URI)!!
                    val intent = Intent(this@MainActivity, CropActivity::class.java)
                    intent.putExtra(CropActivity.ARG_IMAGE_URI, uri.first())
                    intent.putExtra(CropActivity.ARG_ASPECT_X, 1)
                    intent.putExtra(CropActivity.ARG_ASPECT_Y, 1)
                    intent.putExtra(CropActivity.ARG_OUTPUT_X, 200)
                    intent.putExtra(CropActivity.ARG_OUTPUT_Y, 200)
                    startActivityForResult(intent, 4)
                }
            }
        }
    }
}