package com.dsbeckham.nasaimageryfetcher.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.activity.ViewPagerActivity;
import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;
import com.dsbeckham.nasaimageryfetcher.util.DateTimeUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ImageFragment extends Fragment {
    @Bind(R.id.fragment_image_body_textview)
    TextView body;
    @Bind(R.id.fragment_image_date_textview)
    TextView date;
    @Bind(R.id.fragment_image_footer_textview)
    TextView footer;
    @Bind(R.id.fragment_image_imageview)
    ImageView imageView;
    @Bind(R.id.fragment_image_progressbar)
    ProgressBar progressBar;
    @Bind(R.id.fragment_image_scrollview)
    ScrollView scrollView;
    @Bind(R.id.fragment_image_title_textview)
    TextView title;

    private int rectTop;
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

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                Rect rect = new Rect();
                imageView.getLocalVisibleRect(rect);

                if (rectTop != rect.top) {
                    rectTop = rect.top;
                    imageView.setY((float) (rect.top / 2.0));
                }
            }
        });

        UniversalImageModel universalImageModel = null;

        switch (((ViewPagerActivity) getActivity()).currentFragment) {
            case "iotd":
                universalImageModel = ((ViewPagerActivity) getActivity()).iotdModels.get(position);
                break;
            case "apod":
                universalImageModel = ((ViewPagerActivity) getActivity()).apodModels.get(position);
                break;
        }

        if (universalImageModel != null) {
            title.setText(universalImageModel.getTitle());
            date.setText(String.format("%1$s%2$s", DateTimeUtils.convertDateToLongDateFormat(getActivity(), universalImageModel.getDate(), "yyyy-MM-dd"), System.getProperty("line.separator")));

            body.setText(Html.fromHtml(String.format("%1$s%2$s", universalImageModel.getDescription(), "<br>")));
            body.setMovementMethod(LinkMovementMethod.getInstance());

            String credit = "";

            if (universalImageModel.getCredit() != null) {
                credit = String.format("%1$s%2$s", universalImageModel.getCredit(), "<br>");
            }

            footer.setText(Html.fromHtml(String.format("%1$s%2$s", credit, universalImageModel.getPageUrl())));
            footer.setMovementMethod(LinkMovementMethod.getInstance());

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
