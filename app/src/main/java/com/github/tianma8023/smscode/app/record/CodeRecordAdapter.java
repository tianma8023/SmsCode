package com.github.tianma8023.smscode.app.record;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tianma8023.smscode.R;
import com.github.tianma8023.smscode.entity.SmsMsg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CodeRecordAdapter extends BaseQuickAdapter<RecordItem, BaseViewHolder> {

    private final SimpleDateFormat mDateFormat;

    CodeRecordAdapter(@Nullable List<RecordItem> data) {
        super(R.layout.code_record_item, data);

        mDateFormat = new SimpleDateFormat("MM.dd HH:mm", Locale.getDefault());
    }

    @Override
    protected void convert(BaseViewHolder helper, RecordItem item) {
        SmsMsg smsMsg = item.getSmsMsg();
        String company = smsMsg.getCompany();
        if (company == null || company.trim().length() == 0) {
            company = smsMsg.getSender();
        }
        helper.setText(R.id.company_text_view, company)
                .setText(R.id.smscode_text_view, smsMsg.getSmsCode())
                .setText(R.id.date_text_view, mDateFormat.format(new Date(smsMsg.getDate())))
                .setGone(R.id.record_details_view, !TextUtils.isEmpty(smsMsg.getBody()))
                .addOnClickListener(R.id.record_details_view);
        helper.itemView.setSelected(item.isSelected());
    }

    public void setItemSelected(int position, boolean selected) {
        RecordItem recordItem = getItem(position);
        if (recordItem != null) {
            recordItem.setSelected(selected);
            notifyItemChanged(position);
        }
    }

    public boolean isItemSelected(int position) {
        RecordItem recordItem = getItem(position);
        return recordItem != null && recordItem.isSelected();
    }

    public void setAllSelected(boolean selected) {
        for(RecordItem recordItem : getData()) {
            recordItem.setSelected(selected);
        }
        notifyDataSetChanged();
    }


    public boolean isAllSelected() {
        boolean allSelected = true;
        for (int i = 0; i < getItemCount(); i++) {
            if (!isItemSelected(i)) {
                allSelected = false;
                break;
            }
        }
        return allSelected;
    }

    public boolean isAllUnselected() {
        boolean allUnselected = true;
        for (int i = 0; i < getItemCount(); i++) {
            if (isItemSelected(i)) {
                allUnselected = false;
                break;
            }
        }
        return allUnselected;
    }

    public List<SmsMsg> removeSelectedItems() {
        List<RecordItem> recordsToRemove = new ArrayList<>();
        List<SmsMsg> messagesToRemove = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            RecordItem item = getItem(i);
            if (item != null && item.isSelected()) {
                recordsToRemove.add(item);
                messagesToRemove.add(item.getSmsMsg());
            }
        }
        getData().removeAll(recordsToRemove);
        notifyDataSetChanged();

        return messagesToRemove;
    }

    public void addItems(List<SmsMsg> smsMsgList) {
        List<RecordItem> itemsToAdd = new ArrayList<>();
        for (SmsMsg msg : smsMsgList) {
            RecordItem item = new RecordItem(msg);
            if (!getData().contains(item)) {
                itemsToAdd.add(item);
            }
        }

        if (!itemsToAdd.isEmpty()) {
            getData().addAll(itemsToAdd);
            Collections.sort(getData(), new Comparator<RecordItem>() {
                @Override
                public int compare(RecordItem o1, RecordItem o2) {
                    long date1 = o1.getSmsMsg().getDate();
                    long date2 = o2.getSmsMsg().getDate();
                    return Long.compare(date2, date1);
                }
            });
            notifyDataSetChanged();
        }
    }
}
