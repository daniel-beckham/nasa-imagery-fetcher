package com.beckhamd.nasaimageryfetcher.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

import com.beckhamd.nasaimageryfetcher.R;
import com.beckhamd.nasaimageryfetcher.activity.ImageActivity;
import com.beckhamd.nasaimageryfetcher.activity.InformationActivity;
import com.beckhamd.nasaimageryfetcher.model.UniversalImageModel;
import com.beckhamd.nasaimageryfetcher.util.DateUtils;
import com.beckhamd.nasaimageryfetcher.util.TextUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class InformationFragment extends Fragment {
    private static final String BUNDLE_POSITION = "position";

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

    public static InformationFragment newInstance(int page) {
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_POSITION, page);

        InformationFragment informationFragment = new InformationFragment();
        informationFragment.setArguments(bundle);

        return informationFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments() != null) {
            position = getArguments().getInt(BUNDLE_POSITION, 0);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_information, container, false);
        ButterKnife.bind(this, view);

        UniversalImageModel universalImageModel = ((InformationActivity) hostActivity).getModel(position);

        if (universalImageModel != null) {
            title.setText(universalImageModel.getTitle());
            date.setText(DateUtils.convertDateToLongDateFormat(hostActivity, universalImageModel.getDate(), "yyyy-MM-dd"));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ViewCompat.setElevation(subHeaderLayout, 4.0f / getResources().getDisplayMetrics().density);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                description.setText(Html.fromHtml(universalImageModel.getDescription(), Html.FROM_HTML_MODE_LEGACY));
            } else {
                description.setText(Html.fromHtml(universalImageModel.getDescription()));
            }

            description.setMovementMethod(LinkMovementMethod.getInstance());
            TextUtils.stripUnderlines(description);

            if (universalImageModel.getCredit() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    credit.setText(Html.fromHtml(universalImageModel.getCredit(), Html.FROM_HTML_MODE_LEGACY));
                } else {
                    credit.setText(Html.fromHtml(universalImageModel.getCredit()));
                }

                credit.setMovementMethod(LinkMovementMethod.getInstance());
                credit.setVisibility(View.VISIBLE);
                TextUtils.stripUnderlines(credit);
            }

            Glide.with(hostActivity)
                    .load(universalImageModel.getImageThumbnailUrl())
                    .apply(RequestOptions.centerCropTransform())
                    .transition(withCrossFade())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                       DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(hostActivity, ImageActivity.class);
                    if (!((InformationActivity) hostActivity).models.isEmpty()) {
                        intent.putParcelableArrayListExtra(InformationActivity.EXTRA_MODELS, ((InformationActivity) hostActivity).models);
                    }

                    intent.putExtra(InformationActivity.EXTRA_POSITION, position);
                    intent.putExtra(InformationActivity.EXTRA_TYPE, ((InformationActivity) hostActivity).type);
                    startActivityForResult(intent, 0);
                }
            });
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (hostActivity != null) {
            if (((InformationActivity) hostActivity).type != InformationActivity.EXTRA_TYPE_MIXED) {
                ((InformationActivity) hostActivity).informationFragmentStatePagerAdapter.notifyDataSetChanged();
            }

            ((InformationActivity) hostActivity).viewPager.post(new Runnable() {
                @Override
                public void run() {
                    ((InformationActivity) hostActivity).viewPager
                            .setCurrentItem(data.getIntExtra(InformationActivity.EXTRA_POSITION, 0), false);
                }
            });
        }
    }
}
