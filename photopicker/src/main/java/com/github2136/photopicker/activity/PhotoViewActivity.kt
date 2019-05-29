package com.github2136.photopicker.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.github2136.photopicker.R
import com.github2136.photopicker.adapter.PhotoAdapter
import com.github2136.photopicker.fragment.PhotoFragment
import com.github2136.photopicker.widget.PickerViewPager

import java.util.ArrayList

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager

/**
 * 查看图片<br></br>
 * ARG_PHOTOS显示图片路径<br></br>
 * ARG_CURRENT_INDEX显示的图片下标<br></br>
 * ARG_PICKER_PATHS已选中图片路径<br></br>
 * ARG_PICKER_COUNT可选图片数量<br></br>
 * 如果ARG_PICKER_PATHS不为空则会在下方显示单选框选择图片<br></br>
 * 返回路径的key为ARG_PICKER_PATHS
 */
class PhotoViewActivity : AppCompatActivity(), PhotoFragment.OnFragmentInteractionListener {
    private var mPhotoPaths: List<String>? = null
    private var mPickerPaths: ArrayList<String>? = null
    private var mPickerCount: Int = 0
    private var mCurrentIndex: Int = 0
    private var vpPhoto: PickerViewPager? = null
    private var tvTitle: TextView? = null
    private var ibCheck: ImageButton? = null
    private var llCheck: LinearLayout? = null

    private val mOnClickListener = View.OnClickListener {
        val currentPath = mPhotoPaths!![vpPhoto!!.currentItem]
        if (mPickerPaths!!.contains(currentPath)) {
            mPickerPaths!!.remove(currentPath)
        } else {
            if (mPickerCount > mPickerPaths!!.size) {
                mPickerPaths!!.add(currentPath)
            } else {
                Toast.makeText(this@PhotoViewActivity, "最多选择 $mPickerCount 张", Toast.LENGTH_SHORT).show()
            }
        }
        setBottom()
    }

    internal var mPagerChangeListener: ViewPager.SimpleOnPageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            setTitle()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker_view)
        mPhotoPaths = intent.getStringArrayListExtra(ARG_PHOTOS)
        mCurrentIndex = intent.getIntExtra(ARG_CURRENT_INDEX, 0)

        mPickerPaths = intent.getStringArrayListExtra(ARG_PICKER_PATHS)
        mPickerCount = intent.getIntExtra(ARG_PICKER_COUNT, 0)

        val tbTitle = findViewById<View>(R.id.tb_title) as Toolbar
        setSupportActionBar(tbTitle)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        vpPhoto = findViewById<View>(R.id.vp_photo) as PickerViewPager
        vpPhoto!!.adapter = PhotoAdapter(supportFragmentManager, mPhotoPaths!!)
        vpPhoto!!.currentItem = mCurrentIndex
        vpPhoto!!.addOnPageChangeListener(mPagerChangeListener)

        tvTitle = findViewById<View>(R.id.tv_title) as TextView
        ibCheck = findViewById<View>(R.id.ib_check) as ImageButton
        llCheck = findViewById<View>(R.id.ll_check) as LinearLayout
        ibCheck!!.setOnClickListener(mOnClickListener)
        setTitle()
        if (mPickerPaths != null) {
            llCheck!!.visibility = View.VISIBLE
        }
    }

    private fun setTitle() {
        title = String.format("%d/%d", vpPhoto!!.currentItem + 1, mPhotoPaths!!.size)//标题
        setBottom()
    }

    private fun setBottom() {
        if (mPickerPaths != null) {
            if (mPickerPaths!!.contains(mPhotoPaths!![vpPhoto!!.currentItem])) {
                ibCheck!!.setImageResource(R.drawable.ic_photo_check_box)
            } else {
                ibCheck!!.setImageResource(R.drawable.ic_photo_check_box_outline)
            }
            tvTitle!!.text = String.format("%d/%d", mPickerPaths!!.size, mPickerCount)
        }
    }

    /**
     * 菜单点击事件
     *
     * @param item
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setPickerPath()
                finish()
            }
        }
        return true
    }

    override fun onFragmentClick() {
        if (mPickerPaths != null) {
            if (llCheck!!.visibility == View.VISIBLE) {
                val translateAnimation = TranslateAnimation(0f, 0f, 0f, llCheck!!.height.toFloat())
                translateAnimation.duration = 100
                llCheck!!.animation = translateAnimation
                llCheck!!.visibility = View.GONE
            } else {
                val translateAnimation = TranslateAnimation(0f, 0f, llCheck!!.height.toFloat(), 0f)
                translateAnimation.duration = 100
                llCheck!!.animation = translateAnimation
                llCheck!!.visibility = View.VISIBLE
            }
        }
    }

    override fun onBackPressed() {
        setPickerPath()
        super.onBackPressed()
    }

    private fun setPickerPath() {
        if (mPickerPaths != null) {
            val intent = Intent()
            intent.putStringArrayListExtra(ARG_PICKER_PATHS, mPickerPaths)
            setResult(Activity.RESULT_OK, intent)
        }
    }

    companion object {
        val ARG_PHOTOS= "PHOTOS"
        val ARG_CURRENT_INDEX = "CURRENT_INDEX"
        val ARG_PICKER_COUNT = "PICKER_COUNT"//所选图片数量
        val ARG_PICKER_PATHS = "PICKER_PATHS"//所选图片路径
    }
}