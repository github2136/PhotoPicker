package com.github2136.photopicker.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.github2136.photopicker.R
import com.github2136.photopicker.adapter.PhotoPickerAdapter
import com.github2136.photopicker.entity.PhotoPicker
import com.github2136.photopicker.other.PhotoFileUtil
import com.github2136.photopicker.other.PickerImageItemDecoration
import kotlinx.android.synthetic.main.activity_photo_picker.*
import kotlinx.android.synthetic.main.view_picker_title.*
import java.util.*

/**
 *      选择图片
 *      ARG_PICKER_COUNT选择图片数量
 *      ARG_RESULT返回的图片路径
 *      ARG_RESULT_URI返回图片的URI
 */
class PhotoPickerActivity : AppCompatActivity() {
    val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var mFolderName: MutableList<String> = mutableListOf() //文件夹名称
    private var mPickerCount: Int = 0 //可选择图片数量
    private lateinit var mPhotoPickerAdapter: PhotoPickerAdapter
    private val mMimeType = HashSet<String>()
    private lateinit var mFolderDialog: AlertDialog
    private lateinit var mSelectFolderName: String
    val handle = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_picker)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkPermissionDenied(permissions)) {
            requestPermissions(permissions, 1)
        } else {
            initPhoto()
        }
    }

    fun initPhoto() {
        btn_folder.setOnClickListener(mOnClickListener)
        btn_preview.setOnClickListener(mOnClickListener)
        setSupportActionBar(tb_title)
        mPickerCount = intent.getIntExtra(ARG_PICKER_COUNT, 0)
        if (mPickerCount < 1) {
            Toast.makeText(this, "可选图片数量至少为1", Toast.LENGTH_SHORT).show()
            finish()
        }
        mSelectFolderName = "*"
        setToolbarTitle(0, mSelectFolderName)
        mMimeType.add("image/jpeg")
        mMimeType.add("image/png")
        mMimeType.add("image/gif")
        if (mPickerCount == 1) {
            btn_preview.visibility = View.GONE

        }
        // getSupportActionBar().setToolbarTitle("标题");
        // getSupportActionBar().setSubtitle("副标题");
        // getSupportActionBar().setLogo(R.drawable.ic_launcher);

        /* 菜单的监听可以在toolbar里设置，也可以像ActionBar那样，通过Activity的onOptionsItemSelected回调方法来处理 */
        //        tb_title.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
        //            @Override
        //            public boolean onMenuItemClick(MenuItem item) {
        //                switch (item.getItemId()) {
        //                    case R.id.action_settings:
        //                        Toast.makeText(MainActivity.this, "action_settings", 0).show();
        //                        break;
        //                    case R.id.action_share:
        //                        Toast.makeText(MainActivity.this, "action_share", 0).show();
        //                        break;
        //                    default:
        //                        break;
        //                }
        //                return true;
        //            }
        //        });
        //显示返回按钮onOptionsItemSelected中监听id固定为android.R.id.home
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val rvImages = findViewById<View>(R.id.rv_images) as RecyclerView
        rvImages.setHasFixedSize(true)
        val selectImageItemDecoration = PickerImageItemDecoration(3, 5, true)
        rvImages.addItemDecoration(selectImageItemDecoration)

        mFolderName = mutableListOf()
        mFolderName.add("*") //表示全部

        getBucketDisplayName()

        mPhotoPickerAdapter = PhotoPickerAdapter(this, mPickerCount)
        mPhotoPickerAdapter.loadMore = {
            mPhotoPickerAdapter.loading.value = true
            getImages()
        }
        rvImages.adapter = mPhotoPickerAdapter
        mPhotoPickerAdapter.setOnItemClickListener { position ->
            if (mPickerCount == 1) {
                val photoPicker = mPhotoPickerAdapter.getItem(position)!!
                val uris = ArrayList<Uri>()
                val imgs = ArrayList<String>()
                uris.add(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, photoPicker._id.toString()))
                imgs.add(photoPicker.data!!)
                val intent = Intent()
                intent.putParcelableArrayListExtra(ARG_RESULT_URI, uris)
                intent.putStringArrayListExtra(ARG_RESULT, imgs)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                val intent = Intent(this@PhotoPickerActivity, PhotoViewActivity::class.java)
                val imgs = ArrayList<String>()
                for (img in mPhotoPickerAdapter.getItem()!!) {
                    imgs.add(img.data!!)
                }
                intent.putStringArrayListExtra(PhotoViewActivity.ARG_PHOTOS, imgs)
                intent.putExtra(PhotoViewActivity.ARG_CURRENT_INDEX, position)

                intent.putStringArrayListExtra(PhotoViewActivity.ARG_PICKER_PATHS, mPhotoPickerAdapter.pickerPaths)
                intent.putExtra(PhotoViewActivity.ARG_PICKER_COUNT, mPickerCount)

                startActivityForResult(intent, REQUEST_PHOTO_VIEW)
            }
        }
        mPhotoPickerAdapter.setOnSelectImageCallback(object : PhotoPickerAdapter.OnSelectChangeCallback {
            override fun selectChange(selectCount: Int) {
                setToolbarTitle(selectCount, mSelectFolderName)
            }
        })
        getImages()
        val fName = mFolderName.toTypedArray()
        fName[0] = "全部"
        mFolderDialog = AlertDialog.Builder(this)
            .setItems(fName) { _, which ->
                mSelectFolderName = if (which == 0) {
                    "*"
                } else {
                    mFolderName[which]
                }
                mPhotoPickerAdapter.pageIndex = 0
                mPhotoPickerAdapter.refreshing.value = true
                mPhotoPickerAdapter.complete = false
                getImages()
                mPhotoPickerAdapter.notifyDataSetChanged()
                setToolbarTitle(mPhotoPickerAdapter.pickerPaths.size, mSelectFolderName)
            }
            .create()
    }

    private val mOnClickListener = View.OnClickListener { v ->
        if (v.id == R.id.btn_folder) {
            mFolderDialog.show()
        } else if (v.id == R.id.btn_preview) {
            val intent = Intent(this@PhotoPickerActivity, PhotoViewActivity::class.java)

            intent.putStringArrayListExtra(PhotoViewActivity.ARG_PHOTOS, mPhotoPickerAdapter.pickerPaths)
            intent.putExtra(PhotoViewActivity.ARG_CURRENT_INDEX, 0)

            intent.putStringArrayListExtra(PhotoViewActivity.ARG_PICKER_PATHS, mPhotoPickerAdapter.pickerPaths)
            intent.putExtra(PhotoViewActivity.ARG_PICKER_COUNT, mPickerCount)

            startActivityForResult(intent, REQUEST_PHOTO_VIEW)
        }
    }

    private fun setToolbarTitle(selectCount: Int, folderName: String) {
        var folderName = folderName
        if (folderName == "*") {
            folderName = "全部"
        }
        title = if (mPickerCount != 1) {
            String.format("%d/%d", selectCount, mPickerCount) //标题
        } else {
            "图片选择" //标题
        }
        btn_folder.text = folderName
        btn_preview.isEnabled = selectCount > 0
    }

    /**
     * 查询所有文件夹
     */
    private fun getBucketDisplayName() {
        val contentResolver = contentResolver
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Images.ImageColumns.SIZE + " > 0 and " + MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + " is not null) group by (" + MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                null,
                MediaStore.Images.ImageColumns.DATE_TAKEN + " desc"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e.message?.startsWith("Permission Denial") == true) {
                AlertDialog.Builder(this)
                    .setTitle("警告")
                    .setMessage("没有外部存储目录读取权限")
                    .setPositiveButton("关闭") { _, _ -> finish() }
                    .show()
            } else {
                Toast.makeText(this, "图片数据获取失败", Toast.LENGTH_SHORT).show()
            }
        }

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val columnIndex1 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DESCRIPTION)
                val columnIndex2 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.PICASA_ID)
                val columnIndex3 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.IS_PRIVATE)
                val columnIndex4 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.LATITUDE)
                val columnIndex5 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.LONGITUDE)
                val columnIndex6 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN)
                val columnIndex7 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION)
                val columnIndex8 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC)
                val columnIndex9 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID)
                val columnIndex10 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)

                val columnIndex11 = cursor.getColumnIndex(MediaStore.MediaColumns._ID)

                val columnIndex12 = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                val columnIndex13 = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                val columnIndex14 = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val columnIndex15 = cursor.getColumnIndex(MediaStore.MediaColumns.TITLE)
                val columnIndex16 = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)
                val columnIndex17 = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                val columnIndex18 = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                val columnIndex19 = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
                val columnIndex20 = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)

                do {
                    val img = PhotoPicker(
                        cursor.getString(columnIndex1),
                        cursor.getString(columnIndex2),
                        cursor.getInt(columnIndex3),
                        cursor.getDouble(columnIndex4),
                        cursor.getDouble(columnIndex5),
                        cursor.getInt(columnIndex6),
                        cursor.getInt(columnIndex7),
                        cursor.getInt(columnIndex8),
                        cursor.getString(columnIndex9),
                        cursor.getString(columnIndex10),

                        cursor.getLong(columnIndex11),

                        cursor.getString(columnIndex12),
                        cursor.getLong(columnIndex13),
                        cursor.getString(columnIndex14),
                        cursor.getString(columnIndex15),
                        cursor.getLong(columnIndex16),
                        cursor.getLong(columnIndex17),
                        cursor.getString(columnIndex18),
                        cursor.getInt(columnIndex19),
                        cursor.getInt(columnIndex20)
                    )
                    mFolderName.add(img.bucket_display_name)
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
    }

    private fun getImages() {
        val contentResolver = contentResolver
        var cursor: Cursor? = null
        try {
            val folder = if (mSelectFolderName != "*") " and " + MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + " = '$mSelectFolderName' " else ""
            cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Images.ImageColumns.SIZE + " > 0 and " + MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + " is not null" + folder,
                null, MediaStore.Images.ImageColumns.DATE_TAKEN + " desc limit ${mPhotoPickerAdapter.pageIndex * mPhotoPickerAdapter.pageCount} , ${mPhotoPickerAdapter.pageCount} "
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e.message?.startsWith("Permission Denial") == true) {
                AlertDialog.Builder(this)
                    .setTitle("警告")
                    .setMessage("没有外部存储目录读取权限")
                    .setPositiveButton("关闭") { _, _ -> finish() }
                    .show()
            } else {
                Toast.makeText(this, "图片数据获取失败", Toast.LENGTH_SHORT).show()
            }
            //Permission Denial: reading com.android.providers.media.MediaProvider uri content://media/external/images/media from pid=4833,
            // uid=10059 requires android.permission.READ_EXTERNAL_STORAGE, or grantUriPermission()
        }

        val images = ArrayList<PhotoPicker>()
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val columnIndex1 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DESCRIPTION)
                val columnIndex2 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.PICASA_ID)
                val columnIndex3 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.IS_PRIVATE)
                val columnIndex4 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.LATITUDE)
                val columnIndex5 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.LONGITUDE)
                val columnIndex6 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN)
                val columnIndex7 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION)
                val columnIndex8 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC)
                val columnIndex9 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID)
                val columnIndex10 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)

                val columnIndex11 = cursor.getColumnIndex(MediaStore.MediaColumns._ID)

                val columnIndex12 = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                val columnIndex13 = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                val columnIndex14 = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val columnIndex15 = cursor.getColumnIndex(MediaStore.MediaColumns.TITLE)
                val columnIndex16 = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)
                val columnIndex17 = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                val columnIndex18 = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                val columnIndex19 = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
                val columnIndex20 = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)

                do {
                    val img = PhotoPicker(
                        cursor.getString(columnIndex1),
                        cursor.getString(columnIndex2),
                        cursor.getInt(columnIndex3),
                        cursor.getDouble(columnIndex4),
                        cursor.getDouble(columnIndex5),
                        cursor.getInt(columnIndex6),
                        cursor.getInt(columnIndex7),
                        cursor.getInt(columnIndex8),
                        cursor.getString(columnIndex9),
                        cursor.getString(columnIndex10) ?: "null",

                        cursor.getLong(columnIndex11),

                        cursor.getString(columnIndex12),
                        cursor.getLong(columnIndex13),
                        cursor.getString(columnIndex14),
                        cursor.getString(columnIndex15),
                        cursor.getLong(columnIndex16),
                        cursor.getLong(columnIndex17),
                        cursor.getString(columnIndex18),
                        cursor.getInt(columnIndex19),
                        cursor.getInt(columnIndex20)
                    )
                    images.add(img)
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        if (mPhotoPickerAdapter.pageIndex == 0) {
            setData(images)
        } else {
            appendData(images)
        }
    }

    /**
     * 设置首页数据
     */
    fun setData(list: MutableList<PhotoPicker>) {
        handle.post {
            mPhotoPickerAdapter.pageCount = mPhotoPickerAdapter.pageCount
            mPhotoPickerAdapter.pageIndex = mPhotoPickerAdapter.pageIndex + 1
            mPhotoPickerAdapter.refreshing.value = false
            mPhotoPickerAdapter.result.value = true
            if (list.size != mPhotoPickerAdapter.pageCount) {
                //加载完成
                mPhotoPickerAdapter.complete = true
            }
            mPhotoPickerAdapter.setData(list)
        }
    }

    /**
     * 加载更多数据
     */
    fun appendData(list: MutableList<PhotoPicker>) {
        handle.post {
            mPhotoPickerAdapter.pageIndex = mPhotoPickerAdapter.pageIndex + 1
            mPhotoPickerAdapter.loading.value = false
            mPhotoPickerAdapter.result.value = true
            if (list.size != mPhotoPickerAdapter.pageCount) {
                //加载完成
                mPhotoPickerAdapter.complete = true
            }
            mPhotoPickerAdapter.appendData(list)
        }
    }
    /**
     * 创建菜单
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.picker_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (mPickerCount == 1) {
            menu?.findItem(R.id.menu_ok)?.isVisible = false
        }
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * 菜单点击事件
     *
     * @param item
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == android.R.id.home) {
            finish()
        } else if (i == R.id.menu_ok) {
            val uris = mPhotoPickerAdapter.pickerUris
            val imgs = mPhotoPickerAdapter.pickerPaths
            if (!imgs.isNullOrEmpty()) {
                val intent = Intent()
                intent.putParcelableArrayListExtra(ARG_RESULT_URI, uris)
                intent.putStringArrayListExtra(ARG_RESULT, imgs)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this, "至少选择一张图片", Toast.LENGTH_SHORT).show()
            }
        } else if (i == R.id.menu_system) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, REQUEST_FOLDER)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_FOLDER -> {
                    val path = ArrayList<String>()
                    val uri = ArrayList<Uri>()
                    val intent = Intent()
                    val p = PhotoFileUtil.getFileAbsolutePath(this, data!!.data!!)

                    val suffix = PhotoFileUtil.getSuffix(p!!)
                    //获取文件后缀
                    val mimeTypeMap = MimeTypeMap.getSingleton()

                    if (mMimeType.contains(mimeTypeMap.getMimeTypeFromExtension(suffix))) {
                        path.add(p)
                        uri.add(data.data)
                        intent.putStringArrayListExtra(ARG_RESULT, path)
                        intent.putParcelableArrayListExtra(ARG_RESULT_URI, uri)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    } else {
                        Toast.makeText(this, "非图片类型文件(jpg、png、gif)", Toast.LENGTH_SHORT).show()
                    }
                }

                REQUEST_PHOTO_VIEW -> {
                    val pickerPath = data!!.getStringArrayListExtra(PhotoViewActivity.ARG_PICKER_PATHS)
                    mPhotoPickerAdapter.pickerPaths = pickerPath
                    mPhotoPickerAdapter.notifyDataSetChanged()
                    setToolbarTitle(pickerPath.size, mSelectFolderName)
                }
            }
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
        private val REQUEST_PHOTO_VIEW = 434
        private val REQUEST_FOLDER = 525
        val ARG_RESULT = "RESULT" //结果图片路径集合
        val ARG_RESULT_URI = "RESULT_URI" //结果图片路径Uri集合
        val ARG_PICKER_COUNT = "PICKER_COUNT" //所选图片数量
    }
}