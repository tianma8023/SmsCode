package com.github.tianma8023.smscode.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class SmsMsg implements Parcelable {

    // Sender
    private String sender;
    // Message content
    private String body;
    // Receive date
    private long date;
    // Company
    private String company;
    // SMS Code
    private String smsCode;

    public SmsMsg() {
    }

    private SmsMsg(Parcel source) {
        sender = source.readString();
        body = source.readString();
        date = source.readLong();
        company = source.readString();
        smsCode = source.readString();
    }

    @Generated(hash = 1966110634)
    public SmsMsg(String sender, String body, long date, String company,
            String smsCode) {
        this.sender = sender;
        this.body = body;
        this.date = date;
        this.company = company;
        this.smsCode = smsCode;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    public String getSender() {
        return sender;
    }

    public String getBody() {
        return body;
    }

    public long getDate() {
        return date;
    }

    public String getCompany() {
        return company;
    }

    public String getSmsCode() {
        return smsCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sender);
        dest.writeString(body);
        dest.writeLong(date);
        dest.writeString(company);
        dest.writeString(smsCode);
    }

    public static final Creator<SmsMsg> CREATOR = new Creator<SmsMsg>() {

        @Override
        public SmsMsg createFromParcel(Parcel source) {
            return new SmsMsg(source);
        }

        @Override
        public SmsMsg[] newArray(int size) {
            return new SmsMsg[size];
        }
    };
}
