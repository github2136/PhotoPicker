package com.github2136.picturepicker.activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.github2136.picturepicker.R;
import com.github2136.util.CommonUtil;
import com.github2136.util.FileUtil;
import com.github2136.util.SPUtil;

import java.io.File;

/**
 * 图片裁剪<br>
 * ARG_CROP_IMG 需要裁剪的图片路径<br>
 * ARG_ASPECT_X/ARG_ASPECT_Y裁剪框比例<br>
 * ARG_OUTPUT_X/ARG_OUTPUT_Y图片输出尺寸<br>
 * 默认存储只外部私有图片目录下，或在application中添加name为picture_picker_path的&lt;meta&#62;，私有目录下的图片不能添加到媒体库中，选择图片时将会无法查看到<br>
 * OUTPUT_IMG图片保存路径目录，不包括文件名，优先级比picture_picker_path高，可不填<br>
 * ARG_RESULT返回的图片路径
 */
public class CropActivity extends AppCompatActivity {
    public static final String ARG_RESULT = "RESULT";
    public static final String ARG_CROP_IMG = "CROP_IMG";
    public static final String ARG_ASPECT_X = "ASPECT_X";
    public static final String ARG_ASPECT_Y = "ASPECT_Y";
    public static final String ARG_OUTPUT_X = "OUTPUT_X";
    public static final String ARG_OUTPUT_Y = "OUTPUT_Y";
    public static final String ARG_OUTPUT_IMG = "OUTPUT_IMG";
    private static final int REQUEST_CROP = 742;
    private static final String KEY_FILE_NAME = "FILE_NAME";
    private SPUtil mSpUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        mSpUtil = SPUtil.getInstance(this, getClass().getSimpleName());
        if (!(getIntent().hasExtra(ARG_CROP_IMG) &&
                getIntent().hasExtra(ARG_ASPECT_X) &&
                getIntent().hasExtra(ARG_ASPECT_Y) &&
                getIntent().hasExtra(ARG_OUTPUT_X) &&
                getIntent().hasExtra(ARG_OUTPUT_Y))) {
            Toast.makeText(this, "缺少参数", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            String img = getIntent().getStringExtra(ARG_CROP_IMG);
            int aspX = getIntent().getIntExtra(ARG_ASPECT_X, 0);
            int aspY = getIntent().getIntExtra(ARG_ASPECT_Y, 0);
            int outX = getIntent().getIntExtra(ARG_OUTPUT_X, 0);
            int outY = getIntent().getIntExtra(ARG_OUTPUT_Y, 0);
            File outImg;
            if (getIntent().hasExtra(ARG_OUTPUT_IMG)) {
                String out = getIntent().getStringExtra(ARG_OUTPUT_IMG);
                outImg = new File(FileUtil.getExternalStorageRootPath() + File.separator + out, FileUtil.createFileName(".jpg"));
            } else {
                outImg = new File(getPhotoPath(), FileUtil.createFileName(".jpg"));
            }
            if (!outImg.getParentFile().exists()) {
                outImg.getParentFile().mkdirs();
            }
            mSpUtil.edit().putValue(KEY_FILE_NAME, outImg.getPath()).apply();
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(Uri.fromFile(new File(img)), "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", aspX);
            intent.putExtra("aspectY", aspY);
            intent.putExtra("outputX", outX);
            intent.putExtra("outputY", outY);
            intent.putExtra("scale", true);// 如果选择的图小于裁剪大小则进行放大
            intent.putExtra("scaleUpIfNeeded", true);// 如果选择的图小于裁剪大小则进行放大
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outImg));
            intent.putExtra("return-data", false);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("noFaceDetection", true); // no face detection
            startActivityForResult(intent, REQUEST_CROP);
        }
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
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
            data = new Intent();
            data.putExtra(ARG_RESULT, fileName);
            setResult(RESULT_OK, data);
        }
        finish();
    }
}
