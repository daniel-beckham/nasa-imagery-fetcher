package com.dsbeckham.nasaimageryfetcher.util;

import android.app.Activity;
import android.content.Intent;
import android.preference.PreferenceManager;
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
        ApodFragment apodFragment = (ApodFragment) activity.getFragmentManager().findFragmentByTag("apod");

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
        ApodFragment apodFragment = (ApodFragment) activity.getFragmentManager().findFragmentByTag("apod");

        if (apodFragment == null) {
            return;
        }

        apodFragment.fastItemAdapter.clear();

        apodFragment.calendar = (Calendar) intent.getSerializableExtra(ApodFragment.EXTRA_APOD_CALENDAR);
        apodFragment.models = Parcels.unwrap(intent.getParcelableExtra(ApodFragment.EXTRA_APOD_MODELS));

        for (UniversalImageModel universalImageModel : apodFragment.models) {
            apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new RecyclerViewAdapter(universalImageModel));
        }

        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typedValue, true);

        apodFragment.linearLayoutManager.scrollToPositionWithOffset(intent.getIntExtra(ApodFragment.EXTRA_APOD_POSITION, 0), activity.getResources().getDimensionPixelSize(typedValue.resourceId));
    }

    public static void beginQuery(Activity activity) {
        ApodFragment apodFragment = (ApodFragment) activity.getFragmentManager().findFragmentByTag("apod");

        if (apodFragment == null) {
            return;
        }

        if (!apodFragment.loadingData) {
            if (apodFragment.models.isEmpty()) {
                apodFragment.progressBarLayout.setVisibility(View.VISIBLE);
            }

            switch (PreferenceManager.getDefaultSharedPreferences(activity).getString(PreferenceUtils.PREF_APOD_FETCH_SERVICE, "")) {
                case "morph_io":
                    queryMorphIoApi(activity);
                    break;
                case "nasa_gov":
                    apodFragment.nasaGovApiQueries = NASA_GOV_API_QUERIES;
                    queryNasaGovApi(activity);
                    break;
            }
        }
    }

    public static void beginQuery(Activity activity, int viewpager) {
        if (viewpager == VIEWPAGER_IMAGE) {
            if (!((ImageActivity) activity).loadingData) {
                switch (PreferenceManager.getDefaultSharedPreferences(activity).getString(PreferenceUtils.PREF_APOD_FETCH_SERVICE, "")) {
                    case "morph_io":
                        queryMorphIoApi(activity, viewpager);
                        break;
                    case "nasa_gov":
                        ((ImageActivity) activity).nasaGovApiQueries = NASA_GOV_API_QUERIES;
                        queryNasaGovApi(activity, viewpager);
                        break;
                }
            }
        } else if (viewpager == VIEWPAGER_INFORMATION) {
            final InformationActivity informationActivity = (InformationActivity) activity;

            if (informationActivity == null) {
                return;
            }

            if (!((InformationActivity) activity).loadingData) {
                switch (PreferenceManager.getDefaultSharedPreferences(activity).getString(PreferenceUtils.PREF_APOD_FETCH_SERVICE, "")) {
                    case "morph_io":
                        queryMorphIoApi(activity, viewpager);
                        break;
                    case "nasa_gov":
                        ((InformationActivity) activity).nasaGovApiQueries = NASA_GOV_API_QUERIES;
                        queryNasaGovApi(activity, viewpager);
                        break;
                }
            }
        }
    }

    public static void queryMorphIoApi(final Activity activity) {
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
                    apodFragment.progressBarLayout.setVisibility(View.GONE);

                    for (ApodMorphIoModel apodMorphIoModel : response.body()) {
                        UniversalImageModel universalImageModel = ModelUtils.convertApodMorphIoModel(apodMorphIoModel);

                        if (!apodFragment.models.contains(universalImageModel) && !apodMorphIoModel.getPictureThumbnailUrl().isEmpty()) {
                            apodFragment.models.add(universalImageModel);
                            apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new RecyclerViewAdapter(universalImageModel));
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
                apodFragment.progressBarLayout.setVisibility(View.GONE);
                apodFragment.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public static void queryMorphIoApi(final Activity activity, final int viewPager) {
        Calendar calendar = Calendar.getInstance();

        if (viewPager == VIEWPAGER_IMAGE) {
            calendar = ((ImageActivity) activity).apodCalendar;
        } else if (viewPager == VIEWPAGER_INFORMATION) {
            calendar = ((InformationActivity) activity).apodCalendar;
        }

        String query = String.format(Locale.US, "SELECT * FROM data WHERE date <= date('%d-%02d-%02d') ORDER BY date DESC LIMIT 30", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        Call<List<ApodMorphIoModel>> call = morphIoService.get(MORPH_IO_API_KEY, query);
        call.enqueue(new Callback<List<ApodMorphIoModel>>() {
            @Override
            public void onResponse(Call<List<ApodMorphIoModel>> call, Response<List<ApodMorphIoModel>> response) {
                if (response.isSuccessful()) {
                    for (ApodMorphIoModel apodMorphIoModel : response.body()) {
                        UniversalImageModel universalImageModel = ModelUtils.convertApodMorphIoModel(apodMorphIoModel);

                        if (viewPager == VIEWPAGER_IMAGE) {
                            if (!((ImageActivity) activity).apodModels.contains(universalImageModel) && !apodMorphIoModel.getPictureThumbnailUrl().isEmpty()) {
                                ((ImageActivity) activity).apodModels.add(universalImageModel);
                            }

                            ((ImageActivity) activity).apodCalendar.add(Calendar.DAY_OF_YEAR, -1);
                        } else if (viewPager == VIEWPAGER_INFORMATION) {
                            if (!((InformationActivity) activity).apodModels.contains(universalImageModel) && !apodMorphIoModel.getPictureThumbnailUrl().isEmpty()) {
                                ((InformationActivity) activity).apodModels.add(universalImageModel);
                            }

                            ((InformationActivity) activity).apodCalendar.add(Calendar.DAY_OF_YEAR, -1);
                        }
                    }

                    if (viewPager == VIEWPAGER_IMAGE) {
                        ((ImageActivity) activity).loadingData = false;
                        ((ImageActivity) activity).imageFragmentStatePagerAdapter.notifyDataSetChanged();
                    } else if (viewPager == VIEWPAGER_INFORMATION) {
                        ((InformationActivity) activity).loadingData = false;
                        ((InformationActivity) activity).informationFragmentStatePagerAdapter.notifyDataSetChanged();
                    }

                }
            }

            @Override
            public void onFailure(Call<List<ApodMorphIoModel>> call, Throwable t) {
                if (viewPager == VIEWPAGER_IMAGE) {
                    ((ImageActivity) activity).loadingData = false;
                } else if (viewPager == VIEWPAGER_INFORMATION) {
                    ((InformationActivity) activity).loadingData = false;
                }
            }
        });
    }

    public static void queryNasaGovApi(final Activity activity) {
        final ApodFragment apodFragment = (ApodFragment) activity.getFragmentManager().findFragmentByTag("apod");

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
                        apodFragment.swipeRefreshLayout.setRefreshing(false);
                    }

                }
            }

            @Override
            public void onFailure(Call<ApodNasaGovModel> call, Throwable t) {
                apodFragment.footerAdapter.clear();
                apodFragment.loadingData = false;
                apodFragment.progressBarLayout.setVisibility(View.GONE);
                apodFragment.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public static void queryNasaGovApi(final Activity activity, final int viewPager) {
        Calendar calendar = Calendar.getInstance();

        if (viewPager == VIEWPAGER_IMAGE) {
            calendar = ((ImageActivity) activity).apodCalendar;
        } else if (viewPager == VIEWPAGER_INFORMATION) {
            calendar = ((InformationActivity) activity).apodCalendar;
        }

        String date = String.format(Locale.US, "%d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        Call<ApodNasaGovModel> call = nasaGovService.get(NASA_GOV_API_KEY, date);
        call.enqueue(new Callback<ApodNasaGovModel>() {
            @Override
            public void onResponse(Call<ApodNasaGovModel> call, Response<ApodNasaGovModel> response) {
                if (response.isSuccessful()) {
                    UniversalImageModel universalImageModel = ModelUtils.convertApodNasaGovModel(response.body());

                    if (viewPager == VIEWPAGER_IMAGE) {
                        if (!((ImageActivity) activity).apodModels.contains(universalImageModel) && !response.body().getUrl().isEmpty()) {
                            ((ImageActivity) activity).apodModels.add(universalImageModel);
                        }

                        ((ImageActivity) activity).apodCalendar.add(Calendar.DAY_OF_YEAR, -1);
                        ((ImageActivity) activity).nasaGovApiQueries--;

                        if (((ImageActivity) activity).nasaGovApiQueries > 0) {
                            queryNasaGovApi(activity, viewPager);
                        } else {
                            ((ImageActivity) activity).loadingData = false;
                        }

                        ((ImageActivity) activity).imageFragmentStatePagerAdapter.notifyDataSetChanged();
                     } else if (viewPager == VIEWPAGER_INFORMATION) {
                        if (!((InformationActivity) activity).apodModels.contains(universalImageModel) && !response.body().getUrl().isEmpty()) {
                            ((InformationActivity) activity).apodModels.add(universalImageModel);
                        }

                        ((InformationActivity) activity).apodCalendar.add(Calendar.DAY_OF_YEAR, -1);
                        ((InformationActivity) activity).nasaGovApiQueries--;

                        if (((InformationActivity) activity).nasaGovApiQueries > 0) {
                            queryNasaGovApi(activity, viewPager);
                        } else {
                            ((InformationActivity) activity).loadingData = false;
                        }

                        ((InformationActivity) activity).informationFragmentStatePagerAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApodNasaGovModel> call, Throwable t) {
                if (viewPager == VIEWPAGER_IMAGE) {
                    ((ImageActivity) activity).loadingData = false;
                } else if (viewPager == VIEWPAGER_INFORMATION) {
                    ((InformationActivity) activity).loadingData = false;
                }
            }
        });
    }
}
