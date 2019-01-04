package com.github.tianma8023.smscode.app.permissions;

import android.os.Parcel;
import android.os.Parcelable;

public class PermItem implements Parcelable {

    private String title;

    private String content;

    PermItem(String title, String content) {
        this.title = title;
        this.content = content;
    }

    protected PermItem(Parcel in) {
        title = in.readString();
        content = in.readString();
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(content);
    }

    public static final Creator<PermItem> CREATOR = new Creator<PermItem>() {
        @Override
        public PermItem createFromParcel(Parcel in) {
            return new PermItem(in);
        }

        @Override
        public PermItem[] newArray(int size) {
            return new PermItem[size];
        }
    };
}
