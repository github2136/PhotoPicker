package com.github2136.photopicker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by yb on 2018/10/29.
 */
abstract class PhotoBaseAdapter<T>(var list: MutableList<T>? = null) : RecyclerView.Adapter<PhotoVH>() {
    protected lateinit var mLayoutInflater: LayoutInflater

    /**
     * 通过类型获得布局ID
     *
     * @param viewType
     * @return
     */
    @androidx.annotation.LayoutRes
    abstract fun getLayoutId(viewType: Int): Int

    protected abstract fun onBindView(t: T, holder: PhotoVH, position: Int)

    /**
     * 获得对象
     */
    open fun getItem(position: Int): T? {
        return list?.get(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoVH {
        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }
        val v = mLayoutInflater.inflate(getLayoutId(viewType), parent, false)
        return PhotoVH(v, itemClickListener, itemLongClickListener)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    override fun onBindViewHolder(holder: PhotoVH, position: Int) {
        getItem(position)?.let {
            onBindView(it, holder, position)
        }
    }

    protected var itemClickListener: ((Int) -> Unit)? = null
    protected var itemLongClickListener: ((Int) -> Unit)? = null
    protected var viewClickListener: ((position: Int, id: Int) -> Unit)? = null

    fun setOnItemClickListener(itemClickListener: (position: Int) -> Unit) {
        this.itemClickListener = itemClickListener
    }

    fun setOnItemLongClickListener(itemLongClickListener: (position: Int) -> Unit) {
        this.itemLongClickListener = itemLongClickListener
    }

    fun setOnViewClickListener(viewClickListener: (position: Int, id: Int) -> Unit) {
        this.viewClickListener = viewClickListener
    }

    fun setData(list: MutableList<T>) {
        this.list = list
        notifyDataSetChanged()
    }

    fun appendData(list: List<T>) {
        this.list?.let {
            it.addAll(list)
            notifyDataSetChanged()
        }
    }
}