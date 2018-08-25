package com.github.tianma8023.smscode.utils;

import android.content.Context;

import com.github.tianma8023.smscode.constant.IPrefConstants;

public class SPUtils {

    private static final String FIRST_RUN_SINCE_V1 = "first_run_v1";
    // 是否已经对MIUI的"通知类短信"权限进行提示了
    private static final String SERVICE_SMS_PROMPT_SHOWN = "service_sms_prompt_shown";
    private static final String LAST_SMS_DATE = "last_sms_date";
    private static final String LAST_SMS_SENDER = "last_sms_sender";

    private SPUtils() {

    }

    /**
     * 是否在自v1.0版本以来第一次运行
     */
    public static boolean isFirstRunSinceV1(Context context) {
        return PreferenceUtils.getBoolean(context, FIRST_RUN_SINCE_V1, true);
    }

    public static void setFirstRunSinceV1(Context context, boolean value) {
        PreferenceUtils.putBoolean(context, FIRST_RUN_SINCE_V1, value);
    }

    /**
     * MIUI的"通知类短信"权限申请是否已经提示过
     */
    public static boolean isServiceSmsPromptShown(Context context) {
        return PreferenceUtils.getBoolean(context, SERVICE_SMS_PROMPT_SHOWN, false);
    }

    /**
     * 设置MIUI的"通知类短信"权限申请是否已经提示过
     */
    public static void setServiceSmsPromptShown(Context context, boolean shown) {
        PreferenceUtils.putBoolean(context, SERVICE_SMS_PROMPT_SHOWN, shown);
    }

    /**
     * 记录上次SMS的Sender
     */
    public static void setLastSmsSender(Context context, String lastSender) {
        PreferenceUtils.putString(context, LAST_SMS_SENDER, lastSender);
    }

    /**
     * 获取上次SMS的Sender
     */
    public static String getLastSmsSender(Context context) {
        return PreferenceUtils.getString(context, LAST_SMS_SENDER, "");
    }

    /**
     * 设置上次SMS的Date
     */
    public static void setLastSmsDate(Context context, long lastSendDate) {
        PreferenceUtils.putLong(context, LAST_SMS_DATE, lastSendDate);
    }

    /**
     * 获取上次SMS的Date
     */
    public static long getLastSmsDate(Context context) {
        return PreferenceUtils.getLong(context, LAST_SMS_DATE, -1L);
    }

    public static boolean isEnable(Context context) {
        return PreferenceUtils.getBoolean(context,
                IPrefConstants.KEY_ENABLE, IPrefConstants.KEY_ENABLE_DEFAULT);
    }

    public static String getListenMode(Context context) {
        return PreferenceUtils.getString(context,
                IPrefConstants.KEY_LISTEN_MODE, IPrefConstants.KEY_LISTEN_MODE_STANDARD);
    }

    public static boolean isAutoInputRootMode(Context context) {
        return PreferenceUtils.getBoolean(context,
                IPrefConstants.KEY_AUTO_INPUT_MODE_ROOT, IPrefConstants.KEY_AUTO_INPUT_MODE_ROOT_DEFAULT);
    }

    public static boolean isVerboseLogMode(Context context) {
        return PreferenceUtils.getBoolean(context,
                IPrefConstants.KEY_VERBOSE_LOG_MODE, IPrefConstants.KEY_VERBOSE_LOG_MODE_DEFAULT);
    }

    public static boolean showToast(Context context) {
        return PreferenceUtils.getBoolean(context,
                IPrefConstants.KEY_SHOW_TOAST, IPrefConstants.KEY_SHOW_TOAST_DEFAULT);
    }

    public static String getFocusMode(Context context) {
        return PreferenceUtils.getString(context,
                IPrefConstants.KEY_FOCUS_MODE, IPrefConstants.KEY_FOCUS_MODE_AUTO);
    }

    public static String getSMSCodeKeywords(Context context) {
        return PreferenceUtils.getString(context,
                IPrefConstants.KEY_SMSCODE_KEYWORDS, IPrefConstants.KEY_SMSCODE_KEYWORDS_DEFAULT);
    }

}
