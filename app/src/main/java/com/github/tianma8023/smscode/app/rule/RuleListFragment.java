package com.github.tianma8023.smscode.app.rule;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.tianma8023.smscode.R;
import com.github.tianma8023.smscode.adapter.BaseItemCallback;
import com.github.tianma8023.smscode.db.DBManager;
import com.github.tianma8023.smscode.entity.SmsCodeRule;
import com.github.tianma8023.smscode.event.Event;
import com.github.tianma8023.smscode.event.XEventBus;
import com.github.tianma8023.smscode.utils.XLog;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * SMS code codeRule list fragment
 */
public class RuleListFragment extends Fragment {

    @BindView(R.id.rule_list_recycler_view)
    RecyclerView mRecyclerView;

    private RuleAdapter mRuleAdapter;

    @BindView(R.id.rule_list_fab)
    FloatingActionButton mFabButton;

    @BindView(R.id.empty_view)
    View mEmptyView;

    private int mSelectedPosition = -1;

    private Activity mActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rule_list, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = getActivity();

        List<SmsCodeRule> rules = DBManager.get(mActivity).queryAllSmsCodeRules();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRuleAdapter = new RuleAdapter(mActivity, rules);
        mRecyclerView.setAdapter(mRuleAdapter);

        // swipe to remove
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(mSwipeToRemoveCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        mRuleAdapter.setItemCallback(new BaseItemCallback<SmsCodeRule>() {
            @Override
            public void onItemClicked(SmsCodeRule item, int position) {
                mSelectedPosition = position;
                XEventBus.post(new Event.StartRuleEditEvent(
                        RuleEditFragment.EDIT_TYPE_UPDATE, item));
            }

            @Override
            public void onCreateItemContextMenu(ContextMenu menu, View v,
                                                ContextMenu.ContextMenuInfo menuInfo,
                                                SmsCodeRule item, int position) {
                mSelectedPosition = position;
                onCreateContextMenu(menu, v, menuInfo);
            }
        });

        mRuleAdapter.registerAdapterDataObserver(mDataObserver);

        mFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SmsCodeRule emptyRule = new SmsCodeRule();
                XEventBus.post(new Event.StartRuleEditEvent(
                        RuleEditFragment.EDIT_TYPE_CREATE, emptyRule));
            }
        });

        refreshEmptyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mRuleAdapter.unregisterAdapterDataObserver(mDataObserver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_rule_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_import_rules:
                // TODO
                break;
            case R.id.action_export_rules:
                // TODO
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = mActivity.getMenuInflater();
        inflater.inflate(R.menu.context_rule_list, menu);
        menu.setHeaderTitle(R.string.actions);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        SmsCodeRule smsCodeRule = mRuleAdapter.getItemAt(mSelectedPosition);
        switch (item.getItemId()) {
            case R.id.action_edit_rule:
                XEventBus.post(new Event.StartRuleEditEvent(
                        RuleEditFragment.EDIT_TYPE_UPDATE, smsCodeRule));
                break;
            case R.id.action_remove_rule:
                removeItemAt(mSelectedPosition);
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    ItemTouchHelper.Callback mSwipeToRemoveCallback =
            new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END | ItemTouchHelper.START) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    final int position = viewHolder.getAdapterPosition();
                    removeItemAt(position);
                }
            };

    private void removeItemAt(final int position) {
        final SmsCodeRule itemToRemove = mRuleAdapter.getItemAt(position);
        mRuleAdapter.removeItemAt(position);

        Snackbar snackbar = Snackbar.make(mRecyclerView, R.string.removed, Snackbar.LENGTH_LONG);
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (event != DISMISS_EVENT_ACTION) {
                    try {
                        DBManager.get(mActivity).removeSmsCodeRule(itemToRemove);
                    } catch (Exception e) {
                        XLog.e("Remove " + itemToRemove.toString() + " failed", e);
                    }
                }
            }
        });
        snackbar.setAction(R.string.revoke, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRuleAdapter.addRule(position, itemToRemove);
            }
        });
        snackbar.show();
    }

    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            refreshEmptyView();
        }
    };

    private void refreshEmptyView() {
        if (mRuleAdapter.getItemCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    void onRuleSaveOrUpdate(Event.OnRuleCreateOrUpdate event) {
        if (event.type == RuleEditFragment.EDIT_TYPE_CREATE) {
            mRuleAdapter.addRule(event.codeRule);
        } else if (event.type == RuleEditFragment.EDIT_TYPE_UPDATE) {
            mRuleAdapter.updateAt(mSelectedPosition, event.codeRule);
        }
    }
}
