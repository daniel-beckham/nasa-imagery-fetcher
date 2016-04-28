package com.dsbeckham.nasaimageryfetcher.util;

import android.app.Activity;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.View;

import com.dsbeckham.nasaimageryfetcher.BuildConfig;
import com.dsbeckham.nasaimageryfetcher.activity.ViewPagerActivity;
import com.dsbeckham.nasaimageryfetcher.adapter.ApodAdapter;
import com.dsbeckham.nasaimageryfetcher.fragment.ApodFragment;
import com.dsbeckham.nasaimageryfetcher.model.ApodMorphIoModel;
import com.dsbeckham.nasaimageryfetcher.model.ApodNasaGovModel;

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
    public static final String NASA_GOV_BASE_URL = "https://api.nasa.gov/";
    public static final String NASA_GOV_API_KEY = BuildConfig.NASA_GOV_API_KEY;
    public static final int NASA_GOV_API_QUERIES = 5;

    public static final String MORPH_IO_BASE_URL = "https://api.morph.io/";
    public static final String MORPH_IO_API_KEY = BuildConfig.MORPH_IO_API_KEY;

    public static final int MODEL_MORPH_IO = 0;
    public static final int MODEL_NASA_GOV = 1;

    public static final int QUERY_MODE_RECYCLERVIEW = 0;
    public static final int QUERY_MODE_VIEWPAGER = 1;

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

    public static void beginQuery(Activity activity, int mode) {
        if (mode == QUERY_MODE_RECYCLERVIEW) {
            ApodFragment apodFragment = (ApodFragment) activity.getFragmentManager().findFragmentByTag("apod");

            if (apodFragment == null) {
                return;
            }

            if (!apodFragment.loadingData) {
                if (apodFragment.morphIoModels.isEmpty()) {
                    apodFragment.progressBar.setVisibility(View.VISIBLE);
                }

                switch (PreferenceManager.getDefaultSharedPreferences(activity).getString(PreferenceUtils.PREF_APOD_FETCH_SERVICE, "")) {
                    case "morph_io":
                        queryMorphIoApi(activity, QUERY_MODE_RECYCLERVIEW);
                        break;
                    case "nasa_gov":
                        apodFragment.nasaGovApiQueries = NASA_GOV_API_QUERIES;
                        queryNasaGovApi(activity, QUERY_MODE_RECYCLERVIEW);
                        break;
                }
            }
        } else if (mode == QUERY_MODE_VIEWPAGER) {
            final ViewPagerActivity viewPagerActivity = (ViewPagerActivity) activity;

            if (viewPagerActivity == null) {
                return;
            }

            if (!viewPagerActivity.loadingData) {
                switch (PreferenceManager.getDefaultSharedPreferences(activity).getString(PreferenceUtils.PREF_APOD_FETCH_SERVICE, "")) {
                    case "morph_io":
                        queryMorphIoApi(activity, QUERY_MODE_VIEWPAGER);
                        break;
                    case "nasa_gov":
                        viewPagerActivity.nasaGovApiQueries = NASA_GOV_API_QUERIES;
                        queryNasaGovApi(activity, QUERY_MODE_VIEWPAGER);
                        break;
                }
            }
        }
    }

    public static void clearData(Activity activity) {
        ApodFragment apodFragment = (ApodFragment) activity.getFragmentManager().findFragmentByTag("apod");

        if (apodFragment == null) {
            return;
        }

        if (!apodFragment.loadingData) {
            apodFragment.morphIoModels.clear();
            apodFragment.nasaGovModels.clear();
            apodFragment.calendar = Calendar.getInstance();
            apodFragment.endlessRecyclerOnScrollListener.resetPageCount();
            apodFragment.fastItemAdapter.clear();
            apodFragment.footerAdapter.clear();
        }
    }

    public static void queryMorphIoApi(final Activity activity, final int mode) {
        if (mode == QUERY_MODE_RECYCLERVIEW) {
            final ApodFragment apodFragment = (ApodFragment) activity.getFragmentManager().findFragmentByTag("apod");

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
                        apodFragment.progressBar.setVisibility(View.GONE);

                        for (ApodMorphIoModel apodMorphIoModel : response.body()) {
                            if (!apodFragment.morphIoModels.contains(apodMorphIoModel) && !apodMorphIoModel.getPictureThumbnailUrl().isEmpty()) {
                                apodFragment.morphIoModels.add(apodMorphIoModel);
                                apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new ApodAdapter<>(apodMorphIoModel, ApodQueryUtils.MODEL_MORPH_IO));
                            }

                            apodFragment.calendar.add(Calendar.DAY_OF_YEAR, -1);
                        }

                        apodFragment.loadingData = false;
                        apodFragment.swipeRefreshLayout.setRefreshing(false);
                    }
                }

                @Override
                public void onFailure(Call<List<ApodMorphIoModel>> call, Throwable t) {
                    apodFragment.footerAdapter.clear();
                    apodFragment.loadingData = false;
                    apodFragment.progressBar.setVisibility(View.GONE);
                    apodFragment.swipeRefreshLayout.setRefreshing(false);
                }
            });
        } else if (mode == QUERY_MODE_VIEWPAGER) {
            final ViewPagerActivity viewPagerActivity = (ViewPagerActivity) activity;

            if (viewPagerActivity == null) {
                return;
            }

            String query = String.format(Locale.US, "SELECT * FROM data WHERE date <= date('%d-%02d-%02d') ORDER BY date DESC LIMIT 30", viewPagerActivity.calendar.get(Calendar.YEAR), (viewPagerActivity.calendar.get(Calendar.MONTH) + 1), viewPagerActivity.calendar.get(Calendar.DAY_OF_MONTH));
            Call<List<ApodMorphIoModel>> call = morphIoService.get(MORPH_IO_API_KEY, query);
            call.enqueue(new Callback<List<ApodMorphIoModel>>() {
                @Override
                public void onResponse(Call<List<ApodMorphIoModel>> call, Response<List<ApodMorphIoModel>> response) {
                    if (response.isSuccessful()) {
                        for (ApodMorphIoModel apodMorphIoModel : response.body()) {
                            if (!viewPagerActivity.apodMorphIoModels.contains(apodMorphIoModel) && !apodMorphIoModel.getPictureThumbnailUrl().isEmpty()) {
                                viewPagerActivity.apodMorphIoModels.add(apodMorphIoModel);
                            }

                            viewPagerActivity.calendar.add(Calendar.DAY_OF_YEAR, -1);
                        }

                        viewPagerActivity.loadingData = false;
                        viewPagerActivity.imageFragmentStatePagerAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<List<ApodMorphIoModel>> call, Throwable t) {
                    viewPagerActivity.loadingData = false;
                }
            });
        }
    }

    public static void queryNasaGovApi(final Activity activity, final int mode) {
        if (mode == QUERY_MODE_RECYCLERVIEW) {
            final ApodFragment apodFragment = (ApodFragment) activity.getFragmentManager().findFragmentByTag("apod");

            if (apodFragment == null) {
                return;
            }

            apodFragment.loadingData = true;

            String date = String.format(Locale.US, "%d-%02d-%02d", apodFragment.calendar.get(Calendar.YEAR), (apodFragment.calendar.get(Calendar.MONTH) + 1), apodFragment.calendar.get(Calendar.DAY_OF_MONTH));
            Call<ApodNasaGovModel> call = nasaGovService.get(NASA_GOV_API_KEY, date);
            call.enqueue(new Callback<ApodNasaGovModel>() {
                @Override
                public void onResponse(Call<ApodNasaGovModel> call, Response<ApodNasaGovModel> response) {
                    if (response.isSuccessful()) {
                        apodFragment.footerAdapter.clear();
                        apodFragment.progressBar.setVisibility(View.GONE);

                        if (!apodFragment.nasaGovModels.contains(response.body()) && response.body().getMediaType().equals("image")) {
                            apodFragment.nasaGovModels.add(response.body());
                            apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new ApodAdapter<>(response.body(), ApodQueryUtils.MODEL_NASA_GOV));
                        }

                        apodFragment.calendar.add(Calendar.DAY_OF_YEAR, -1);
                        apodFragment.nasaGovApiQueries--;

                        if (apodFragment.nasaGovApiQueries > 0) {
                            queryNasaGovApi(activity, mode);
                        } else {
                            apodFragment.loadingData = false;
                            apodFragment.swipeRefreshLayout.setRefreshing(false);
                        }

                    }
                }

                @Override
                public void onFailure(Call<ApodNasaGovModel> call, Throwable t) {
                    apodFragment.footerAdapter.clear();
                    apodFragment.loadingData = false;
                    apodFragment.progressBar.setVisibility(View.GONE);
                    apodFragment.swipeRefreshLayout.setRefreshing(false);
                }
            });
        } else if (mode == QUERY_MODE_VIEWPAGER) {
            final ViewPagerActivity viewPagerActivity = (ViewPagerActivity) activity;

            if (viewPagerActivity == null) {
                return;
            }

            viewPagerActivity.loadingData = true;

            String date = String.format(Locale.US, "%d-%02d-%02d", viewPagerActivity.calendar.get(Calendar.YEAR), (viewPagerActivity.calendar.get(Calendar.MONTH) + 1), viewPagerActivity.calendar.get(Calendar.DAY_OF_MONTH));
            Call<ApodNasaGovModel> call = nasaGovService.get(NASA_GOV_API_KEY, date);
            call.enqueue(new Callback<ApodNasaGovModel>() {
                @Override
                public void onResponse(Call<ApodNasaGovModel> call, Response<ApodNasaGovModel> response) {
                    if (response.isSuccessful()) {
                        if (!viewPagerActivity.apodNasaGovModels.contains(response.body()) && !response.body().getUrl().isEmpty()) {
                            viewPagerActivity.apodNasaGovModels.add(response.body());
                        }

                        viewPagerActivity.calendar.add(Calendar.DAY_OF_YEAR, -1);
                        viewPagerActivity.nasaGovApiQueries--;

                        if (viewPagerActivity.nasaGovApiQueries > 0) {
                            queryNasaGovApi(activity, mode);
                        } else {
                            viewPagerActivity.loadingData = false;
                        }

                        viewPagerActivity.imageFragmentStatePagerAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<ApodNasaGovModel> call, Throwable t) {
                    viewPagerActivity.loadingData = false;
                }
            });
        }
    }

    public static void updateData(Activity activity, Intent intent) {
        ApodFragment apodFragment = (ApodFragment) activity.getFragmentManager().findFragmentByTag("apod");

        if (apodFragment == null) {
            return;
        }

        apodFragment.calendar = (Calendar) intent.getSerializableExtra(ApodFragment.EXTRA_APOD_CALENDAR);
        apodFragment.fastItemAdapter.clear();

        switch (PreferenceManager.getDefaultSharedPreferences(activity).getString(PreferenceUtils.PREF_APOD_FETCH_SERVICE, "")) {
            case "morph_io":
                apodFragment.morphIoModels = Parcels.unwrap(intent.getParcelableExtra(ApodFragment.EXTRA_APOD_MORPH_IO_MODELS));

                for (ApodMorphIoModel apodMorphIoModel : apodFragment.morphIoModels) {
                    apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new ApodAdapter<>(apodMorphIoModel, ApodQueryUtils.MODEL_MORPH_IO));
                }
                break;
            case "nasa_gov":
                apodFragment.nasaGovModels = Parcels.unwrap(intent.getParcelableExtra(ApodFragment.EXTRA_APOD_NASA_GOV_MODELS));

                for (ApodNasaGovModel apodNasaGovModel : apodFragment.nasaGovModels) {
                    apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new ApodAdapter<>(apodNasaGovModel, ApodQueryUtils.MODEL_NASA_GOV));
                }
                break;
        }

        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typedValue, true);

        apodFragment.linearLayoutManager.scrollToPositionWithOffset(intent.getIntExtra(ApodFragment.EXTRA_APOD_POSITION, 0), activity.getResources().getDimensionPixelSize(typedValue.resourceId));
    }
}
