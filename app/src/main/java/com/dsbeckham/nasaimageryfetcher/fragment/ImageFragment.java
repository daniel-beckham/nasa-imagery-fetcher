package com.dsbeckham.nasaimageryfetcher.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.activity.ViewPagerActivity;
import com.dsbeckham.nasaimageryfetcher.model.ApodMorphIoModel;
import com.dsbeckham.nasaimageryfetcher.model.ApodNasaGovModel;
import com.dsbeckham.nasaimageryfetcher.model.IotdRssModel;
import com.dsbeckham.nasaimageryfetcher.util.DateTimeUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ImageFragment extends Fragment {
    @Bind(R.id.fragment_image_credit_textview)
    TextView credit;
    @Bind(R.id.fragment_image_date_textview)
    TextView date;
    @Bind(R.id.fragment_image_description_textview)
    TextView description;
    @Bind(R.id.fragment_image_imageview)
    ImageView imageView;
    @Bind(R.id.fragment_image_detail_progressbar)
    View progressBar;
    @Bind(R.id.fragment_image_title_textview)
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

        switch (((ViewPagerActivity) getActivity()).currentFragment) {
            case "iotd":
                IotdRssModel.Channel.Item iotdRssModelItem = ((ViewPagerActivity) getActivity()).imageFragmentStatePagerAdapter.iotdRssModels.get(position);
                date.setText(String.format("%1$s%2$s", DateTimeUtils.formatDate(getActivity(), iotdRssModelItem.getPubDate(), "EEE, dd MMM yyyy HH:mm zzz"), System.getProperty ("line.separator")));
                description.setText(String.format("%1$s%2$s", iotdRssModelItem.getDescription(), System.getProperty ("line.separator")));
                title.setText(iotdRssModelItem.getTitle());
                Picasso.with(getContext())
                        .load(iotdRssModelItem.getEnclosure().getUrl().replace("styles/full_width_feature/public/", ""))
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
                break;
            case "apod":
                switch (((ViewPagerActivity) getActivity()).apodFetchService) {
                    case "morph_io":
                        ApodMorphIoModel apodMorphIoModel = ((ViewPagerActivity) getActivity()).imageFragmentStatePagerAdapter.apodMorphIoModels.get(position);
                        credit.setText(Html.fromHtml(apodMorphIoModel.getCredit()));
                        credit.setMovementMethod(LinkMovementMethod.getInstance());
                        date.setText(String.format("%1$s%2$s", DateTimeUtils.formatDate(getActivity(), apodMorphIoModel.getDate(), "yyyy-MM-dd"), System.getProperty ("line.separator")));
                        description.setText(Html.fromHtml(String.format("%1$s%2$s", apodMorphIoModel.getExplanation(), "<br>")));
                        description.setMovementMethod(LinkMovementMethod.getInstance());
                        title.setText(apodMorphIoModel.getTitle());
                        Picasso.with(getContext())
                                .load(apodMorphIoModel.getPictureThumbnailUrl())
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
                        break;
                    case "nasa_gov":
                        ApodNasaGovModel apodNasaGovModel = ((ViewPagerActivity) getActivity()).imageFragmentStatePagerAdapter.apodNasaGovModels.get(position);
                        credit.setText(apodNasaGovModel.getCopyright());
                        date.setText(String.format("%1$s%2$s", DateTimeUtils.formatDate(getActivity(), apodNasaGovModel.getDate(), "yyyy-MM-dd"), System.getProperty ("line.separator")));
                        description.setText(String.format("%1$s%2$s", apodNasaGovModel.getExplanation(), System.getProperty ("line.separator")));
                        title.setText(apodNasaGovModel.getTitle());
                        Picasso.with(imageView.getContext())
                                .load(apodNasaGovModel.getUrl())
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
                        break;
                }
                break;
        }

        return view;
    }
}
