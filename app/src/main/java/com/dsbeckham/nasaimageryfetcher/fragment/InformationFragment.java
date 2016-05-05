package com.dsbeckham.nasaimageryfetcher.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.activity.ImageActivity;
import com.dsbeckham.nasaimageryfetcher.activity.InformationActivity;
import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;
import com.dsbeckham.nasaimageryfetcher.util.DateTimeUtils;
import com.dsbeckham.nasaimageryfetcher.util.PreferenceUtils;
import com.dsbeckham.nasaimageryfetcher.util.TextUtils;
import com.dsbeckham.nasaimageryfetcher.util.UiUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InformationFragment extends Fragment {
    @BindView(R.id.fragment_information_credit_textview)
    TextView credit;
    @BindView(R.id.fragment_information_date_textview)
    TextView date;
    @BindView(R.id.fragment_information_description_textview)
    TextView description;
    @BindView(R.id.fragment_information_header_layout)
    FrameLayout headerLayout;
    @BindView(R.id.fragment_information_imageview)
    ImageView imageView;
    @BindView(R.id.fragment_information_progressbar)
    ProgressBar progressBar;
    @BindView(R.id.fragment_information_scrollview)
    NestedScrollView nestedScrollView;
    @BindView(R.id.fragment_information_subheader_layout)
    RelativeLayout subHeaderLayout;
    @BindView(R.id.fragment_information_title_textview)
    TextView title;

    private int position;
    private UniversalImageModel universalImageModel;

    public static InformationFragment newInstance(int page) {
        InformationFragment InformationFragment = new InformationFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", page);
        InformationFragment.setArguments(bundle);
        return InformationFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        position = getArguments().getInt("position", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_information, container, false);
        ButterKnife.bind(this, view);

        // This controls the parallax effec
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView nestedScrollView, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                ViewCompat.setTranslationY(imageView, scrollY * 0.5f);

                if (scrollY > (headerLayout.getHeight() - ((InformationActivity) getActivity()).toolbar.getHeight())) {
                    ((InformationActivity) getActivity()).toolbar.setBackground(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.colorPrimary)));
                    UiUtils.resetStatusBarTransparency(getActivity());
                } else {
                    ((InformationActivity) getActivity()).toolbar.setBackgroundResource(R.drawable.gradient_toolbar);
                    UiUtils.makeStatusBarTransparent(getActivity());
                }
            }
        });

        switch (PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "")) {
            case "iotd":
                universalImageModel = ((InformationActivity) getActivity()).iotdModels.get(position);
                break;
            case "apod":
                universalImageModel = ((InformationActivity) getActivity()).apodModels.get(position);
                break;
        }

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
                .config(Bitmap.Config.RGB_565)
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

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ImageActivity.class);

                switch (PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "")) {
                    case "iotd":
                        intent.putExtra(IotdFragment.EXTRA_IOTD_MODELS, Parcels.wrap(((InformationActivity) getActivity()).iotdModels));
                        intent.putExtra(IotdFragment.EXTRA_IOTD_POSITION, position);
                        break;
                    case "apod":
                        intent.putExtra(ApodFragment.EXTRA_APOD_CALENDAR, ((InformationActivity) getActivity()).apodCalendar);
                        intent.putExtra(ApodFragment.EXTRA_APOD_MODELS, Parcels.wrap(((InformationActivity) getActivity()).apodModels));
                        intent.putExtra(ApodFragment.EXTRA_APOD_POSITION, position);
                        break;
                }

                startActivityForResult(intent, 0);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "")) {
            case "iotd":
                ((InformationActivity) getActivity()).viewPager.post(new Runnable() {
                    @Override
                    public void run() {
                        ((InformationActivity) getActivity()).viewPager.setCurrentItem(data.getIntExtra(IotdFragment.EXTRA_IOTD_POSITION, 0), false);
                    }
                });
                break;
            case "apod":
                ((InformationActivity) getActivity()).apodCalendar = (Calendar) data.getSerializableExtra(ApodFragment.EXTRA_APOD_CALENDAR);
                ((InformationActivity) getActivity()).apodModels = Parcels.unwrap(data.getParcelableExtra(ApodFragment.EXTRA_APOD_MODELS));
                ((InformationActivity) getActivity()).informationFragmentStatePagerAdapter.notifyDataSetChanged();

                ((InformationActivity) getActivity()).viewPager.post(new Runnable() {
                    @Override
                    public void run() {
                        ((InformationActivity) getActivity()).viewPager.setCurrentItem(data.getIntExtra(ApodFragment.EXTRA_APOD_POSITION, 0), false);
                    }
                });
                break;
        }
    }
}
