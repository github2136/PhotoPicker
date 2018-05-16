package com.github2136.picturepicker.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github2136.base.BaseRecyclerAdapter;
import com.github2136.base.ViewHolderRecyclerView;
import com.github2136.picturepicker.R;
import com.github2136.picturepicker.entity.PicturePicker;
import com.github2136.picturepicker.other.ImageLoader;
import com.github2136.picturepicker.other.ImageLoaderInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yb on 2017/8/26.
 */

public class PicturePickerAdapter extends BaseRecyclerAdapter<PicturePicker> {
    private ArrayList<String> mPickerPaths;
    private int mViewSize;
    private int mImgSize;
    private int mSelectCount;
    private RelativeLayout.LayoutParams layoutParams;
    private OnSelectChangeCallback mOnSelectChangeCallback;

    public PicturePickerAdapter(Context context, List<PicturePicker> list, int selectCount) {
        super(context, list);
        mViewSize = (context.getResources().getDisplayMetrics().widthPixels - 5 * 4) / 3;
        mImgSize = mViewSize;
        layoutParams = new RelativeLayout.LayoutParams(mViewSize, mViewSize);
        mPickerPaths = new ArrayList<>();
        mSelectCount = selectCount;
    }

    public void setOnSelectImageCallback(OnSelectChangeCallback onSelectImageCallback) {
        this.mOnSelectChangeCallback = onSelectImageCallback;
    }

    public void setPickerPaths(ArrayList<String> mPickerPaths) {
        this.mPickerPaths = mPickerPaths;
    }

    public ArrayList<String> getPickerPaths() {
        return mPickerPaths;
    }

    @Override
    public int getLayoutId(int viewType) {
        return R.layout.item_picture_picker;
    }

    @Override
    protected void onBindView(final PicturePicker picturePicker, ViewHolderRecyclerView holder, final int position) {
        ImageView ivImage = holder.getView(R.id.iv_image);
        final ImageButton ibCheck = holder.getView(R.id.ib_check);
        if (mSelectCount == 1) {
            ibCheck.setVisibility(View.GONE);
        }
        ivImage.setLayoutParams(layoutParams);
        ImageLoader loader = ImageLoaderInstance.getInstance(mContext).getImageLoader();
        if (loader.supportAnimatedGifThumbnail()) {
            loader.loadThumbnail(mContext, mImgSize, mImgSize, ivImage, picturePicker.getData());
        } else {
            loader.loadAnimatedGifThumbnail(mContext, mImgSize, mImgSize, ivImage, picturePicker.getData());
        }
        if (mPickerPaths.contains(picturePicker.getData())) {
            ibCheck.setImageResource(R.drawable.ic_picker_check_box);
        } else {
            ibCheck.setImageResource(R.drawable.ic_picker_check_box_outline);
        }
        ibCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPickerPaths.contains(picturePicker.getData())) {
                    mPickerPaths.remove(picturePicker.getData());
                    ibCheck.setImageResource(R.drawable.ic_picker_check_box_outline);
                } else {
                    if (mSelectCount > mPickerPaths.size()) {
                        mPickerPaths.add(picturePicker.getData());
                        ibCheck.setImageResource(R.drawable.ic_picker_check_box);
                    } else {
                        Toast.makeText(mContext, "最多选择 " + mSelectCount + " 张", Toast.LENGTH_SHORT).show();
                    }
                }
                if (mOnSelectChangeCallback != null) {
                    mOnSelectChangeCallback.selectChange(mPickerPaths.size());
                }
            }
        });
    }

    public List<PicturePicker> getItem() {
        return mList;
    }

    public void clearSelectPaths() {
        mPickerPaths = new ArrayList<>();
    }

    public interface OnSelectChangeCallback {
        void selectChange(int selectCount);
    }
}
