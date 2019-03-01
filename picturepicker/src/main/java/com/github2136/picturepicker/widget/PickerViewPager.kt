package com.github2136.picturepicker.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import com.github2136.picturepicker.R

import androidx.viewpager.widget.ViewPager

/**
 * Created by yb on 2017/9/20.
 */

class PickerViewPager : ViewPager {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        try {
            return super.onInterceptTouchEvent(ev)
        } catch (e: Exception) {
        }
        return false
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        try {
            return super.onTouchEvent(ev)
        } catch (ex: IllegalArgumentException) {
        }
        return false
    }
}