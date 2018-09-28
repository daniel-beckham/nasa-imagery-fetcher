package com.beckhamd.nasaimageryfetcher.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;

import com.beckhamd.nasaimageryfetcher.BuildConfig;
import com.beckhamd.nasaimageryfetcher.activity.ImageActivity;
import com.beckhamd.nasaimageryfetcher.activity.InformationActivity;
import com.beckhamd.nasaimageryfetcher.application.MainApplication;
import com.beckhamd.nasaimageryfetcher.fragment.ApodFragment;
import com.beckhamd.nasaimageryfetcher.item.ImageItem;
import com.beckhamd.nasaimageryfetcher.model.ApodMorphIoModel;
import com.beckhamd.nasaimageryfetcher.model.ApodNasaGovModel;
import com.beckhamd.nasaimageryfetcher.model.UniversalImageModel;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class ApodQueryUtils {
    private static final String NASA_GOV_BASE_URL = "https://api.nasa.gov/";
    private static final String NASA_GOV_API_KEY = BuildConfig.NASA_GOV_API_KEY;

    private static final String MORPH_IO_BASE_URL = "https://api.morph.io/";
    private static final String MORPH_IO_API_KEY = BuildConfig.MORPH_IO_API_KEY;

    private static final int NUMBER_OF_RESULTS = 30;
    private static final int MAX_RESULTS = 90;

    public static final int TYPE_RECYCLERVIEW = 0;
    public static final int TYPE_VIEWPAGER_IMAGE = 1;
    public static final int TYPE_VIEWPAGER_INFORMATION = 2;

    public interface NasaGovService {
        @SuppressWarnings("SameParameterValue")
        @GET("planetary/apod")
        Call<List<ApodNasaGovModel>> get(
                @Query("api_key") String apiKey,
                @Query("start_date") String startDate,
                @Query("end_date") String endDate);
    }

    public interface MorphIoService {
        @SuppressWarnings("SameParameterValue")
        @GET("beckhamd/apod-scraper/data.json")
        Call<List<ApodMorphIoModel>> get(
                @Query("key") String key,
                @Query("query") String query);
    }

    public static void setUpIoServices(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NASA_GOV_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ((MainApplication) context.getApplicationContext()).setApodNasaGovService(retrofit.create(NasaGovService.class));

        retrofit = new Retrofit.Builder()
                .baseUrl(MORPH_IO_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ((MainApplication) context.getApplicationContext()).setApodMorphIoService(retrofit.create(MorphIoService.class));
    }

    public static void clearData(Activity activity) {
        ApodFragment apodFragment = (ApodFragment) ((AppCompatActivity) activity).getSupportFragmentManager()
                .findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);

        if (apodFragment == null) {
            return;
        }

        if (((MainApplication) activity.getApplication()).isApodInactive()) {
            ((MainApplication) activity.getApplication()).setApodCalendar(Calendar.getInstance());
            ((MainApplication) activity.getApplication()).getApodModels().clear();
            apodFragment.endlessRecyclerOnScrollListener.resetPageCount();
            apodFragment.fastItemAdapter.clear();
            apodFragment.footerAdapter.clear();
        }
    }

    public static void updateData(Activity activity, Intent intent) {
        ApodFragment apodFragment = (ApodFragment) ((AppCompatActivity) activity).getSupportFragmentManager()
                .findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);

        if (apodFragment == null) {
            return;
        }

        apodFragment.fastItemAdapter.clear();

        for (UniversalImageModel universalImageModel : ((MainApplication) activity.getApplication()).getApodModels()) {
            apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new ImageItem(universalImageModel));
        }

        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typedValue, true);

        apodFragment.linearLayoutManager.scrollToPositionWithOffset(intent.getIntExtra(InformationActivity.EXTRA_POSITION, 0),
                activity.getResources().getDimensionPixelSize(typedValue.resourceId));
    }

    public static void beginQuery(Activity activity, int type, boolean fallback) {
        if (((MainApplication) activity.getApplication()).isApodInactive()) {
            if (((MainApplication) activity.getApplication()).getApodModels().size() >= MAX_RESULTS) {
                return;
            }

            if (type == TYPE_RECYCLERVIEW) {
                ApodFragment apodFragment = (ApodFragment) ((AppCompatActivity) activity).getSupportFragmentManager()
                        .findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);

                if (apodFragment == null) {
                    return;
                }

                if (!((MainApplication) activity.getApplication()).getApodModels().isEmpty()) {
                    apodFragment.footerAdapter.clear();
                    apodFragment.footerAdapter.add(new ProgressItem().withEnabled(false));
                } else {
                    apodFragment.progressBarLayout.setVisibility(View.VISIBLE);
                }
            }

            if (!fallback) {
                queryNasaGovApi(activity, type);
            } else {
                queryMorphIoApi(activity, type);
            }
        }
    }

    private static void queryNasaGovApi(final Activity activity, final int type) {
        ((MainApplication) activity.getApplication()).setApodInactive(false);

        Calendar apodStartCalendar = (Calendar) ((MainApplication) activity.getApplication()).getApodCalendar().clone();
        apodStartCalendar.add(Calendar.DAY_OF_YEAR, -NUMBER_OF_RESULTS);

        Calendar apodEndCalendar = (Calendar) ((MainApplication) activity.getApplication()).getApodCalendar().clone();

        String startDate = String.format(Locale.US, "%d-%02d-%02d",
                apodStartCalendar.get(Calendar.YEAR),
                apodStartCalendar.get(Calendar.MONTH) + 1,
                apodStartCalendar.get(Calendar.DAY_OF_MONTH));

        String endDate = String.format(Locale.US, "%d-%02d-%02d",
                apodEndCalendar.get(Calendar.YEAR),
                apodEndCalendar.get(Calendar.MONTH) + 1,
                apodEndCalendar.get(Calendar.DAY_OF_MONTH));

        Call<List<ApodNasaGovModel>> call = ((MainApplication) activity.getApplication()).getApodNasaGovService()
                .get(NASA_GOV_API_KEY, startDate, endDate);
        call.enqueue(new Callback<List<ApodNasaGovModel>>() {
            @Override
            public void onResponse(Call<List<ApodNasaGovModel>> call, Response<List<ApodNasaGovModel>> response) {
                ApodFragment apodFragment = null;

                if (type == TYPE_RECYCLERVIEW) {
                    apodFragment = (ApodFragment) ((AppCompatActivity) activity).getSupportFragmentManager()
                            .findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);

                    if (apodFragment == null) {
                        return;
                    }
                }

                if (type == TYPE_RECYCLERVIEW) {
                    apodFragment.footerAdapter.clear();
                    apodFragment.progressBarLayout.setVisibility(View.GONE);
                }

                if (response.isSuccessful()) {
                    if (type == TYPE_RECYCLERVIEW) {
                        apodFragment.footerAdapter.clear();
                        apodFragment.progressBarLayout.setVisibility(View.GONE);
                    }

                    if (response.body() != null) {
                        // Reverse the list since the results are returned backwards
                        Collections.reverse(response.body());

                        for (ApodNasaGovModel apodNasaGovModel : response.body()) {
                            UniversalImageModel universalImageModel = ModelUtils.convertApodNasaGovModel(apodNasaGovModel);
                            Calendar apodCalendar = DateUtils.convertDateToCalendar(universalImageModel.getDate(), "yyyy-MM-dd");

                            if (!((MainApplication) activity.getApplication()).getApodModels().contains(universalImageModel)
                                    && !universalImageModel.getImageThumbnailUrl().isEmpty()) {
                                ((MainApplication) activity.getApplication()).getApodModels().add(universalImageModel);

                                switch (type) {
                                    case TYPE_RECYCLERVIEW:
                                        apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new ImageItem(universalImageModel));
                                        break;
                                    case TYPE_VIEWPAGER_IMAGE:
                                        ((ImageActivity) activity).imageFragmentStatePagerAdapter.notifyDataSetChanged();
                                        break;
                                    case TYPE_VIEWPAGER_INFORMATION:
                                        ((InformationActivity) activity).informationFragmentStatePagerAdapter.notifyDataSetChanged();
                                        break;
                                }
                            }

                            ((MainApplication) activity.getApplication()).setApodCalendar(apodCalendar);
                            ((MainApplication) activity.getApplication()).getApodCalendar().add(Calendar.DAY_OF_YEAR, -1);
                        }

                        ((MainApplication) activity.getApplication()).setApodInactive(true);

                        if (type == TYPE_RECYCLERVIEW) {
                            apodFragment.swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                } else {
                    // If the response is not successful, then fall back to morph.io
                    ((MainApplication) activity.getApplication()).setApodInactive(true);
                    beginQuery(activity, type, true);
                }
            }

            @Override
            public void onFailure(Call<List<ApodNasaGovModel>> call, Throwable t) {
                // If an exception occurs, then fall back to morph.io
                ((MainApplication) activity.getApplication()).setApodInactive(true);
                beginQuery(activity, type, true);
            }
        });
    }

    private static void queryMorphIoApi(final Activity activity, final int type) {
        ((MainApplication) activity.getApplication()).setApodInactive(false);

        String query = String.format(Locale.US, "SELECT * FROM data WHERE date <= date('%d-%02d-%02d') ORDER BY date DESC LIMIT %d",
                ((MainApplication) activity.getApplication()).getApodCalendar().get(Calendar.YEAR),
                ((MainApplication) activity.getApplication()).getApodCalendar().get(Calendar.MONTH) + 1,
                ((MainApplication) activity.getApplication()).getApodCalendar().get(Calendar.DAY_OF_MONTH), NUMBER_OF_RESULTS);

        Call<List<ApodMorphIoModel>> call = ((MainApplication) activity.getApplication()).getApodMorphIoService()
                .get(MORPH_IO_API_KEY, query);
        call.enqueue(new Callback<List<ApodMorphIoModel>>() {
            @Override
            public void onResponse(Call<List<ApodMorphIoModel>> call, Response<List<ApodMorphIoModel>> response) {
                ApodFragment apodFragment = null;

                if (type == TYPE_RECYCLERVIEW) {
                    apodFragment = (ApodFragment) ((AppCompatActivity) activity).getSupportFragmentManager()
                            .findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);

                    if (apodFragment == null) {
                        return;
                    }
                }

                if (response.isSuccessful()) {
                    if (type == TYPE_RECYCLERVIEW) {
                        apodFragment.footerAdapter.clear();
                        apodFragment.progressBarLayout.setVisibility(View.GONE);
                    }

                    if (response.body() != null) {
                        for (ApodMorphIoModel apodMorphIoModel : response.body()) {
                            UniversalImageModel universalImageModel = ModelUtils.convertApodMorphIoModel(apodMorphIoModel);
                            Calendar apodCalendar = DateUtils.convertDateToCalendar(universalImageModel.getDate(), "yyyy-MM-dd");

                            if (!((MainApplication) activity.getApplication()).getApodModels().contains(universalImageModel)
                                    && !universalImageModel.getImageThumbnailUrl().isEmpty()) {
                                ((MainApplication) activity.getApplication()).getApodModels().add(universalImageModel);

                                switch (type) {
                                    case TYPE_RECYCLERVIEW:
                                        apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new ImageItem(universalImageModel));
                                        break;
                                    case TYPE_VIEWPAGER_IMAGE:
                                        ((ImageActivity) activity).imageFragmentStatePagerAdapter.notifyDataSetChanged();
                                        break;
                                    case TYPE_VIEWPAGER_INFORMATION:
                                        ((InformationActivity) activity).informationFragmentStatePagerAdapter.notifyDataSetChanged();
                                        break;
                                }
                            }

                            ((MainApplication) activity.getApplication()).setApodCalendar(apodCalendar);
                            ((MainApplication) activity.getApplication()).getApodCalendar().add(Calendar.DAY_OF_YEAR, -1);
                        }
                    }

                    ((MainApplication) activity.getApplication()).setApodInactive(true);

                    if (type == TYPE_RECYCLERVIEW) {
                        apodFragment.swipeRefreshLayout.setRefreshing(false);
                    }
                } else {
                    // Stop here since nasa.gov should have already been queried
                    ((MainApplication) activity.getApplication()).setApodInactive(true);

                    if (type == TYPE_RECYCLERVIEW) {
                        apodFragment.footerAdapter.clear();
                        apodFragment.progressBarLayout.setVisibility(View.GONE);
                        apodFragment.swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ApodMorphIoModel>> call, Throwable t) {
                // Stop here since nasa.gov should have already been queried
                ((MainApplication) activity.getApplication()).setApodInactive(true);

                if (type == TYPE_RECYCLERVIEW) {
                    ApodFragment apodFragment = (ApodFragment) ((AppCompatActivity) activity).getSupportFragmentManager()
                            .findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);

                    if (apodFragment != null) {
                        apodFragment.footerAdapter.clear();
                        apodFragment.progressBarLayout.setVisibility(View.GONE);
                        apodFragment.swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }
        });
    }

    static void getLatestImage(final Context context, final boolean fallback) {
        Calendar calendar = Calendar.getInstance();

        if (!fallback) {
            Calendar apodStartCalendar = (Calendar) calendar.clone();
            apodStartCalendar.add(Calendar.DAY_OF_YEAR, -NUMBER_OF_RESULTS);

            Calendar apodEndCalendar = (Calendar) calendar.clone();

            String startDate = String.format(Locale.US, "%d-%02d-%02d",
                    apodStartCalendar.get(Calendar.YEAR),
                    apodStartCalendar.get(Calendar.MONTH) + 1,
                    apodStartCalendar.get(Calendar.DAY_OF_MONTH));
            String endDate = String.format(Locale.US, "%d-%02d-%02d",
                    apodEndCalendar.get(Calendar.YEAR),
                    apodEndCalendar.get(Calendar.MONTH) + 1,
                    apodEndCalendar.get(Calendar.DAY_OF_MONTH));

            Call<List<ApodNasaGovModel>> call = ((MainApplication) context.getApplicationContext()).getApodNasaGovService()
                    .get(NASA_GOV_API_KEY, startDate, endDate);

            try {
                Response<List<ApodNasaGovModel>> response = call.execute();

                if (response.isSuccessful()) {
                    UniversalImageModel universalImageModel = null;

                    if (response.body() != null && !response.body().isEmpty()) {
                        universalImageModel = ModelUtils.convertApodNasaGovModel(response.body().get(0));
                    }

                    if (universalImageModel != null && !universalImageModel.getImageThumbnailUrl().isEmpty()) {
                        Calendar imageCalendar = DateUtils.convertDateToCalendar(universalImageModel.getDate(), "yyyy-MM-dd");
                        Calendar preferenceCalendar = DateUtils.convertDateToCalendar(PreferenceManager.getDefaultSharedPreferences(context)
                                .getString(PreferenceUtils.PREF_LAST_APOD_DATE, "1970-01-01"), "yyyy-MM-dd");

                        if (imageCalendar.after(preferenceCalendar)) {
                            BackgroundUtils.processLatestImage(context, universalImageModel);
                            PreferenceManager.getDefaultSharedPreferences(context)
                                    .edit()
                                    .putString(PreferenceUtils.PREF_LAST_APOD_DATE, universalImageModel.getDate())
                                    .apply();
                        }
                    }
                } else {
                    // If the response is not successful, then fall back morph.io
                    getLatestImage(context, true);
                }
            } catch (IOException e) {
                // If an exception occurs, then fall back morph.io
                getLatestImage(context, true);
            }
        } else {
            String query = String.format(Locale.US, "SELECT * FROM data WHERE date <= date('%d-%02d-%02d') ORDER BY date DESC LIMIT 2",
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));

            Call<List<ApodMorphIoModel>> call = ((MainApplication) context.getApplicationContext()).getApodMorphIoService()
                    .get(MORPH_IO_API_KEY, query);

            try {
                Response<List<ApodMorphIoModel>> response = call.execute();

                if (response.isSuccessful()) {
                    UniversalImageModel universalImageModel = null;

                    if (response.body() != null && !response.body().isEmpty()) {
                        universalImageModel = ModelUtils.convertApodMorphIoModel(response.body().get(0));
                    }

                    if (universalImageModel != null && !universalImageModel.getImageThumbnailUrl().isEmpty()) {
                        Calendar imageCalendar = DateUtils.convertDateToCalendar(universalImageModel.getDate(), "yyyy-MM-dd");
                        Calendar preferenceCalendar = DateUtils.convertDateToCalendar(PreferenceManager
                                .getDefaultSharedPreferences(context).getString(PreferenceUtils.PREF_LAST_APOD_DATE, "1970-01-01"), "yyyy-MM-dd");

                        if (imageCalendar.after(preferenceCalendar)) {
                            BackgroundUtils.processLatestImage(context, universalImageModel);
                            PreferenceManager.getDefaultSharedPreferences(context)
                                    .edit()
                                    .putString(PreferenceUtils.PREF_LAST_APOD_DATE, universalImageModel.getDate())
                                    .apply();
                        }
                    }
                }
            } catch (IOException e) {
                // Stop here since nasa.gov should have already been queried
            }
        }
    }
}
