package com.github.tianma8023.smscode.app.rule;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.tianma8023.smscode.R;
import com.github.tianma8023.smscode.db.DBManager;
import com.github.tianma8023.smscode.entity.SmsCodeRule;
import com.github.tianma8023.smscode.event.Event;
import com.github.tianma8023.smscode.event.XEventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RuleEditFragment extends Fragment {

    public static final int EDIT_TYPE_CREATE = 1;
    public static final int EDIT_TYPE_UPDATE = 2;

    @IntDef({EDIT_TYPE_CREATE, EDIT_TYPE_UPDATE})
    public @interface RuleEditType {
    }

    private static final String KEY_RULE_EDIT_TYPE = "rule_edit_type";
    private static final String KEY_CODE_RULE = "code_rule";

    @BindView(R.id.rule_company_edit_text)
    TextInputEditText mCompanyEditText;

    @BindView(R.id.rule_keyword_edit_text)
    TextInputEditText mKeywordEditText;

    @BindView(R.id.rule_code_regex_quick_choose)
    Button mQuickChooseBtn;

    @BindView(R.id.rule_code_regex_edit_text)
    TextInputEditText mCodeRegexEditText;

    private Activity mActivity;

    private int mCodeTypeIndex = 0;

    private int mRuleEditType;
    private SmsCodeRule mCodeRule;

    public static RuleEditFragment newInstance(int ruleEditType, SmsCodeRule codeRule) {
        Bundle args = new Bundle();
        args.putInt(KEY_RULE_EDIT_TYPE, ruleEditType);
        args.putParcelable(KEY_CODE_RULE, codeRule);
        RuleEditFragment fragment = new RuleEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rule_edit, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
        mQuickChooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuickChooseDialog();
            }
        });

        initArguments();
    }

    private void initArguments() {
        Bundle args = getArguments();
        mRuleEditType = args.getInt(KEY_RULE_EDIT_TYPE);
        mCodeRule = args.getParcelable(KEY_CODE_RULE);
        if (mRuleEditType == EDIT_TYPE_UPDATE && mCodeRule != null) {
            mCompanyEditText.setText(mCodeRule.getCompany());
            mKeywordEditText.setText(mCodeRule.getCodeKeyword());
            mCodeRegexEditText.setText(mCodeRule.getCodeRegex());
        } else {
            mCodeRule = new SmsCodeRule();
        }
    }

    private void showQuickChooseDialog() {
        final String[] codeTypes = mActivity.getResources().getStringArray(R.array.sms_code_type_list);

        final View dialogView = LayoutInflater.from(mActivity).inflate(R.layout.dialog_code_regex_quick_chcoose, null);
        final AppCompatSpinner spinner = dialogView.findViewById(R.id.rule_code_type_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCodeTypeIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final TextInputEditText codeLenEditText = dialogView.findViewById(R.id.code_rule_length_edit_text);

        MaterialDialog quickChooseDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.quick_choose)
                .customView(dialogView, false)
                .negativeText(R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .positiveText(R.string.confirm)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String codeType = codeTypes[mCodeTypeIndex];
                        String codeLenText = codeLenEditText.getText().toString();

                        if (TextUtils.isEmpty(codeLenText)) {
                            codeLenEditText.setError(getString(R.string.code_length_empty_prompt));
                            return;
                        }

                        String codeRegex = String.format("%s{%s}", codeType, codeLenText);
                        mCodeRegexEditText.setText(codeRegex);
                        mCodeRegexEditText.setSelection(codeRegex.length());
                        dialog.dismiss();
                    }
                })
                .autoDismiss(false)
                .build();
        quickChooseDialog.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_rule, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rules_tick:
                saveIfValid();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void saveIfValid() {
        if (!checkValid()) {
            return;
        }

        String company = mCompanyEditText.getText().toString();
        String keyword = mKeywordEditText.getText().toString();
        String codeRegex = mCodeRegexEditText.getText().toString();

        mCodeRule.setCompany(company);
        mCodeRule.setCodeKeyword(keyword);
        mCodeRule.setCodeRegex(codeRegex);

        DBManager dbManager = DBManager.get(mActivity);
        if (mRuleEditType == EDIT_TYPE_CREATE) {
            boolean duplicate = dbManager.isExist(mCodeRule);
            if (duplicate) {
                Toast.makeText(mActivity, R.string.rule_duplicated_prompt, Toast.LENGTH_LONG).show();
            } else {
                long id = dbManager.addSmsCodeRule(mCodeRule);
                mCodeRule.setId(id);
                XEventBus.post(new Event.OnRuleCreateOrUpdate(mRuleEditType, mCodeRule));
                mActivity.onBackPressed();
            }
        } else if (mRuleEditType == EDIT_TYPE_UPDATE) {
            dbManager.updateSmsCodeRule(mCodeRule);
            XEventBus.post(new Event.OnRuleCreateOrUpdate(mRuleEditType, mCodeRule));
            mActivity.onBackPressed();
        }
    }

    private boolean checkValid() {
        boolean companyValid = true;
        if (isEmpty(mCompanyEditText)) {
            mCompanyEditText.setError(getString(R.string.rule_company_empty_hint));
            companyValid = false;
        }

        boolean keywordValid = true;
        if (isEmpty(mKeywordEditText)) {
            mKeywordEditText.setError(getString(R.string.rule_keyword_empty_hint));
            keywordValid = false;
        }

        boolean codeRegexValid = true;
        if (isEmpty(mCodeRegexEditText)) {
            mCodeRegexEditText.setError(getString(R.string.rule_code_regex_empty_hint));
            codeRegexValid = false;
        }

        return companyValid && keywordValid && codeRegexValid;
    }

    private boolean isEmpty(EditText editText) {
        return TextUtils.isEmpty(editText.getText());
    }
}
