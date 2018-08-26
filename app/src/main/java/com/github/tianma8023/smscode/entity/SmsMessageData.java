package com.github.tianma8023.smscode.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class SmsMessageData implements Parcelable {

    // Sender
    private String mSender;
    // Message content
    private String mBody;
    // Receive date
    private long mDate;

    public SmsMessageData setSender(String sender) {
        mSender = sender;
        return this;
    }

    public SmsMessageData setBody(String body) {
        mBody = body;
        return this;
    }

    public SmsMessageData setDate(long date) {
        mDate = date;
        return this;
    }

    public String getSender() {
        return mSender;
    }

    public String getBody() {
        return mBody;
    }

    public long getDate() {
        return mDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private SmsMessageData(Parcel source) {
        mSender = source.readString();
        mBody = source.readString();
        mDate = source.readLong();
    }

    public SmsMessageData() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSender);
        dest.writeString(mBody);
        dest.writeLong(mDate);
    }

    public static final Creator<SmsMessageData> CREATOR = new Creator<SmsMessageData>() {

        @Override
        public SmsMessageData createFromParcel(Parcel source) {
            return new SmsMessageData(source);
        }

        @Override
        public SmsMessageData[] newArray(int size) {
            return new SmsMessageData[size];
        }
    };
}
