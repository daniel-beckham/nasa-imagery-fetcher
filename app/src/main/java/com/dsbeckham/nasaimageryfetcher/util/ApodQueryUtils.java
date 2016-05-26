package com.dsbeckham.nasaimageryfetcher.util;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;

import com.dsbeckham.nasaimageryfetcher.BuildConfig;
import com.dsbeckham.nasaimageryfetcher.activity.ImageActivity;
import com.dsbeckham.nasaimageryfetcher.activity.InformationActivity;
import com.dsbeckham.nasaimageryfetcher.adapter.RecyclerViewAdapter;
import com.dsbeckham.nasaimageryfetcher.fragment.ApodFragment;
import com.dsbeckham.nasaimageryfetcher.model.ApodMorphIoModel;
import com.dsbeckham.nasaimageryfetcher.model.ApodNasaGovModel;
import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;
import com.dsbeckham.nasaimageryfetcher.service.BackgroundService;

import org.parceler.Parcels;

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

    public static final int VIEWPAGER_IMAGE = 0;
    public static final int VIEWPAGER_INFORMATION = 1;

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

    public static MorphIoService morphIoService;
    public static NasaGovService nasaGovService;

    public static void setUpIoServices() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(MORPH_IO_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        morphIoService = retrofit.create(MorphIoService.class);

        retrofit = new Retrofit.Builder().baseUrl(NASA_GOV_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        nasaGovService = retrofit.create(NasaGovService.class);
    }

    public static void clearData(Activity activity) {
        ApodFragment apodFragment = (ApodFragment) ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);

        if (apodFragment == null) {
            return;
        }

        if (!apodFragment.loadingData) {
            apodFragment.calendar = Calendar.getInstance();
            apodFragment.endlessRecyclerOnScrollListener.resetPageCount();
            apodFragment.models.clear();
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

        apodFragment.calendar = (Calendar) intent.getSerializableExtra(InformationActivity.EXTRA_CALENDAR);
        apodFragment.models = Parcels.unwrap(intent.getParcelableExtra(InformationActivity.EXTRA_MODELS));

        for (UniversalImageModel universalImageModel : apodFragment.models) {
            apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new RecyclerViewAdapter(universalImageModel));
        }

        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typedValue, true);

        apodFragment.linearLayoutManager.scrollToPositionWithOffset(intent.getIntExtra(InformationActivity.EXTRA_POSITION, 0), activity.getResources().getDimensionPixelSize(typedValue.resourceId));
    }

    public static void beginQuery(Activity activity, boolean fallback) {
        ApodFragment apodFragment = (ApodFragment) ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);

        if (apodFragment == null) {
            return;
        }

        if (fallback) {
            apodFragment.loadingData = false;
        }


        if (!apodFragment.loadingData) {
            if (apodFragment.models.isEmpty()) {
                apodFragment.progressBarLayout.setVisibility(View.VISIBLE);
            }

            if (!fallback) {
                queryMorphIoApi(activity);
            } else {
                queryNasaGovApi(activity);
            }
        }
    }

    public static void beginQuery(Activity activity, int viewPager, boolean fallback) {
        if (viewPager == VIEWPAGER_IMAGE) {
            if (fallback) {
                ((ImageActivity) activity).loadingData = false;
            }

            if (!((ImageActivity) activity).loadingData) {
                if (!fallback) {
                    queryMorphIoApi(activity, viewPager);
                } else {
                    queryNasaGovApi(activity, viewPager);
                }
            }
        } else if (viewPager == VIEWPAGER_INFORMATION) {
            if (fallback) {
                ((InformationActivity) activity).loadingData = false;
            }

            if (!((InformationActivity) activity).loadingData) {
                if (!fallback) {
                    queryMorphIoApi(activity, viewPager);
                } else {
                    queryNasaGovApi(activity, viewPager);
                }
            }
        }
    }

    public static void queryMorphIoApi(final Activity activity) {
        final ApodFragment apodFragment = (ApodFragment) ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);

        if (apodFragment == null) {
            return;
        }

        apodFragment.loadingData = true;

        String query = String.format(Locale.US, "SELECT * FROM data WHERE date <= date('%d-%02d-%02d') ORDER BY date DESC LIMIT 30", apodFragment.calendar.get(Calendar.YEAR), (apodFragment.calendar.get(Calendar.MONTH) + 1), apodFragment.calendar.get(Calendar.DAY_OF_MONTH));
        Call<List<ApodMorphIoModel>> call = morphIoService.get(MORPH_IO_API_KEY, query);
        call.enqueue(new Callback<List<ApodMorphIoModel>>() {
            @Override
            public void onResponse(Call<List<ApodMorphIoModel>> call, Response<List<ApodMorphIoModel>> response) {
                if (response.isSuccessful()) {
                    apodFragment.footerAdapter.clear();
                    apodFragment.progressBarLayout.setVisibility(View.GONE);

                    boolean getLatestImage = false;

                    for (ApodMorphIoModel apodMorphIoModel : response.body()) {
                        Calendar calendar = DateUtils.convertDateToCalendar(apodMorphIoModel.getDate(), "yyyy-MM-dd");
                        UniversalImageModel universalImageModel = ModelUtils.convertApodMorphIoModel(apodMorphIoModel);

                        if (apodFragment.models.isEmpty()) {
                            if (apodFragment.calendar.get(Calendar.DAY_OF_YEAR) > calendar.get(Calendar.DAY_OF_YEAR)) {
                                getLatestImage = true;
                            }
                        }

                        if (!apodFragment.models.contains(universalImageModel) && !apodMorphIoModel.getPictureThumbnailUrl().isEmpty()) {
                            apodFragment.models.add(universalImageModel);

                            if (!getLatestImage) {
                                apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new RecyclerViewAdapter(universalImageModel));
                            }
                        }

                        apodFragment.calendar = calendar;
                        apodFragment.calendar.add(Calendar.DAY_OF_YEAR, -1);
                    }

                    if (getLatestImage) {
                        // If morph.io does not have the latest image yet, then obtain it from nasa.gov.
                        getLatestImage(activity);
                    } else {
                        apodFragment.loadingData = false;
                        apodFragment.swipeRefreshLayout.setRefreshing(false);
                    }
                } else {
                    // If morph.io fails to load the data, then fall back nasa.gov.
                    beginQuery(activity, true);
                }
            }

            @Override
            public void onFailure(Call<List<ApodMorphIoModel>> call, Throwable t) {
                // If morph.io fails to load the data, then fall back nasa.gov.
                beginQuery(activity, true);
            }
        });
    }

    public static void queryMorphIoApi(final Activity activity, final int viewPager) {
        Calendar calendar = Calendar.getInstance();

        if (viewPager == VIEWPAGER_IMAGE) {
            calendar = ((ImageActivity) activity).calendar;
        } else if (viewPager == VIEWPAGER_INFORMATION) {
            calendar = ((InformationActivity) activity).calendar;
        }

        String query = String.format(Locale.US, "SELECT * FROM data WHERE date <= date('%d-%02d-%02d') ORDER BY date DESC LIMIT 30", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        Call<List<ApodMorphIoModel>> call = morphIoService.get(MORPH_IO_API_KEY, query);
        call.enqueue(new Callback<List<ApodMorphIoModel>>() {
            @Override
            public void onResponse(Call<List<ApodMorphIoModel>> call, Response<List<ApodMorphIoModel>> response) {
                if (response.isSuccessful()) {
                    for (ApodMorphIoModel apodMorphIoModel : response.body()) {
                        Calendar calendar = DateUtils.convertDateToCalendar(apodMorphIoModel.getDate(), "yyyy-MM-dd");
                        UniversalImageModel universalImageModel = ModelUtils.convertApodMorphIoModel(apodMorphIoModel);

                        if (viewPager == VIEWPAGER_IMAGE) {
                            if (!((ImageActivity) activity).models.contains(universalImageModel) && !apodMorphIoModel.getPictureThumbnailUrl().isEmpty()) {
                                ((ImageActivity) activity).models.add(universalImageModel);
                            }

                            ((ImageActivity) activity).calendar = calendar;
                            ((ImageActivity) activity).calendar.add(Calendar.DAY_OF_YEAR, -1);
                        } else if (viewPager == VIEWPAGER_INFORMATION) {
                            if (!((InformationActivity) activity).models.contains(universalImageModel) && !apodMorphIoModel.getPictureThumbnailUrl().isEmpty()) {
                                ((InformationActivity) activity).models.add(universalImageModel);
                            }

                            ((InformationActivity) activity).calendar = calendar;
                            ((InformationActivity) activity).calendar.add(Calendar.DAY_OF_YEAR, -1);
                        }
                    }

                    if (viewPager == VIEWPAGER_IMAGE) {
                        ((ImageActivity) activity).loadingData = false;
                        ((ImageActivity) activity).imageFragmentStatePagerAdapter.notifyDataSetChanged();
                    } else if (viewPager == VIEWPAGER_INFORMATION) {
                        ((InformationActivity) activity).loadingData = false;
                        ((InformationActivity) activity).informationFragmentStatePagerAdapter.notifyDataSetChanged();
                    }

                } else {
                    // If morph.io fails to load the data, then fall back nasa.gov.
                    beginQuery(activity, viewPager, true);
                }
            }

            @Override
            public void onFailure(Call<List<ApodMorphIoModel>> call, Throwable t) {
                // If morph.io fails to load the data, then fall back nasa.gov.
                beginQuery(activity, viewPager, true);
            }
        });
    }

    public static void queryNasaGovApi(final Activity activity) {
        final ApodFragment apodFragment = (ApodFragment) ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);

        if (apodFragment == null) {
            return;
        }

        apodFragment.loadingData = true;

        String date = String.format(Locale.US, "%d-%02d-%02d", apodFragment.calendar.get(Calendar.YEAR), apodFragment.calendar.get(Calendar.MONTH) + 1, apodFragment.calendar.get(Calendar.DAY_OF_MONTH));
        Call<ApodNasaGovModel> call = nasaGovService.get(NASA_GOV_API_KEY, date);
        call.enqueue(new Callback<ApodNasaGovModel>() {
            @Override
            public void onResponse(Call<ApodNasaGovModel> call, Response<ApodNasaGovModel> response) {
                if (response.isSuccessful()) {
                    apodFragment.footerAdapter.clear();
                    apodFragment.progressBarLayout.setVisibility(View.GONE);

                    UniversalImageModel universalImageModel = ModelUtils.convertApodNasaGovModel(response.body());

                    if (!apodFragment.models.contains(universalImageModel) && response.body().getMediaType().equals("image")) {
                        apodFragment.models.add(universalImageModel);
                        apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new RecyclerViewAdapter(universalImageModel));
                    }

                    apodFragment.calendar.add(Calendar.DAY_OF_YEAR, -1);
                    apodFragment.nasaGovApiQueries--;

                    if (apodFragment.nasaGovApiQueries > 0) {
                        queryNasaGovApi(activity);
                    } else {
                        apodFragment.loadingData = false;
                        apodFragment.nasaGovApiQueries = NASA_GOV_API_QUERIES;
                        apodFragment.swipeRefreshLayout.setRefreshing(false);
                    }

                } else {
                    apodFragment.footerAdapter.clear();
                    apodFragment.loadingData = false;
                    apodFragment.nasaGovApiQueries = NASA_GOV_API_QUERIES;
                    apodFragment.progressBarLayout.setVisibility(View.GONE);
                    apodFragment.swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<ApodNasaGovModel> call, Throwable t) {
                apodFragment.footerAdapter.clear();
                apodFragment.loadingData = false;
                apodFragment.nasaGovApiQueries = NASA_GOV_API_QUERIES;
                apodFragment.progressBarLayout.setVisibility(View.GONE);
                apodFragment.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public static void queryNasaGovApi(final Activity activity, final int viewPager) {
        Calendar calendar = Calendar.getInstance();

        if (viewPager == VIEWPAGER_IMAGE) {
            calendar = ((ImageActivity) activity).calendar;
        } else if (viewPager == VIEWPAGER_INFORMATION) {
            calendar = ((InformationActivity) activity).calendar;
        }

        String date = String.format(Locale.US, "%d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        Call<ApodNasaGovModel> call = nasaGovService.get(NASA_GOV_API_KEY, date);
        call.enqueue(new Callback<ApodNasaGovModel>() {
            @Override
            public void onResponse(Call<ApodNasaGovModel> call, Response<ApodNasaGovModel> response) {
                if (response.isSuccessful()) {
                    UniversalImageModel universalImageModel = ModelUtils.convertApodNasaGovModel(response.body());

                    if (viewPager == VIEWPAGER_IMAGE) {
                        if (!((ImageActivity) activity).models.contains(universalImageModel) && !response.body().getUrl().isEmpty()) {
                            ((ImageActivity) activity).models.add(universalImageModel);
                        }

                        ((ImageActivity) activity).calendar.add(Calendar.DAY_OF_YEAR, -1);
                        ((ImageActivity) activity).nasaGovApiQueries--;

                        if (((ImageActivity) activity).nasaGovApiQueries > 0) {
                            queryNasaGovApi(activity, viewPager);
                        } else {
                            ((ImageActivity) activity).loadingData = false;
                            ((ImageActivity) activity).nasaGovApiQueries = NASA_GOV_API_QUERIES;
                        }

                        ((ImageActivity) activity).imageFragmentStatePagerAdapter.notifyDataSetChanged();
                     } else if (viewPager == VIEWPAGER_INFORMATION) {
                        if (!((InformationActivity) activity).models.contains(universalImageModel) && !response.body().getUrl().isEmpty()) {
                            ((InformationActivity) activity).models.add(universalImageModel);
                        }

                        ((InformationActivity) activity).calendar.add(Calendar.DAY_OF_YEAR, -1);
                        ((InformationActivity) activity).nasaGovApiQueries--;

                        if (((InformationActivity) activity).nasaGovApiQueries > 0) {
                            queryNasaGovApi(activity, viewPager);
                        } else {
                            ((InformationActivity) activity).loadingData = false;
                            ((InformationActivity) activity).nasaGovApiQueries = NASA_GOV_API_QUERIES;
                        }

                        ((InformationActivity) activity).informationFragmentStatePagerAdapter.notifyDataSetChanged();
                    }
                } else {
                    if (viewPager == VIEWPAGER_IMAGE) {
                        ((ImageActivity) activity).loadingData = false;
                        ((ImageActivity) activity).nasaGovApiQueries = NASA_GOV_API_QUERIES;
                    } else if (viewPager == VIEWPAGER_INFORMATION) {
                        ((InformationActivity) activity).loadingData = false;
                        ((InformationActivity) activity).nasaGovApiQueries = NASA_GOV_API_QUERIES;
                    }
                }
            }

            @Override
            public void onFailure(Call<ApodNasaGovModel> call, Throwable t) {
                if (viewPager == VIEWPAGER_IMAGE) {
                    ((ImageActivity) activity).loadingData = false;
                    ((ImageActivity) activity).nasaGovApiQueries = NASA_GOV_API_QUERIES;
                } else if (viewPager == VIEWPAGER_INFORMATION) {
                    ((InformationActivity) activity).loadingData = false;
                    ((InformationActivity) activity).nasaGovApiQueries = NASA_GOV_API_QUERIES;
                }
            }
        });
    }

    public static void getLatestImage(final Activity activity) {
        final Calendar calendar = Calendar.getInstance();

        final ApodFragment apodFragment = (ApodFragment) ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);

        if (apodFragment == null) {
            return;
        }

        String date = String.format(Locale.US, "%d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        Call<ApodNasaGovModel> call = nasaGovService.get(NASA_GOV_API_KEY, date);
        call.enqueue(new Callback<ApodNasaGovModel>() {
            @Override
            public void onResponse(Call<ApodNasaGovModel> call, Response<ApodNasaGovModel> response) {
                if (response.isSuccessful()) {
                    if (DateUtils.convertDateToCalendar(response.body().getDate(), "yyyy-MM-dd").get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)) {
                        apodFragment.models.add(0, ModelUtils.convertApodNasaGovModel(response.body()));

                        for (UniversalImageModel universalImageModel : apodFragment.models) {
                            apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new RecyclerViewAdapter(universalImageModel));
                        }
                    }
                }

                apodFragment.loadingData = false;
                apodFragment.swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ApodNasaGovModel> call, Throwable t) {
                apodFragment.loadingData = false;
                apodFragment.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public static void getLatestImage(final IntentService intentService, final boolean fallback) {
        Calendar calendar = Calendar.getInstance();

        if (!fallback) {
            String date = String.format(Locale.US, "%d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            Call<ApodNasaGovModel> call = nasaGovService.get(NASA_GOV_API_KEY, date);
            call.enqueue(new Callback<ApodNasaGovModel>() {
                @Override
                public void onResponse(Call<ApodNasaGovModel> call, Response<ApodNasaGovModel> response) {
                    if (response.isSuccessful()) {
                        UniversalImageModel universalImageModel = ModelUtils.convertApodNasaGovModel(response.body());

                        if (universalImageModel != null) {
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
                    // If nasa.gov fails to load the data, then fall back morph.io.
                    getLatestImage(intentService, true);
                }
            });
        } else {
            String query = String.format(Locale.US, "SELECT * FROM data WHERE date = date('%d-%02d-%02d') ORDER BY date DESC LIMIT 1", calendar.get(Calendar.YEAR), (calendar.get(Calendar.MONTH) + 1), calendar.get(Calendar.DAY_OF_MONTH));
            Call<List<ApodMorphIoModel>> call = morphIoService.get(MORPH_IO_API_KEY, query);
            call.enqueue(new Callback<List<ApodMorphIoModel>>() {
                @Override
                public void onResponse(Call<List<ApodMorphIoModel>> call, Response<List<ApodMorphIoModel>> response) {
                    if (response.isSuccessful()) {
                        UniversalImageModel universalImageModel = null;

                        if (!response.body().isEmpty()) {
                            universalImageModel = ModelUtils.convertApodMorphIoModel(response.body().get(0));
                        }

                        if (universalImageModel != null) {
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
                public void onFailure(Call<List<ApodMorphIoModel>> call, Throwable t) {}
            });
        }
    }
}
