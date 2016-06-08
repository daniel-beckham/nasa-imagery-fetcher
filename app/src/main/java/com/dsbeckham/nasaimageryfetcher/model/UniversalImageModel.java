package com.dsbeckham.nasaimageryfetcher.model;

import android.os.Parcel;
import android.os.Parcelable;

public class UniversalImageModel implements Parcelable {
    private String credit;
    private String date;
    private String description;
    private String imageThumbnailUrl;
    private String imageUrl;
    private String pageUrl;
    private String title;
    private String type;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object object) {
        return this == object || ((!(object == null || getClass() != object.getClass()) && date.equals(((UniversalImageModel) object).date)));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.credit);
        dest.writeString(this.date);
        dest.writeString(this.description);
        dest.writeString(this.imageThumbnailUrl);
        dest.writeString(this.imageUrl);
        dest.writeString(this.pageUrl);
        dest.writeString(this.title);
        dest.writeString(this.type);
    }

    public static final Parcelable.Creator<UniversalImageModel> CREATOR = new Parcelable.Creator<UniversalImageModel>() {
        @Override
        public UniversalImageModel createFromParcel(Parcel source) {
            return new UniversalImageModel(source);
        }

        @Override
        public UniversalImageModel[] newArray(int size) {
            return new UniversalImageModel[size];
        }
    };

    public UniversalImageModel() {}

    protected UniversalImageModel(Parcel in) {
        this.credit = in.readString();
        this.date = in.readString();
        this.description = in.readString();
        this.imageThumbnailUrl = in.readString();
        this.imageUrl = in.readString();
        this.pageUrl = in.readString();
        this.title = in.readString();
        this.type = in.readString();
    }
}
