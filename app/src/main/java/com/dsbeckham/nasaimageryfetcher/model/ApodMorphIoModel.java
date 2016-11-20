package com.dsbeckham.nasaimageryfetcher.model;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("ALL")
public class ApodMorphIoModel {
    private String url;
    private String date;
    private String title;
    private String credit;
    private String explanation;
    @SerializedName("picture_thumbnail_url")
    private String pictureThumbnailUrl;
    @SerializedName("picture_url")
    private String pictureUrl;
    @SerializedName("video_url")
    private String videoUrl;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getPictureThumbnailUrl() {
        return pictureThumbnailUrl;
    }

    public void setPictureThumbnailUrl(String pictureThumbnailUrl) {
        this.pictureThumbnailUrl = pictureThumbnailUrl;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    @Override
    public boolean equals(Object object) {
        return this == object || ((!(object == null || getClass() != object.getClass()) && date.equals(((ApodMorphIoModel) object).date)));
    }
}
