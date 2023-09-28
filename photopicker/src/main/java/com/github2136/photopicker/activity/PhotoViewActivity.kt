package com.github2136.photopicker.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.github2136.photopicker.R
import com.github2136.photopicker.adapter.PhotoAdapter
import com.github2136.photopicker.fragment.PhotoFragment
import kotlinx.android.synthetic.main.activity_picker_view.*

/**
 *      查看图片
 *      ARG_PHOTOS显示图片路径
 *      ARG_CURRENT_INDEX显示的图片下标
 *      ARG_PICKER_PATHS已选中图片路径
 *      ARG_PICKER_COUNT可选图片数量
 *      如果ARG_PICKER_PATHS不为空则会在下方显示单选框选择图片
 *      返回路径的key为ARG_PICKER_PATHS
 */
class PhotoViewActivity : AppCompatActivity(), PhotoFragment.OnFragmentInteractionListener {
    val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private val mPhotoPaths by lazy { intent.getStringArrayListExtra(ARG_PHOTOS) }
    private val mPickerPaths by lazy { intent.getStringArrayListExtra(ARG_PICKER_PATHS) }
    private var mPickerCount: Int = 0
    private var mCurrentIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker_view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkPermissionDenied(permissions)) {
            requestPermissions(permissions, 1)
        } else {
            initPhoto()
        }
    }

    fun initPhoto() {
        mCurrentIndex = intent.getIntExtra(ARG_CURRENT_INDEX, 0)
        mPickerCount = intent.getIntExtra(ARG_PICKER_COUNT, 0)
        val tbTitle = findViewById<View>(R.id.tb_title) as Toolbar
        setSupportActionBar(tbTitle)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        vp_photo.adapter = PhotoAdapter(supportFragmentManager, mPhotoPaths!!)
        vp_photo.currentItem = mCurrentIndex
        vp_photo.addOnPageChangeListener(mPagerChangeListener)


        ib_check!!.setOnClickListener(mOnClickListener)
        setTitle()
        if (mPickerPaths != null) {
            ll_check.visibility = View.VISIBLE
        }
    }

    private fun setTitle() {
        title = String.format("%d/%d", vp_photo.currentItem + 1, mPhotoPaths!!.size) //标题
        setBottom()
    }

    private fun setBottom() {
        if (mPickerPaths != null) {
            if (mPickerPaths!!.contains(mPhotoPaths!![vp_photo.currentItem])) {
                ib_check.setImageResource(R.drawable.ic_photo_check_box)
            } else {
                ib_check.setImageResource(R.drawable.ic_photo_check_box_outline)
            }
            tv_title.text = String.format("%d/%d", mPickerPaths!!.size, mPickerCount)
        }
    }

    private val mOnClickListener = View.OnClickListener {
        val currentPath = mPhotoPaths!![vp_photo.currentItem]
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

    /**
     * 创建菜单
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if ((mPickerPaths != null)) {
            menuInflater.inflate(R.menu.view_menu, menu)
        }
        return super.onCreateOptionsMenu(menu)
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
                finish()
            }

            R.id.menu_ok -> {
                setPickerPath()
                finish()
            }
        }
        return true
    }

    override fun onFragmentClick() {
        if (mPickerPaths != null) {
            if (ll_check.visibility == View.VISIBLE) {
                val translateAnimation = TranslateAnimation(0f, 0f, 0f, ll_check.height.toFloat())
                translateAnimation.duration = 100
                ll_check.animation = translateAnimation
                ll_check.visibility = View.GONE
            } else {
                val translateAnimation = TranslateAnimation(0f, 0f, ll_check.height.toFloat(), 0f)
                translateAnimation.duration = 100
                ll_check.animation = translateAnimation
                ll_check.visibility = View.VISIBLE
            }
        }
    }

    private fun setPickerPath() {
        if (mPickerPaths != null) {
            val intent = Intent()
            intent.putStringArrayListExtra(ARG_PICKER_PATHS, mPickerPaths)
            setResult(Activity.RESULT_OK, intent)
        }
    }

    override fun onRestart() {
        super.onRestart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkPermissionDenied(permissions)) {
            requestPermissions(permissions, 1)
        } else {
            initPhoto()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var allow = true
        var denied = mutableListOf<String>()
        for ((index, permission) in permissions.withIndex()) {
            //  拒绝的权限
            if (grantResults[index] == PackageManager.PERMISSION_DENIED) {
                allow = false
                denied.add(permission)
                //判断是否点击不再提示
                val showRationale = shouldShowRequestPermissionRationale(permission);
                if (!showRationale) {
                    // 用户点击不再提醒，打开设置页让用户开启权限
                    AlertDialog.Builder(this)
                        .setTitle("警告")
                        .setMessage("缺少 ${getString(packageManager.getPermissionInfo(permission, 0).labelRes)} 权限，是否打开设置修改权限？")
                        .setPositiveButton("打开设置") { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            val uri = Uri.fromParts("package", packageName, null);
                            intent.data = uri;
                            startActivity(intent);
                        }
                        .setNegativeButton("取消", null)
                        .show()
                    break
                }
            }

            if (!allow) {
                // 用户点击了取消...
                AlertDialog.Builder(this)
                    .setTitle("警告")
                    .setMessage("缺少 ${denied.joinToString { getString(packageManager.getPermissionInfo(it, 0).labelRes) }} 权限，继续使用请重新请求权限")
                    .setPositiveButton("请求权限") { _, _ ->
                        requestPermissions(permissions, 1)
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        }
        if (allow) {
            initPhoto()
        }
    }

    /**
     * 判断权限拒绝
     */
    fun checkPermissionDenied(permissions: Array<String>): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissions.firstOrNull { checkSelfPermission(it) == PackageManager.PERMISSION_DENIED } != null
        } else {
            false
        }
    }

    companion object {
        val ARG_PHOTOS = "PHOTOS" //显示的图片路径
        val ARG_CURRENT_INDEX = "CURRENT_INDEX" //显示的图片下标
        val ARG_PICKER_COUNT = "PICKER_COUNT" //可选图片数量
        val ARG_PICKER_PATHS = "PICKER_PATHS" //所选图片路径
    }
}