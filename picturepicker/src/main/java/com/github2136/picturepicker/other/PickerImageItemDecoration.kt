package com.github2136.picturepicker.other

import android.graphics.Rect
import android.view.View

import androidx.recyclerview.widget.RecyclerView


/**
 * Created by yb on 2017/9/7.
 */

class PickerImageItemDecoration(private val mSpanCount: Int, private val mSpacing: Int, private val mIncludeEdge: Boolean) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view) // item position
        val column = position % mSpanCount // item colum
        if (mIncludeEdge) {
            outRect.left = mSpacing - column * mSpacing / mSpanCount
            outRect.right = (column + 1) * mSpacing / mSpanCount
            if (position < mSpanCount) { // top edge
                outRect.top = mSpacing
            }
            outRect.bottom = mSpacing // item bottom
        } else {
            outRect.left = column * mSpacing / mSpanCount
            outRect.right = mSpacing - (column + 1) * mSpacing / mSpanCount
            if (position >= mSpanCount) {
                outRect.top = mSpacing // item top
            }
        }
    }
}