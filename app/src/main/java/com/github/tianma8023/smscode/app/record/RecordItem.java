package com.github.tianma8023.smscode.app.record;

import com.github.tianma8023.smscode.entity.SmsMsg;

public class RecordItem {

    private SmsMsg smsMsg;
    private boolean selected;

    RecordItem(SmsMsg smsMsg, boolean selected) {
        this.smsMsg = smsMsg;
        this.selected = selected;
    }

    public SmsMsg getSmsMsg() {
        return smsMsg;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return "RecordItem{" +
                "smsMsg=" + smsMsg +
                ", selected=" + selected +
                '}';
    }
}
