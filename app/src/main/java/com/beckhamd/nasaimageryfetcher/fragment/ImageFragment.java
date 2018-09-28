package com.beckhamd.nasaimageryfetcher.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.beckhamd.nasaimageryfetcher.R;
import com.beckhamd.nasaimageryfetcher.activity.ImageActivity;
import com.beckhamd.nasaimageryfetcher.model.UniversalImageModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageFragment extends Fragment {
    @BindView(R.id.fragment_image_subsamplingscaleimageview)
    SubsamplingScaleImageView subsamplingScaleImageView;
    @BindView(R.id.fragment_image_progressbar)
    ProgressBar progressBar;

    private Activity hostActivity;
    private int position;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            hostActivity = (Activity) context;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            hostActivity = activity;
        }
    }

    public static ImageFragment newInstance(int page) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", page);

        ImageFragment imageFragment = new ImageFragment();
        imageFragment.setArguments(bundle);

        return imageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments() != null) {
            position = getArguments().getInt("position", 0);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        ButterKnife.bind(this, view);

        UniversalImageModel universalImageModel = ((ImageActivity) hostActivity).getModel(position);

        if (universalImageModel != null) {
            Glide.with(hostActivity)
                    .asBitmap()
                    .load(universalImageModel.getImageThumbnailUrl())
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                            subsamplingScaleImageView.setImage(ImageSource.bitmap(resource).tilingDisabled());
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadFailed(Drawable errorDrawable) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });

            subsamplingScaleImageView.setMinimumDpi(60);
            subsamplingScaleImageView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return ((ImageActivity) hostActivity).gestureDetector.onTouchEvent(motionEvent);
                }
            });
        }

        return view;
    }
}
