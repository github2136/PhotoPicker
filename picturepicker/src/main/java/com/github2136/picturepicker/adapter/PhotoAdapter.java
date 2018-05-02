package com.github2136.picturepicker.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.github2136.picturepicker.fragment.PhotoFragment;

import java.util.List;

/**
 * Created by yubin on 2017/9/20.
 */

public class PhotoAdapter extends FragmentStatePagerAdapter {
    private List<String> mPhotoPath;

    public PhotoAdapter(FragmentManager fm, List<String> photoPath) {
        super(fm);
        mPhotoPath = photoPath;
    }

    @Override
    public Fragment getItem(int position) {
        return PhotoFragment.newInstance(mPhotoPath.get(position));
    }

    @Override
    public int getCount() {
        return mPhotoPath.size();
    }
}
