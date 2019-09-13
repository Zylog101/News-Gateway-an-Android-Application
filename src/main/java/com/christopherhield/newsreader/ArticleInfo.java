package com.christopherhield.newsreader;

import android.os.Parcel;
import android.os.Parcelable;

public class ArticleInfo implements Parcelable {
    public String Author;
    public String Title;
    public String Description;
    public String UrlToImage;
    public String PublishedAt;
    public String url;

    public ArticleInfo() {
    }

    protected ArticleInfo(Parcel in) {
        Author = in.readString();
        Title = in.readString();
        Description = in.readString();
        UrlToImage = in.readString();
        PublishedAt = in.readString();
        url = in.readString();
    }

    public static final Creator<ArticleInfo> CREATOR = new Creator<ArticleInfo>() {
        @Override
        public ArticleInfo createFromParcel(Parcel in) {
            return new ArticleInfo(in);
        }

        @Override
        public ArticleInfo[] newArray(int size) {
            return new ArticleInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(Author);
        parcel.writeString(Title);
        parcel.writeString(Description);
        parcel.writeString(UrlToImage);
        parcel.writeString(PublishedAt);
        parcel.writeString(url);
    }
}
