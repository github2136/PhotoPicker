package com.github2136.picturepicker.adapter

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView

import com.github2136.base.BaseRecyclerAdapter
import com.github2136.base.ViewHolderRecyclerView
import com.github2136.picturepicker.R
import com.github2136.picturepicker.entity.PicturePicker
import com.github2136.picturepicker.other.ImageLoader
import com.github2136.picturepicker.other.ImageLoaderInstance

import java.util.ArrayList

/**
 * Created by yb on 2017/8/26.
 */

class PicturePickerAdapter(val context: Context, list: MutableList<PicturePicker>, private val mSelectCount: Int) :
        BaseRecyclerAdapter<PicturePicker>(list) {
    var pickerPaths: ArrayList<String>? = null
    private val mViewSize: Int
    private val mImgSize: Int
    private val layoutParams: RelativeLayout.LayoutParams
    private var mOnSelectChangeCallback: OnSelectChangeCallback? = null

    init {
        mViewSize = (context.resources.displayMetrics.widthPixels - 5 * 4) / 3
        mImgSize = mViewSize
        layoutParams = RelativeLayout.LayoutParams(mViewSize, mViewSize)
        pickerPaths = arrayListOf()
    }

    fun setOnSelectImageCallback(onSelectImageCallback: OnSelectChangeCallback) {
        this.mOnSelectChangeCallback = onSelectImageCallback
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.item_picture_picker
    }

    override fun onBindView(picturePicker: PicturePicker, holder: ViewHolderRecyclerView, position: Int) {
        val ivImage = holder.getView<ImageView>(R.id.iv_image)
        val ivCheck = holder.getView<AppCompatImageView>(R.id.iv_check)
        val flCheck = holder.getView<FrameLayout>(R.id.fl_check)
        if (mSelectCount == 1) {
            ivCheck!!.visibility = View.GONE
        }
        ivImage!!.layoutParams = layoutParams
        val loader = ImageLoaderInstance.getInstance(context)!!.imageLoader
        if (loader!!.supportAnimatedGifThumbnail()) {
            loader.loadThumbnail(context, mImgSize, mImgSize, ivImage, picturePicker.data!!)
        } else {
            loader.loadAnimatedGifThumbnail(context, mImgSize, mImgSize, ivImage, picturePicker.data!!)
        }
        if (pickerPaths!!.contains(picturePicker.data!!)) {
            ivCheck!!.setImageResource(R.drawable.ic_picker_check_box)
            ivCheck.setBackgroundResource(R.drawable.sha_picker_check_bg)
        } else {
            ivCheck!!.setImageResource(R.drawable.ic_picker_check_box_outline)
            ivCheck.setBackgroundColor(Color.TRANSPARENT)
        }
        flCheck!!.setOnClickListener {
            if (pickerPaths!!.contains(picturePicker.data!!)) {
                pickerPaths!!.remove(picturePicker.data!!)
                ivCheck.setImageResource(R.drawable.ic_picker_check_box_outline)
                ivCheck.setBackgroundColor(Color.TRANSPARENT)
            } else {
                if (mSelectCount > pickerPaths!!.size) {
                    pickerPaths!!.add(picturePicker.data!!)
                    ivCheck.setImageResource(R.drawable.ic_picker_check_box)
                    ivCheck.setBackgroundResource(R.drawable.sha_picker_check_bg)
                } else {
                    Toast.makeText(context, "最多选择 $mSelectCount 张", Toast.LENGTH_SHORT).show()
                }
            }
            if (mOnSelectChangeCallback != null) {
                mOnSelectChangeCallback!!.selectChange(pickerPaths!!.size)
            }
        }
    }


    fun clearSelectPaths() {
        pickerPaths = arrayListOf()
    }

    fun getItem() = list

    interface OnSelectChangeCallback {
        fun selectChange(selectCount: Int)
    }
}
