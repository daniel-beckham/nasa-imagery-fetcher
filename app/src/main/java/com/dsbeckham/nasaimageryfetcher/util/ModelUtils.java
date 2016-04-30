package com.dsbeckham.nasaimageryfetcher.util;

import com.dsbeckham.nasaimageryfetcher.model.ApodMorphIoModel;
import com.dsbeckham.nasaimageryfetcher.model.ApodNasaGovModel;
import com.dsbeckham.nasaimageryfetcher.model.IotdRssModel;
import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;

public class ModelUtils {
    public static UniversalImageModel convertIotdRssModelChannelItem(IotdRssModel.Channel.Item iotdRssModelChannelItem) {
        UniversalImageModel universalImageModel = new UniversalImageModel();
        universalImageModel.setDate(DateTimeUtils.convertDateToCustomDateFormat(iotdRssModelChannelItem.getPubDate(), "EEE, dd MMM yyyy HH:mm zzz", "yyyy-MM-dd"));
        universalImageModel.setDescription(iotdRssModelChannelItem.getDescription());
        universalImageModel.setImageThumbnailUrl(iotdRssModelChannelItem.getEnclosure().getUrl());
        // The RSS feed always links to the image thumbnail URL. The full image URL is a few directories down.
        universalImageModel.setImageUrl(iotdRssModelChannelItem.getEnclosure().getUrl().replace("styles/full_width_feature/public/", ""));
        universalImageModel.setPageUrl(iotdRssModelChannelItem.getLink());
        universalImageModel.setTitle(iotdRssModelChannelItem.getTitle());
        return universalImageModel;
    }

    public static UniversalImageModel convertApodMorphIoModel(ApodMorphIoModel apodMorphIoModel) {
        UniversalImageModel universalImageModel = new UniversalImageModel();
        universalImageModel.setCredit(apodMorphIoModel.getCredit());
        universalImageModel.setDate(apodMorphIoModel.getDate());
        universalImageModel.setDescription(apodMorphIoModel.getExplanation());
        universalImageModel.setImageThumbnailUrl(apodMorphIoModel.getPictureThumbnailUrl());
        universalImageModel.setImageUrl(apodMorphIoModel.getPictureUrl());
        universalImageModel.setPageUrl(apodMorphIoModel.getUrl());
        universalImageModel.setTitle(apodMorphIoModel.getTitle());
        return universalImageModel;
    }

    public static UniversalImageModel convertApodNasaGovModel(ApodNasaGovModel apodNasaGovModel) {
        UniversalImageModel universalImageModel = new UniversalImageModel();
        universalImageModel.setCredit(apodNasaGovModel.getCopyright());
        universalImageModel.setDate(apodNasaGovModel.getDate());
        universalImageModel.setDescription(apodNasaGovModel.getExplanation());
        universalImageModel.setImageThumbnailUrl(apodNasaGovModel.getUrl());
        universalImageModel.setImageUrl(apodNasaGovModel.getHdUrl());
        // The NASA API doesn't include the page URL, but it can be obtained from the date of the image.
        universalImageModel.setPageUrl(String.format("http://apod.nasa.gov/apod/ap%1$s.html", DateTimeUtils.convertDateToCustomDateFormat(apodNasaGovModel.getDate(), "yyyy-MM-dd", "yyMMdd")));
        universalImageModel.setTitle(apodNasaGovModel.getTitle());
        return universalImageModel;
    }
}
