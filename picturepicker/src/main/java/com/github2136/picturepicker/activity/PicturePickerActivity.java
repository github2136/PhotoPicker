package com.github2136.picturepicker.activity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.github2136.base.BaseRecyclerAdapter;
import com.github2136.picturepicker.R;
import com.github2136.picturepicker.adapter.PicturePickerAdapter;
import com.github2136.picturepicker.adapter.SpinnerAdapter;
import com.github2136.picturepicker.entity.PicturePicker;
import com.github2136.picturepicker.other.SelectImageItemDecoration;
import com.github2136.util.CollectionsUtil;
import com.github2136.util.FileUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 选择图片<br>
 * ARG_SELECT_COUNT选择图片数量<br>
 * ARG_PICKER_COUNT返回的图片路径
 */
public class PicturePickerActivity extends AppCompatActivity {
    private static final int REQUEST_PICTURE_VIEW = 434;
    private static final int REQUEST_FOLDER = 525;
    public static final String ARG_RESULT = "RESULT";//结果图片路径集合
    public static final String ARG_PICKER_COUNT = "PICKER_COUNT";//所选图片数量
    private List<String> mFolderName;//文件夹名称
    private Map<String, List<PicturePicker>> mFolderPath;//文件夹名称对应图片
    private int mPickerCount;//可选择图片数量
    private PicturePickerAdapter mPicturePickerAdapter;
    private Set<String> mMimeType = new HashSet<>();
    private AlertDialog mFolderDialog;
    private String mSelectFolderName;
    private Button btnFolder, btnPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_util_select_image);
        Toolbar tbTitle = (Toolbar) findViewById(R.id.tb_title);
        btnFolder = (Button) findViewById(R.id.btn_folder);
        btnPreview = (Button) findViewById(R.id.btn_preview);
        btnFolder.setOnClickListener(mOnClickListener);
        btnPreview.setOnClickListener(mOnClickListener);
        setSupportActionBar(tbTitle);
        mPickerCount = getIntent().getIntExtra(ARG_PICKER_COUNT, 0);
        mSelectFolderName = "*";
        setToolbarTitle(0, mSelectFolderName);
        mMimeType.add("image/jpeg");
        mMimeType.add("image/png");
        mMimeType.add("image/gif");

        // getSupportActionBar().setToolbarTitle("标题");
        // getSupportActionBar().setSubtitle("副标题");
        // getSupportActionBar().setLogo(R.drawable.ic_launcher);

        /* 菜单的监听可以在toolbar里设置，也可以像ActionBar那样，通过Activity的onOptionsItemSelected回调方法来处理 */
//        tbTitle.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView rvImages = (RecyclerView) findViewById(R.id.rv_images);
        rvImages.setHasFixedSize(true);
        SelectImageItemDecoration selectImageItemDecoration = new SelectImageItemDecoration(3, 5, true);
        rvImages.addItemDecoration(selectImageItemDecoration);

        mFolderName = new ArrayList<>();
        mFolderName.add("*");//表示全部
        mFolderPath = new HashMap<>();

        getImages();
        mPicturePickerAdapter = new PicturePickerAdapter(this, mFolderPath.get("*"), mPickerCount);
        rvImages.setAdapter(mPicturePickerAdapter);
        mPicturePickerAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter baseRecyclerAdapter, int i) {
                Intent intent = new Intent(PicturePickerActivity.this, PictureViewActivity.class);
                ArrayList<String> path = new ArrayList<>();
                for (PicturePicker img : mPicturePickerAdapter.getItem()) {
                    path.add(img.getData());
                }
                intent.putStringArrayListExtra(PictureViewActivity.ARG_PICTURES, path);
                intent.putExtra(PictureViewActivity.ARG_CURRENT_INDEX, i);

                intent.putStringArrayListExtra(PictureViewActivity.ARG_PICKER_PATHS, mPicturePickerAdapter.getPickerPaths());
                intent.putExtra(PictureViewActivity.ARG_PICKER_COUNT, mPickerCount);

                startActivityForResult(intent, REQUEST_PICTURE_VIEW);
            }
        });
        mPicturePickerAdapter.setOnSelectImageCallback(new PicturePickerAdapter.OnSelectChangeCallback() {
            @Override
            public void selectChange(int selectCount) {
                setToolbarTitle(selectCount, mSelectFolderName);
            }
        });
        SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.item_spinner, mFolderName);
        mFolderDialog = new AlertDialog.Builder(this)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectFolderName = mFolderName.get(which);
                        mPicturePickerAdapter.setData(mFolderPath.get(mSelectFolderName));
//                        mPicturePickerAdapter.clearSelectPaths();
                        mPicturePickerAdapter.notifyDataSetChanged();
//                        setToolbarTitle(0, mSelectFolderName);
                        setToolbarTitle(mPicturePickerAdapter.getPickerPaths().size(), mSelectFolderName);
                    }
                }).create();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_folder) {
                mFolderDialog.show();
            } else if (v.getId() == R.id.btn_preview) {
                Intent intent = new Intent(PicturePickerActivity.this, PictureViewActivity.class);

                intent.putStringArrayListExtra(PictureViewActivity.ARG_PICTURES, mPicturePickerAdapter.getPickerPaths());
                intent.putExtra(PictureViewActivity.ARG_CURRENT_INDEX, 0);

                intent.putStringArrayListExtra(PictureViewActivity.ARG_PICKER_PATHS, mPicturePickerAdapter.getPickerPaths());
                intent.putExtra(PictureViewActivity.ARG_PICKER_COUNT, mPickerCount);

                startActivityForResult(intent, REQUEST_PICTURE_VIEW);
            }
        }
    };

    private void setToolbarTitle(int selectCount, String folderName) {
        if (folderName.equals("*")) {
            folderName = "全部";
        }
        setTitle(String.format("%d/%d", selectCount, mPickerCount));//标题
        btnFolder.setText(folderName);
        if (selectCount > 0) {
            btnPreview.setEnabled(true);
        } else {
            btnPreview.setEnabled(false);
        }
    }

    private void getImages() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().startsWith("Permission Denial")) {
                new AlertDialog
                        .Builder(this)
                        .setTitle("警告")
                        .setMessage("没有外部存储目录读取权限")
                        .setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            } else {
                Toast.makeText(this, "图片数据获取失败", Toast.LENGTH_SHORT).show();
            }
            //Permission Denial: reading com.android.providers.media.MediaProvider uri content://media/external/images/media from pid=4833,
            // uid=10059 requires android.permission.READ_EXTERNAL_STORAGE, or grantUriPermission()
        }
        List<PicturePicker> images = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex1 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DESCRIPTION);
                int columnIndex2 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.PICASA_ID);
                int columnIndex3 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.IS_PRIVATE);
                int columnIndex4 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.LATITUDE);
                int columnIndex5 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.LONGITUDE);
                int columnIndex6 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN);
                int columnIndex7 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION);
                int columnIndex8 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC);
                int columnIndex9 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID);
                int columnIndex10 = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);

                int columnIndex11 = cursor.getColumnIndex(MediaStore.MediaColumns._ID);

                int columnIndex12 = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                int columnIndex13 = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE);
                int columnIndex14 = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                int columnIndex15 = cursor.getColumnIndex(MediaStore.MediaColumns.TITLE);
                int columnIndex16 = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED);
                int columnIndex17 = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED);
                int columnIndex18 = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE);
                int columnIndex19 = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH);
                int columnIndex20 = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT);

                do {
                    PicturePicker img = new PicturePicker();
                    img.setDescription(cursor.getString(columnIndex1));
                    img.setPicasa_id(cursor.getString(columnIndex2));
                    img.setIs_private(cursor.getInt(columnIndex3));
                    img.setLatitude(cursor.getDouble(columnIndex4));
                    img.setLongitude(cursor.getDouble(columnIndex5));
                    img.setDate_taken(cursor.getInt(columnIndex6));
                    img.setOrientation(cursor.getInt(columnIndex7));
                    img.setMini_thumb_magic(cursor.getInt(columnIndex8));
                    img.setBucket_id(cursor.getString(columnIndex9));
                    img.setBucket_display_name(cursor.getString(columnIndex10));

                    img.set_id(cursor.getLong(columnIndex11));

                    img.setData(cursor.getString(columnIndex12));
                    img.setSize(cursor.getLong(columnIndex13));
                    img.setDisplay_name(cursor.getString(columnIndex14));
                    img.setTitle(cursor.getString(columnIndex15));
                    img.setDate_added(cursor.getLong(columnIndex16));
                    img.setDate_modified(cursor.getLong(columnIndex17));
                    img.setMime_type(cursor.getString(columnIndex18));
                    img.setWidth(cursor.getInt(columnIndex19));
                    img.setHeight(cursor.getInt(columnIndex20));

                    int index2 = img.getData().lastIndexOf("/");
                    int index1 = img.getData().substring(0, index2).lastIndexOf("/");
                    String folderName = img.getData().substring(index1 + 1, index2);
                    if (!mFolderName.contains(folderName)) {
                        mFolderName.add(folderName);
                    }
                    List<PicturePicker> imgs;
                    if (mFolderPath.containsKey(folderName)) {
                        imgs = mFolderPath.get(folderName);
                    } else {
                        imgs = new ArrayList<>();
                        mFolderPath.put(folderName, imgs);
                    }
                    imgs.add(img);
                    images.add(img);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        mFolderPath.put("*", images);
    }

    /**
     * 创建菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 菜单点击事件
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.menu_ok) {
            ArrayList<String> imgs = mPicturePickerAdapter.getPickerPaths();
            if (CollectionsUtil.isNotEmpty(imgs)) {
                Intent intent = new Intent();
                intent.putStringArrayListExtra(ARG_RESULT, mPicturePickerAdapter.getPickerPaths());
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, "至少选择一张图片", Toast.LENGTH_SHORT).show();
            }
        } /*else if (i == R.id.menu_folder) {
            mFolderDialog.show();
        }*/ else if (i == R.id.menu_system) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_FOLDER);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_FOLDER:
                    ArrayList<String> path = new ArrayList<>();
                    Intent intent = new Intent();
                    String p = FileUtil.getFileAbsolutePath(this, data.getData());

                    String suffix = FileUtil.getSuffix(p);
                    //获取文件后缀
                    MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

                    if (mMimeType.contains(mimeTypeMap.getMimeTypeFromExtension(suffix))) {
                        path.add(p);
                        intent.putStringArrayListExtra(ARG_RESULT, path);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        Toast.makeText(this, "非图片类型文件(jpg、png、gif)", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case REQUEST_PICTURE_VIEW:
                    ArrayList<String> pickerPath = data.getStringArrayListExtra(PictureViewActivity.ARG_PICKER_PATHS);
                    mPicturePickerAdapter.setPickerPaths(pickerPath);
                    mPicturePickerAdapter.notifyDataSetChanged();
                    setToolbarTitle(pickerPath.size(), mSelectFolderName);
                    break;
            }
        }
    }
}