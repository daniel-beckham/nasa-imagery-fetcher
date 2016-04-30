package com.dsbeckham.nasaimageryfetcher.util;

import android.app.Activity;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;

import com.dsbeckham.nasaimageryfetcher.adapter.RecyclerViewAdapter;
import com.dsbeckham.nasaimageryfetcher.fragment.IotdFragment;
import com.dsbeckham.nasaimageryfetcher.model.IotdRssModel;
import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import retrofit2.http.GET;

public class IotdQueryUtils {
    public static final String RSS_BASE_URL = "https://www.nasa.gov/";

    public interface RssService {
        @GET("rss/dyn/lg_image_of_the_day.rss")
        Call<IotdRssModel> get();
    }

    public static RssService rssService;

    public static void setUpIoService() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(RSS_BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.createNonStrict())
                .build();

        rssService = retrofit.create(RssService.class);
    }

    public static void beginFetch(Activity activity) {
        IotdFragment iotdFragment = (IotdFragment) activity.getFragmentManager().findFragmentByTag("iotd");

        if (iotdFragment == null) {
            return;
        }

        if (!iotdFragment.loadingData) {
            if (iotdFragment.models.isEmpty()) {
                iotdFragment.progressBar.setVisibility(View.VISIBLE);
            }

            fetchRssFeed(activity);
        }
    }

    public static void clearData(Activity activity) {
        IotdFragment iotdFragment = (IotdFragment) activity.getFragmentManager().findFragmentByTag("iotd");

        if (iotdFragment == null) {
            return;
        }

        if (!iotdFragment.loadingData) {
            iotdFragment.fastItemAdapter.clear();
            iotdFragment.footerAdapter.clear();
            iotdFragment.models.clear();
        }
    }

    public static void fetchRssFeed(final Activity activity) {
        final IotdFragment iotdFragment = (IotdFragment) activity.getFragmentManager().findFragmentByTag("iotd");

        if (iotdFragment == null) {
            return;
        }

        iotdFragment.loadingData = true;

        Call<IotdRssModel> call = rssService.get();
        call.enqueue(new Callback<IotdRssModel>() {
            @Override
            public void onResponse(Call<IotdRssModel> call, Response<IotdRssModel> response) {
                if (response.isSuccessful()) {
                    iotdFragment.footerAdapter.clear();
                    iotdFragment.progressBar.setVisibility(View.GONE);

                    for (IotdRssModel.Channel.Item iotdRssModelChannelItem : response.body().getChannel().getItems()) {
                        UniversalImageModel universalImageModel = ModelUtils.convertIotdRssModelChannelItem(iotdRssModelChannelItem);

                        if (!iotdFragment.models.contains(universalImageModel) && !iotdRssModelChannelItem.getEnclosure().getUrl().isEmpty()) {
                            iotdFragment.models.add(universalImageModel);
                            iotdFragment.fastItemAdapter.add(iotdFragment.fastItemAdapter.getAdapterItemCount(), new RecyclerViewAdapter(universalImageModel));
                        }
                    }

                    iotdFragment.loadingData = false;
                    iotdFragment.swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<IotdRssModel> call, Throwable t) {
                iotdFragment.footerAdapter.clear();
                iotdFragment.loadingData = false;
                iotdFragment.progressBar.setVisibility(View.GONE);
                iotdFragment.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public static void updateData(Activity activity, Intent intent) {
        final IotdFragment iotdFragment = (IotdFragment) activity.getFragmentManager().findFragmentByTag("iotd");

        if (iotdFragment == null) {
            return;
        }

        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typedValue, true);

        iotdFragment.linearLayoutManager.scrollToPositionWithOffset(intent.getIntExtra(IotdFragment.EXTRA_IOTD_POSITION, 0), activity.getResources().getDimensionPixelSize(typedValue.resourceId));
    }
}
