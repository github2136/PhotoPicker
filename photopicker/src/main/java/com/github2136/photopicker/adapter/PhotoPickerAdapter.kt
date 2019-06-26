package com.github2136.photopicker.adapter

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import com.github2136.photopicker.R
import com.github2136.photopicker.entity.PhotoPicker
import com.github2136.photopicker.other.ImageLoaderInstance
import java.util.*

/**
 * Created by yb on 2017/8/26.
 */

class PhotoPickerAdapter(val context: Context, list: MutableList<PhotoPicker>, private val mSelectCount: Int) :
    PhotoBaseAdapter<PhotoPicker>(list) {
    var pickerUris: ArrayList<Uri> = arrayListOf()
    var pickerPaths: ArrayList<String> = arrayListOf()
    private val mViewSize: Int = (context.resources.displayMetrics.widthPixels - 5 * 4) / 3
    private val mImgSize: Int
    private val layoutParams: RelativeLayout.LayoutParams
    private var mOnSelectChangeCallback: OnSelectChangeCallback? = null

    init {
        mImgSize = mViewSize
        layoutParams = RelativeLayout.LayoutParams(mViewSize, mViewSize)
    }

    fun setOnSelectImageCallback(onSelectImageCallback: OnSelectChangeCallback) {
        this.mOnSelectChangeCallback = onSelectImageCallback
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.item_photo_picker
    }

    override fun onBindView(photoPicker: PhotoPicker, holder: PhotoVH, position: Int) {
        val ivImage = holder.getView<ImageView>(R.id.iv_image)
        val ivCheck = holder.getView<AppCompatImageView>(R.id.iv_check)
        val flCheck = holder.getView<FrameLayout>(R.id.fl_check)
        if (mSelectCount == 1) {
            ivCheck!!.visibility = View.GONE
        }
        ivImage!!.layoutParams = layoutParams
        val loader = ImageLoaderInstance.getInstance(context)!!.imageLoader
        if (loader!!.supportAnimatedGifThumbnail()) {
            loader.loadThumbnail(context, mImgSize, mImgSize, ivImage, photoPicker.data!!)
        } else {
            loader.loadAnimatedGifThumbnail(context, mImgSize, mImgSize, ivImage, photoPicker.data!!)
        }
        val uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, photoPicker._id!!.toString())
        val path = photoPicker.data!!
        if (pickerPaths.contains(path)) {
            ivCheck!!.setImageResource(R.drawable.ic_photo_check_box)
            ivCheck.setBackgroundResource(R.drawable.sha_picker_check_bg)
        } else {
            ivCheck!!.setImageResource(R.drawable.ic_photo_check_box_outline)
            ivCheck.setBackgroundColor(Color.TRANSPARENT)
        }
        flCheck!!.setOnClickListener {
            if (pickerPaths.contains(path)) {
                pickerPaths.remove(path)
                pickerUris.remove(uri)
                ivCheck.setImageResource(R.drawable.ic_photo_check_box_outline)
                ivCheck.setBackgroundColor(Color.TRANSPARENT)
            } else {
                if (mSelectCount > pickerPaths.size) {
                    pickerPaths.add(path)
                    pickerUris.add(uri)
                    ivCheck.setImageResource(R.drawable.ic_photo_check_box)
                    ivCheck.setBackgroundResource(R.drawable.sha_picker_check_bg)
                } else {
                    Toast.makeText(context, "最多选择 $mSelectCount 张", Toast.LENGTH_SHORT).show()
                }
            }
            if (mOnSelectChangeCallback != null) {
                mOnSelectChangeCallback!!.selectChange(pickerPaths.size)
            }
        }
    }


    fun clearSelectPaths() {
        pickerPaths.clear()
        pickerUris.clear()
    }

    fun getItem() = list

    interface OnSelectChangeCallback {
        fun selectChange(selectCount: Int)
    }
}
