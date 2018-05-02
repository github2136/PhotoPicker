package com.github2136.picturepicker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;
import com.github2136.picturepicker.R;
import com.github2136.picturepicker.other.GlideApp;

public class PhotoFragment extends Fragment {
    public static final String ARG_PHOTO_PATH = "PHOTO_PATH";
    private OnFragmentInteractionListener mListener;
    private String photoPath;
    private PhotoView ivPhoto;

    public PhotoFragment() { }

    public static PhotoFragment newInstance(String photoPath) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHOTO_PATH, photoPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            photoPath = getArguments().getString(ARG_PHOTO_PATH);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void onFragmentClick() {
        if (mListener != null) {
            mListener.onFragmentClick();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ivPhoto = (PhotoView) view.findViewById(R.id.iv_photo);

        GlideApp.with(getContext())
                .load(photoPath)
                .diskCacheStrategy(DiskCacheStrategy.ALL.ALL)
                .placeholder(R.drawable.img_picker_place)
                .error(R.drawable.img_picker_fail)
                .into(ivPhoto);
        ivPhoto.setOnClickListener(mOnClickListener);
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onFragmentClick();
        }
    };

    public interface OnFragmentInteractionListener {
        void onFragmentClick();
    }
}
