package com.github.tianma8023.smscode.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

import java.util.Objects;

@Entity
public class SmsMsg implements Parcelable {

    @Id(autoincrement = true)
    private Long id;

    // Sender
    private String sender;

    // Message content
    @Transient
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
        if (source.readByte() == 0) {
            id = null;
        } else {
            id = source.readLong();
        }
        sender = source.readString();
        body = source.readString();
        date = source.readLong();
        company = source.readString();
        smsCode = source.readString();
    }

    @Generated(hash = 1682862228)
    public SmsMsg(Long id, String sender, long date, String company,
            String smsCode) {
        this.id = id;
        this.sender = sender;
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
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(sender);
        dest.writeString(body);
        dest.writeLong(date);
        dest.writeString(company);
        dest.writeString(smsCode);
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "SmsMsg{" +
                "id=" + id +
                ", date=" + date +
                ", company='" + company + '\'' +
                ", smsCode='" + smsCode + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SmsMsg)) return false;
        SmsMsg smsMsg = (SmsMsg) o;
        return Objects.equals(id, smsMsg.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
