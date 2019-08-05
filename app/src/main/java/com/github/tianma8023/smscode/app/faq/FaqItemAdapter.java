package com.github.tianma8023.smscode.app.faq;

import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tianma8023.smscode.R;

import java.util.List;

public class FaqItemAdapter extends BaseQuickAdapter<FaqItem, BaseViewHolder> {
    private final String mQuestionPrefix;
    private final String mAnswerPrefix;

    FaqItemAdapter(Context context, List<FaqItem> items) {
        super(R.layout.item_faq, items);
        mQuestionPrefix = context.getString(R.string.simplified_question);
        mAnswerPrefix = context.getString(R.string.simplified_answer);
    }

    @Override
    protected void convert(BaseViewHolder helper, FaqItem item) {
        String question = mQuestionPrefix + item.getQuestion();
        String answer = mAnswerPrefix + item.getAnswer();
        helper.setText(R.id.item_question, question)
                .setText(R.id.item_answer, answer);
    }
}
