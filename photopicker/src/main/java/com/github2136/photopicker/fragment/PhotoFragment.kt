package com.github2136.photopicker.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.github.chrisbanes.photoview.PhotoView
import com.github2136.photopicker.R
import com.github2136.photopicker.other.ImageLoaderInstance
import androidx.fragment.app.Fragment

class PhotoFragment : Fragment() {
    private var mListener: OnFragmentInteractionListener? = null
    private var photoPath: String? = null
    private var ivPhoto: PhotoView? = null

    internal var mOnClickListener: View.OnClickListener = View.OnClickListener { onFragmentClick() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            photoPath = arguments!!.getString(ARG_PHOTO_PATH)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
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
        val loader = ImageLoaderInstance.getInstance(context!!)!!.imageLoader
        if (loader!!.supportAnimatedGif()) {
            loader.loadAnimatedGifImage(context!!, ivPhoto!!, photoPath!!)
        } else {
            loader.loadImage(context!!, ivPhoto!!, photoPath!!)
        }
        ivPhoto!!.setOnClickListener(mOnClickListener)
    }

    interface OnFragmentInteractionListener {
        fun onFragmentClick()
    }

    companion object {
        val ARG_PHOTO_PATH = "PHOTO_PATH"

        fun newInstance(photoPath: String): PhotoFragment {
            val fragment = PhotoFragment()
            val args = Bundle()
            args.putString(ARG_PHOTO_PATH, photoPath)
            fragment.arguments = args
            return fragment
        }
    }
}
