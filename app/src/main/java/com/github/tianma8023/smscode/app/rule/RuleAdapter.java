package com.github.tianma8023.smscode.app.rule;

import android.view.ContextMenu;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tianma8023.smscode.R;
import com.github.tianma8023.smscode.adapter.OnCreateItemContextMenuListener;
import com.github.tianma8023.smscode.entity.SmsCodeRule;

import java.util.List;

public class RuleAdapter extends BaseQuickAdapter<SmsCodeRule, BaseViewHolder> {

    private OnCreateItemContextMenuListener mContextMenuListener;

    RuleAdapter(List<SmsCodeRule> ruleList) {
        super(R.layout.item_rule, ruleList);
    }

    @Override
    protected void convert(BaseViewHolder helper, SmsCodeRule item) {
        helper.setText(R.id.rule_company_text_view, item.getCompany())
                .setText(R.id.rule_keyword_text_view, item.getCodeKeyword())
                .setText(R.id.rule_regex_text_view, item.getCodeRegex());
    }

    public void setContextMenuListener(OnCreateItemContextMenuListener contextMenuListener) {
        mContextMenuListener = contextMenuListener;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final int pos = position;
        holder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                if (mContextMenuListener != null) {
                    mContextMenuListener.onCreateItemContextMenu(menu, v, menuInfo, pos);
                }
            }
        });
    }

    public void addRule(SmsCodeRule newRule) {
        if (!getData().contains(newRule)) {
            addData(newRule);
        }
    }

    public void addRule(List<SmsCodeRule> ruleList) {
        addData(ruleList);
    }

    public void addRule(int position, SmsCodeRule newRule) {
        if (!getData().contains(newRule)) {
            addData(position, newRule);
        }
    }

    public void updateAt(int position, SmsCodeRule updatedRule) {
        SmsCodeRule item = getItem(position);
        if (item != null) {
            item.copyFrom(updatedRule);
            notifyDataSetChanged();
        }
    }

    public void removeItemAt(int position) {
        remove(position);
    }

    public List<SmsCodeRule> getRuleList() {
        return getData();
    }

    public void setRules(List<SmsCodeRule> ruleList) {
        setNewData(ruleList);
    }
}
