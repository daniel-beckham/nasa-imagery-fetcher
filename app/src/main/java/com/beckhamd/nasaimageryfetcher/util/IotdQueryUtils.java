package com.beckhamd.nasaimageryfetcher.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;

import com.beckhamd.nasaimageryfetcher.activity.ImageActivity;
import com.beckhamd.nasaimageryfetcher.activity.InformationActivity;
import com.beckhamd.nasaimageryfetcher.application.MainApplication;
import com.beckhamd.nasaimageryfetcher.fragment.IotdFragment;
import com.beckhamd.nasaimageryfetcher.item.ImageItem;
import com.beckhamd.nasaimageryfetcher.model.IotdRssModel;
import com.beckhamd.nasaimageryfetcher.model.UniversalImageModel;

import java.io.IOException;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import retrofit2.http.GET;

public class IotdQueryUtils {
    private static final String RSS_BASE_URL = "https://www.nasa.gov/";

    public static final int TYPE_RECYCLERVIEW = 0;
    public static final int TYPE_VIEWPAGER_IMAGE = 1;
    public static final int TYPE_VIEWPAGER_INFORMATION = 2;

    public interface RssService {
        @GET("rss/dyn/lg_image_of_the_day.rss")
        Call<IotdRssModel> get();
    }

    public static void setUpIoService(Context context) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(RSS_BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.createNonStrict())
                .build();

        ((MainApplication) context.getApplicationContext()).setIotdRssService(retrofit.create(RssService.class));
    }

    public static void clearData(Activity activity) {
        IotdFragment iotdFragment = (IotdFragment) ((AppCompatActivity) activity).getSupportFragmentManager()
                .findFragmentByTag(PreferenceUtils.FRAGMENT_IOTD);

        if (iotdFragment == null) {
            return;
        }

        if (((MainApplication) activity.getApplication()).isIotdInactive()) {
            ((MainApplication) activity.getApplication()).getIotdModels().clear();
            iotdFragment.fastItemAdapter.clear();
        }
    }

    public static void updateData(Activity activity, Intent intent) {
        final IotdFragment iotdFragment = (IotdFragment) ((AppCompatActivity) activity).getSupportFragmentManager()
                .findFragmentByTag(PreferenceUtils.FRAGMENT_IOTD);

        if (iotdFragment == null) {
            return;
        }

        iotdFragment.fastItemAdapter.clear();

        for (UniversalImageModel universalImageModel : ((MainApplication) activity.getApplication()).getIotdModels()) {
            iotdFragment.fastItemAdapter.add(iotdFragment.fastItemAdapter.getAdapterItemCount(), new ImageItem(universalImageModel));
        }

        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typedValue, true);

        iotdFragment.linearLayoutManager
                .scrollToPositionWithOffset(intent.getIntExtra(InformationActivity.EXTRA_POSITION, 0),
                        activity.getResources().getDimensionPixelSize(typedValue.resourceId));
    }

    public static void beginFetch(Activity activity, int type) {
        if (((MainApplication) activity.getApplication()).isIotdInactive()) {
            if (type == TYPE_RECYCLERVIEW
                    && ((MainApplication) activity.getApplication()).getIotdModels().isEmpty()) {
                IotdFragment iotdFragment = (IotdFragment) ((AppCompatActivity) activity).getSupportFragmentManager()
                        .findFragmentByTag(PreferenceUtils.FRAGMENT_IOTD);

                if (iotdFragment == null) {
                    return;
                }

                iotdFragment.progressBarLayout.setVisibility(View.VISIBLE);
            }

            fetchRssFeed(activity, type);
        }
    }

    private static void fetchRssFeed(final Activity activity, final int type) {
        ((MainApplication) activity.getApplication()).setIotdInactive(false);

        Call<IotdRssModel> call = ((MainApplication) activity.getApplication())
                .getIotdRssService().get();
        call.enqueue(new Callback<IotdRssModel>() {
            @Override
            public void onResponse(Call<IotdRssModel> call, Response<IotdRssModel> response) {
                IotdFragment iotdFragment = null;

                if (type == TYPE_RECYCLERVIEW) {
                    iotdFragment = (IotdFragment) ((AppCompatActivity) activity).getSupportFragmentManager()
                            .findFragmentByTag(PreferenceUtils.FRAGMENT_IOTD);

                    if (iotdFragment == null) {
                        return;
                    }
                }

                if (response.isSuccessful()) {
                    if (type == TYPE_RECYCLERVIEW) {
                        iotdFragment.progressBarLayout.setVisibility(View.GONE);
                    }

                    if (response.body() != null) {
                        for (IotdRssModel.Channel.Item iotdRssModelChannelItem : response.body().getChannel().getItems()) {
                            UniversalImageModel universalImageModel = ModelUtils.convertIotdRssModelChannelItem(iotdRssModelChannelItem);

                            if (!((MainApplication) activity.getApplication()).getIotdModels().contains(universalImageModel)
                                    && !iotdRssModelChannelItem.getEnclosure().getUrl().isEmpty()
                                    && !iotdRssModelChannelItem.getPubDate().isEmpty()) {
                                ((MainApplication) activity.getApplication()).getIotdModels().add(universalImageModel);

                                switch (type) {
                                    case TYPE_RECYCLERVIEW:
                                        iotdFragment.fastItemAdapter.add(iotdFragment.fastItemAdapter.getAdapterItemCount(), new ImageItem(universalImageModel));
                                        break;
                                    case TYPE_VIEWPAGER_IMAGE:
                                        ((ImageActivity) activity).imageFragmentStatePagerAdapter.notifyDataSetChanged();
                                        break;
                                    case TYPE_VIEWPAGER_INFORMATION:
                                        ((InformationActivity) activity).informationFragmentStatePagerAdapter.notifyDataSetChanged();
                                        break;
                                }
                            }
                        }
                    }

                    ((MainApplication) activity.getApplication()).setIotdInactive(true);

                    if (type == TYPE_RECYCLERVIEW) {
                        iotdFragment.swipeRefreshLayout.setRefreshing(false);
                    }
                } else {
                    ((MainApplication) activity.getApplication()).setIotdInactive(true);

                    if (type == TYPE_RECYCLERVIEW) {
                        iotdFragment.progressBarLayout.setVisibility(View.GONE);
                        iotdFragment.swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<IotdRssModel> call, Throwable t) {
                ((MainApplication) activity.getApplication()).setIotdInactive(true);

                if (type == TYPE_RECYCLERVIEW) {
                    IotdFragment iotdFragment = (IotdFragment) ((AppCompatActivity) activity).getSupportFragmentManager()
                            .findFragmentByTag(PreferenceUtils.FRAGMENT_IOTD);

                    if (iotdFragment != null) {
                        iotdFragment.progressBarLayout.setVisibility(View.GONE);
                        iotdFragment.swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }
        });
    }

    public static void getLatestImage(final Context context) {
        Call<IotdRssModel> call = ((MainApplication) context.getApplicationContext()).getIotdRssService().get();

        try {
            Response<IotdRssModel> response = call.execute();

            if (response.isSuccessful()) {
                UniversalImageModel universalImageModel = null;

                if (response.body() != null && !response.body().getChannel().getItems().isEmpty()) {
                    universalImageModel = ModelUtils.convertIotdRssModelChannelItem(response.body().getChannel().getItems().get(0));
                }

                if (universalImageModel != null) {
                    Calendar imageCalendar = DateUtils.convertDateToCalendar(universalImageModel.getDate(), "yyyy-MM-dd");
                    Calendar preferenceCalendar = DateUtils.convertDateToCalendar(PreferenceManager.getDefaultSharedPreferences(context)
                            .getString(PreferenceUtils.PREF_LAST_IOTD_DATE, "1970-01-01"), "yyyy-MM-dd");

                    if (imageCalendar.after(preferenceCalendar)) {
                        BackgroundUtils.processLatestImage(context, universalImageModel);
                        PreferenceManager.getDefaultSharedPreferences(context).edit()
                                .putString(PreferenceUtils.PREF_LAST_IOTD_DATE, universalImageModel.getDate()).apply();
                    }
                }
            }

        } catch (IOException e) {
            // Do nothing if an error occurs
        }
    }
}
