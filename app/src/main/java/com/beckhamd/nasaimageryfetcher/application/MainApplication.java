package com.beckhamd.nasaimageryfetcher.application;

import android.app.Application;

import com.beckhamd.nasaimageryfetcher.job.BackgroundJobCreator;
import com.beckhamd.nasaimageryfetcher.model.UniversalImageModel;
import com.beckhamd.nasaimageryfetcher.util.ApodQueryUtils;
import com.beckhamd.nasaimageryfetcher.util.IotdQueryUtils;
import com.evernote.android.job.JobManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainApplication extends Application {
    private List<UniversalImageModel> apodModels = new ArrayList<>();
    private List<UniversalImageModel> iotdModels = new ArrayList<>();

    private ApodQueryUtils.MorphIoService apodMorphIoService;
    private ApodQueryUtils.NasaGovService apodNasaGovService;
    private IotdQueryUtils.RssService iotdRssService;

    private boolean apodInactive = true;
    private boolean iotdInactive = true;

    private Calendar apodCalendar = Calendar.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();
        JobManager.create(this).addJobCreator(new BackgroundJobCreator());
    }

    public List<UniversalImageModel> getApodModels() {
        return apodModels;
    }

    public void setApodModels(List<UniversalImageModel> apodModels) {
        this.apodModels = apodModels;
    }

    public List<UniversalImageModel> getIotdModels() {
        return iotdModels;
    }

    public void setIotdModels(List<UniversalImageModel> iotdModels) {
        this.iotdModels = iotdModels;
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

    public boolean isApodInactive() {
        return apodInactive;
    }

    public void setApodInactive(boolean apodInactive) {
        this.apodInactive = apodInactive;
    }

    public boolean isIotdInactive() {
        return iotdInactive;
    }

    public void setIotdInactive(boolean iotdInactive) {
        this.iotdInactive = iotdInactive;
    }

    public Calendar getApodCalendar() {
        return apodCalendar;
    }

    public void setApodCalendar(Calendar apodCalendar) {
        this.apodCalendar = apodCalendar;
    }
}
