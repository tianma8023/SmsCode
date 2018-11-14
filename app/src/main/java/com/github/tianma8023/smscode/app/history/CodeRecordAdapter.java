package com.github.tianma8023.smscode.app.history;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.tianma8023.smscode.R;
import com.github.tianma8023.smscode.entity.SmsMsg;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CodeRecordAdapter extends RecyclerView.Adapter<CodeRecordAdapter.VH> {

    private Context mContext;
    private List<SmsMsg> mRecords;

    private SimpleDateFormat mFormat;

    CodeRecordAdapter(Context context, List<SmsMsg> records) {
        mContext = context;
        mRecords = records;

        mFormat = new SimpleDateFormat("MM.dd HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.code_record_item, parent, false);
        return new VH(rootView);
    }

    @Override
    public int getItemCount() {
        return mRecords == null ? 0 : mRecords.size();
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final SmsMsg data = mRecords.get(position);
        holder.bindData(data);
        holder.bindListener(data);
    }

    class VH extends RecyclerView.ViewHolder {

        @BindView(R.id.company_text_view)
        TextView mCompanyTv;

        @BindView(R.id.smscode_text_view)
        TextView mSmsCodeTv;

        @BindView(R.id.date_text_view)
        TextView mDateTv;

        VH(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(SmsMsg data) {
            mCompanyTv.setText(data.getCompany());
            mSmsCodeTv.setText(data.getSmsCode());
            mDateTv.setText(mFormat.format(new Date(data.getDate())));
        }

        void bindListener(SmsMsg data) {

        }
    }

}
