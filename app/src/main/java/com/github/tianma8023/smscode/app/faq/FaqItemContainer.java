package com.github.tianma8023.smscode.app.faq;

import android.content.Context;
import android.content.res.Resources;

import com.github.tianma8023.smscode.R;

import java.util.ArrayList;
import java.util.List;


public class FaqItemContainer {

    private List<FaqItem> mFaqItems = new ArrayList<>();

    FaqItemContainer(Context context) {
        loadItems(context);
    }

    private void loadItems(Context context) {
        Resources res = context.getResources();
        String[] questionArr = res.getStringArray(R.array.question_list);
        String[] answerArr = res.getStringArray(R.array.answer_list);
        for (int i = 0; i < questionArr.length; i++) {
            mFaqItems.add(new FaqItem(questionArr[i], answerArr[i]));
        }
    }

    List<FaqItem> getFaqItems() {
        return mFaqItems;
    }
}
