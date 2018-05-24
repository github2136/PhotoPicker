package com.github2136.picker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github2136.picturepicker.activity.CaptureActivity;
import com.github2136.picturepicker.activity.CropActivity;
import com.github2136.picturepicker.activity.PicturePickerActivity;
import com.github2136.picturepicker.activity.PictureViewActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> selectPaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnImg = (Button) findViewById(R.id.im_select_img);
        btnImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PicturePickerActivity.class);
                intent.putExtra(PicturePickerActivity.ARG_PICKER_COUNT, 1);
                startActivityForResult(intent, 1);
            }
        });
        Button btnImgs = (Button) findViewById(R.id.im_select_imgs);
        btnImgs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PicturePickerActivity.class);
                intent.putExtra(PicturePickerActivity.ARG_PICKER_COUNT, 5);
                startActivityForResult(intent, 1);
            }
        });
        Button btnCapture = (Button) findViewById(R.id.im_capture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                intent.putExtra(CaptureActivity.ARG_FILE_PATH, "fffff");
                startActivityForResult(intent, 2);
            }
        });
        Button btnPreview = (Button) findViewById(R.id.im_preview);
        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PictureViewActivity.class);
                ArrayList<String> path = new ArrayList<>();
                path.add("/storage/emulated/legacy/Pictures/FireServices/image_20171221_160400.jpg");
                path.add("/storage/emulated/legacy/Pictures/FireServices/image_20171221_160831.jpg");
                path.add("/storage/emulated/legacy/com.kuntu.mobile.fireservices.unit/20171221_160733320.jpg");

                intent.putStringArrayListExtra(PictureViewActivity.ARG_PICTURES, path);
                startActivityForResult(intent, 2);
            }
        });
        Button btnSelectImgsPicker = (Button) findViewById(R.id.im_select_imgs_picker);
        btnSelectImgsPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PicturePickerActivity.class);
                intent.putExtra(PicturePickerActivity.ARG_PICKER_COUNT, 5);
                if (selectPaths != null) {
                    intent.putStringArrayListExtra(PicturePickerActivity.ARG_PICKER_PATHS, selectPaths);
                }
                startActivityForResult(intent, 3);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1: {
                    ArrayList<String> result = data.getStringArrayListExtra(PicturePickerActivity.ARG_RESULT);
                    for (int i = 0; i < result.size(); i++) {
                        String s = result.get(i);
                        Log.e("path", s);
                    }
                }
                break;
                case 2: {
                    String result = data.getStringExtra(CaptureActivity.ARG_RESULT);
                    Intent intent = new Intent(MainActivity.this, CropActivity.class);
                    intent.putExtra(CropActivity.ARG_CROP_IMG, result);
                    intent.putExtra(CropActivity.ARG_ASPECT_X, 1);
                    intent.putExtra(CropActivity.ARG_ASPECT_Y, 1);
                    intent.putExtra(CropActivity.ARG_OUTPUT_X, 200);
                    intent.putExtra(CropActivity.ARG_OUTPUT_Y, 200);
                    intent.putExtra(CropActivity.ARG_OUTPUT_IMG, "fffsdf");
                    startActivityForResult(intent, 3);
                }
                break;
                case 3: {
                    selectPaths = data.getStringArrayListExtra(PicturePickerActivity.ARG_RESULT);
                }
                break;
            }

        }
    }
}
