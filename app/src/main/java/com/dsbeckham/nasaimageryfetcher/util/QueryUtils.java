package com.dsbeckham.nasaimageryfetcher.util;

import android.app.Activity;
import android.view.View;

import com.dsbeckham.nasaimageryfetcher.BuildConfig;
import com.dsbeckham.nasaimageryfetcher.adapter.ApodAdapter;
import com.dsbeckham.nasaimageryfetcher.adapter.IotdAdapter;
import com.dsbeckham.nasaimageryfetcher.fragment.ApodFragment;
import com.dsbeckham.nasaimageryfetcher.fragment.IotdFragment;
import com.dsbeckham.nasaimageryfetcher.model.ApodMorphIoModel;
import com.dsbeckham.nasaimageryfetcher.model.ApodNasaModel;
import com.dsbeckham.nasaimageryfetcher.model.IotdRssModel;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class QueryUtils {
    public static final String APOD_NASA_BASE_URL = "https://api.nasa.gov/";
    public static final String APOD_NASA_API_KEY = BuildConfig.APOD_NASA_API_KEY;
    public static final int APOD_NASA_API_QUERIES = 5;

    public static final String APOD_MORPH_IO_BASE_URL = "https://api.morph.io/";
    public static final String APOD_MORPH_IO_API_KEY = BuildConfig.APOD_MORPH_IO_API_KEY;

    public static final String IOTD_RSS_BASE_URL = "https://www.nasa.gov/";

    public static final int APOD_MODEL_MORPH_IO = 0;
    public static final int APOD_MODEL_NASA = 1;

    public interface ApodMorphIoService {
        @GET("dsbeckham/apod-scraper/data.json")
        Call<List<ApodMorphIoModel>> get(
                @Query("key") String key,
                @Query("query") String query);
    }

    public interface ApodNasaService {
        @GET("planetary/apod")
        Call<ApodNasaModel> get(
                @Query("api_key") String apiKey,
                @Query("date") String date);
    }

    public interface IotdRssService {
        @GET("rss/dyn/lg_image_of_the_day.rss")
        Call<IotdRssModel> get();
    }

    public static ApodMorphIoService apodMorphIoService;
    public static ApodNasaService apodNasaService;
    public static IotdRssService iotdRssService;

    public static void setUpIoServices() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(APOD_MORPH_IO_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apodMorphIoService = retrofit.create(ApodMorphIoService.class);

        retrofit = new Retrofit.Builder().baseUrl(APOD_NASA_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apodNasaService = retrofit.create(ApodNasaService.class);

        retrofit = new Retrofit.Builder().baseUrl(IOTD_RSS_BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.createNonStrict())
                .build();

        iotdRssService = retrofit.create(IotdRssService.class);
    }

    public static void beginApodQuery(Activity activity) {
        ApodFragment apodFragment = (ApodFragment) activity.getFragmentManager().findFragmentByTag("apod");

        if (apodFragment == null) {
            return;
        }

        if (!apodFragment.loadingData) {
            if (apodFragment.apodMorphIoModels.isEmpty()) {
                apodFragment.progressBar.setVisibility(View.VISIBLE);
            }

            // Add a check here that determines which API should be used based
            // on the user settings. (Also, add the relevant setting.)
            queryApodMorphIoApi(activity);
            // If the NASA API is being used, reset the query count first.
            // apodFragment.nasaApiQueryCount = APOD_NASA_API_QUERIES;
            // queryApodNasaApi(activity);
        }
    }

    public static void clearApodData(Activity activity) {
        ApodFragment apodFragment = (ApodFragment) activity.getFragmentManager().findFragmentByTag("apod");

        if (apodFragment == null) {
            return;
        }

        if (!apodFragment.loadingData) {
            apodFragment.apodMorphIoModels.clear();
            apodFragment.apodNasaModels.clear();
            apodFragment.calendar = Calendar.getInstance();
            apodFragment.fastItemAdapter.clear();
            apodFragment.footerAdapter.clear();
        }
    }

    public static void queryApodMorphIoApi(final Activity activity) {
        final ApodFragment apodFragment = (ApodFragment) activity.getFragmentManager().findFragmentByTag("apod");

        if (apodFragment == null) {
            return;
        }

        apodFragment.loadingData = true;

        String query =  String.format(Locale.US, "SELECT * FROM data WHERE date <= date('%d-%02d-%02d') ORDER BY date DESC LIMIT 30", apodFragment.calendar.get(Calendar.YEAR), (apodFragment.calendar.get(Calendar.MONTH) + 1), apodFragment.calendar.get(Calendar.DAY_OF_MONTH));
        Call<List<ApodMorphIoModel>> call = apodMorphIoService.get(APOD_MORPH_IO_API_KEY, query);
        call.enqueue(new Callback<List<ApodMorphIoModel>>() {
            @Override
            public void onResponse (Call<List<ApodMorphIoModel>> call, Response<List<ApodMorphIoModel>> response) {
                if (response.isSuccessful()) {
                    apodFragment.footerAdapter.clear();
                    apodFragment.progressBar.setVisibility(View.GONE);

                    for (ApodMorphIoModel apodMorphIoModel : response.body()) {

                        if (!apodFragment.apodMorphIoModels.contains(apodMorphIoModel) && !apodMorphIoModel.getPictureThumbnailUrl().isEmpty()) {
                            apodFragment.apodMorphIoModels.add(apodMorphIoModel);
                            apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new ApodAdapter<>(apodMorphIoModel, QueryUtils.APOD_MODEL_MORPH_IO));
                        }

                        apodFragment.calendar.add(Calendar.DAY_OF_YEAR, -1);
                    }

                    apodFragment.loadingData = false;
                    apodFragment.swipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onFailure (Call<List<ApodMorphIoModel>> call, Throwable t) {
                apodFragment.footerAdapter.clear();
                apodFragment.loadingData = false;
                apodFragment.progressBar.setVisibility(View.GONE);
                apodFragment.swipeContainer.setRefreshing(false);
            }
        });
    }

    public static void queryApodNasaApi(final Activity activity) {
        final ApodFragment apodFragment = (ApodFragment) activity.getFragmentManager().findFragmentByTag("apod");

        if (apodFragment == null) {
            return;
        }

        apodFragment.loadingData = true;

        String date = String.format(Locale.US, "%d-%02d-%02d", apodFragment.calendar.get(Calendar.YEAR), (apodFragment.calendar.get(Calendar.MONTH) + 1), apodFragment.calendar.get(Calendar.DAY_OF_MONTH));
        Call<ApodNasaModel> call = apodNasaService.get(APOD_NASA_API_KEY, date);
        call.enqueue(new Callback<ApodNasaModel>() {
            @Override
            public void onResponse(Call<ApodNasaModel> call, Response<ApodNasaModel> response) {
                if (response.isSuccessful()) {
                    apodFragment.footerAdapter.clear();
                    apodFragment.progressBar.setVisibility(View.GONE);

                    if (!apodFragment.apodNasaModels.contains(response.body()) && response.body().getMediaType().equals("image")) {
                        apodFragment.apodNasaModels.add(response.body());
                        apodFragment.fastItemAdapter.add(apodFragment.fastItemAdapter.getAdapterItemCount(), new ApodAdapter<>(response.body(), QueryUtils.APOD_MODEL_NASA));
                    }

                    apodFragment.calendar.add(Calendar.DAY_OF_YEAR, -1);
                    apodFragment.nasaApiQueryCount--;

                    if (apodFragment.nasaApiQueryCount > 0) {
                        queryApodNasaApi(activity);
                    } else {
                        apodFragment.loadingData = false;
                        apodFragment.swipeContainer.setRefreshing(false);
                    }

                }
            }

            @Override
            public void onFailure(Call<ApodNasaModel> call, Throwable t) {
                apodFragment.footerAdapter.clear();
                apodFragment.loadingData = false;
                apodFragment.progressBar.setVisibility(View.GONE);
                apodFragment.swipeContainer.setRefreshing(false);
            }
        });
    }

    public static void beginIotdFetch(Activity activity) {
        IotdFragment iotdFragment = (IotdFragment) activity.getFragmentManager().findFragmentByTag("iotd");

        if (iotdFragment == null) {
            return;
        }

        if (!iotdFragment.loadingData) {
            if (iotdFragment.iotdRssModels.isEmpty()) {
                iotdFragment.progressBar.setVisibility(View.VISIBLE);
            }

            fetchIotdRssFeed(activity);
        }
    }

    public static void clearIotdData(Activity activity) {
        IotdFragment iotdFragment = (IotdFragment) activity.getFragmentManager().findFragmentByTag("iotd");

        if (iotdFragment == null) {
            return;
        }

        if (!iotdFragment.loadingData) {
            iotdFragment.iotdRssModels.clear();
            iotdFragment.calendar = Calendar.getInstance();
            iotdFragment.fastItemAdapter.clear();
            iotdFragment.footerAdapter.clear();
        }
    }

    public static void fetchIotdRssFeed(final Activity activity) {
        final IotdFragment iotdFragment = (IotdFragment) activity.getFragmentManager().findFragmentByTag("iotd");

        if (iotdFragment == null) {
            return;
        }

        iotdFragment.loadingData = true;

        Call<IotdRssModel> call = iotdRssService.get();
        call.enqueue(new Callback<IotdRssModel>() {
            @Override
            public void onResponse (Call<IotdRssModel> call, Response<IotdRssModel> response) {
                if (response.isSuccessful()) {
                    iotdFragment.footerAdapter.clear();
                    iotdFragment.progressBar.setVisibility(View.GONE);

                    for (IotdRssModel.Channel.Item iotdRssModelItem : response.body().getChannel().getItems()) {

                        if (!iotdFragment.iotdRssModels.contains(iotdRssModelItem) && !iotdRssModelItem.getEnclosure().getUrl().isEmpty()) {
                            iotdFragment.iotdRssModels.add(iotdRssModelItem);
                            iotdFragment.fastItemAdapter.add(iotdFragment.fastItemAdapter.getAdapterItemCount(), new IotdAdapter(iotdRssModelItem));
                        }

                        iotdFragment.calendar.add(Calendar.DAY_OF_YEAR, -1);
                    }

                    iotdFragment.loadingData = false;
                    iotdFragment.swipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onFailure (Call<IotdRssModel> call, Throwable t) {
                iotdFragment.footerAdapter.clear();
                iotdFragment.loadingData = false;
                iotdFragment.progressBar.setVisibility(View.GONE);
                iotdFragment.swipeContainer.setRefreshing(false);
            }
        });
    }
}
