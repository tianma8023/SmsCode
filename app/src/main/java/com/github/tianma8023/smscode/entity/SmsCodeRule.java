package com.github.tianma8023.smscode.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

import java.util.Objects;

@Entity
public class SmsCodeRule implements Parcelable {

    /**
     * id
     */
    @Id(autoincrement = true)
    private Long id;

    /**
     * company or company or organization name
     */
    private String company;

    /**
     * verification code keyword
     */
    @NotNull
    private String codeKeyword;

    /**
     * verification code regex
     */
    @NotNull
    private String codeRegex;

    /**
     * case sensitive
     */
    private boolean caseSensitive;

    public SmsCodeRule(String company, @NotNull String codeKeyword,
                       @NotNull String codeRegex, boolean caseSensitive) {
        this.company = company;
        this.codeKeyword = codeKeyword;
        this.codeRegex = codeRegex;
        this.caseSensitive = caseSensitive;
    }

    @Generated(hash = 1902054369)
    public SmsCodeRule(Long id, String company, @NotNull String codeKeyword,
                       @NotNull String codeRegex, boolean caseSensitive) {
        this.id = id;
        this.company = company;
        this.codeKeyword = codeKeyword;
        this.codeRegex = codeRegex;
        this.caseSensitive = caseSensitive;
    }

    @Generated(hash = 1135501737)
    public SmsCodeRule() {
    }


    private SmsCodeRule(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        company = in.readString();
        codeKeyword = in.readString();
        codeRegex = in.readString();
        caseSensitive = in.readByte() != 0;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCodeKeyword() {
        return codeKeyword;
    }

    public void setCodeKeyword(String codeKeyword) {
        this.codeKeyword = codeKeyword;
    }

    public String getCodeRegex() {
        return codeRegex;
    }

    public void setCodeRegex(String codeRegex) {
        this.codeRegex = codeRegex;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public Long getId() {
        return this.id;
    }

    public boolean getCaseSensitive() {
        return this.caseSensitive;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SmsCodeRule)) return false;
        SmsCodeRule that = (SmsCodeRule) o;
        return caseSensitive == that.caseSensitive &&
                Objects.equals(company, that.company) &&
                Objects.equals(codeKeyword, that.codeKeyword) &&
                Objects.equals(codeRegex, that.codeRegex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(company, codeKeyword, codeRegex, caseSensitive);
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
        dest.writeString(company);
        dest.writeString(codeKeyword);
        dest.writeString(codeRegex);
        dest.writeByte((byte) (caseSensitive ? 1 : 0));
    }

    public static final Creator<SmsCodeRule> CREATOR = new Creator<SmsCodeRule>() {
        @Override
        public SmsCodeRule createFromParcel(Parcel in) {
            return new SmsCodeRule(in);
        }

        @Override
        public SmsCodeRule[] newArray(int size) {
            return new SmsCodeRule[size];
        }
    };

    public void copyFrom(SmsCodeRule newRule) {
        this.id = newRule.id;
        this.company = newRule.company;
        this.codeKeyword = newRule.codeKeyword;
        this.codeRegex = newRule.codeRegex;
        this.caseSensitive = newRule.caseSensitive;
    }

    @Override
    public String toString() {
        return "SmsCodeRule{" +
                "company='" + company + '\'' +
                ", codeKeyword='" + codeKeyword + '\'' +
                ", codeRegex='" + codeRegex + '\'' +
                ", caseSensitive=" + caseSensitive +
                '}';
    }
}