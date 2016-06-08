package com.dsbeckham.nasaimageryfetcher.util;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;

import com.dsbeckham.nasaimageryfetcher.BuildConfig;
import com.dsbeckham.nasaimageryfetcher.activity.ImageActivity;
import com.dsbeckham.nasaimageryfetcher.activity.InformationActivity;
import com.dsbeckham.nasaimageryfetcher.adapter.RecyclerViewAdapter;
import com.dsbeckham.nasaimageryfetcher.application.MainApplication;
import com.dsbeckham.nasaimageryfetcher.fragment.ApodFragment;
import com.dsbeckham.nasaimageryfetcher.model.ApodMorphIoModel;
import com.dsbeckham.nasaimageryfetcher.model.ApodNasaGovModel;
import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;
import com.dsbeckham.nasaimageryfetcher.service.BackgroundService;

import java.util.Calendar;
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
    public static final int NASA_GOV_API_QUERIES = 5;

    private static final String MORPH_IO_BASE_URL = "https://api.morph.io/";
    private static final String MORPH_IO_API_KEY = BuildConfig.MORPH_IO_API_KEY;

    public static final int TYPE_RECYCLERVIEW = 0;
    public static final int TYPE_VIEWPAGER_IMAGE = 1;
    public static final int TYPE_VIEWPAGER_INFORMATION = 2;

    public interface MorphIoService {
        @GET("dsbeckham/apod-scraper/data.json")
        Call<List<ApodMorphIoModel>> get(
                @Query("key") String key,
                @Query("query") String query);
    }

    public interface NasaGovService {
        @GET("planetary/apod")
        Call<ApodNasaGovModel> get(
                @Query("api_key") String apiKey,
                @Query("date") String date);
    }

    public static void setUpIoServices(Context context) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(MORPH_IO_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ((MainApplication) context.getApplicationContext()).setApodMorphIoService(retrofit.create(MorphIoService.class));

        retrofit = new Retrofit.Builder().baseUrl(NASA_GOV_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ((MainApplication) context.getApplicationContext()).setApodNasaGovService(retrofit.create(NasaGovService.class));
    }

    public static void clearData(Activity activity) {
        ApodFragment apodFragment = (ApodFragment) ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);

        if (apodFragment == null) {
            return;
        }

        if (!((MainApplication) activity.getApplication()).isApodLoadingData()) {
            ((MainApplication) activity.getApplication()).setApodCalendar(Calendar.getInstance());
            ((MainApplication) activity.getApplication()).getApodModels().clear();
            apodFragment.endlessRecyclerOnScrollListener.resetPageCount();
            apodFragment.fastItemAdapter.clear();
            apodFragment.footerAdapter.clear();
        }
    }

    public static void updateData(Activity activity, Intent intent) {
        ApodFragment apodFragment = (ApodFragment) ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);

        if (apodFragment == null) {
            return;
        }

        apodFragment.fastItemAdapter.clear();

        for (UniversalImageModel universalImageModel : ((MainApplication) activity.getApplication()).getApodModels()) {
            apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new RecyclerViewAdapter(universalImageModel));
        }

        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typedValue, true);

        apodFragment.linearLayoutManager.scrollToPositionWithOffset(intent.getIntExtra(InformationActivity.EXTRA_POSITION, 0), activity.getResources().getDimensionPixelSize(typedValue.resourceId));
    }

    public static void beginQuery(Activity activity, int type, boolean fallback) {
        if (!((MainApplication) activity.getApplication()).isApodLoadingData()) {
            if (type == TYPE_RECYCLERVIEW
                    && ((MainApplication) activity.getApplication()).getApodModels().isEmpty()) {
                ApodFragment apodFragment = (ApodFragment) ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);

                if (apodFragment == null) {
                    return;
                }

                apodFragment.progressBarLayout.setVisibility(View.VISIBLE);
            }

            if (!fallback) {
                queryMorphIoApi(activity, type);
            } else {
                queryNasaGovApi(activity, type);
            }
        }
    }

    public static void queryMorphIoApi(final Activity activity, final int type) {
        ((MainApplication) activity.getApplication()).setApodLoadingData(true);

        String query = String.format(Locale.US, "SELECT * FROM data WHERE date <= date('%d-%02d-%02d') ORDER BY date DESC LIMIT 30", ((MainApplication) activity.getApplication()).getApodCalendar().get(Calendar.YEAR), (((MainApplication) activity.getApplication()).getApodCalendar().get(Calendar.MONTH) + 1), ((MainApplication) activity.getApplication()).getApodCalendar().get(Calendar.DAY_OF_MONTH));
        Call<List<ApodMorphIoModel>> call = ((MainApplication) activity.getApplication()).getApodMorphIoService().get(MORPH_IO_API_KEY, query);
        call.enqueue(new Callback<List<ApodMorphIoModel>>() {
            @Override
            public void onResponse(Call<List<ApodMorphIoModel>> call, Response<List<ApodMorphIoModel>> response) {
                ApodFragment apodFragment = null;

                if (type == TYPE_RECYCLERVIEW) {
                    apodFragment = (ApodFragment) ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);

                    if (apodFragment == null) {
                        return;
                    }
                }

                if (response.isSuccessful()) {
                    boolean getLatestImage = false;

                    if (type == TYPE_RECYCLERVIEW) {
                        apodFragment.footerAdapter.clear();
                        apodFragment.progressBarLayout.setVisibility(View.GONE);
                    }

                    for (ApodMorphIoModel apodMorphIoModel : response.body()) {
                        UniversalImageModel universalImageModel = ModelUtils.convertApodMorphIoModel(apodMorphIoModel);
                        Calendar calendar = DateUtils.convertDateToCalendar(universalImageModel.getDate(), "yyyy-MM-dd");

                        if (type == TYPE_RECYCLERVIEW
                                && ((MainApplication) activity.getApplication()).getApodModels().isEmpty()
                                && ((MainApplication) activity.getApplication()).getApodCalendar().get(Calendar.DAY_OF_YEAR) > calendar.get(Calendar.DAY_OF_YEAR)) {
                            getLatestImage = true;
                        }

                        if (!((MainApplication) activity.getApplication()).getApodModels().contains(universalImageModel)
                                && !universalImageModel.getImageThumbnailUrl().isEmpty()) {
                            ((MainApplication) activity.getApplication()).getApodModels().add(universalImageModel);

                            switch (type) {
                                case TYPE_RECYCLERVIEW:
                                    if (!getLatestImage) {
                                        apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new RecyclerViewAdapter(universalImageModel));
                                    }
                                    break;
                                case TYPE_VIEWPAGER_IMAGE:
                                    ((ImageActivity) activity).imageFragmentStatePagerAdapter.notifyDataSetChanged();
                                    break;
                                case TYPE_VIEWPAGER_INFORMATION:
                                    ((InformationActivity) activity).informationFragmentStatePagerAdapter.notifyDataSetChanged();
                                    break;
                            }
                        }

                        ((MainApplication) activity.getApplication()).setApodCalendar(calendar);
                        ((MainApplication) activity.getApplication()).getApodCalendar().add(Calendar.DAY_OF_YEAR, -1);
                    }

                    ((MainApplication) activity.getApplication()).setApodLoadingData(false);

                    if (type == TYPE_RECYCLERVIEW) {
                        if (getLatestImage) {
                            // If morph.io does not have the latest image yet, then obtain it from nasa.gov.
                            getLatestImage(activity);
                        } else {
                            apodFragment.swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                } else {
                    // If morph.io fails to load the data, then fall back nasa.gov.
                    ((MainApplication) activity.getApplication()).setApodLoadingData(false);
                    beginQuery(activity, type, true);
                }
            }

            @Override
            public void onFailure(Call<List<ApodMorphIoModel>> call, Throwable t) {
                // Again, fall back nasa.gov.
                ((MainApplication) activity.getApplication()).setApodLoadingData(false);
                beginQuery(activity, type, true);
            }
        });
    }

    public static void queryNasaGovApi(final Activity activity, final int type) {
        ((MainApplication) activity.getApplication()).setApodLoadingData(true);

        String date = String.format(Locale.US, "%d-%02d-%02d", ((MainApplication) activity.getApplication()).getApodCalendar().get(Calendar.YEAR), ((MainApplication) activity.getApplication()).getApodCalendar().get(Calendar.MONTH) + 1, ((MainApplication) activity.getApplication()).getApodCalendar().get(Calendar.DAY_OF_MONTH));
        Call<ApodNasaGovModel> call = ((MainApplication) activity.getApplication()).getApodNasaGovService().get(NASA_GOV_API_KEY, date);
        call.enqueue(new Callback<ApodNasaGovModel>() {
            @Override
            public void onResponse(Call<ApodNasaGovModel> call, Response<ApodNasaGovModel> response) {
                ApodFragment apodFragment = null;

                if (type == TYPE_RECYCLERVIEW) {
                    apodFragment = (ApodFragment) ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);

                    if (apodFragment == null) {
                        return;
                    }
                }

                if (type == TYPE_RECYCLERVIEW) {
                    apodFragment.footerAdapter.clear();
                    apodFragment.progressBarLayout.setVisibility(View.GONE);
                }

                if (response.isSuccessful()) {
                    UniversalImageModel universalImageModel = ModelUtils.convertApodNasaGovModel(response.body());

                    if (!((MainApplication) activity.getApplication()).getApodModels().contains(universalImageModel)
                            && !universalImageModel.getImageThumbnailUrl().isEmpty()) {
                        ((MainApplication) activity.getApplication()).getApodModels().add(universalImageModel);

                        switch (type) {
                            case TYPE_RECYCLERVIEW:
                                apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new RecyclerViewAdapter(universalImageModel));
                                break;
                            case TYPE_VIEWPAGER_IMAGE:
                                ((ImageActivity) activity).imageFragmentStatePagerAdapter.notifyDataSetChanged();
                                break;
                            case TYPE_VIEWPAGER_INFORMATION:
                                ((InformationActivity) activity).informationFragmentStatePagerAdapter.notifyDataSetChanged();
                                break;
                        }
                    }

                    ((MainApplication) activity.getApplication()).getApodCalendar().add(Calendar.DAY_OF_YEAR, -1);
                    ((MainApplication) activity.getApplication()).setApodNasaGovApiQueries(((MainApplication) activity.getApplication()).getApodNasaGovApiQueries() - 1);

                    if (((MainApplication) activity.getApplication()).getApodNasaGovApiQueries() > 0) {
                        queryNasaGovApi(activity, type);
                    } else {
                        ((MainApplication) activity.getApplication()).setApodLoadingData(false);
                        ((MainApplication) activity.getApplication()).setApodNasaGovApiQueries(NASA_GOV_API_QUERIES);

                        if (type == TYPE_RECYCLERVIEW) {
                            apodFragment.swipeRefreshLayout.setRefreshing(false);
                        }
                    }

                } else {
                    // Stop here since morph.io should have already been queried.
                    ((MainApplication) activity.getApplication()).setApodLoadingData(false);
                    ((MainApplication) activity.getApplication()).setApodNasaGovApiQueries(NASA_GOV_API_QUERIES);

                    if (type == TYPE_RECYCLERVIEW) {
                        apodFragment.footerAdapter.clear();
                        apodFragment.progressBarLayout.setVisibility(View.GONE);
                        apodFragment.swipeRefreshLayout.setRefreshing(false);
                    }

                }
            }

            @Override
            public void onFailure(Call<ApodNasaGovModel> call, Throwable t) {
                // Again, stop here since morph.io should have already been queried.
                ((MainApplication) activity.getApplication()).setApodLoadingData(false);
                ((MainApplication) activity.getApplication()).setApodNasaGovApiQueries(NASA_GOV_API_QUERIES);

                if (type == TYPE_RECYCLERVIEW) {
                    ApodFragment apodFragment = (ApodFragment) ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);

                    if (apodFragment != null) {
                        apodFragment.footerAdapter.clear();
                        apodFragment.progressBarLayout.setVisibility(View.GONE);
                        apodFragment.swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }
        });
    }

    public static void getLatestImage(final Activity activity) {
        final ApodFragment apodFragment = (ApodFragment) ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);

        if (apodFragment == null) {
            return;
        }

        final Calendar calendar = Calendar.getInstance();
        String date = String.format(Locale.US, "%d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        Call<ApodNasaGovModel> call = ((MainApplication) activity.getApplication()).getApodNasaGovService().get(NASA_GOV_API_KEY, date);
        call.enqueue(new Callback<ApodNasaGovModel>() {
            @Override
            public void onResponse(Call<ApodNasaGovModel> call, Response<ApodNasaGovModel> response) {
                if (response.isSuccessful()) {
                    UniversalImageModel universalImageModel = ModelUtils.convertApodNasaGovModel(response.body());


                    if (DateUtils.convertDateToCalendar(universalImageModel.getDate(), "yyyy-MM-dd").get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)) {
                        if (!((MainApplication) activity.getApplication()).getApodModels().contains(universalImageModel)
                                && !universalImageModel.getImageThumbnailUrl().isEmpty()) {
                            ((MainApplication) activity.getApplication()).getApodModels().add(0, ModelUtils.convertApodNasaGovModel(response.body()));
                        }
                    }
                }

                // Stop here regardless of the result since morph.io was already queried.
                for (UniversalImageModel universalImageModel : ((MainApplication) activity.getApplication()).getApodModels()) {
                    apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new RecyclerViewAdapter(universalImageModel));
                }

                ((MainApplication) activity.getApplication()).setApodLoadingData(false);
                apodFragment.swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ApodNasaGovModel> call, Throwable t) {
                // Again, stop here since morph.io was already queried.
                for (UniversalImageModel universalImageModel : ((MainApplication) activity.getApplication()).getApodModels()) {
                    apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new RecyclerViewAdapter(universalImageModel));
                }

                ((MainApplication) activity.getApplication()).setApodLoadingData(false);
                apodFragment.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public static void getLatestImage(final IntentService intentService, final boolean fallback) {
        Calendar calendar = Calendar.getInstance();

        if (!fallback) {
            String date = String.format(Locale.US, "%d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            Call<ApodNasaGovModel> call = ((MainApplication) intentService.getApplication()).getApodNasaGovService().get(NASA_GOV_API_KEY, date);
            call.enqueue(new Callback<ApodNasaGovModel>() {
                @Override
                public void onResponse(Call<ApodNasaGovModel> call, Response<ApodNasaGovModel> response) {
                    if (response.isSuccessful()) {
                        UniversalImageModel universalImageModel = ModelUtils.convertApodNasaGovModel(response.body());

                        if (!universalImageModel.getImageThumbnailUrl().isEmpty()) {
                            Calendar imageCalendar = DateUtils.convertDateToCalendar(universalImageModel.getDate(), "yyyy-MM-dd");
                            Calendar preferenceCalendar = DateUtils.convertDateToCalendar(PreferenceManager.getDefaultSharedPreferences(intentService).getString(PreferenceUtils.PREF_LAST_APOD_DATE, "1970-01-01"), "yyyy-MM-dd");

                            if (imageCalendar.after(preferenceCalendar)) {
                                ((BackgroundService) intentService).processLatestImage(universalImageModel);
                                PreferenceManager.getDefaultSharedPreferences(intentService).edit().putString(PreferenceUtils.PREF_LAST_APOD_DATE, universalImageModel.getDate()).apply();
                            }
                        }
                    } else {
                        // If nasa.gov fails to load the data, then fall back morph.io.
                        getLatestImage(intentService, true);
                    }
                }

                @Override
                public void onFailure(Call<ApodNasaGovModel> call, Throwable t) {
                    // Again, fall back morph.io.
                    getLatestImage(intentService, true);
                }
            });
        } else {
            String query = String.format(Locale.US, "SELECT * FROM data WHERE date = date('%d-%02d-%02d') ORDER BY date DESC LIMIT 1", calendar.get(Calendar.YEAR), (calendar.get(Calendar.MONTH) + 1), calendar.get(Calendar.DAY_OF_MONTH));
            Call<List<ApodMorphIoModel>> call = ((MainApplication) intentService.getApplication()).getApodMorphIoService().get(MORPH_IO_API_KEY, query);
            call.enqueue(new Callback<List<ApodMorphIoModel>>() {
                @Override
                public void onResponse(Call<List<ApodMorphIoModel>> call, Response<List<ApodMorphIoModel>> response) {
                    if (response.isSuccessful()) {
                        UniversalImageModel universalImageModel = null;

                        if (!response.body().isEmpty()) {
                            universalImageModel = ModelUtils.convertApodMorphIoModel(response.body().get(0));
                        }

                        if (universalImageModel != null
                                && !universalImageModel.getImageThumbnailUrl().isEmpty()) {
                            Calendar imageCalendar = DateUtils.convertDateToCalendar(universalImageModel.getDate(), "yyyy-MM-dd");
                            Calendar preferenceCalendar = DateUtils.convertDateToCalendar(PreferenceManager.getDefaultSharedPreferences(intentService).getString(PreferenceUtils.PREF_LAST_APOD_DATE, "1970-01-01"), "yyyy-MM-dd");

                            if (imageCalendar.after(preferenceCalendar)) {
                                ((BackgroundService) intentService).processLatestImage(universalImageModel);
                                PreferenceManager.getDefaultSharedPreferences(intentService).edit().putString(PreferenceUtils.PREF_LAST_APOD_DATE, universalImageModel.getDate()).apply();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<ApodMorphIoModel>> call, Throwable t) {
                    // Stop here since morph.io should have already been queried.
                }
            });
        }
    }
}
