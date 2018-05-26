package com.github2136.picturepicker.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.github2136.picturepicker.R;
import com.github2136.util.FileUtil;
import com.github2136.util.SPUtil;

import java.io.File;

/**
 * 图片拍摄<br>
 * 默认存储只外部私有图片目录下，或在application中添加name为picture_picker_path的&lt;meta&#62;，私有目录下的图片不能添加到媒体库中，选择图片时将会无法查看到<br>
 * ARG_FILE_PATH图片保存路径目录，不包括文件名，优先级比picture_picker_path高，可不填<br>
 * ARG_RESULT返回的图片路径
 */
public class CaptureActivity extends AppCompatActivity {
    public static final String ARG_RESULT = "RESULT";
    public static final String ARG_FILE_PATH = "FILE_PATH";//图片保存路径

    private static final int REQUEST_CAPTURE = 706;
    private static final String KEY_FILE_NAME = "FILE_NAME";
    private SPUtil mSpUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        mSpUtil = SPUtil.getInstance(this, getClass().getSimpleName());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String filePath;
        File file;
        if (getIntent().hasExtra(ARG_FILE_PATH)) {
            filePath = getIntent().getStringExtra(ARG_FILE_PATH);
            file = new File(FileUtil.getExternalStorageRootPath() + File.separator + filePath, FileUtil.createFileName(".jpg"));
        } else {
            file = new File(getPhotoPath(), FileUtil.createFileName(".jpg"));
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        Uri mShootUri = getUri(file);
        mSpUtil.edit().putValue(KEY_FILE_NAME, file.getPath()).apply();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mShootUri);
        startActivityForResult(intent, REQUEST_CAPTURE);
    }

    private String getPhotoPath() {
        String mPhotoPath = null;
        try {
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = applicationInfo.metaData;
            if (metaData != null) {
                mPhotoPath = metaData.getString("picture_picker_path");
                if (!TextUtils.isEmpty(mPhotoPath)) {
                    mPhotoPath = FileUtil.getExternalStorageRootPath() + File.separator + mPhotoPath;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(mPhotoPath)) {
            mPhotoPath = FileUtil.getExternalStoragePrivatePicPath(this);
        }
        return mPhotoPath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            String fileName = mSpUtil.getString(KEY_FILE_NAME);
            File f = new File(fileName);
            Uri contentUri =getUri(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
            data = new Intent();
            data.putExtra(ARG_RESULT, fileName);
            setResult(RESULT_OK, data);
        }
        finish();
    }

    /**
     * 返回不同的图片uri
     *
     * @param file
     * @return
     */
    private Uri getUri(File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(file);
        } else {
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        }
        return uri;
    }
}
