package com.dsbeckham.nasaimageryfetcher.model;

import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public class UniversalImageModel {
    private String credit;
    private String date;
    private String description;
    private String imageThumbnailUrl;
    private String imageUrl;
    private String pageUrl;
    private String title;

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageThumbnailUrl() {
        return imageThumbnailUrl;
    }

    public void setImageThumbnailUrl(String imageThumbnailUrl) {
        this.imageThumbnailUrl = imageThumbnailUrl;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object object) {
        return this == object || ((!(object == null || getClass() != object.getClass()) && date.equals(((UniversalImageModel) object).date)));
    }
}
