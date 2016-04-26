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
import com.dsbeckham.nasaimageryfetcher.model.IotdRssModel;
import com.dsbeckham.nasaimageryfetcher.util.DateTimeUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ImageFragment extends Fragment {
    @Bind(R.id.fragment_image_credit_textview)
    TextView credit;
    @Bind(R.id.fragment_image_description_textview)
    TextView description;
    @Bind(R.id.fragment_image_imageview)
    ImageView image;
    @Bind(R.id.fragment_image_detail_progressbar)
    View progressBar;
    @Bind(R.id.fragment_image_title_textview)
    TextView title;

    private int page;
    private String type;

    public static ImageFragment newInstance(int page, String type) {
        ImageFragment imageFragment = new ImageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("page", page);
        bundle.putString("type", type);
        imageFragment.setArguments(bundle);
        return imageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        page = getArguments().getInt("page", 0);
        type = getArguments().getString("type", "iotd");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        ButterKnife.bind(this, view);

        switch (type) {
            case "iotd":
                IotdRssModel.Channel.Item iotdRssModelItem = ((ViewPagerActivity) getActivity()).imageFragmentStatePagerAdapter.iotdRssModels.get(page);
                description.setText(Html.fromHtml(getActivity().getString(R.string.image_fragment_description, DateTimeUtils.formatDate(getActivity(), iotdRssModelItem.getPubDate(), "EEE, dd MMM yyyy HH:mm zzz"), iotdRssModelItem.getDescription())));
                title.setText(iotdRssModelItem.getTitle());
                Picasso.with(image.getContext())
                        .load(iotdRssModelItem.getEnclosure().getUrl().replace("styles/full_width_feature/public/", ""))
                        .fit()
                        .centerCrop()
                        .into(image, new Callback() {
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
                // Add a check here that determines which API should be used based
                // on the user settings. (Also, add the relevant setting.)
                ApodMorphIoModel apodMorphIoModel = ((ViewPagerActivity) getActivity()).imageFragmentStatePagerAdapter.apodMorphIoModels.get(page);
                credit.setText(Html.fromHtml(getActivity().getString(R.string.image_fragment_credit, apodMorphIoModel.getCredit())));
                credit.setMovementMethod(LinkMovementMethod.getInstance());
                description.setText(Html.fromHtml(getActivity().getString(R.string.image_fragment_description, DateTimeUtils.formatDate(getActivity(), apodMorphIoModel.getDate(), "yyyy-MM-dd"), apodMorphIoModel.getExplanation())));
                description.setMovementMethod(LinkMovementMethod.getInstance());
                title.setText(apodMorphIoModel.getTitle());
                Picasso.with(image.getContext())
                        .load(apodMorphIoModel.getPictureThumbnailUrl())
                        .fit()
                        .centerCrop()
                        .into(image, new Callback() {
                            @Override
                            public void onSuccess() {
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                /*
                ApodNasaModel apodNasaModel = ((ViewPagerActivity) getActivity()).imageFragmentStatePagerAdapter.apodNasaModels.get(page);
                description.setText(apodNasaModel.getExplanation());
                subtitle.setText(Html.fromHtml(String.format("%1$s<br>%2$s<br>", apodNasaModel.getDate(), apodNasaModel.getCopyright())));
                title.setText(apodNasaModel.getTitle());
                Picasso.with(image.getContext())
                        .load(apodNasaModel.getUrl())
                        .fit()
                        .centerInside()
                        .into(image, new Callback() {
                            @Override
                            public void onSuccess() {
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                */
                break;
        }

        return view;
    }
}
