package com.github2136.photopicker.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.chrisbanes.photoview.PhotoView
import com.github2136.photopicker.R
import com.github2136.photopicker.other.ImageLoaderInstance

class PhotoFragment : Fragment() {
    private var mListener: OnFragmentInteractionListener? = null
    private var photoView: com.github2136.photopicker.entity.PhotoEntity? = null
    private var ivPhoto: PhotoView? = null
    private var tvPhotoInfo: TextView? = null

    internal var mOnClickListener: View.OnClickListener = View.OnClickListener { onFragmentClick() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photoView = arguments?.getParcelable(ARG_PHOTO_VIEW)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(requireContext().toString() + " must implement OnFragmentInteractionListener")
        }
    }

    fun onFragmentClick() {
        if (mListener != null) {
            mListener!!.onFragmentClick()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_photo, container, false)
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ivPhoto = view.findViewById<View>(R.id.iv_photo) as PhotoView
        tvPhotoInfo = view.findViewById<TextView>(R.id.tv_photo_info) as TextView
        val loader = ImageLoaderInstance.getInstance(requireContext())!!.imageLoader
        if (loader!!.supportAnimatedGif()) {
            loader.loadAnimatedGifImage(requireContext(), ivPhoto!!, photoView?.photoPath ?: "")
        } else {
            loader.loadImage(requireContext(), ivPhoto!!, photoView?.photoPath ?: "")
        }
        if (!TextUtils.isEmpty(photoView?.photoInfo)) {
            tvPhotoInfo?.also { photo ->
                photo.visibility = View.VISIBLE
                photo.text = photoView?.photoInfo
                photo.setOnClickListener {
                    if (photo.ellipsize == TextUtils.TruncateAt.END) {
                        photo.maxLines = Int.MAX_VALUE
                        photo.ellipsize = null
                    } else {
                        photo.maxLines = 1
                        photo.ellipsize = TextUtils.TruncateAt.END
                    }
                }
            }
        }
        ivPhoto!!.setOnClickListener(mOnClickListener)
    }

    interface OnFragmentInteractionListener {
        fun onFragmentClick()
    }

    companion object {
        const val ARG_PHOTO_VIEW = "PHOTO_VIEW" //显示的图片对象优先级更高

        fun newInstance(photoView: com.github2136.photopicker.entity.PhotoEntity): PhotoFragment {
            val fragment = PhotoFragment()
            val args = Bundle()
            args.putParcelable(ARG_PHOTO_VIEW, photoView)
            fragment.arguments = args
            return fragment
        }
    }
}
