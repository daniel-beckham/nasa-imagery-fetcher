package com.dsbeckham.nasaimageryfetcher.util;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;

import com.dsbeckham.nasaimageryfetcher.activity.InformationActivity;
import com.dsbeckham.nasaimageryfetcher.adapter.RecyclerViewAdapter;
import com.dsbeckham.nasaimageryfetcher.fragment.IotdFragment;
import com.dsbeckham.nasaimageryfetcher.model.IotdRssModel;
import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;
import com.dsbeckham.nasaimageryfetcher.service.BackgroundService;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import retrofit2.http.GET;

public class IotdQueryUtils {
    private static final String RSS_BASE_URL = "https://www.nasa.gov/";

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

    public static void clearData(Activity activity) {
        IotdFragment iotdFragment = (IotdFragment) ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag(PreferenceUtils.FRAGMENT_IOTD);

        if (iotdFragment == null) {
            return;
        }

        if (!iotdFragment.loadingData) {
            iotdFragment.fastItemAdapter.clear();
            iotdFragment.footerAdapter.clear();
            iotdFragment.models.clear();
        }
    }

    public static void updateData(Activity activity, Intent intent) {
        final IotdFragment iotdFragment = (IotdFragment) ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag(PreferenceUtils.FRAGMENT_IOTD);

        if (iotdFragment == null) {
            return;
        }

        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typedValue, true);

        iotdFragment.linearLayoutManager.scrollToPositionWithOffset(intent.getIntExtra(InformationActivity.EXTRA_POSITION, 0), activity.getResources().getDimensionPixelSize(typedValue.resourceId));
    }

    public static void beginFetch(Activity activity) {
        IotdFragment iotdFragment = (IotdFragment) ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag(PreferenceUtils.FRAGMENT_IOTD);

        if (iotdFragment == null) {
            return;
        }

        if (!iotdFragment.loadingData) {
            if (iotdFragment.models.isEmpty()) {
                iotdFragment.progressBarLayout.setVisibility(View.VISIBLE);
            }

            fetchRssFeed(activity);
        }
    }

    public static void fetchRssFeed(final Activity activity) {
        final IotdFragment iotdFragment = (IotdFragment) ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag(PreferenceUtils.FRAGMENT_IOTD);

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
                    iotdFragment.progressBarLayout.setVisibility(View.GONE);

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
                iotdFragment.progressBarLayout.setVisibility(View.GONE);
                iotdFragment.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public static void getLatestImage(final IntentService intentService) {
        Call<IotdRssModel> call = rssService.get();
        call.enqueue(new Callback<IotdRssModel>() {
            @Override
            public void onResponse(Call<IotdRssModel> call, Response<IotdRssModel> response) {
                if (response.isSuccessful()) {
                    UniversalImageModel universalImageModel = ModelUtils.convertIotdRssModelChannelItem(response.body().getChannel().getItems().get(0));

                    Calendar imageCalendar = DateUtils.convertDateToCalendar(universalImageModel.getDate(), "yyyy-MM-dd");
                    Calendar preferenceCalendar = DateUtils.convertDateToCalendar(PreferenceManager.getDefaultSharedPreferences(intentService).getString(PreferenceUtils.PREF_LAST_IOTD_DATE, "1970-01-01"), "yyyy-MM-dd");

                    if (imageCalendar.after(preferenceCalendar)) {
                        ((BackgroundService) intentService).processLatestImage(universalImageModel);
                        PreferenceManager.getDefaultSharedPreferences(intentService).edit().putString(PreferenceUtils.PREF_LAST_IOTD_DATE, universalImageModel.getDate()).apply();
                    }
                }
            }

            @Override
            public void onFailure(Call<IotdRssModel> call, Throwable t) {}
        });
    }
}
