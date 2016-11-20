package com.dsbeckham.nasaimageryfetcher.application;

import android.app.Application;

import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;
import com.dsbeckham.nasaimageryfetcher.util.ApodQueryUtils;
import com.dsbeckham.nasaimageryfetcher.util.IotdQueryUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@SuppressWarnings("ALL")
public class MainApplication extends Application {
    private List<UniversalImageModel> apodModels = new ArrayList<>();
    private List<UniversalImageModel> iotdModels = new ArrayList<>();

    private ApodQueryUtils.MorphIoService apodMorphIoService;
    private ApodQueryUtils.NasaGovService apodNasaGovService;
    private IotdQueryUtils.RssService iotdRssService;

    private boolean apodLoadingData;
    private boolean iotdLoadingData;

    private Calendar apodCalendar = Calendar.getInstance();
    private int apodNasaGovApiQueries = ApodQueryUtils.NASA_GOV_API_QUERIES;

    public List<UniversalImageModel> getApodModels() {
        return apodModels;
    }

    public List<UniversalImageModel> getIotdModels() {
        return iotdModels;
    }

    public ApodQueryUtils.MorphIoService getApodMorphIoService() {
        return apodMorphIoService;
    }

    public void setApodMorphIoService(ApodQueryUtils.MorphIoService apodMorphIoService) {
        this.apodMorphIoService = apodMorphIoService;
    }

    public ApodQueryUtils.NasaGovService getApodNasaGovService() {
        return apodNasaGovService;
    }

    public void setApodNasaGovService(ApodQueryUtils.NasaGovService apodNasaGovService) {
        this.apodNasaGovService = apodNasaGovService;
    }

    public IotdQueryUtils.RssService getIotdRssService() {
        return iotdRssService;
    }

    public void setIotdRssService(IotdQueryUtils.RssService iotdRssService) {
        this.iotdRssService = iotdRssService;
    }

    public boolean isApodLoadingData() {
        return apodLoadingData;
    }

    public void setApodLoadingData(boolean apodLoadingData) {
        this.apodLoadingData = apodLoadingData;
    }

    public boolean isIotdLoadingData() {
        return iotdLoadingData;
    }

    public void setIotdLoadingData(boolean iotdLoadingData) {
        this.iotdLoadingData = iotdLoadingData;
    }

    public Calendar getApodCalendar() {
        return apodCalendar;
    }

    public void setApodCalendar(Calendar apodCalendar) {
        this.apodCalendar = apodCalendar;
    }

    public int getApodNasaGovApiQueries() {
        return apodNasaGovApiQueries;
    }

    public void setApodNasaGovApiQueries(int apodNasaGovApiQueries) {
        this.apodNasaGovApiQueries = apodNasaGovApiQueries;
    }
}
