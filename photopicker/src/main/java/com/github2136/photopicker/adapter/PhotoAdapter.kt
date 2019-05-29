package com.github2136.photopicker.adapter

import com.github2136.photopicker.fragment.PhotoFragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * Created by yb on 2017/9/20.
 */

class PhotoAdapter(fm: FragmentManager, private val mPhotoPath: List<String>) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return PhotoFragment.newInstance(mPhotoPath[position])
    }

    override fun getCount(): Int {
        return mPhotoPath.size
    }
}
