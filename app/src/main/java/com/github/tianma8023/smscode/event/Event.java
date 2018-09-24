package com.github.tianma8023.smscode.event;

import com.github.tianma8023.smscode.app.rule.RuleEditFragment;
import com.github.tianma8023.smscode.entity.SmsCodeRule;

public class Event {

    private Event() {
    }
    /**
     * Start to edit codeRule event
     */
    public static class StartRuleEditEvent {
        public @RuleEditFragment.RuleEditType int type;
        public SmsCodeRule codeRule;
        public StartRuleEditEvent(@RuleEditFragment.RuleEditType int type, SmsCodeRule codeRule) {
            this.type = type;
            this.codeRule = codeRule;
        }
    }

    public static class OnRuleCreateOrUpdate {
        public SmsCodeRule codeRule;
        public @RuleEditFragment.RuleEditType int type;
        public OnRuleCreateOrUpdate(@RuleEditFragment.RuleEditType int type, SmsCodeRule rule) {
            this.codeRule = rule;
            this.type = type;
        }
    }

}
