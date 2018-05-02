package com.github2136.picturepicker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.github2136.picturepicker.R;
import com.github2136.picturepicker.adapter.PhotoAdapter;
import com.github2136.picturepicker.fragment.PhotoFragment;
import com.github2136.picturepicker.widget.SelectViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * 查看图片
 */
public class PictureViewActivity extends AppCompatActivity implements PhotoFragment.OnFragmentInteractionListener {
    public static final String ARG_PICTURES = "PICTURES";
    public static final String ARG_CURRENT_INDEX = "CURRENT_INDEX";
    public static final String ARG_PICKER_COUNT = "PICKER_COUNT";//所选图片数量
    public static final String ARG_PICKER_PATHS = "PICKER_PATHS";//所选图片路径
    private List<String> mPhotoPaths;
    private ArrayList<String> mPickerPaths;
    private int mPickerCount;
    private int mCurrentIndex;
    private SelectViewPager vpPhoto;
    private TextView tvTitle;
    private ImageButton ibCheck;
    private LinearLayout llCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker_view);
        mPhotoPaths = getIntent().getStringArrayListExtra(ARG_PICTURES);
        mCurrentIndex = getIntent().getIntExtra(ARG_CURRENT_INDEX, 0);

        mPickerPaths = getIntent().getStringArrayListExtra(ARG_PICKER_PATHS);
        mPickerCount = getIntent().getIntExtra(ARG_PICKER_COUNT, 0);

        Toolbar tbTitle = (Toolbar) findViewById(R.id.tb_title);
        setSupportActionBar(tbTitle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        vpPhoto = (SelectViewPager) findViewById(R.id.vp_photo);
        vpPhoto.setAdapter(new PhotoAdapter(getSupportFragmentManager(), mPhotoPaths));
        vpPhoto.setCurrentItem(mCurrentIndex);
        vpPhoto.addOnPageChangeListener(mPagerChangeListener);

        tvTitle = (TextView) findViewById(R.id.tv_title);
        ibCheck = (ImageButton) findViewById(R.id.ib_check);
        llCheck = (LinearLayout) findViewById(R.id.ll_check);
        ibCheck.setOnClickListener(mOnClickListener);
        setTitle();
        if (mPickerPaths != null) {
            llCheck.setVisibility(View.VISIBLE);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String currentPath = mPhotoPaths.get(vpPhoto.getCurrentItem());
            if (mPickerPaths.contains(currentPath)) {
                mPickerPaths.remove(currentPath);
            } else {
                if (mPickerCount > mPickerPaths.size()) {
                    mPickerPaths.add(currentPath);
                } else {
                    Toast.makeText(PictureViewActivity.this, "最多选择 " + mPickerCount + " 张", Toast.LENGTH_SHORT).show();
                }
            }
            setBottom();
        }
    };

    private void setTitle() {
        setTitle(String.format("%d/%d", vpPhoto.getCurrentItem() + 1, mPhotoPaths.size()));//标题
        setBottom();
    }

    private void setBottom() {
        if (mPickerPaths.contains(mPhotoPaths.get(vpPhoto.getCurrentItem()))) {
            ibCheck.setImageResource(R.drawable.ic_select_checkbox_check);
        } else {
            ibCheck.setImageResource(R.drawable.ic_select_checkbox_uncheck);
        }
        tvTitle.setText(String.format("%d/%d", mPickerPaths.size(), mPickerCount));
    }

    ViewPager.SimpleOnPageChangeListener mPagerChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            setTitle();
        }
    };

    /**
     * 菜单点击事件
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setPickerPath();
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onFragmentClick() {
        if (mPickerPaths != null) {
            if (llCheck.getVisibility() == View.VISIBLE) {
                TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, llCheck.getHeight());
                translateAnimation.setDuration(100);
                llCheck.setAnimation(translateAnimation);
                llCheck.setVisibility(View.GONE);
            } else {
                TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, llCheck.getHeight(), 0);
                translateAnimation.setDuration(100);
                llCheck.setAnimation(translateAnimation);
                llCheck.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        setPickerPath();
        super.onBackPressed();
    }

    private void setPickerPath() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(ARG_PICKER_PATHS, mPickerPaths);
        setResult(RESULT_OK, intent);
    }
}