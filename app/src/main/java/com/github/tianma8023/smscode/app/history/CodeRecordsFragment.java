package com.github.tianma8023.smscode.app.history;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.tianma8023.smscode.R;
import com.github.tianma8023.smscode.db.DBManager;
import com.github.tianma8023.smscode.entity.SmsMsg;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CodeRecordsFragment extends Fragment {

    private Activity mActivity;

    @BindView(R.id.code_records_recycler_view)
    RecyclerView mRecyclerView;

    private CodeRecordAdapter mCodeRecordAdapter;

    public static CodeRecordsFragment newInstance() {
        return new CodeRecordsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_code_records, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = getActivity();

        List<SmsMsg> records = DBManager.get(mActivity).queryAllSmsMsg();
        mCodeRecordAdapter = new CodeRecordAdapter(mActivity, records);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRecyclerView.setAdapter(mCodeRecordAdapter);
    }
}
