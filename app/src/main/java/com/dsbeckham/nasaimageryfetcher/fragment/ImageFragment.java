package com.dsbeckham.nasaimageryfetcher.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.activity.ImageActivity;
import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageFragment extends Fragment {
    @BindView(R.id.fragment_image_subsamplingscaleimageview)
    SubsamplingScaleImageView subsamplingScaleImageView;
    @BindView(R.id.fragment_image_progressbar)
    ProgressBar progressBar;

    private int position;

    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            subsamplingScaleImageView.setImage(ImageSource.bitmap(bitmap).tilingDisabled());
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onBitmapFailed(android.graphics.drawable.Drawable errorDrawable) {
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onPrepareLoad(android.graphics.drawable.Drawable placeHolderDrawable) {}
    };

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
        position = getArguments().getInt("position", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        ButterKnife.bind(this, view);

        UniversalImageModel universalImageModel = ((ImageActivity) getActivity()).getModel(position);

        if (universalImageModel != null) {
            Picasso.with(getContext())
                    .load(universalImageModel.getImageThumbnailUrl())
                    .resize(2048, 2048)
                    .centerInside()
                    .config(Bitmap.Config.RGB_565)
                    .into(target);

            subsamplingScaleImageView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return ((ImageActivity) getActivity()).gestureDetector.onTouchEvent(motionEvent);
                }
            });
        }

        return view;
    }
}
