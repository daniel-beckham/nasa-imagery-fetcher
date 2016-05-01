package com.dsbeckham.nasaimageryfetcher.fragment;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.activity.ViewPagerActivity;
import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;
import com.dsbeckham.nasaimageryfetcher.util.DateTimeUtils;
import com.dsbeckham.nasaimageryfetcher.util.PreferenceUtils;
import com.dsbeckham.nasaimageryfetcher.util.TextUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageFragment extends Fragment {
    @BindView(R.id.fragment_image_credit_textview)
    TextView credit;
    @BindView(R.id.fragment_image_date_textview)
    TextView date;
    @BindView(R.id.fragment_image_description_textview)
    TextView description;
    @BindView(R.id.fragment_image_header_layout)
    FrameLayout headerLayout;
    @BindView(R.id.fragment_image_imageview)
    ImageView imageView;
    @BindView(R.id.fragment_image_progressbar)
    ProgressBar progressBar;
    @BindView(R.id.fragment_image_scrollview)
    NestedScrollView nestedScrollView;
    @BindView(R.id.fragment_image_subheader_layout)
    RelativeLayout subHeaderLayout;
    @BindView(R.id.fragment_image_title_textview)
    TextView title;

    private int position;

    public static ImageFragment newInstance(int page) {
        ImageFragment imageFragment = new ImageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", page);
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

        final TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typedValue, true);

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView nestedScrollView, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                // This creates a parallax effect for the image.
                ViewCompat.setTranslationY(imageView, scrollY * 0.5f);

                // This makes the ToolBar change from a gradient to a solid color and vice versa.
                if (scrollY > (headerLayout.getHeight() - getResources().getDimensionPixelSize(typedValue.resourceId))) {
                    ((ViewPagerActivity) getActivity()).toolbar.setBackground(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.colorPrimary)));
                } else {
                    ((ViewPagerActivity) getActivity()).toolbar.setBackgroundResource(R.drawable.gradient_viewpager_toolbar);
                }
            }
        });

        UniversalImageModel universalImageModel = null;

        switch (PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "")) {
            case "iotd":
                universalImageModel = ((ViewPagerActivity) getActivity()).iotdModels.get(position);
                break;
            case "apod":
                universalImageModel = ((ViewPagerActivity) getActivity()).apodModels.get(position);
                break;
        }

        if (universalImageModel != null) {
            title.setText(universalImageModel.getTitle());
            date.setText(DateTimeUtils.convertDateToLongDateFormat(getActivity(), universalImageModel.getDate(), "yyyy-MM-dd"));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ViewCompat.setElevation(subHeaderLayout, 4.0f / getResources().getDisplayMetrics().density);
            }

            description.setText(Html.fromHtml(universalImageModel.getDescription()));
            description.setMovementMethod(LinkMovementMethod.getInstance());
            TextUtils.stripUnderlines(description);

            if (universalImageModel.getCredit() != null) {
                credit.setText(Html.fromHtml(universalImageModel.getCredit()));
                credit.setMovementMethod(LinkMovementMethod.getInstance());
                credit.setVisibility(View.VISIBLE);
                TextUtils.stripUnderlines(credit);
            }

            Picasso.with(getContext())
                    .load(universalImageModel.getImageThumbnailUrl())
                    .fit()
                    .centerCrop()
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }

        return view;
    }
}
